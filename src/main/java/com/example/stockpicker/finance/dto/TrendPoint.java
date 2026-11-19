package com.example.stockpicker.finance.dto;

import java.math.BigDecimal;

/**
 * 看板折线/柱状图数据点：按月份或按日期聚合的收入与支出。
 */
public record TrendPoint(String label, BigDecimal income, BigDecimal expense) {
}
