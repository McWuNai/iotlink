package com.yunze.business.annotation;

import java.lang.annotation.*;

/**
 * auther:
 * return:
 * 描述：自定义实体类所需要的bean 属性
 * 时间： 2021/2/2 13:05
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ExcelColumnAnnotation {
    /**
     * Excel标题
     *
     * @return
     * @author Lynch
     */
    String value() default "";

    /**
     * Excel从左往右排列位置
     *
     * @return
     * @author Lynch
     */
    int col() default 0;
}
