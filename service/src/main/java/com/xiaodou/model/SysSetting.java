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
 * 系统设置表
 * </p>
 *
 * @author luoxiaodou
 * @since 2025-11-16
 */
@Getter
@Setter
@ToString
@TableName("sys_setting")
public class SysSetting implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 设置项的唯一键 (例如: site.name)
     */
    private String settingKey;

    /**
     * 设置项的值
     */
    private String settingValue;

    /**
     * 设置项的显示名称
     */
    private String settingName;

    /**
     * 设置项分组 (basic, contact, wechat)
     */
    private String settingGroup;

    /**
     * 分组显示名称 (例如: 基础设置)
     */
    private String groupName;

    /**
     * 设置项的类型 (text, number, boolean, json, textarea, richtext)
     */
    private String settingType;

    /**
     * 排序字段
     */
    private Integer sort;

    /**
     * 简短描述
     */
    private String description;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
