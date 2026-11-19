package com.example.stockpicker.system.aspect;

import com.example.stockpicker.security.LoginUser;
import com.example.stockpicker.security.UserContext;
import com.example.stockpicker.system.annotation.OperLogRecord;
import com.example.stockpicker.system.entity.OperLog;
import com.example.stockpicker.system.repository.OperLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * 操作日志切面：记录接口调用人、接口、耗时与结果。
 * 日志落库失败不影响业务主流程。
 */
@Aspect
@Component
public class OperLogAspect {

    private static final Logger log = LoggerFactory.getLogger(OperLogAspect.class);
    private static final int MAX_PARAMS_LENGTH = 1000;
    private static final int MAX_ERROR_LENGTH = 500;

    private final OperLogRepository operLogRepository;
    private final ObjectMapper objectMapper;

    public OperLogAspect(OperLogRepository operLogRepository, ObjectMapper objectMapper) {
        this.operLogRepository = operLogRepository;
        this.objectMapper = objectMapper;
    }

    @Around("@annotation(record)")
    public Object around(ProceedingJoinPoint joinPoint, OperLogRecord record) throws Throwable {
        long start = System.currentTimeMillis();
        boolean success = true;
        String errorMsg = null;
        try {
            return joinPoint.proceed();
        } catch (Throwable throwable) {
            success = false;
            errorMsg = throwable.getMessage();
            throw throwable;
        } finally {
            saveLog(joinPoint, record, System.currentTimeMillis() - start, success, errorMsg);
        }
    }

    private void saveLog(ProceedingJoinPoint joinPoint, OperLogRecord record,
                         long durationMs, boolean success, String errorMsg) {
        try {
            OperLog operLog = new OperLog();
            LoginUser user = UserContext.get();
            operLog.setUsername(user == null ? "anonymous" : user.username());
            operLog.setModule(record.module());
            operLog.setAction(record.action());
            operLog.setMethod(joinPoint.getSignature().toShortString());
            operLog.setUri(currentUri());
            operLog.setParams(serializeArgs(joinPoint.getArgs()));
            operLog.setDurationMs(durationMs);
            operLog.setSuccess(success);
            operLog.setErrorMsg(truncate(errorMsg, MAX_ERROR_LENGTH));
            operLog.setIp(currentIp());
            operLogRepository.save(operLog);
        } catch (Exception e) {
            log.warn("操作日志写入失败：{}", e.getMessage());
        }
    }

    private String serializeArgs(Object[] args) {
        String text = Arrays.stream(args)
                .filter(arg -> arg != null
                        && !(arg instanceof ServletRequest)
                        && !(arg instanceof ServletResponse))
                .map(this::toJson)
                .collect(Collectors.joining(", "));
        return truncate(text, MAX_PARAMS_LENGTH);
    }

    private String toJson(Object arg) {
        try {
            return objectMapper.writeValueAsString(arg);
        } catch (Exception e) {
            return String.valueOf(arg);
        }
    }

    private String truncate(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }

    private HttpServletRequest currentRequest() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes == null ? null : attributes.getRequest();
    }

    private String currentUri() {
        HttpServletRequest request = currentRequest();
        return request == null ? null : request.getRequestURI();
    }

    private String currentIp() {
        HttpServletRequest request = currentRequest();
        if (request == null) {
            return null;
        }
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
