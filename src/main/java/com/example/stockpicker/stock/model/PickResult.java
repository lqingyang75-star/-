package com.example.stockpicker.stock.model;

/**
 * 选股结果：股票 + 评分 + 命中理由。
 *
 * @param stock  股票信息
 * @param score  综合评分（越高越优）
 * @param reason 命中理由
 */
public record PickResult(Stock stock, double score, String reason) {
}
