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
     */
    @GetMapping("/pick")
    public List<PickResult> pick(@RequestParam(defaultValue = "5") int limit) {
        return stockPickerService.pick(limit);
    }
}
