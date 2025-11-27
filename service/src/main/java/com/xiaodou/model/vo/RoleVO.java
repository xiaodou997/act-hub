package com.xiaodou.model.vo;

import com.xiaodou.model.Role;
import com.xiaodou.utils.DateTimeUtils;
import lombok.Data;

/**
 * 角色视图对象
 * <p>
 * 用于向前端返回角色信息，时间字段转换为毫秒时间戳
 * </p>
 */
@Data
public class RoleVO {

    /**
     * 角色ID
     */
    private String id;

    /**
     * 角色名称
     */
    private String name;

    /**
     * 角色编码
     */
    private String code;

    /**
     * 描述
     */
    private String description;

    /**
     * 是否平台级角色：1-是，0-否
     */
    private Byte isPlatform;

    /**
     * 创建时间（毫秒时间戳）
     */
    private Long createdAt;

    /**
     * 从实体对象转换
     */
    public static RoleVO fromEntity(Role role) {
        if (role == null) {
            return null;
        }
        RoleVO vo = new RoleVO();
        vo.setId(role.getId());
        vo.setName(role.getName());
        vo.setCode(role.getCode());
        vo.setDescription(role.getDescription());
        vo.setIsPlatform(role.getIsPlatform());
        vo.setCreatedAt(DateTimeUtils.toTimestampAtUTC8(role.getCreatedAt()));
        return vo;
    }
}
