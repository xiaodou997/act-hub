package com.xiaodou.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiaodou.model.User;
import com.xiaodou.model.dto.user.UserWithRolesDTO;
import com.xiaodou.model.dto.user.UserDTO;
import com.xiaodou.model.dto.user.UserPageDTO;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * TLC 用户表（平台管理员 + 租户操作员） Mapper 接口
 * </p>
 *
 * @author luoxiaodou
 * @since 2025-10-01
 */
public interface UserMapper extends BaseMapper<User> {
    /**
     * 根据用户名查询用户及其角色信息
     *
     * @param username 用户名
     * @return 包含角色信息的用户DTO
     */
    UserWithRolesDTO selectUserWithRolesByUsername(@Param("username") String username);

    Page<UserDTO> selectUsersByPage(@Param("page") Page<UserDTO> page, @Param("query") UserPageDTO query);
}
