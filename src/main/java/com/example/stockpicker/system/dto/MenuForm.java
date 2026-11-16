package com.example.stockpicker.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MenuForm(
        Long parentId,

        @NotBlank(message = "菜单名称不能为空")
        @Size(max = 50, message = "菜单名称不能超过 50 个字符")
        String name,

        @NotNull(message = "请选择菜单类型")
        Integer type,

        @Size(max = 100, message = "路由路径不能超过 100 个字符")
        String path,

        @Size(max = 120, message = "组件路径不能超过 120 个字符")
        String component,

        @Size(max = 100, message = "权限标识不能超过 100 个字符")
        String perms,

        @Size(max = 50, message = "图标不能超过 50 个字符")
        String icon,

        Integer sort,

        Integer status) {
}
