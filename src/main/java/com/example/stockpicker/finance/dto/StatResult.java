package com.example.stockpicker.finance.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * 区间统计结果。
 */
public record StatResult(
        BigDecimal totalIncome,
        BigDecimal totalExpense,
        BigDecimal balance,
        List<CategorySum> categories) {

    public record CategorySum(
            Long categoryId,
            String categoryName,
            Integer type,
            BigDecimal amount) {
    }
}
