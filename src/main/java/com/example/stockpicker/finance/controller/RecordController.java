package com.example.stockpicker.finance.controller;

import com.alibaba.excel.EasyExcel;
import com.example.stockpicker.common.PageResult;
import com.example.stockpicker.common.R;
import com.example.stockpicker.finance.dto.CategoryPiePoint;
import com.example.stockpicker.finance.dto.IdsForm;
import com.example.stockpicker.finance.dto.RecordExportRow;
import com.example.stockpicker.finance.dto.RecordForm;
import com.example.stockpicker.finance.dto.RecordQuery;
import com.example.stockpicker.finance.dto.RecordVO;
import com.example.stockpicker.finance.dto.StatResult;
import com.example.stockpicker.finance.dto.TrendPoint;
import com.example.stockpicker.finance.service.FinanceService;
import com.example.stockpicker.security.RequirePerm;
import com.example.stockpicker.security.UserContext;
import com.example.stockpicker.system.annotation.OperLogRecord;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/finance/records")
public class RecordController {

    private final FinanceService financeService;

    public RecordController(FinanceService financeService) {
        this.financeService = financeService;
    }

    @GetMapping
    @RequirePerm("fin:record:list")
    public R<PageResult<RecordVO>> page(RecordQuery query) {
        return R.ok(financeService.pageRecords(UserContext.require().id(), query));
    }

    @GetMapping("/stat")
    @RequirePerm("fin:record:list")
    public R<StatResult> stat(RecordQuery query) {
        return R.ok(financeService.stat(UserContext.require().id(), query));
    }

    @PostMapping
    @RequirePerm("fin:record:add")
    @OperLogRecord(module = "财务收支", action = "新增记录")
    public R<RecordVO> create(@RequestBody @Valid RecordForm form) {
        return R.ok(financeService.createRecord(UserContext.require().id(), form));
    }

    @PutMapping("/{id}")
    @RequirePerm("fin:record:edit")
    @OperLogRecord(module = "财务收支", action = "编辑记录")
    public R<RecordVO> update(@PathVariable Long id, @RequestBody @Valid RecordForm form) {
        return R.ok(financeService.updateRecord(UserContext.require().id(), id, form));
    }

    @DeleteMapping("/{id}")
    @RequirePerm("fin:record:delete")
    @OperLogRecord(module = "财务收支", action = "删除记录")
    public R<Void> delete(@PathVariable Long id) {
        financeService.deleteRecord(UserContext.require().id(), id);
        return R.ok();
    }

    @PostMapping("/batch-delete")
    @RequirePerm("fin:record:batch")
    @OperLogRecord(module = "财务收支", action = "批量删除")
    public R<Void> batchDelete(@RequestBody @Valid IdsForm form) {
        financeService.deleteRecords(UserContext.require().id(), form.ids());
        return R.ok();
    }

    /**
     * 导出 Excel：带筛选条件或选中 ids，二者都为空时导出全部。
     */
    @GetMapping("/export")
    @RequirePerm("fin:record:export")
    @OperLogRecord(module = "财务收支", action = "导出Excel")
    public void export(RecordQuery query,
                       @RequestParam(required = false) String ids,
                       HttpServletResponse response) throws IOException {
        List<Long> idList = StringUtils.hasText(ids)
                ? Arrays.stream(ids.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .map(Long::valueOf)
                        .toList()
                : List.of();
        List<RecordExportRow> rows = financeService.exportRecords(UserContext.require().id(), query, idList)
                .stream()
                .map(this::toExportRow)
                .toList();

        String fileName = URLEncoder.encode("收支记录.xlsx", StandardCharsets.UTF_8);
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        response.setHeader("Content-Disposition",
                "attachment;filename=" + fileName + ";filename*=utf-8''" + fileName);
        EasyExcel.write(response.getOutputStream(), RecordExportRow.class)
                .sheet("收支记录")
                .doWrite(rows);
    }

    /** 近 N 个月收支趋势，看板折线图使用 */
    @GetMapping("/chart/monthly-trend")
    public R<List<TrendPoint>> monthlyTrend(@RequestParam(defaultValue = "6") int months) {
        return R.ok(financeService.monthlyTrend(UserContext.require().id(), months));
    }

    /** 近 7 天收支，看板柱状图使用 */
    @GetMapping("/chart/weekly-bar")
    public R<List<TrendPoint>> weeklyBar() {
        return R.ok(financeService.weeklyTrend(UserContext.require().id()));
    }

    /** 分类占比，看板饼图使用 */
    @GetMapping("/chart/category-pie")
    public R<List<CategoryPiePoint>> categoryPie(RecordQuery query) {
        return R.ok(financeService.categoryPie(UserContext.require().id(), query));
    }

    private RecordExportRow toExportRow(RecordVO vo) {
        RecordExportRow row = new RecordExportRow();
        row.setRecordDate(vo.recordDate() == null ? null : vo.recordDate().toString());
        row.setTypeText(Integer.valueOf(1).equals(vo.type()) ? "收入" : "支出");
        row.setCategoryName(vo.categoryName());
        row.setAmount(vo.amount());
        row.setStockCode(vo.stockCode());
        row.setTradeSideText("BUY".equalsIgnoreCase(vo.tradeSide()) ? "买入"
                : "SELL".equalsIgnoreCase(vo.tradeSide()) ? "卖出" : null);
        row.setQuantity(vo.quantity());
        row.setTradePrice(vo.tradePrice());
        row.setCommission(vo.commission());
        row.setStampDuty(vo.stampDuty());
        row.setTransferFee(vo.transferFee());
        row.setRemark(vo.remark());
        return row;
    }
}
