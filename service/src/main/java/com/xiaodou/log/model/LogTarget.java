package com.xiaodou.log.model;

/**
 * 日志目标信息
 *
 * @author xiaodou V=>dddou117
 * @version V1.0
 * @since 2025/10/9
 */
public record LogTarget(String targetType, String targetId) {
    public static LogTarget of(String targetType, String targetId) {
        return new LogTarget(targetType, targetId);
    }

    public boolean hasTarget() {
        return targetType != null && targetId != null;
    }
}
