package com.xiaodou.model.auth;

import com.xiaodou.model.Role;
import com.xiaodou.model.User;
import com.xiaodou.model.dto.user.UserWithRolesDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户信息视图对象（Value Object）
 * <p>封装用户基础信息、组织关系、权限及业务数据，用于传输层展示</p>
 *
 * @author xiaodou V=>dddou117
 * @version V1.0
 * @since 2025/5/9
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoVO {
    /**
     * 用户唯一标识符
     */
    private String userId;

    /**
     * 用户登录名/显示名
     */
    private String username;

    /**
     * 所属租户ID（role=tenant 时必填）
     */
    private String tenantId;

    private String email;

    private Byte status;
    
    /**
     * 用户角色列表（如：["ADMIN", "MEMBER"]）
     */
    @Builder.Default
    private List<String> roles = new ArrayList<>();

    /**
     * 用户权限列表
     */
    @Builder.Default
    private List<String> permissions = new ArrayList<>();

    /**
     * 最后登录时间（UTC时区）
     */
    private LocalDateTime lastLoginAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    /**
     * 从包含角色信息的DTO构建用户视图对象
     *
     * @param user 包含角色信息的用户数据传输对象
     * @return 构建完成的用户视图对象
     */
    public static UserInfoVO fromUserWithRolesDTO(UserWithRolesDTO user) {
        if (user == null)
            return null;

        List<String> roleList = user.getRoles()
            .stream()
            .map(Role::getCode)
            .toList();

        return UserInfoVO.builder()
            .userId(user.getUserId())
            .username(user.getUsername())
            .tenantId(user.getTenantId())
            .roles(roleList)
            .lastLoginAt(user.getLastLoginAt())
            .build();
    }

    public static UserInfoVO fromUser(User user) {
        if (user == null) {
            return null;
        }

        UserInfoVO userVO = new UserInfoVO();
        userVO.setUserId(user.getId());
        userVO.setUsername(user.getUsername());
        userVO.setEmail(user.getEmail());
        userVO.setStatus(user.getStatus());
        userVO.setLastLoginAt(user.getLastLoginAt());
        userVO.setCreatedAt(user.getCreatedAt());
        userVO.setUpdatedAt(user.getUpdatedAt());
        return userVO;
    }
}
