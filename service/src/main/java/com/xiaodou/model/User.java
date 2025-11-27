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
 * TLC 用户表（平台管理员 + 租户操作员）
 * </p>
 *
 * @author luoxiaodou
 * @since 2025-10-01
 */
@Getter
@Setter
@ToString
@TableName("user")
public class User implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID，主键（建议 UUID）
     */
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 用户名（登录用）
     */
    private String username;

    /**
     * 邮箱（可选，可用于登录或通知）
     */
    private String email;

    /**
     * 密码哈希值（推荐 bcrypt/scrypt）
     */
    private String password;

    /**
     * 备注信息
     */
    private String remark;

    /**
     * 状态：0-禁用, 1-正常, 2-锁定
     */
    private Byte status;

    /**
     * 所属租户ID（role=tenant 时必填）
     */
    private String tenantId;

    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginAt;

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
