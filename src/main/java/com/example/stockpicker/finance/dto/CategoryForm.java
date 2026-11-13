package com.example.stockpicker.finance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 分类新增/编辑入参。type：1 收入 2 支出。
 */
public record CategoryForm(
        @NotBlank(message = "分类名称不能为空")
        @Size(max = 50, message = "分类名称不能超过 50 个字符")
        String name,

        @NotNull(message = "请选择分类类型")
        Integer type,

        Integer sort) {
}
