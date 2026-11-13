package com.example.stockpicker.common;

/**
 * 业务异常。code 直接映射为返回体错误码，message 为可直接展示的中文提示。
 */
public class BizException extends RuntimeException {

    private final int code;

    public BizException(String message) {
        this(400, message);
    }

    public BizException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
