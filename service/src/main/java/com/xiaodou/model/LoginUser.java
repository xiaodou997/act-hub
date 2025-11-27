package com.xiaodou.model;

import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Spring Security 用户认证信息封装类
 * <p>
 * 实现了 Spring Security 的 UserDetails 接口，用于封装系统登录用户的认证信息，
 * 包含用户身份标识、权限信息和客户端类型等核心安全要素。
 * <p>
 * 设计特点：
 * <ul>
 * <li>支持多角色权限体系（roles）</li>
 * <li>支持细粒度功能权限控制（featureCodeList）</li>
 * <li>区分客户端类型（用户端/管理端）实现差异化权限控制</li>
 * <li>与 Spring Security 框架深度集成</li>
 * </ul>
 * <p>
 * 安全特性：
 * <ul>
 * <li>账户状态默认为有效（所有状态检查方法返回 true）</li>
 * <li>密码字段返回 null（使用 token 认证机制）</li>
 * <li>权限信息基于角色动态生成</li>
 * </ul>
 *
 * @author xiaodou V=>dddou117
 * @version V1.0
 * @see UserDetails Spring Security 用户详情接口
 * @since 2025/7/2
 */
@Data
public class LoginUser implements UserDetails {
    /**
     * 用户唯一标识符
     * <p>
     * 对应系统用户表的主键，用于：
     * <ul>
     * <li>用户身份识别</li>
     * <li>关联用户详细信息</li>
     * <li>审计日志记录</li>
     * </ul>
     */
    private String userId;

    /**
     * 用户登录名
     * <p>
     * 用于系统登录认证的用户名，要求：
     * <ul>
     * <li>全局唯一</li>
     * <li>大小写敏感</li>
     * <li>符合系统命名规范</li>
     * </ul>
     */
    private String username;

    /**
     * 所属团队ID
     * <p>
     * 标识用户所属的业务团队，用于：
     * <ul>
     * <li>数据隔离控制</li>
     * <li>团队权限继承</li>
     * <li>业务数据归属</li>
     * </ul>
     */
    private String tenantId;

    private String password;

    /**
     * 用户角色列表
     * <p>
     * 存储用户拥有的系统角色标识，例如：
     * <ul>
     * <li>ROLE_ADMIN - 系统管理员</li>
     * <li>ROLE_USER - 普通用户</li>
     * <li>ROLE_TEAM_LEADER - 团队负责人</li>
     * </ul>
     * <p>
     * 特点：
     * <ul>
     * <li>支持多角色叠加</li>
     * <li>角色名需符合 Spring Security 规范（建议 ROLE_ 前缀）</li>
     * <li>自动转换为 GrantedAuthority 权限</li>
     * </ul>
     */
    private List<String> roles;

    /**
     * 功能权限代码列表
     * <p>
     * 存储用户可访问的功能点权限代码，实现细粒度权限控制：
     * <ul>
     * <li>user:create - 创建用户权限</li>
     * <li>team:delete - 删除团队权限</li>
     * <li>report:view - 查看报表权限</li>
     * </ul>
     * <p>
     * 使用场景：
     * <ul>
     * <li>前端按钮级权限控制</li>
     * <li>API 接口访问权限校验</li>
     * <li>数据操作权限验证</li>
     * </ul>
     */
    private List<String> permissionList = new ArrayList<>();

    /**
     * Spring Security 权限集合
     * <p>
     * 由 roles 列表自动转换生成的权限集合：
     * <ul>
     * <li>每个角色转换为 SimpleGrantedAuthority</li>
     * <li>用于 Spring Security 的权限校验</li>
     * <li>通过 @PreAuthorize 等注解使用</li>
     * </ul>
     */
    private Collection<? extends GrantedAuthority> authorities;

    /**
     * 客户端类型
     * <p>
     * 标识用户登录的客户端类型，用于实现差异化权限控制：
     * <ul>
     * <li>USER - 用户端（普通用户操作界面）</li>
     * <li>ADMIN - 管理端（系统管理后台）</li>
     * </ul>
     * <p>
     * 应用场景：
     * <ul>
     * <li>区分不同客户端的权限范围</li>
     * <li>实现客户端特定的安全策略</li>
     * <li>审计日志记录客户端来源</li>
     * </ul>
     */
    private String clientType; // ➕ 用户端 or 管理端

    public LoginUser() {
    }

    /**
     * 构造登录用户对象
     * <p>
     * 初始化用户认证信息，自动将角色列表转换为 Spring Security 权限集合
     *
     * @param userId 用户唯一标识符（必填）
     * @param username 用户登录名（必填）
     * @param tenantId 所属租户ID（可为空）
     * @param password 密码
     * @param roles 用户角色列表（必填，不可为空）
     * @throws IllegalArgumentException 当 roles 为空时抛出
     */
    public LoginUser(String userId, String username, String tenantId, String password, List<String> roles) {
        this.userId = userId;
        this.username = username;
        this.tenantId = tenantId;
        this.password = password;
        // roles 保持原始值（不带前缀），与数据库一致
        this.roles = roles;
        // authorities 添加 ROLE_ 前缀，供 Spring Security 使用
        this.authorities = roles.stream()
            .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toList());
    }

    /**
     * 获取用户权限集合
     * <p>
     * 实现 UserDetails 接口方法，返回用户拥有的所有权限
     *
     * @return 权限集合（基于角色生成的 GrantedAuthority）
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    /**
     * 获取用户密码
     */
    @Override
    public String getPassword() {
        return password;
    }

    /**
     * 获取用户名
     * <p>
     * 实现 UserDetails 接口方法，返回登录用户名
     *
     * @return 用户登录名
     */
    @Override
    public String getUsername() {
        return username;
    }

    /**
     * 检查账户是否未过期
     * <p>
     * 实现 UserDetails 接口方法，本系统账户默认永不过期
     *
     * @return true（账户未过期）
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * 检查账户是否未锁定
     * <p>
     * 实现 UserDetails 接口方法，本系统账户默认不锁定
     *
     * @return true（账户未锁定）
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * 检查凭证是否未过期
     * <p>
     * 实现 UserDetails 接口方法，本系统使用 token 认证，
     * token 过期由网关层处理，此处返回 true
     *
     * @return true（凭证未过期）
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * 检查账户是否启用
     * <p>
     * 实现 UserDetails 接口方法，本系统账户默认启用
     *
     * @return true（账户已启用）
     */
    @Override
    public boolean isEnabled() {
        return true;
    }

    /**
     * 获取显示名称（用于日志）
     */
    public String getDisplayName() {
        return StringUtils.hasText(this.getUsername()) ? this.getUsername() : String.valueOf(this.getUserId());
    }
}

