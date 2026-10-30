package com.example.stockpicker.service;

import com.example.stockpicker.model.Stock;

import java.util.List;

/**
 * 股票数据来源。雏形阶段用内置样例数据，后续可替换为真实行情接口。
 */
public interface StockDataProvider {

    List<Stock> allStocks();
}
