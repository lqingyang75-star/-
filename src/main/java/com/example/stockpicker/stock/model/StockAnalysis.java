package com.example.stockpicker.stock.model;

import java.util.List;

/**
 * 个股分析报告。
 *
 * @param stock         股票信息
 * @param score         综合评分（均衡型策略，0~100）
 * @param rating        评级（强烈推荐/推荐/中性/谨慎/回避）
 * @param valuation     估值评价
 * @param profitability 盈利评价
 * @param risks         风险提示列表
 * @param suggestion    操作建议
 */
public record StockAnalysis(
        Stock stock,
        double score,
        String rating,
        String valuation,
        String profitability,
        List<String> risks,
        String suggestion
) {
}
