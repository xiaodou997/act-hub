package com.xiaodou.config.mybatis;

import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import com.xiaodou.auth.util.UserContextHolder;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;

import java.util.Arrays;
import java.util.List;

/**
 * 自定义租户处理器
 * <p>
 * 核心职责：
 * 1. 从 UserContextHolder 获取当前操作的租户ID。
 * 2. 定义哪些表不需要进行租户过滤（例如：用户表、角色表等全局表）。
 * </p>
 */
@Slf4j
public class ProjectTenantLineHandler implements TenantLineHandler {

    /**
     * 不需要应用租户过滤的表名列表
     */
    private static final List<String> IGNORED_TABLES = Arrays.asList(
        "tenant",
        "user",
        "role",
        "menu",
        "permission",
        "role_permission",
        "user_role",
        "sys_setting",
        "wechat_user"
        // 注意：我们新创建的 task, task_participation, task_target_user, ai_app_type
        // 都不在此列表中，因此它们会自动应用租户过滤。
    );

    /**
     * 获取当前用户的租户ID
     *
     * @return 租户ID表达式
     */
    @Override
    public Expression getTenantId() {
        // 从安全上下文中获取当前用户的租户ID
        String tenantId = UserContextHolder.getTenantId();

        // 如果是超级管理员或未登录用户，tenantId可能为空，此时不进行过滤
        if (tenantId == null) {
            return null;
        }
        return new StringValue(tenantId);
    }

    /**
     * 获取租户ID在数据库中的列名
     *
     * @return 列名
     */
    @Override
    public String getTenantIdColumn() {
        return "tenant_id";
    }

    /**
     * 判断某张表是否需要忽略租户过滤
     *
     * @param tableName 表名
     * @return 如果在忽略列表中，返回 true
     */
    @Override
    public boolean ignoreTable(String tableName) {
        return IGNORED_TABLES.contains(tableName);
    }
}
