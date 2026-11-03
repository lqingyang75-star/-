package com.example.stockpicker.controller;

import com.example.stockpicker.model.CompareResult;
import com.example.stockpicker.model.PickResult;
import com.example.stockpicker.model.StockAnalysis;
import com.example.stockpicker.service.StockPickerService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

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
     * 按指定股票代码查询并打分（均衡型策略）。
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

    /**
     * 智能选股：扫描内置股票池，按策略评分并返回排名靠前的股票。
     *
     * @param strategy 策略代码：{@code value}（价值型）/ {@code growth}（成长型）/ {@code balanced}（均衡型），缺省均衡型
     * @param limit    返回条数，默认 10
     */
    @GetMapping("/screen")
    public List<PickResult> screen(
            @RequestParam(required = false) String strategy,
            @RequestParam(defaultValue = "10") int limit) {
        return stockPickerService.screen(strategy, limit);
    }

    /**
     * 多策略对比：对同一批股票分别按 价值/成长/均衡 策略打分。
     *
     * @param codes 逗号分隔的股票代码列表
     */
    @GetMapping("/compare")
    public List<CompareResult> compare(@RequestParam String codes) {
        return stockPickerService.compare(codes);
    }

    /**
     * 个股分析：生成一只股票的分析报告。
     *
     * @param code 6 位股票代码
     */
    @GetMapping("/analyze")
    public StockAnalysis analyze(@RequestParam String code) {
        try {
            return stockPickerService.analyze(code);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }
}
