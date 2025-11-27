package com.xiaodou.auth.util;

import com.xiaodou.constant.SystemRole;
import com.xiaodou.model.LoginUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.List;

/**
 * 用户上下文持有工具类
 * <p>
 * 提供从Spring Security上下文中获取当前认证用户信息的静态方法。
 * 该类封装了用户信息的获取逻辑，避免业务代码直接操作SecurityContext。
 * </p>
 *
 * @author xiaodou V=>dddou117
 * @version V1.0
 * @since 2025/7/2
 */
public class UserContextHolder {

    /**
     * 获取当前登录用户完整信息
     * <p>
     * 从SecurityContext中获取Authentication对象，
     * 并转换为LoginUser对象返回
     * </p>
     *
     * @return LoginUser 当前登录用户对象，未认证时返回null
     */
    public static LoginUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext()
            .getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser) {
            return (LoginUser)authentication.getPrincipal();
        }
        return null;
    }

    /**
     * 获取当前用户ID
     *
     * @return String 用户ID，未认证时返回null
     */
    public static String getUserId() {
        LoginUser user = getCurrentUser();
        return user != null ? user.getUserId() : null;
    }

    /**
     * 获取当前用户所属租户ID
     *
     * @return String 租户ID，未认证或用户无团队时返回null
     */
    public static String getTenantId() {
        LoginUser user = getCurrentUser();
        return user != null ? user.getTenantId() : null;
    }

    /**
     * 获取当前用户角色列表
     *
     * @return List<String> 角色列表，未认证时返回空列表
     */
    public static List<String> getRoles() {
        LoginUser user = getCurrentUser();
        return user != null ? user.getRoles() : Collections.emptyList();
    }

    /**
     * 获取当前用户功能权限列表
     *
     * @return List<String> 功能权限列表，未认证时返回空列表
     */
    public static List<String> getPermissionList() {
        LoginUser user = getCurrentUser();
        return user != null ? user.getPermissionList() : Collections.emptyList();
    }

    /**
     * 获取当前客户端类型编码
     *
     * @return String 客户端类型编码，未认证时返回null
     */
    public static String getClientType() {
        LoginUser user = getCurrentUser();
        return user != null ? user.getClientType() : null;
    }

    /**
     * 检查当前用户是否拥有指定角色
     * <p>
     * 支持传入带前缀或不带前缀的角色名，如 "SUPER_ADMIN" 或 "ROLE_SUPER_ADMIN"
     * </p>
     *
     * @param role 角色名称
     * @return boolean 是否拥有该角色
     */
    public static boolean hasRole(String role) {
        List<String> roles = getRoles();
        if (roles == null || roles.isEmpty()) {
            return false;
        }
        // 统一去掉 ROLE_ 前缀后比较
        String normalizedRole = role.startsWith("ROLE_") ? role.substring(5) : role;
        return roles.stream()
            .map(r -> r.startsWith("ROLE_") ? r.substring(5) : r)
            .anyMatch(r -> r.equals(normalizedRole));
    }

    /**
     * 判断当前用户是否是超级管理员 (ROLE_SUPER_ADMIN)
     *
     * @return boolean 如果是超级管理员返回 true，否则 false
     */
    public static boolean isSuperAdmin() {
        return hasRole(SystemRole.SUPER_ADMIN.getAuthority());
    }

    /**
     * 判断当前用户是否是超级管理员 (ROLE_SUPER_ADMIN)
     *
     * @return boolean 如果是超级管理员返回 true，否则 false
     */
    public static boolean isAdmin() {
        return hasRole(SystemRole.SUPER_ADMIN.getAuthority()) || hasRole(SystemRole.SYS_ADMIN.getAuthority());
    }

    /**
     * 判断当前用户是否是租户 (ROLE_SUPER_ADMIN)
     *
     * @return boolean 如果是超级管理员返回 true，否则 false
     */
    public static boolean isTenant() {
        return hasRole(SystemRole.TENANT.getAuthority());
    }
}
