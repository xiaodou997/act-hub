package com.xiaodou.log.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 系统日志模块注解，用于标记需要自动注入系统日志的类。
 * <p>
 * 该注解可以指定日志模块的名称和日志字段的名称，默认字段名为 "sysLogger"。
 * 结合 {@link com.xiaodou.log.aspect.InjectLoggerBeanPostProcessor} 使用，实现日志字段的自动注入。
 *
 * @author xiaodou V=>dddou117
 * @version V1.0
 * @since 2025/10/9
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface InjectLogger {
    /**
     * 日志模块的名称，用于区分不同的日志模块。
     *
     * @return 模块名称
     */
    String value();

    /**
     * 日志字段的名称，默认为 "sysLogger"。
     *
     * @return 字段名称
     */
    String field() default "sysLogger";
}
