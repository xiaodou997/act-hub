package com.xiaodou.log.model;

/**
 * 用户类型枚举
 *
 * @author xiaodou V=>dddou117
 * @version V1.0
 * @since 2025/10/9
 */
public enum UserType {
    /**
     * 系统用户
     */
    SYSTEM("SYSTEM", "系统用户"),
    /**
     * 匿名用户
     */
    ANONYMOUS("ANONYMOUS", "匿名用户"),
    /**
     * API用户
     */
    API_USER("API_USER", "API用户"),
    /**
     * 批处理任务
     */
    BATCH_JOB("BATCH_JOB", "批处理任务"),
    /**
     * 外部系统
     */
    EXTERNAL_SYSTEM("EXTERNAL_SYSTEM", "外部系统");

    private final String value;
    private final String description;

    UserType(String value, String description) {
        this.value = value;
        this.description = description;
    }

    public String getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

    public static UserType fromValue(String value) {
        for (UserType type : values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        return SYSTEM;
    }
}
