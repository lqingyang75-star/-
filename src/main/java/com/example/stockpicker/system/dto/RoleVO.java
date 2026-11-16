package com.example.stockpicker.system.dto;

public record RoleVO(
        Long id,
        String name,
        String code,
        String remark,
        int menuCount) {
}
