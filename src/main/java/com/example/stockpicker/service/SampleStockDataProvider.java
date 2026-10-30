package com.example.stockpicker.service;

import com.example.stockpicker.model.Stock;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 内置样例数据源（雏形阶段使用，非真实行情）。
 */
@Component
public class SampleStockDataProvider implements StockDataProvider {

    private static final List<Stock> STOCKS = List.of(
            new Stock("600519", "贵州茅台", 1680.00, 28.5, 8.2, 32.0, 1.20),
            new Stock("000651", "格力电器", 42.30, 8.1, 1.6, 22.5, -0.80),
            new Stock("601318", "中国平安", 48.60, 9.4, 1.1, 12.3, 0.45),
            new Stock("000333", "美的集团", 68.90, 13.2, 2.8, 20.1, 0.90),
            new Stock("600036", "招商银行", 35.70, 6.5, 0.9, 15.8, -0.30),
            new Stock("002415", "海康威视", 31.20, 22.4, 3.5, 16.2, 2.10),
            new Stock("600887", "伊利股份", 27.80, 18.6, 3.1, 18.9, -1.10),
            new Stock("601899", "紫金矿业", 15.40, 14.8, 3.9, 25.6, 3.40)
    );

    @Override
    public List<Stock> allStocks() {
        return STOCKS;
    }
}
