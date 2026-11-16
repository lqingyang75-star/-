package com.example.stockpicker.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * 用户新增/编辑入参。编辑时忽略 username 与 password，改密码走重置接口。
 */
public record UserForm(
        @NotBlank(message = "账号不能为空")
        @Size(max = 50, message = "账号不能超过 50 个字符")
        String username,

        String password,

        @Size(max = 50, message = "姓名不能超过 50 个字符")
        String realName,

        Integer status,

        List<Long> roleIds) {
}
