package com.example.stockpicker.security;

import com.example.stockpicker.common.BizException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * JWT 拦截器：解析 token、装载当前登录人与权限码，请求结束后清理线程上下文。
 */
@Component
public class JwtInterceptor implements HandlerInterceptor {

    private static final String BEARER = "Bearer ";

    private final JwtUtils jwtUtils;
    private final LoginUserLoader loginUserLoader;

    public JwtInterceptor(JwtUtils jwtUtils, LoginUserLoader loginUserLoader) {
        this.jwtUtils = jwtUtils;
        this.loginUserLoader = loginUserLoader;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 跨域预检直接放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith(BEARER)) {
            throw new BizException(401, "未登录或登录已过期");
        }
        Long userId = jwtUtils.parseUserId(token.substring(BEARER.length()));
        UserContext.set(loginUserLoader.load(userId));
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        UserContext.clear();
    }
}
