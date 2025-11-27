package com.xiaodou.log.model;

/**
 * 日志用户信息
 *
 * @author xiaodou V=>dddou117
 * @version V1.0
 * @since 2025/10/9
 */
public record LogUser(String userId, String tenantId, UserType userType) {
    /**
     * 匿名用户
     */
    public static LogUser anonymous() {
        return new LogUser("anonymous", "public", UserType.ANONYMOUS);
    }

    public static LogUser system() {
        return new LogUser("system", "system", UserType.SYSTEM);
    }

    public static LogUser of(String userId, String tenantId) {
        return new LogUser(userId, tenantId, UserType.SYSTEM);
    }
}
