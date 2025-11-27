package com.xiaodou.log.strategy.context;

import com.xiaodou.log.model.LogContext;
/**
 * 空上下文解析策略
 *
 * @author xiaodou V=>dddou117
 * @version V1.0
 * @since 2025/10/9
 */
public class EmptyContextStrategy implements LogContextStrategy {

    @Override
    public LogContext resolveContext() {
        return LogContext.empty();
    }

    @Override
    public String getStrategyName() {
        return "EMPTY";
    }
}
