package com.xiaodou.model.dto.user;

import com.xiaodou.model.Role;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @ClassName: UserWithRolesDTO
 * @Description: TODO
 * @Author: xiaodou V=>dddou117
 * @Date: 2025/5/15
 * @Version: V1.0
 * @JDK: JDK21
 */
@Data
public class UserWithRolesDTO {
    private String userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 密码哈希值
     */
    private String password;

    /**
     * 状态：0-禁用, 1-正常, 2-锁定
     */
    private Integer status;

    /**
     * 租户ID，超级管理员可为空
     */
    private String tenantId;

    /**
     * 备注信息
     */
    private String remark;

    /**
     * 角色列表
     */
    private List<Role> roles;

    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginAt;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}

