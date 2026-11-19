package com.example.stockpicker.finance.dto;

import java.math.BigDecimal;

/**
 * 分类占比饼图数据点。
 */
public record CategoryPiePoint(String name, BigDecimal value, Integer type) {
}
