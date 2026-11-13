package com.example.stockpicker.finance.controller;

import com.example.stockpicker.common.PageResult;
import com.example.stockpicker.common.R;
import com.example.stockpicker.finance.dto.RecordForm;
import com.example.stockpicker.finance.dto.RecordQuery;
import com.example.stockpicker.finance.dto.RecordVO;
import com.example.stockpicker.finance.dto.StatResult;
import com.example.stockpicker.finance.service.FinanceService;
import com.example.stockpicker.security.RequirePerm;
import com.example.stockpicker.security.UserContext;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public R<RecordVO> create(@RequestBody @Valid RecordForm form) {
        return R.ok(financeService.createRecord(UserContext.require().id(), form));
    }

    @PutMapping("/{id}")
    @RequirePerm("fin:record:edit")
    public R<RecordVO> update(@PathVariable Long id, @RequestBody @Valid RecordForm form) {
        return R.ok(financeService.updateRecord(UserContext.require().id(), id, form));
    }

    @DeleteMapping("/{id}")
    @RequirePerm("fin:record:delete")
    public R<Void> delete(@PathVariable Long id) {
        financeService.deleteRecord(UserContext.require().id(), id);
        return R.ok();
    }
}
