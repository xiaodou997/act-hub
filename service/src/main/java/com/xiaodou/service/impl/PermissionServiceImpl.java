package com.xiaodou.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaodou.exception.AppException;
import com.xiaodou.result.ResultCodeEnum;
import com.xiaodou.mapper.PermissionMapper;
import com.xiaodou.mapper.RolePermissionMapper;
import com.xiaodou.model.Permission;
import com.xiaodou.model.RolePermission;
import com.xiaodou.model.dto.permission.PermissionCreateDTO;
import com.xiaodou.model.dto.permission.PermissionUpdateDTO;
import com.xiaodou.service.PermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 权限表 服务实现类
 *
 * @author luoxiaodou
 * @since 2025-10-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionServiceImpl extends ServiceImpl<PermissionMapper, Permission> implements PermissionService {

    private final RolePermissionMapper rolePermissionMapper;

    @Override
    public IPage<Permission> pageList(Page<Permission> page, String name, String code, Byte type) {
        log.info("查询权限分页列表, name: {}, code: {}, type: {}", name, code, type);
        LambdaQueryWrapper<Permission> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(name)) {
            wrapper.like(Permission::getName, name);
        }
        if (StringUtils.hasText(code)) {
            wrapper.like(Permission::getCode, code);
        }
        if (type != null) {
            wrapper.eq(Permission::getType, type);
        }
        wrapper.orderByDesc(Permission::getCreatedAt);
        return page(page, wrapper);
    }

    @Override
    public List<Permission> listAll() {
        LambdaQueryWrapper<Permission> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(Permission::getCode);
        return list(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Permission create(PermissionCreateDTO dto) {
        log.info("创建权限: {}", dto);

        // 检查权限编码是否已存在
        LambdaQueryWrapper<Permission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Permission::getCode, dto.getCode());
        if (count(wrapper) > 0) {
            throw new AppException(ResultCodeEnum.BAD_REQUEST, "权限编码已存在");
        }

        Permission permission = new Permission();
        permission.setName(dto.getName());
        permission.setCode(dto.getCode());
        permission.setType(dto.getType());
        permission.setDescription(dto.getDescription());

        save(permission);
        log.info("权限创建成功, id: {}", permission.getId());
        return permission;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Permission update(PermissionUpdateDTO dto) {
        log.info("更新权限: {}", dto);

        Permission permission = getById(dto.getId());
        if (permission == null) {
            throw new AppException(ResultCodeEnum.DATA_NOT_FOUND, "权限不存在");
        }

        // 检查权限编码是否与其他权限重复
        LambdaQueryWrapper<Permission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Permission::getCode, dto.getCode())
               .ne(Permission::getId, dto.getId());
        if (count(wrapper) > 0) {
            throw new AppException(ResultCodeEnum.BAD_REQUEST, "权限编码已存在");
        }

        permission.setName(dto.getName());
        permission.setCode(dto.getCode());
        permission.setType(dto.getType());
        permission.setDescription(dto.getDescription());

        updateById(permission);
        log.info("权限更新成功, id: {}", permission.getId());
        return permission;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(String id) {
        log.info("删除权限, id: {}", id);

        Permission permission = getById(id);
        if (permission == null) {
            throw new AppException(ResultCodeEnum.DATA_NOT_FOUND, "权限不存在");
        }

        // 检查是否有角色关联了此权限
        LambdaQueryWrapper<RolePermission> rpWrapper = new LambdaQueryWrapper<>();
        rpWrapper.eq(RolePermission::getPermissionId, id);
        long roleCount = rolePermissionMapper.selectCount(rpWrapper);
        if (roleCount > 0) {
            throw new AppException(ResultCodeEnum.BAD_REQUEST, "该权限已被角色关联，无法删除");
        }

        removeById(id);
        log.info("权限删除成功, id: {}", id);
    }

    @Override
    public List<Permission> getPermissionsByRoleId(String roleId) {
        // 查询角色关联的权限ID
        LambdaQueryWrapper<RolePermission> rpWrapper = new LambdaQueryWrapper<>();
        rpWrapper.eq(RolePermission::getRoleId, roleId);
        List<RolePermission> rolePermissions = rolePermissionMapper.selectList(rpWrapper);

        if (rolePermissions.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> permissionIds = rolePermissions.stream()
            .map(RolePermission::getPermissionId)
            .collect(Collectors.toList());

        return listByIds(permissionIds);
    }
}
