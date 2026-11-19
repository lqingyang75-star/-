package com.example.stockpicker.system.entity;

import com.example.stockpicker.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * 操作日志。记录谁在什么时间调用了哪个接口、耗时多少、是否成功。
 */
@Entity
@Table(name = "sys_oper_log")
public class OperLog extends BaseEntity {

    @Column(length = 50)
    private String username;

    @Column(length = 50)
    private String module;

    @Column(length = 50)
    private String action;

    @Column(length = 200)
    private String method;

    @Column(length = 200)
    private String uri;

    @Column(columnDefinition = "TEXT")
    private String params;

    @Column(name = "duration_ms")
    private long durationMs;

    @Column(nullable = false)
    private boolean success;

    @Column(name = "error_msg", length = 500)
    private String errorMsg;

    @Column(length = 64)
    private String ip;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getParams() {
        return params;
    }

    public void setParams(String params) {
        this.params = params;
    }

    public long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(long durationMs) {
        this.durationMs = durationMs;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }
}
