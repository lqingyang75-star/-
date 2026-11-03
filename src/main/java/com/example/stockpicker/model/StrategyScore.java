package com.example.stockpicker.model;

/**
 * 某一策略下的打分结果。
 *
 * @param strategy      策略代码（如 {@code value}）
 * @param strategyLabel 策略中文名（如 {@code 价值型}）
 * @param score         该策略得分（0~100）
 * @param reason        命中理由
 */
public record StrategyScore(
        String strategy,
        String strategyLabel,
        double score,
        String reason
) {
}
