package com.xiaodou.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiaodou.auth.util.UserContextHolder;
import com.xiaodou.log.annotation.SystemLog;
import com.xiaodou.model.Permission;
import com.xiaodou.model.Role;
import com.xiaodou.model.dto.role.RoleCreateDTO;
import com.xiaodou.model.dto.role.RolePermissionDTO;
import com.xiaodou.model.dto.role.RoleUpdateDTO;
import com.xiaodou.model.vo.PermissionVO;
import com.xiaodou.model.vo.RoleVO;
import com.xiaodou.result.Result;
import com.xiaodou.service.PermissionService;
import com.xiaodou.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 角色表 前端控制器
 * </p>
 *
 * @author luoxiaodou
 * @since 2025-10-01
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/role")
@Tag(name = "角色管理", description = "角色相关接口")
public class RoleController {
    private final RoleService roleService;
    private final PermissionService permissionService;

    /**
     * 创建角色
     */
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PostMapping
    @SystemLog(module = "角色管理", action = "创建角色", recordResponse = true)
    @Operation(summary = "创建角色", description = "创建新的角色")
    public Result<RoleVO> create(@Valid @RequestBody RoleCreateDTO dto) {
        log.info("创建角色: {}", dto);
        Role role = roleService.create(dto);
        return Result.success(RoleVO.fromEntity(role));
    }

    /**
     * 更新角色
     */
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PutMapping
    @SystemLog(module = "角色管理", action = "更新角色", recordResponse = true)
    @Operation(summary = "更新角色", description = "更新角色信息")
    public Result<RoleVO> update(@Valid @RequestBody RoleUpdateDTO dto) {
        log.info("更新角色: {}", dto);
        Role role = roleService.update(dto);
        return Result.success(RoleVO.fromEntity(role));
    }

    /**
     * 删除角色
     */
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @DeleteMapping("/{id}")
    @SystemLog(module = "角色管理", action = "删除角色")
    @Operation(summary = "删除角色", description = "删除指定角色（不能删除已被用户关联的角色）")
    public Result<Void> delete(@PathVariable String id) {
        log.info("删除角色, id: {}", id);
        roleService.delete(id);
        return Result.success();
    }

    /**
     * 获取角色详情
     */
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @GetMapping("/{id}")
    @Operation(summary = "获取角色详情", description = "根据ID获取角色详细信息")
    public Result<RoleVO> get(@Parameter(description = "角色ID") @PathVariable String id) {
        log.info("获取角色详情, id: {}", id);
        Role role = roleService.getById(id);
        if (role == null) {
            return Result.fail("角色不存在");
        }
        return Result.success(RoleVO.fromEntity(role));
    }

    /**
     * 分页查询角色列表
     */
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @GetMapping("/page")
    @Operation(summary = "分页查询角色", description = "分页查询角色列表")
    public Result<IPage<RoleVO>> pageList(
        @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") int pageNum,
        @Parameter(description = "每页数量", example = "10") @RequestParam(defaultValue = "20") int pageSize,
        @Parameter(description = "名称") @RequestParam(required = false) String name,
        @Parameter(description = "编码") @RequestParam(required = false) String code,
        @Parameter(description = "描述") @RequestParam(required = false) String description) {
        String userId = UserContextHolder.getUserId();
        log.info("分页查询角色, userId: {}", userId);
        Page<Role> page = new Page<>(pageNum, pageSize);
        IPage<Role> result = roleService.pageList(page, name, code, description);

        // 转换为 VO
        IPage<RoleVO> voPage = result.convert(RoleVO::fromEntity);
        return Result.success(voPage);
    }

    /**
     * 获取所有角色列表（用于用户分配角色时的下拉选项）
     */
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @GetMapping("/list")
    @Operation(summary = "获取所有角色", description = "获取所有角色列表（不分页）")
    public Result<List<RoleVO>> list() {
        log.info("获取所有角色列表");
        List<Role> roles = roleService.listAll();
        List<RoleVO> voList = roles.stream()
            .map(RoleVO::fromEntity)
            .collect(Collectors.toList());
        return Result.success(voList);
    }

    /**
     * 获取角色的权限ID列表
     */
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @GetMapping("/{id}/permissions")
    @Operation(summary = "获取角色权限ID列表", description = "获取指定角色的权限ID列表")
    public Result<List<String>> getPermissionIds(
            @Parameter(description = "角色ID") @PathVariable String id) {
        log.info("获取角色权限, roleId: {}", id);
        List<String> permissionIds = roleService.getPermissionIds(id);
        return Result.success(permissionIds);
    }

    /**
     * 获取角色的权限详情列表
     */
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @GetMapping("/{id}/permissions/detail")
    @Operation(summary = "获取角色权限详情", description = "获取指定角色的权限详细信息列表")
    public Result<List<PermissionVO>> getPermissions(
            @Parameter(description = "角色ID") @PathVariable String id) {
        log.info("获取角色权限详情, roleId: {}", id);
        List<Permission> permissions = permissionService.getPermissionsByRoleId(id);
        List<PermissionVO> voList = permissions.stream()
            .map(PermissionVO::fromEntity)
            .collect(Collectors.toList());
        return Result.success(voList);
    }

    /**
     * 为角色分配权限
     */
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PostMapping("/permissions")
    @SystemLog(module = "角色管理", action = "分配权限")
    @Operation(summary = "分配权限", description = "为角色分配权限（全量替换）")
    public Result<Void> assignPermissions(@Valid @RequestBody RolePermissionDTO dto) {
        log.info("为角色分配权限, roleId: {}, permissionIds: {}", dto.getRoleId(), dto.getPermissionIds());
        roleService.assignPermissions(dto.getRoleId(), dto.getPermissionIds());
        return Result.success();
    }
}
