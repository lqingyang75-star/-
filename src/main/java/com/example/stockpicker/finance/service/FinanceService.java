package com.example.stockpicker.finance.service;

import com.example.stockpicker.common.BizException;
import com.example.stockpicker.common.PageResult;
import com.example.stockpicker.finance.dto.CategoryForm;
import com.example.stockpicker.finance.dto.RecordForm;
import com.example.stockpicker.finance.dto.RecordQuery;
import com.example.stockpicker.finance.dto.RecordVO;
import com.example.stockpicker.finance.dto.StatResult;
import com.example.stockpicker.finance.entity.FinCategory;
import com.example.stockpicker.finance.entity.FinRecord;
import com.example.stockpicker.finance.repository.FinCategoryRepository;
import com.example.stockpicker.finance.repository.FinRecordRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 财务收支服务：分类与记录的增删改查、区间统计。
 * 所有数据按 userId 隔离，非本人数据一律视为不存在。
 */
@Service
public class FinanceService {

    private static final LocalDate MIN_DATE = LocalDate.of(1970, 1, 1);
    private static final LocalDate MAX_DATE = LocalDate.of(2999, 12, 31);

    private final FinCategoryRepository categoryRepository;
    private final FinRecordRepository recordRepository;

    public FinanceService(FinCategoryRepository categoryRepository, FinRecordRepository recordRepository) {
        this.categoryRepository = categoryRepository;
        this.recordRepository = recordRepository;
    }

    // ==================== 分类 ====================

    @Transactional(readOnly = true)
    public List<FinCategory> listCategories(Long userId, Integer type) {
        return categoryRepository.findByUserIdOrderBySortAscIdAsc(userId).stream()
                .filter(c -> type == null || type.equals(c.getType()))
                .toList();
    }

    @Transactional
    public FinCategory createCategory(Long userId, CategoryForm form) {
        checkType(form.type());
        if (categoryRepository.existsByUserIdAndNameAndType(userId, form.name().trim(), form.type())) {
            throw new BizException(400, "同类型下已存在同名分类");
        }
        FinCategory category = new FinCategory();
        category.setUserId(userId);
        category.setName(form.name().trim());
        category.setType(form.type());
        category.setSort(form.sort() == null ? 0 : form.sort());
        return categoryRepository.save(category);
    }

    @Transactional
    public void updateCategory(Long userId, Long id, CategoryForm form) {
        checkType(form.type());
        FinCategory category = requireCategory(userId, id);
        if (categoryRepository.existsByUserIdAndNameAndTypeAndIdNot(userId, form.name().trim(), form.type(), id)) {
            throw new BizException(400, "同类型下已存在同名分类");
        }
        if (!category.getType().equals(form.type())
                && recordRepository.countByUserIdAndCategoryId(userId, id) > 0) {
            throw new BizException(400, "该分类已被记录使用，不能修改类型");
        }
        category.setName(form.name().trim());
        category.setType(form.type());
        category.setSort(form.sort() == null ? 0 : form.sort());
        categoryRepository.save(category);
    }

    @Transactional
    public void deleteCategory(Long userId, Long id) {
        FinCategory category = requireCategory(userId, id);
        if (recordRepository.countByUserIdAndCategoryId(userId, id) > 0) {
            throw new BizException(400, "该分类下存在收支记录，无法删除");
        }
        categoryRepository.delete(category);
    }

    // ==================== 收支记录 ====================

    @Transactional(readOnly = true)
    public PageResult<RecordVO> pageRecords(Long userId, RecordQuery query) {
        int pageNo = Math.max(query.getPage(), 1);
        int size = Math.min(Math.max(query.getSize(), 1), 100);
        Pageable pageable = PageRequest.of(pageNo - 1, size,
                Sort.by(Sort.Direction.DESC, "recordDate").and(Sort.by(Sort.Direction.DESC, "id")));
        Page<FinRecord> page = recordRepository.findAll(buildSpec(userId, query), pageable);
        Map<Long, FinCategory> categories = categoryMap(userId);
        List<RecordVO> items = page.getContent().stream()
                .map(r -> toVO(r, categories))
                .toList();
        return PageResult.of(page.getTotalElements(), pageNo, size, items);
    }

    @Transactional
    public RecordVO createRecord(Long userId, RecordForm form) {
        FinRecord record = new FinRecord();
        record.setUserId(userId);
        applyForm(record, userId, form);
        recordRepository.save(record);
        return toVO(record, categoryMap(userId));
    }

    @Transactional
    public RecordVO updateRecord(Long userId, Long id, RecordForm form) {
        FinRecord record = requireRecord(userId, id);
        applyForm(record, userId, form);
        recordRepository.save(record);
        return toVO(record, categoryMap(userId));
    }

    @Transactional
    public void deleteRecord(Long userId, Long id) {
        recordRepository.delete(requireRecord(userId, id));
    }

    // ==================== 统计 ====================

