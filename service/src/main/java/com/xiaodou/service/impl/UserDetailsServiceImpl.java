package com.xiaodou.service.impl;

import com.xiaodou.mapper.PermissionMapper;
import com.xiaodou.model.LoginUser;
import com.xiaodou.model.Role;
import com.xiaodou.model.dto.user.UserWithRolesDTO;
import com.xiaodou.service.UserService;
import com.xiaodou.token.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户详情服务实现类
 * <p>
 * 实现Spring Security的UserDetailsService接口，用于加载用户详情信息
 * 主要用于Spring Security认证过程中根据用户名获取用户信息和权限
 * </p>
 *
 * @author xiaodou
 * @version 1.0
 * @since JDK 21
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserService userService;
    private final PermissionMapper permissionMapper;
    private final TokenService tokenService;

    /**
     * 根据用户名加载用户详情
     * <p>
     * 此方法会查询用户基本信息及其角色信息，并转换为Spring Security认识的UserDetails对象
     * </p>
     *
     * @param username 用户名（此处使用邮箱作为唯一标识）
     * @return UserDetails 包含用户信息和权限的Spring Security用户对象
     * @throws UsernameNotFoundException 当用户不存在时抛出
     * @throws DisabledException 当用户状态异常时抛出
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 通过用户服务查询用户及其角色信息
        UserWithRolesDTO user = userService.selectUserWithRolesByUsername(username);
        if (user == null) {
            log.error("user not found");
            throw new UsernameNotFoundException("用户不存在");
        }

        // 检查用户状态
        if (user.getStatus() != 1) {
            log.error("user status is: {}", user.getStatus());
            throw new DisabledException("用户状态异常");
        }

        List<String> roleList = user.getRoles()
            .stream()
            .map(Role::getCode)
            .toList();

        // 查询用户的权限列表
        List<String> permissionList = permissionMapper.selectPermissionCodesByUserId(user.getUserId());
        log.debug("用户 {} 的权限列表: {}", username, permissionList);

        // 保存权限到 Redis 缓存
        tokenService.saveUserPermissions(user.getUserId(), permissionList);

        // 创建 LoginUser 并设置权限列表
        LoginUser loginUser = new LoginUser(user.getUserId(), user.getUsername(), user.getTenantId(), user.getPassword(), roleList);
        loginUser.setPermissionList(permissionList);

        return loginUser;
    }
}
