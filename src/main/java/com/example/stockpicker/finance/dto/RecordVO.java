package com.example.stockpicker.finance.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 收支记录出参。
 */
public record RecordVO(
        Long id,
        Long categoryId,
        String categoryName,
        Integer type,
        BigDecimal amount,
        LocalDate recordDate,
        String remark,
        String stockCode,
        String tradeSide,
        Integer quantity,
        BigDecimal tradePrice,
        BigDecimal commission,
        BigDecimal stampDuty,
        BigDecimal transferFee,
        LocalDateTime createdAt) {
}
