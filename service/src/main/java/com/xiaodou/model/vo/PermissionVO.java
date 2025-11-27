package com.xiaodou.model.vo;

import com.xiaodou.model.Permission;
import com.xiaodou.utils.DateTimeUtils;
import lombok.Data;

/**
 * 权限视图对象
 * <p>
 * 用于向前端返回权限信息，时间字段转换为毫秒时间戳
 * </p>
 */
@Data
public class PermissionVO {

    /**
     * 权限ID
     */
    private String id;

    /**
     * 权限名称
     */
    private String name;

    /**
     * 权限编码（唯一标识）
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

    /**
     * 创建时间（毫秒时间戳）
     */
    private Long createdAt;

    /**
     * 从实体对象转换
     */
    public static PermissionVO fromEntity(Permission permission) {
        if (permission == null) {
            return null;
        }
        PermissionVO vo = new PermissionVO();
        vo.setId(permission.getId());
        vo.setName(permission.getName());
        vo.setCode(permission.getCode());
        vo.setType(permission.getType());
        vo.setDescription(permission.getDescription());
        vo.setCreatedAt(DateTimeUtils.toTimestampAtUTC8(permission.getCreatedAt()));
        return vo;
    }
}
