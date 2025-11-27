package com.xiaodou.controller;

import com.xiaodou.model.SysSetting;
import com.xiaodou.result.Result;
import com.xiaodou.service.SysSettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 系统设置表 前端控制器
 * </p>
 *
 * @author luoxiaodou
 * @since 2025-11-16
 */
@RestController
@RequestMapping("/settings")
@RequiredArgsConstructor
public class SysSettingController {

    private final SysSettingService sysSettingService;

    // ===================================================================
    // Admin Endpoints (需要管理员权限)
    // ===================================================================

    /**
     * [管理员] 获取所有设置项，按组分类
     */
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Map<String, List<SysSetting>>> getAllSettingsGrouped() {
        return Result.success(sysSettingService.getAllGrouped());
    }

    /**
     * [管理员] 批量更新或新增设置项
     */
    @PostMapping("/admin/batch")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> batchUpsertSettings(@RequestBody Map<String, String> settings) {
        sysSettingService.batchUpsert(settings);
        return Result.success();
    }

    /**
     * [管理员] 批量删除设置项
     */
    @DeleteMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> batchDeleteSettings(@RequestBody List<String> keys) {
        sysSettingService.deleteByKeys(keys);
        return Result.success();
    }

    // ===================================================================
    // Public Endpoints (无需权限)
    // ===================================================================

    /**
     * [公开] 获取所有公共设置项
     */
    @GetMapping("/public")
    public Result<Map<String, String>> getPublicSettings() {
        return Result.success(sysSettingService.getPublicSettings());
    }
}
