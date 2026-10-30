package com.example.stockpicker.controller;

import com.example.stockpicker.model.PickResult;
import com.example.stockpicker.service.StockPickerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 选股接口。
 */
@RestController
@RequestMapping("/api/stocks")
public class StockController {

    private final StockPickerService stockPickerService;

    public StockController(StockPickerService stockPickerService) {
        this.stockPickerService = stockPickerService;
    }

    /**
     * 获取推荐股票列表。
     *
     * @param limit 返回条数，默认 5
     * @param codes 可选，逗号分隔的股票代码列表（如 {@code 600519,000651}）。
     *              提供时按实时行情仅对这些股票打分；缺省时使用内置样例数据
     */
    @GetMapping("/pick")
    public List<PickResult> pick(
            @RequestParam(defaultValue = "5") int limit,
            @RequestParam(required = false) String codes) {
        return stockPickerService.pick(limit, codes);
    }
}
