package com.xiaodou.model;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * AI 应用配置主表
 * </p>
 *
 * @author luoxiaodou
 * @since 2025-11-17
 */
@Getter
@Setter
@ToString
@TableName("ai_application")
public class AiApplication implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID（雪花算法生成）
     */
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 应用名称，如：文本摘要
     */
    private String name;

    /**
     * 应用描述
     */
    private String description;

    /**
     * Spring Bean 名称，如 summaryAiHandler
     */
    private String handlerBean;

    /**
     * 应用类型ID（关联 hgs_ai_application_type）
     */
    private String typeId;

    /**
     * 租户ID，多租户场景使用
     */
    private String tenantId;

    /**
     * 入参 JSON Schema，用于校验/表单渲染
     */
    private String paramSchema;

    /**
     * 出参 JSON Schema，用于文档/前端展示
     */
    private String resultSchema;

    /**
     * 应用版本，用于灰度/迭代
     */
    private String version;

    /**
     * 单次调用价格（单位：分，0=免费）
     */
    private Integer price;

    /**
     * 是否启用（1=启用，0=禁用）
     */
    private Byte enabled;

    /**
     * 执行超时时间（毫秒）
     */
    private Integer timeoutMs;

    /**
     * 系统级扩展配置（非用户参数）
     */
    private String config;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    /**
     * 最后更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
