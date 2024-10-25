package com.yunze.business.annotation;

import java.lang.annotation.*;

/**
 * auther:
 * return:
 * 描述： 日志注解自定义
 * 时间： 2021/5/26 11:26
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LogAnnotation {
    String action() default "";
}
