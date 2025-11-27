package com.xiaodou.utils;

import com.xiaodou.constant.SystemRole;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色工具类
 *
 * @author xiaodou V=>dddou117
 * @version V1.0
 * @since 2025/10/9
 */
public class RoleUtils {

    // 管理员角色组
    public static final List<String> ADMIN_ROLES =
        Arrays.asList(SystemRole.SUPER_ADMIN.name(), SystemRole.SYS_ADMIN.name());

    /**
     * 检查是否为管理员角色
     */
    public static boolean isAdmin(List<String> roles) {
        return roles.stream()
            .anyMatch(ADMIN_ROLES::contains);
    }

    /**
     * 检查是否为租户角色
     */
    public static boolean isTenant(List<String> roles) {
        return roles.contains(SystemRole.TENANT.name());
    }

    /**
     * 转换角色为 Spring Security 格式（添加 ROLE_ 前缀）
     */
    public static List<String> toAuthorityList(List<String> roles) {
        return roles.stream()
            .map(role -> "ROLE_" + role)
            .collect(Collectors.toList());
    }

    /**
     * 从权限字符串中提取角色名称（移除 ROLE_ 前缀）
     */
    public static String extractRoleName(String authority) {
        if (authority.startsWith("ROLE_")) {
            return authority.substring(5);
        }
        return authority;
    }
}
