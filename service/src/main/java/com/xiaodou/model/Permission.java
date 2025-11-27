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
 * 权限表
 * </p>
 *
 * @author luoxiaodou
 * @since 2025-10-01
 */
@Getter
@Setter
@ToString
@TableName("permission")
public class Permission implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 权限ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 权限名称，如“删除激活码”
     */
    private String name;

    /**
     * 权限编码（唯一标识，用于代码判断）
     */
    private String code;

    /**
     * 类型：1-菜单, 2-操作按钮, 3-API
     */
    private Byte type;

    /**
     * 描述
     */
    private String description;

    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
