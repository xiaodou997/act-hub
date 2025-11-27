package com.xiaodou.log.model;

/**
 * 日志级别枚举
 *
 * @author xiaodou V=>dddou117
 * @version V1.0
 * @since 2025/10/9
 */
public enum LogLevel {
    DEBUG("DEBUG"), INFO("INFO"), WARN("WARN"), ERROR("ERROR"), AUDIT("AUDIT");

    private final String value;

    LogLevel(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static LogLevel fromValue(String value) {
        for (LogLevel level : values()) {
            if (level.value.equalsIgnoreCase(value)) {
                return level;
            }
        }
        return INFO;
    }
}

