package com.example.stockpicker.system.service;

import com.example.stockpicker.common.PageResult;
import com.example.stockpicker.system.dto.OperLogQuery;
import com.example.stockpicker.system.dto.OperLogVO;
import com.example.stockpicker.system.entity.OperLog;
import com.example.stockpicker.system.repository.OperLogRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 操作日志查询服务。
 */
@Service
public class OperLogService {

    private final OperLogRepository operLogRepository;

    public OperLogService(OperLogRepository operLogRepository) {
        this.operLogRepository = operLogRepository;
    }

    @Transactional(readOnly = true)
    public PageResult<OperLogVO> page(OperLogQuery query) {
        int pageNo = Math.max(query.getPage(), 1);
        int size = Math.min(Math.max(query.getSize(), 1), 100);
        Pageable pageable = PageRequest.of(pageNo - 1, size, Sort.by(Sort.Direction.DESC, "id"));
        Page<OperLog> page = operLogRepository.findAll(buildSpec(query), pageable);
        List<OperLogVO> items = page.getContent().stream()
                .map(l -> new OperLogVO(l.getId(), l.getUsername(), l.getModule(), l.getAction(),
                        l.getMethod(), l.getUri(), l.getParams(), l.getDurationMs(),
                        l.isSuccess(), l.getErrorMsg(), l.getIp(), l.getCreatedAt()))
                .toList();
        return PageResult.of(page.getTotalElements(), pageNo, size, items);
    }

    private Specification<OperLog> buildSpec(OperLogQuery query) {
        return (root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.hasText(query.getModule())) {
                predicates.add(cb.equal(root.get("module"), query.getModule()));
            }
            if (StringUtils.hasText(query.getKeyword())) {
                String like = "%" + query.getKeyword().trim() + "%";
                predicates.add(cb.or(
                        cb.like(root.get("username"), like),
                        cb.like(root.get("action"), like),
                        cb.like(root.get("uri"), like)));
            }
            if (query.getStartDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"),
                        query.getStartDate().atStartOfDay()));
            }
            if (query.getEndDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"),
                        query.getEndDate().atTime(LocalTime.MAX)));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
