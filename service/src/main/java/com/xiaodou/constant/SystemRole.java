package com.xiaodou.constant;

import lombok.Getter;

/**
 * 系统角色枚举（System Role Enum）
 * <p>
 * 所有系统角色在此统一定义，每个枚举值对应一个标准角色标识（带 ROLE_ 前缀）。
 * </p>
 *
 * @author xiaodou V=>dddou117
 * @since 1.0
 */
@Getter
public enum SystemRole {

    /**
     * 超级管理员角色
     */
    SUPER_ADMIN("ROLE_SUPER_ADMIN"),

    /**
     * 系统管理员角色
     */
    SYS_ADMIN("ROLE_SYS_ADMIN"),

    /**
     * 租户角色
     */
    TENANT("ROLE_TENANT"),

    /**
     * 普通用户角色
     */
    USER("ROLE_USER");

    /**
     * -- GETTER --
     *  获取 Spring Security 兼容的角色字符串（如 "ROLE_SUPER_ADMIN"）
     */
    private final String authority;

    SystemRole(String authority) {
        this.authority = authority;
    }

    /**
     * 可选：用于 Jackson 序列化为字符串（配合 @JsonValue）
     */
    @Override
    public String toString() {
        return this.authority;
    }
}