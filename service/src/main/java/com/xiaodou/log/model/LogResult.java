package com.xiaodou.log.model;

/**
 * 执行结果信息
 *
 * @author xiaodou V=>dddou117
 * @version V1.0
 * @since 2025/10/9
 */
public record LogResult(boolean success, String errorCode, String errorMessage, String stackTrace, Long costTimeMs) {
    public static LogResult success(Long costTimeMs) {
        return new LogResult(true, null, null, null, costTimeMs);
    }

    public static LogResult error(String errorCode, String errorMessage, String stackTrace, Long costTimeMs) {
        return new LogResult(false, errorCode, errorMessage, stackTrace, costTimeMs);
    }
}
