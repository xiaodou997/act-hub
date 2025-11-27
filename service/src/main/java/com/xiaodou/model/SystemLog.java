package com.xiaodou.model;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.xiaodou.log.model.LogLevel;
import com.xiaodou.log.model.UserType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统日志实体类 - MyBatis-Plus 优化版
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("system_log")  // MyBatis-Plus 表名注解
public class SystemLog implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 日志ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 链路追踪ID
     */
    @TableField("trace_id")
    private String traceId;

    /**
     * 跨度ID
     */
    @TableField("span_id")
    private String spanId;

    /**
     * 日志级别
     */
    @TableField(value = "log_level", insertStrategy = FieldStrategy.NOT_NULL)
    @Builder.Default
    private String logLevel = "INFO";

    /**
     * 操作人用户ID
     */
    @TableField(value = "operator_user_id", insertStrategy = FieldStrategy.NOT_NULL)
    private String operatorUserId;

    /**
     * 操作人所属租户ID
     */
    @TableField("tenant_id")
    private String tenantId;

    /**
     * 操作人类型
     */
    @TableField(value = "operator_user_type", insertStrategy = FieldStrategy.NOT_NULL)
    @Builder.Default
    private String operatorUserType = "SYSTEM";

    /**
     * 功能模块
     */
    @TableField(value = "module", insertStrategy = FieldStrategy.NOT_NULL)
    private String module;

    /**
     * 操作动作
     */
    @TableField(value = "action", insertStrategy = FieldStrategy.NOT_NULL)
    private String action;

    /**
     * 目标对象类型
     */
    @TableField("target_type")
    private String targetType;

    /**
     * 目标对象ID
     */
    @TableField("target_id")
    private String targetId;

    /**
     * 操作描述
     */
    @TableField("description")
    private String description;

    /**
     * 详细内容 (JSON格式)
     */
    @TableField("detail")
    private String detail;

    /**
     * 是否成功
     */
    @TableField(value = "success", insertStrategy = FieldStrategy.NOT_NULL)
    @Builder.Default
    private Integer success = 1;

    /**
     * 错误码
     */
    @TableField("error_code")
    private String errorCode;

    /**
     * 错误信息
     */
    @TableField("error_message")
    private String errorMessage;

    /**
     * 堆栈跟踪
     */
    @TableField("stack_trace")
    private String stackTrace;

    /**
     * 耗时(毫秒)
     */
    @TableField("cost_time_ms")
    private Long costTimeMs;

    /**
     * 操作IP
     */
    @TableField("ip_address")
    private String ipAddress;

    /**
     * User-Agent
     */
    @TableField("user_agent")
    private String userAgent;

    /**
     * 标签信息 (JSON格式)
     */
    @TableField("tags")
    private String tags;

    /**
     * 创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    // ========== 业务方法 ==========

    /**
     * 获取日志级别枚举
     */
    @JsonIgnore
    public LogLevel getLogLevelEnum() {
        return LogLevel.fromValue(logLevel);
    }

    /**
     * 设置日志级别枚举
     */
    public void setLogLevelEnum(LogLevel logLevel) {
        this.logLevel = logLevel.getValue();
    }

    /**
     * 获取用户类型枚举
     */
    @JsonIgnore
    public UserType getOperatorUserTypeEnum() {
        return UserType.fromValue(operatorUserType);
    }

    /**
     * 设置用户类型枚举
     */
    public void setOperatorUserTypeEnum(UserType userType) {
        this.operatorUserType = userType.getValue();
    }

    /**
     * 创建简单的成功日志 - 静态工厂方法
     */
    public static SystemLog createSuccess(String module, String action, String description, String operatorUserId,
        String tenantId) {
        return SystemLog.builder()
            .module(module)
            .action(action)
            .description(description)
            .operatorUserId(operatorUserId)
            .tenantId(tenantId)
            .build();
    }

    /**
     * 创建错误日志 - 静态工厂方法
     */
    public static SystemLog createError(String module, String action, String description, String operatorUserId,
        String tenantId, String errorCode, String errorMessage) {
        return SystemLog.builder()
            .module(module)
            .action(action)
            .description(description)
            .operatorUserId(operatorUserId)
            .tenantId(tenantId)
            .success(0)
            .errorCode(errorCode)
            .errorMessage(errorMessage)
            .logLevel("ERROR")
            .build();
    }
}