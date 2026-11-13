package com.example.stockpicker.security;

import com.example.stockpicker.common.BizException;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

/**
 * 权限校验切面：拦截 @RequirePerm 注解，校验当前登录人是否拥有该权限码。
 * 与前端 v-permission 指令形成前后端双重校验。
 */
@Aspect
@Component
public class PermAspect {

    @Before("@annotation(requirePerm)")
    public void check(JoinPoint joinPoint, RequirePerm requirePerm) {
        LoginUser user = UserContext.require();
        if (!user.hasPerm(requirePerm.value())) {
            throw new BizException(403, "无权限执行该操作");
        }
    }
}
