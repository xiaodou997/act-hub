package com.xiaodou.model;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 菜单权限表
 * </p>
 *
 * @author luoxiaodou
 * @since 2025-11-16
 */
@Getter
@Setter
@ToString
public class Menu implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 菜单ID，主键
     */
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 父菜单ID，0表示为顶级菜单
     */
    private String parentId;

    /**
     * 菜单名称，用于显示（如“系统管理”）
     */
    private String name;

    /**
     * 菜单类型（0:目录, 1:菜单, 2:按钮）
     */
    private Byte type;

    /**
     * 路由地址（类型为菜单时必填，如“/system/user”）
     */
    private String path;

    /**
     * 组件键名（类型为菜单时必填，如"MenuManagement"）
     * <p>
     * 前端根据此键名映射到实际组件路径，提高安全性和灵活性
     * </p>
     */
    private String componentName;

    /**
     * 权限标识（关联权限表的code，用于控制显隐）
     */
    private String permissionCode;

    /**
     * 菜单图标
     */
    private String icon;

    /**
     * 显示排序，数字越小越靠前
     */
    private Integer sortOrder;

    /**
     * 菜单状态（1:正常, 0:禁用）
     */
    private Byte status;

    /**
     * 是否可见（1:可见, 0:隐藏，隐藏后不会在侧边栏显示）
     */
    private Byte isVisible;

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
