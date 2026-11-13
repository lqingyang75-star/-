package com.example.stockpicker.finance.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 收支记录入参。
 * 普通收支：填 type + amount。
 * 证券交易：填 tradeSide + quantity + tradePrice + 各项费用，金额与收支类型由后端计算，前端传的 type/amount 会被忽略。
 */
public record RecordForm(
        @NotNull(message = "请选择分类")
        Long categoryId,

        Integer type,

        BigDecimal amount,

        @NotNull(message = "请选择记账日期")
        LocalDate recordDate,

        @Size(max = 255, message = "备注不能超过 255 个字符")
        String remark,

        String stockCode,

        String tradeSide,

        Integer quantity,

        BigDecimal tradePrice,

        BigDecimal commission,

        BigDecimal stampDuty,

        BigDecimal transferFee) {
}
