package com.example.stockpicker.system.dto;

import java.time.LocalDateTime;

/**
 * 操作日志出参。
 */
public record OperLogVO(
        Long id,
        String username,
        String module,
        String action,
        String method,
        String uri,
        String params,
        long durationMs,
        boolean success,
        String errorMsg,
        String ip,
        LocalDateTime createdAt) {
}
