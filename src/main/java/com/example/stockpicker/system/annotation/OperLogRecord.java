package com.example.stockpicker.system.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 操作日志注解：标注在需要留痕的接口方法上，由切面统一落库。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OperLogRecord {

    /** 所属模块，如「财务收支」 */
    String module();

    /** 操作动作，如「新增记录」 */
    String action();
}
