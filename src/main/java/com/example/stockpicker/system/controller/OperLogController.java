package com.example.stockpicker.system.controller;

import com.example.stockpicker.common.PageResult;
import com.example.stockpicker.common.R;
import com.example.stockpicker.security.RequirePerm;
import com.example.stockpicker.system.dto.OperLogQuery;
import com.example.stockpicker.system.dto.OperLogVO;
import com.example.stockpicker.system.service.OperLogService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/system/logs")
public class OperLogController {

    private final OperLogService operLogService;

    public OperLogController(OperLogService operLogService) {
        this.operLogService = operLogService;
    }

    @GetMapping
    @RequirePerm("system:log:list")
    public R<PageResult<OperLogVO>> page(OperLogQuery query) {
        return R.ok(operLogService.page(query));
    }
}
