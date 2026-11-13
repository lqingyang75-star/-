package com.example.stockpicker.stock.model;

import java.util.List;

/**
 * 多策略对比结果：一只股票在各策略下的打分。
 *
 * @param stock      股票信息
 * @param strategies 各策略打分（按 价值/成长/均衡 顺序）
 */
public record CompareResult(Stock stock, List<StrategyScore> strategies) {
}
