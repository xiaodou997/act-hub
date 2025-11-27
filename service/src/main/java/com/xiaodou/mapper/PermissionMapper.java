package com.xiaodou.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiaodou.model.Permission;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 权限表 Mapper 接口
 * </p>
 *
 * @author luoxiaodou
 * @since 2025-10-01
 */
public interface PermissionMapper extends BaseMapper<Permission> {

    /**
     * 根据用户ID查询用户拥有的所有权限code列表
     * <p>
     * 通过 user_role -> role_permission -> permission 关联查询
     * </p>
     *
     * @param userId 用户ID
     * @return 权限code列表
     */
    List<String> selectPermissionCodesByUserId(@Param("userId") String userId);

    /**
     * 根据角色ID列表查询权限code列表
     *
     * @param roleIds 角色ID列表
     * @return 权限code列表
     */
    List<String> selectPermissionCodesByRoleIds(@Param("roleIds") List<String> roleIds);
}
