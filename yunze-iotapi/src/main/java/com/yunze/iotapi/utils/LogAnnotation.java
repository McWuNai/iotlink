package com.yunze.iotapi.utils;

import java.lang.annotation.*;

/**
 * 日志注解自定义
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LogAnnotation {
    String action() default "";
}
