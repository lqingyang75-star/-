package com.example.stockpicker.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RoleForm(
        @NotBlank(message = "角色名称不能为空")
        @Size(max = 50, message = "角色名称不能超过 50 个字符")
        String name,

        @NotBlank(message = "角色标识不能为空")
        @Size(max = 50, message = "角色标识不能超过 50 个字符")
        String code,

        @Size(max = 200, message = "备注不能超过 200 个字符")
        String remark) {
}
