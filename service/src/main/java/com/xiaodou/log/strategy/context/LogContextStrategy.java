package com.xiaodou.log.strategy.context;

import com.xiaodou.log.model.LogContext;

/**
 * 日志上下文解析策略
 *
 * @author xiaodou V=>dddou117
 * @version V1.0
 * @since 2025/10/9
 */
@FunctionalInterface
public interface LogContextStrategy {

    /**
     * 解析日志上下文
     */
    LogContext resolveContext();

    /**
     * 策略名称
     */
    default String getStrategyName() {
        return this.getClass()
            .getSimpleName();
    }
}
