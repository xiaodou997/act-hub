package com.xiaodou.model.dto.syslog;

import lombok.Data;

/**
 * 系统日志分页查询返回 DTO
 * 用于前端展示，不包含敏感或大字段（如 stackTrace、tags）
 *
 * @author xiaodou V=>dddou117
 * @version V1.0
 * @since 2025/10/9
 */
@Data
public class SystemLogDTO {

    /**
     * 日志ID（UUID）
     */
    private String id;

    /**
     * 操作人用户ID
     */
    private String operatorUserId;

    /**
     * 操作人用户名（来自 tlc_user 表）
     */
    private String operatorUserName;

    /**
     * 操作人所属租户ID
     */
    private String operatorTenantId;

    /**
     * 租户名称（来自 tlc_tenant 表）
     */
    private String operatorTenantName;

    /**
     * 功能模块（如：USER、TENANT、PLUGIN）
     */
    private String module;

    /**
     * 操作动作（如：CREATE、UPDATE、DELETE）
     */
    private String action;

    /**
     * 目标对象类型（如：User、Role）
     */
    private String targetType;

    /**
     * 目标对象ID
     */
    private String targetId;

    /**
     * 操作描述（人类可读）
     */
    private String description;

    /**
     * 详细内容（JSON 格式，可用于审计）
     */
    private String detail;

    /**
     * 是否成功：1=成功，0=失败
     */
    private Integer success;

    /**
     * 错误信息（失败时有值）
     */
    private String errorMessage;

    /**
     * 异常堆栈跟踪（简化版，非完整 full stack trace）
     * 例如：仅包含 root cause 或前 3 行
     */
    private String stackTrace;

    /**
     * 标签集合（JSON 格式，如：{"env":"prod","region":"cn-hz"}）
     */
    private String tags;

    /**
     * 操作IP地址
     */
    private String ipAddress;

    /**
     * User-Agent 信息
     */
    private String userAgent;

    /**
     * 创建时间（毫秒时间戳，便于前端处理）
     */
    private Long createdAt;

    // ========== 新增业务字段 ==========

    /**
     * 链路追踪ID（用于分布式追踪）
     */
    private String traceId;

    /**
     * 跨度ID（Span ID）
     */
    private String spanId;

    /**
     * 日志级别（INFO/WARN/ERROR 等）
     */
    private String logLevel;

    /**
     * 操作人类型（SYSTEM/ADMIN/USER 等）
     */
    private String operatorUserType;

    /**
     * 错误码（业务错误码）
     */
    private String errorCode;

    /**
     * 操作耗时（毫秒）
     */
    private Long costTimeMs;
}
