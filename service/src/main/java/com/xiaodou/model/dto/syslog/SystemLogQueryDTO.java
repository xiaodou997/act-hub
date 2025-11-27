package com.xiaodou.model.dto.syslog;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * <p>
 * TLC系统操作日志表
 * </p>
 *
 * @author luoxiaodou
 * @since 2025-10-01
 */
@Getter
@Setter
@ToString
public class SystemLogQueryDTO {
    private int pageNum = 1;
    private int pageSize = 10;

    /**
     * 日志ID
     */
    private String id;

    private String operatorUserName;

    private String operatorTenantName;

    /**
     * 操作人所属租户ID
     */
    private String operatorTenantId;

    /**
     * 功能模块（如 tenant, order, activation_code）
     */
    private String module;

    /**
     * 操作动作（如 create, update, delete, activate, disable）
     */
    private String action;

    /**
     * 操作简要描述
     */
    private String description;

    /**
     * 是否成功：1-成功, 0-失败
     */
    private Byte success;

    /**
     * 失败错误信息
     */
    private String errorMessage;

    private String stackTrace;

    private String tags;

    /**
     * 操作IP（支持IPv6）
     */
    private String ipAddress;

    private String logLevel;

    private String operatorUserType;

    private Long createdAtStart; // 例如：1717020800000L
    private Long createdAtEnd;   // 例如：1717107199999L
}
