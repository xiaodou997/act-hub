package com.xiaodou.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaodou.model.User;
import com.xiaodou.model.dto.user.UserWithRolesDTO;
import com.xiaodou.model.dto.user.ChangePasswordDTO;
import com.xiaodou.model.dto.user.UserDTO;
import com.xiaodou.model.dto.user.UserPageDTO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * <p>
 * TLC 用户表（平台管理员 + 租户操作员） 服务类
 * </p>
 *
 * @author luoxiaodou
 * @since 2025-10-01
 */
public interface UserService extends IService<User> {

    /**
     * 根据用户ID查询用户及其角色信息
     *
     * @param username 用户名
     * @return 包含角色信息的用户DTO
     */
    UserWithRolesDTO selectUserWithRolesByUsername(String username);

    Page<UserDTO> selectUsersByPage(Page<UserDTO> page, UserPageDTO req);

    void changePassword(String userId, @Valid ChangePasswordDTO req);

    boolean removeOne(String id);

    void assignRoles(String userId, List<String> roleIds);
}
