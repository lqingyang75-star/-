package com.example.stockpicker.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * JWT 配置项，对应 application.yml 中的 app.jwt。
 */
@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(String secret, long expireMinutes) {
}
