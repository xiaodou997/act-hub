package com.xiaodou.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiaodou.log.annotation.SystemLog;
import com.xiaodou.model.Permission;
import com.xiaodou.model.dto.permission.PermissionCreateDTO;
import com.xiaodou.model.dto.permission.PermissionUpdateDTO;
import com.xiaodou.model.vo.PermissionVO;
import com.xiaodou.result.Result;
import com.xiaodou.service.PermissionService;
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
 * 权限管理 前端控制器
 *
 * @author luoxiaodou
 * @since 2025-11-24
 */
@Slf4j
@RestController
@RequestMapping("/permission")
@RequiredArgsConstructor
@Tag(name = "权限管理", description = "权限相关接口")
public class PermissionController {

    private final PermissionService permissionService;

    /**
     * 分页查询权限列表
     */
    @GetMapping("/page")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "分页查询权限", description = "分页查询权限列表")
    public Result<IPage<PermissionVO>> page(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "20") Integer pageSize,
            @Parameter(description = "权限名称") @RequestParam(required = false) String name,
            @Parameter(description = "权限编码") @RequestParam(required = false) String code,
            @Parameter(description = "权限类型") @RequestParam(required = false) Byte type) {
        log.info("分页查询权限, pageNum: {}, pageSize: {}, name: {}, code: {}, type: {}",
                 pageNum, pageSize, name, code, type);
        Page<Permission> page = new Page<>(pageNum, pageSize);
        IPage<Permission> result = permissionService.pageList(page, name, code, type);

        // 转换为 VO
        IPage<PermissionVO> voPage = result.convert(PermissionVO::fromEntity);
        return Result.success(voPage);
    }

    /**
     * 获取所有权限列表（用于角色分配权限时的下拉选项）
     */
    @GetMapping("/list")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "获取所有权限", description = "获取所有权限列表（不分页）")
    public Result<List<PermissionVO>> list() {
        log.info("获取所有权限列表");
        List<Permission> permissions = permissionService.listAll();
        List<PermissionVO> voList = permissions.stream()
            .map(PermissionVO::fromEntity)
            .collect(Collectors.toList());
        return Result.success(voList);
    }

    /**
     * 获取权限详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "获取权限详情", description = "根据ID获取权限详细信息")
    public Result<PermissionVO> getById(
            @Parameter(description = "权限ID") @PathVariable String id) {
        log.info("获取权限详情, id: {}", id);
        Permission permission = permissionService.getById(id);
        if (permission == null) {
            return Result.fail("权限不存在");
        }
        return Result.success(PermissionVO.fromEntity(permission));
    }

    /**
     * 创建权限
     */
    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @SystemLog(module = "权限管理", action = "创建权限", recordResponse = true)
    @Operation(summary = "创建权限", description = "创建新的权限")
    public Result<PermissionVO> create(@Valid @RequestBody PermissionCreateDTO dto) {
        log.info("创建权限: {}", dto);
        Permission permission = permissionService.create(dto);
        return Result.success(PermissionVO.fromEntity(permission));
    }

    /**
     * 更新权限
     */
    @PutMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @SystemLog(module = "权限管理", action = "更新权限", recordResponse = true)
    @Operation(summary = "更新权限", description = "更新权限信息")
    public Result<PermissionVO> update(@Valid @RequestBody PermissionUpdateDTO dto) {
        log.info("更新权限: {}", dto);
        Permission permission = permissionService.update(dto);
        return Result.success(PermissionVO.fromEntity(permission));
    }

    /**
     * 删除权限
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @SystemLog(module = "权限管理", action = "删除权限")
    @Operation(summary = "删除权限", description = "删除指定权限（不能删除已被角色关联的权限）")
    public Result<Void> delete(
            @Parameter(description = "权限ID") @PathVariable String id) {
        log.info("删除权限, id: {}", id);
        permissionService.delete(id);
        return Result.success();
    }
}
