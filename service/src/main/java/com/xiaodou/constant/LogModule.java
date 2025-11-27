package com.xiaodou.constant;

import lombok.Getter;

/**
 * 系统日志模块枚举
 * <p>
 * 所有业务模块应在此统一注册，确保日志模块命名规范、可维护、可审计。
 * </p>
 */
@Getter
public enum LogModule {

    // 格式：MODULE_NAME("显示名称")
    /**
     * 租户管理
     */
    TENANT_MANA("租户管理"),
    USER_MANAGEMENT("用户管理"),
    ORDER_SYSTEM("订单系统"),
    PERMISSION_CONTROL("权限控制"),
    SECURITY_AUDIT("安全审计"),
    FILE_STORAGE("文件存储"),
    SYSTEM_CONFIG("系统配置");

    private final String displayName;

    LogModule(String displayName) {
        this.displayName = displayName;
    }

    /**
     * 获取用于日志记录的模块名（即 displayName）
     */
    public String getModuleName() {
        return this.displayName;
    }
}
