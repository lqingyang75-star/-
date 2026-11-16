package com.example.stockpicker.system.dto;

import java.time.LocalDateTime;
import java.util.List;

public record UserVO(
        Long id,
        String username,
        String realName,
        Integer status,
        List<RoleBrief> roles,
        LocalDateTime createdAt) {

    public record RoleBrief(Long id, String name, String code) {
    }
}
