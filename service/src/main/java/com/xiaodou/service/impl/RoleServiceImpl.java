package com.xiaodou.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaodou.exception.AppException;
import com.xiaodou.result.ResultCodeEnum;
import com.xiaodou.mapper.RoleMapper;
import com.xiaodou.mapper.RolePermissionMapper;
import com.xiaodou.mapper.UserRoleMapper;
import com.xiaodou.model.Role;
import com.xiaodou.model.RolePermission;
import com.xiaodou.model.UserRole;
import com.xiaodou.model.dto.role.RoleCreateDTO;
import com.xiaodou.model.dto.role.RoleUpdateDTO;
import com.xiaodou.service.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 角色表 服务实现类
 * </p>
 *
 * @author luoxiaodou
 * @since 2025-10-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {

    private final RolePermissionMapper rolePermissionMapper;
    private final UserRoleMapper userRoleMapper;

    @Override
    public IPage<Role> pageList(Page<Role> page, String name, String code, String description) {
        log.info("分页查询角色列表");
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(name)) {
            wrapper.like(Role::getName, name);
        }
        if (StringUtils.hasText(code)) {
            wrapper.like(Role::getCode, code);
        }
        if (StringUtils.hasText(description)) {
            wrapper.like(Role::getDescription, description);
        }
        wrapper.orderByDesc(Role::getCreatedAt);
        return page(page, wrapper);
    }

    @Override
    public List<Role> listAll() {
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(Role::getCode);
        return list(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Role create(RoleCreateDTO dto) {
        log.info("创建角色: {}", dto);

        // 检查角色编码是否已存在
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Role::getCode, dto.getCode());
        if (count(wrapper) > 0) {
            throw new AppException(ResultCodeEnum.BAD_REQUEST, "角色编码已存在");
        }

        Role role = new Role();
        role.setName(dto.getName());
        role.setCode(dto.getCode());
        role.setDescription(dto.getDescription());
        role.setIsPlatform(dto.getIsPlatform());

        save(role);
        log.info("角色创建成功, id: {}", role.getId());
        return role;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Role update(RoleUpdateDTO dto) {
        log.info("更新角色: {}", dto);

        Role role = getById(dto.getId());
        if (role == null) {
            throw new AppException(ResultCodeEnum.DATA_NOT_FOUND, "角色不存在");
        }

        // 检查角色编码是否与其他角色重复
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Role::getCode, dto.getCode())
               .ne(Role::getId, dto.getId());
        if (count(wrapper) > 0) {
            throw new AppException(ResultCodeEnum.BAD_REQUEST, "角色编码已存在");
        }

        role.setName(dto.getName());
        role.setCode(dto.getCode());
        role.setDescription(dto.getDescription());
        if (dto.getIsPlatform() != null) {
            role.setIsPlatform(dto.getIsPlatform());
        }

        updateById(role);
        log.info("角色更新成功, id: {}", role.getId());
        return role;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(String id) {
        log.info("删除角色, id: {}", id);

        Role role = getById(id);
        if (role == null) {
            throw new AppException(ResultCodeEnum.DATA_NOT_FOUND, "角色不存在");
        }

        // 检查是否有用户关联了此角色
        LambdaQueryWrapper<UserRole> urWrapper = new LambdaQueryWrapper<>();
        urWrapper.eq(UserRole::getRoleId, id);
        long userCount = userRoleMapper.selectCount(urWrapper);
        if (userCount > 0) {
            throw new AppException(ResultCodeEnum.BAD_REQUEST, "该角色已被用户关联，无法删除");
        }

        // 删除角色权限关联
        LambdaQueryWrapper<RolePermission> rpWrapper = new LambdaQueryWrapper<>();
        rpWrapper.eq(RolePermission::getRoleId, id);
        rolePermissionMapper.delete(rpWrapper);

        // 删除角色
        removeById(id);
        log.info("角色删除成功, id: {}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignPermissions(String roleId, List<String> permissionIds) {
        log.info("为角色分配权限, roleId: {}, permissionIds: {}", roleId, permissionIds);

        Role role = getById(roleId);
        if (role == null) {
            throw new AppException(ResultCodeEnum.DATA_NOT_FOUND, "角色不存在");
        }

        // 先删除原有的权限关联
        LambdaQueryWrapper<RolePermission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RolePermission::getRoleId, roleId);
        rolePermissionMapper.delete(wrapper);

        // 添加新的权限关联
        if (!CollectionUtils.isEmpty(permissionIds)) {
            List<RolePermission> rolePermissions = new ArrayList<>();
            for (String permissionId : permissionIds) {
                RolePermission rp = new RolePermission();
                rp.setRoleId(roleId);
                rp.setPermissionId(permissionId);
                rolePermissions.add(rp);
            }
            for (RolePermission rp : rolePermissions) {
                rolePermissionMapper.insert(rp);
            }
        }

        log.info("角色权限分配成功, roleId: {}, count: {}", roleId,
                 permissionIds == null ? 0 : permissionIds.size());
    }

    @Override
    public List<String> getPermissionIds(String roleId) {
        LambdaQueryWrapper<RolePermission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RolePermission::getRoleId, roleId);
        List<RolePermission> rolePermissions = rolePermissionMapper.selectList(wrapper);
        return rolePermissions.stream()
            .map(RolePermission::getPermissionId)
            .collect(Collectors.toList());
    }
}
