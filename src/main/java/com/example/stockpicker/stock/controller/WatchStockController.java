package com.example.stockpicker.stock.controller;

import com.example.stockpicker.common.R;
import com.example.stockpicker.security.RequirePerm;
import com.example.stockpicker.security.UserContext;
import com.example.stockpicker.stock.model.PickResult;
import com.example.stockpicker.stock.model.WatchForm;
import com.example.stockpicker.stock.service.WatchStockService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 自选股接口：按登录用户隔离。
 */
@RestController
@RequestMapping("/api/stocks/watchlist")
public class WatchStockController {

    private final WatchStockService watchStockService;

    public WatchStockController(WatchStockService watchStockService) {
        this.watchStockService = watchStockService;
    }

    @GetMapping
    @RequirePerm("stock:watch:list")
    public R<List<PickResult>> list() {
        return R.ok(watchStockService.list(UserContext.require().id()));
    }

    /** 仅返回代码，供行情页回显星标状态 */
    @GetMapping("/codes")
    @RequirePerm("stock:watch:list")
    public R<List<String>> codes() {
        return R.ok(watchStockService.codes(UserContext.require().id()));
    }

    @PostMapping
    @RequirePerm("stock:watch:add")
    public R<Void> add(@RequestBody WatchForm form) {
        watchStockService.add(UserContext.require().id(), form.code());
        return R.ok();
    }

    @DeleteMapping("/{code}")
    @RequirePerm("stock:watch:delete")
    public R<Void> remove(@PathVariable String code) {
        watchStockService.remove(UserContext.require().id(), code);
        return R.ok();
    }
}
