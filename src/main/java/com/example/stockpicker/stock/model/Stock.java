package com.example.stockpicker.stock.model;

/**
 * 股票基础信息。
 *
 * @param code          股票代码
 * @param name          股票名称
 * @param price         最新价
 * @param pe            市盈率
 * @param pb            市净率
 * @param roe           净资产收益率（%）
 * @param changePercent 当日涨跌幅（%）
 */
public record Stock(
        String code,
        String name,
        double price,
        double pe,
        double pb,
        double roe,
        double changePercent
) {
}
