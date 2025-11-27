package com.xiaodou.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaodou.model.Permission;
import com.xiaodou.model.dto.permission.PermissionCreateDTO;
import com.xiaodou.model.dto.permission.PermissionUpdateDTO;

import java.util.List;

/**
 * 权限表 服务类
 *
 * @author luoxiaodou
 * @since 2025-10-01
 */
public interface PermissionService extends IService<Permission> {

    /**
     * 分页查询权限列表
     *
     * @param page        分页参数
     * @param name        权限名称（模糊查询）
     * @param code        权限编码（模糊查询）
     * @param type        权限类型
     * @return 权限分页列表
     */
    IPage<Permission> pageList(Page<Permission> page, String name, String code, Byte type);

    /**
     * 获取所有权限列表（不分页）
     *
     * @return 权限列表
     */
    List<Permission> listAll();

    /**
     * 创建权限
     *
     * @param dto 创建请求
     * @return 创建的权限
     */
    Permission create(PermissionCreateDTO dto);

    /**
     * 更新权限
     *
     * @param dto 更新请求
     * @return 更新后的权限
     */
    Permission update(PermissionUpdateDTO dto);

    /**
     * 删除权限
     *
     * @param id 权限ID
     */
    void delete(String id);

    /**
     * 根据角色ID获取权限列表
     *
     * @param roleId 角色ID
     * @return 权限列表
     */
    List<Permission> getPermissionsByRoleId(String roleId);
}
