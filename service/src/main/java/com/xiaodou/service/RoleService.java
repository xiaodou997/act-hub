package com.xiaodou.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaodou.model.Role;
import com.xiaodou.model.dto.role.RoleCreateDTO;
import com.xiaodou.model.dto.role.RoleUpdateDTO;

import java.util.List;

/**
 * <p>
 * 角色表 服务类
 * </p>
 *
 * @author luoxiaodou
 * @since 2025-10-01
 */
public interface RoleService extends IService<Role> {

    /**
     * 分页查询角色列表
     */
    IPage<Role> pageList(Page<Role> page, String name, String code, String description);

    /**
     * 获取所有角色列表（用于用户分配角色时的下拉选项）
     */
    List<Role> listAll();

    /**
     * 创建角色
     */
    Role create(RoleCreateDTO dto);

    /**
     * 更新角色
     */
    Role update(RoleUpdateDTO dto);

    /**
     * 删除角色
     */
    void delete(String id);

    /**
     * 为角色分配权限
     *
     * @param roleId        角色ID
     * @param permissionIds 权限ID列表
     */
    void assignPermissions(String roleId, List<String> permissionIds);

    /**
     * 获取角色的权限ID列表
     *
     * @param roleId 角色ID
     * @return 权限ID列表
     */
    List<String> getPermissionIds(String roleId);
}