    @Transactional(readOnly = true)
    public StatResult stat(Long userId, RecordQuery query) {
        LocalDate start = query.getStartDate() == null ? MIN_DATE : query.getStartDate();
        LocalDate end = query.getEndDate() == null ? MAX_DATE : query.getEndDate();
        if (start.isAfter(end)) {
            throw new BizException(400, "开始日期不能晚于结束日期");
        }
        BigDecimal income = recordRepository.sumAmount(userId, 1, start, end);
        BigDecimal expense = recordRepository.sumAmount(userId, 2, start, end);
        Map<Long, FinCategory> categories = categoryMap(userId);
        List<StatResult.CategorySum> sums = recordRepository.sumGroupByCategory(userId, start, end).stream()
                .map(row -> {
                    Long categoryId = ((Number) row[0]).longValue();
                    FinCategory category = categories.get(categoryId);
                    return new StatResult.CategorySum(
                            categoryId,
                            category == null ? "已删除分类" : category.getName(),
                            category == null ? 0 : category.getType(),
                            (BigDecimal) row[1]);
                })
                .sorted(Comparator.comparing(StatResult.CategorySum::amount).reversed())
                .toList();
        return new StatResult(income, expense, income.subtract(expense), sums);
    }

    // ==================== 内部方法 ====================

    private void applyForm(FinRecord record, Long userId, RecordForm form) {
        FinCategory category = requireCategory(userId, form.categoryId());
        boolean trade = StringUtils.hasText(form.tradeSide());

        if (trade) {
            if (form.quantity() == null || form.quantity() <= 0) {
                throw new BizException(400, "成交数量必须大于 0");
            }
            if (form.tradePrice() == null || form.tradePrice().signum() <= 0) {
                throw new BizException(400, "成交价必须大于 0");
            }
            BigDecimal turnover = form.tradePrice().multiply(BigDecimal.valueOf(form.quantity()));
            boolean buy = "BUY".equalsIgnoreCase(form.tradeSide());
            // 印花税仅卖出收取，买入先清零再参与金额计算，避免把传上来的印花税也算进成本
            BigDecimal stampDuty = buy ? BigDecimal.ZERO : nz(form.stampDuty());
            BigDecimal fees = nz(form.commission()).add(stampDuty).add(nz(form.transferFee()));
            // 买入为支出（成交额 + 费用），卖出为收入（成交额 - 费用）
            record.setType(buy ? 2 : 1);
            record.setAmount(buy ? turnover.add(fees) : turnover.subtract(fees));
            record.setStockCode(form.stockCode());
            record.setTradeSide(buy ? "BUY" : "SELL");
            record.setQuantity(form.quantity());
            record.setTradePrice(form.tradePrice());
            record.setCommission(nz(form.commission()));
            record.setStampDuty(stampDuty);
            record.setTransferFee(nz(form.transferFee()));
        } else {
            checkType(form.type());
            if (form.amount() == null || form.amount().signum() <= 0) {
                throw new BizException(400, "金额必须大于 0");
            }
            record.setType(form.type());
            record.setAmount(form.amount());
            record.setStockCode(null);
            record.setTradeSide(null);
            record.setQuantity(null);
            record.setTradePrice(null);
            record.setCommission(null);
            record.setStampDuty(null);
            record.setTransferFee(null);
        }

        if (!category.getType().equals(record.getType())) {
            throw new BizException(400, "所选分类与收支类型不匹配");
        }
        record.setCategoryId(category.getId());
        record.setRecordDate(form.recordDate());
        record.setRemark(form.remark());
    }

    private Specification<FinRecord> buildSpec(Long userId, RecordQuery query) {
        return (root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("userId"), userId));
            if (query.getType() != null) {
                predicates.add(cb.equal(root.get("type"), query.getType()));
            }
            if (query.getCategoryId() != null) {
                predicates.add(cb.equal(root.get("categoryId"), query.getCategoryId()));
            }
            if (query.getStartDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("recordDate"), query.getStartDate()));
            }
            if (query.getEndDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("recordDate"), query.getEndDate()));
            }
            if (StringUtils.hasText(query.getKeyword())) {
                String like = "%" + query.getKeyword().trim() + "%";
                predicates.add(cb.or(
                        cb.like(root.get("remark"), like),
                        cb.like(root.get("stockCode"), like)));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Map<Long, FinCategory> categoryMap(Long userId) {
        return categoryRepository.findByUserIdOrderBySortAscIdAsc(userId).stream()
                .collect(Collectors.toMap(FinCategory::getId, Function.identity()));
    }

    private FinCategory requireCategory(Long userId, Long id) {
        return categoryRepository.findById(id)
                .filter(c -> c.getUserId().equals(userId))
                .orElseThrow(() -> new BizException(400, "分类不存在或无权访问"));
    }

    private FinRecord requireRecord(Long userId, Long id) {
        return recordRepository.findById(id)
                .filter(r -> r.getUserId().equals(userId))
                .orElseThrow(() -> new BizException(400, "记录不存在或无权访问"));
    }

    private void checkType(Integer type) {
        if (type == null || (type != 1 && type != 2)) {
            throw new BizException(400, "收支类型只能是 1（收入）或 2（支出）");
        }
    }

    private BigDecimal nz(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private RecordVO toVO(FinRecord r, Map<Long, FinCategory> categories) {
        FinCategory category = categories.get(r.getCategoryId());
        return new RecordVO(r.getId(), r.getCategoryId(),
                category == null ? "已删除分类" : category.getName(),
                r.getType(), r.getAmount(), r.getRecordDate(), r.getRemark(),
                r.getStockCode(), r.getTradeSide(), r.getQuantity(), r.getTradePrice(),
                r.getCommission(), r.getStampDuty(), r.getTransferFee(), r.getCreatedAt());
    }
}
