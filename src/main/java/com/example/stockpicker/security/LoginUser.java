package com.example.stockpicker.security;

import java.util.Set;

/**
 * 当前登录人信息，放在 ThreadLocal 中供各层读取。
 */
public record LoginUser(Long id, String username, String realName, Set<String> perms) {

    public boolean hasPerm(String perm) {
        return perms != null && perms.contains(perm);
    }
}
