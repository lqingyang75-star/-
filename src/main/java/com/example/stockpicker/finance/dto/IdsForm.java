package com.example.stockpicker.finance.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * 批量操作入参：记录 id 集合。
 */
public record IdsForm(@NotEmpty(message = "请至少选择一条记录") List<Long> ids) {
}
