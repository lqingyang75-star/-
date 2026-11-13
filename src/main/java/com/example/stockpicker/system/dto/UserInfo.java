package com.example.stockpicker.system.dto;

import java.util.Set;

/**
 * 登录人信息，前端存 Pinia，用于侧边栏与按钮权限控制。
 */
public record UserInfo(Long id, String username, String realName, Set<String> perms) {
}
