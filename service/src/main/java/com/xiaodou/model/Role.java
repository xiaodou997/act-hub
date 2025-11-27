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
 * 角色表
 * </p>
 *
 * @author luoxiaodou
 * @since 2025-10-01
 */
@Getter
@Setter
@ToString
@TableName("role")
public class Role implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 角色ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 角色名称，如“平台管理员”
     */
    private String name;

    /**
     * 角色编码，如 admin, tenant_operator
     */
    private String code;

    /**
     * 描述
     */
    private String description;

    /**
     * 是否平台级角色：1-是（如admin），0-否（租户角色）
     */
    private Byte isPlatform;

    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
