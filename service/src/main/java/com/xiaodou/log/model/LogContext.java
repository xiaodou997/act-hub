package com.xiaodou.log.model;

import java.util.Map;

/**
 * 日志上下文信息
 *
 * @author xiaodou V=>dddou117
 * @version V1.0
 * @since 2025/10/9
 */
public record LogContext(String traceId, String spanId, String ipAddress, String userAgent,
                         Map<String, String> headers) {
    public static LogContext empty() {
        return new LogContext(null, null, null, null, Map.of());
    }

    public static LogContext of(String traceId, String spanId, String ipAddress, String userAgent) {
        return new LogContext(traceId, spanId, ipAddress, userAgent, Map.of());
    }
}
