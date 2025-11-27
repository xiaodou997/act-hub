package com.xiaodou.controller;

import com.xiaodou.log.annotation.SystemLog;
import com.xiaodou.model.Menu;
import com.xiaodou.model.dto.menu.MenuCreateDTO;
import com.xiaodou.model.dto.menu.MenuSortDTO;
import com.xiaodou.model.dto.menu.MenuUpdateDTO;
import com.xiaodou.model.vo.MenuVO;
import com.xiaodou.result.Result;
import com.xiaodou.service.MenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 菜单权限表 前端控制器
 * </p>
 *
 * @author luoxiaodou
 * @since 2025-11-16
 */
@Slf4j
@RestController
@RequestMapping("/menu")
@RequiredArgsConstructor
@Tag(name = "菜单管理", description = "菜单权限相关接口")
public class MenuController {

    private final MenuService menuService;

    /**
     * 获取当前登录用户的导航菜单
     * <p>
     * 前端在用户登录后调用此接口，动态生成侧边栏导航。
     * </p>
     *
     * @return 包含菜单树的统一响应结果
     */
    @GetMapping("/nav")
    @Operation(summary = "获取用户导航菜单", description = "根据当前用户权限获取可访问的菜单树")
    public Result<List<MenuVO>> getUserNavMenu() {
        List<MenuVO> menuTree = menuService.buildMenuTreeForCurrentUser();
        return Result.success(menuTree);
    }

    /**
     * 获取完整的菜单树（管理端使用）
     */
    @GetMapping("/tree")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "获取菜单树", description = "获取完整的菜单树结构（管理端使用）")
    public Result<List<MenuVO>> getMenuTree() {
        log.info("获取菜单树");
        List<MenuVO> menuTree = menuService.getMenuTree();
        return Result.success(menuTree);
    }

    /**
     * 获取菜单详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "获取菜单详情", description = "根据ID获取菜单详细信息")
    public Result<Menu> getById(
            @Parameter(description = "菜单ID") @PathVariable String id) {
        log.info("获取菜单详情, id: {}", id);
        Menu menu = menuService.getById(id);
        if (menu == null) {
            return Result.fail("菜单不存在");
        }
        return Result.success(menu);
    }

    /**
     * 创建菜单
     */
    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @SystemLog(module = "菜单管理", action = "创建菜单", recordResponse = true)
    @Operation(summary = "创建菜单", description = "创建新的菜单项")
    public Result<Menu> create(@Valid @RequestBody MenuCreateDTO dto) {
        log.info("创建菜单: {}", dto);
        Menu menu = menuService.create(dto);
        return Result.success(menu);
    }

    /**
     * 更新菜单
     */
    @PutMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @SystemLog(module = "菜单管理", action = "更新菜单", recordResponse = true)
    @Operation(summary = "更新菜单", description = "更新菜单信息")
    public Result<Menu> update(@Valid @RequestBody MenuUpdateDTO dto) {
        log.info("更新菜单: {}", dto);
        Menu menu = menuService.update(dto);
        return Result.success(menu);
    }

    /**
     * 删除菜单
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @SystemLog(module = "菜单管理", action = "删除菜单")
    @Operation(summary = "删除菜单", description = "删除指定菜单（不能删除有子菜单的菜单）")
    public Result<Void> delete(
            @Parameter(description = "菜单ID") @PathVariable String id) {
        log.info("删除菜单, id: {}", id);
        menuService.delete(id);
        return Result.success();
    }

    /**
     * 更新菜单状态
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @SystemLog(module = "菜单管理", action = "更新菜单状态")
    @Operation(summary = "更新菜单状态", description = "启用或禁用菜单")
    public Result<Void> updateStatus(
            @Parameter(description = "菜单ID") @PathVariable String id,
            @Parameter(description = "状态（1:正常, 0:禁用）") @RequestParam Byte status) {
        log.info("更新菜单状态, id: {}, status: {}", id, status);
        menuService.updateStatus(id, status);
        return Result.success();
    }

    /**
     * 菜单排序（支持跨父级移动）
     */
    @PutMapping("/sort")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @SystemLog(module = "菜单管理", action = "排序调整")
    @Operation(summary = "菜单排序", description = "调整指定父菜单的子节点顺序，支持跨父级移动")
    public Result<Void> sort(@Valid @RequestBody MenuSortDTO dto) {
        log.info("菜单排序, parentId: {}, orderedIds: {}", dto.getParentId(), dto.getOrderedIds());
        menuService.sort(dto);
        return Result.success();
    }
}
