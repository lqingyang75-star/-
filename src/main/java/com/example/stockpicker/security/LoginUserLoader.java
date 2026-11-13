package com.example.stockpicker.security;

/**
 * 按用户 id 装载登录人与权限码。由 system 模块实现，避免 security 层反向依赖具体模块。
 */
public interface LoginUserLoader {

    LoginUser load(Long userId);
}
