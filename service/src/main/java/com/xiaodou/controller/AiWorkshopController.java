package com.xiaodou.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiaodou.aiapp.AiAppExecutor;
import com.xiaodou.auth.util.UserContextHolder;
import com.xiaodou.log.annotation.SystemLog;
import com.xiaodou.model.AiAppType;
import com.xiaodou.model.AiApplication;
import com.xiaodou.model.vo.AiAppTypeVO;
import com.xiaodou.model.vo.AiApplicationVO;
import com.xiaodou.result.Result;
import com.xiaodou.service.AiAppTypeService;
import com.xiaodou.service.AiApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * AI工坊控制器
 * <p>
 * 提供AI工坊功能的相关接口：
 * 1. 获取AI应用分类列表
 * 2. 根据分类获取AI应用列表
 * 3. 执行AI应用生成内容
 * </p>
 *
 * @author luoxiaodou
 * @since 2025-11-27
 */
@Slf4j
@RestController
@RequestMapping("/ai-workshop")
@RequiredArgsConstructor
@Tag(name = "AI工坊", description = "AI工坊相关接口")
public class AiWorkshopController {

    private final AiAppTypeService aiAppTypeService;
    private final AiApplicationService aiApplicationService;
    private final AiAppExecutor aiAppExecutor;

    /**
     * 获取AI应用分类列表
     * <p>
     * 返回所有启用状态的AI应用分类，用于前端展示分类卡片
     * </p>
     *
     * @return 分类列表
     */
    @GetMapping("/categories")
    @Operation(summary = "获取AI应用分类列表", description = "获取所有启用的AI应用分类")
    public Result<List<AiAppTypeVO>> getCategories() {
        log.info("获取AI工坊分类列表");

        LambdaQueryWrapper<AiAppType> query = new LambdaQueryWrapper<>();
        query.eq(AiAppType::getStatus, 1) // 只查询启用状态
            .orderByAsc(AiAppType::getCreatedAt);

        List<AiAppType> types = aiAppTypeService.list(query);
        List<AiAppTypeVO> voList = types.stream()
            .map(AiAppTypeVO::fromEntity)
            .collect(Collectors.toList());

        return Result.success(voList);
    }

    /**
     * 根据分类ID获取AI应用列表
     * <p>
     * 返回指定分类下所有启用的AI应用，用于前端展示应用选择卡片
     * </p>
     *
     * @param typeId 分类ID
     * @return AI应用列表
     */
    @GetMapping("/applications")
    @Operation(summary = "根据分类获取AI应用列表", description = "获取指定分类下的所有启用AI应用")
    public Result<List<AiApplicationVO>> getApplicationsByType(
        @Parameter(description = "分类ID") @RequestParam String typeId) {
        log.info("获取分类下的AI应用列表 - typeId: {}", typeId);

        LambdaQueryWrapper<AiApplication> query = new LambdaQueryWrapper<>();
        query.eq(AiApplication::getTypeId, typeId)
            .eq(AiApplication::getEnabled, 1) // 只查询启用状态
            .orderByDesc(AiApplication::getUpdatedAt);

        List<AiApplication> apps = aiApplicationService.list(query);
        List<AiApplicationVO> voList = apps.stream()
            .map(AiApplicationVO::fromEntity)
            .collect(Collectors.toList());

        return Result.success(voList);
    }

    /**
     * 获取AI应用详情（包含参数Schema）
     * <p>
     * 前端根据paramSchema动态渲染参数输入表单
     * </p>
     *
     * @param appId 应用ID
     * @return AI应用详情
     */
    @GetMapping("/application/{appId}")
    @Operation(summary = "获取AI应用详情", description = "获取AI应用详情，包含参数Schema用于动态表单渲染")
    public Result<AiApplicationVO> getApplicationDetail(
        @Parameter(description = "应用ID") @PathVariable String appId) {
        log.info("获取AI应用详情 - appId: {}", appId);

        AiApplication app = aiApplicationService.getById(appId);
        if (app == null) {
            return Result.fail("应用不存在");
        }

        return Result.success(AiApplicationVO.fromEntity(app));
    }

    /**
     * 执行AI应用（生成内容）
     * <p>
     * 调用指定的AI应用生成内容
     * </p>
     *
     * @param appId  AI应用ID
     * @param params 生成参数（由前端根据paramSchema构建）
     * @return 生成结果
     */
    @PostMapping("/execute/{appId}")
    @SystemLog(module = "AI工坊", action = "执行应用", recordResponse = true)
    @Operation(summary = "执行AI应用", description = "调用AI应用生成内容")
    public Result<Object> execute(
        @Parameter(description = "应用ID") @PathVariable Long appId,
        @RequestBody Map<String, Object> params) {
        log.info("执行AI应用 - appId: {}, params: {}", appId, params);

        try {
            String userId = UserContextHolder.getUserId();

            AiApplication application = aiApplicationService.getById(appId);
            if (application == null) {
                return Result.fail("应用不存在");
            }

            if (application.getEnabled() != 1) {
                return Result.fail("该应用已禁用");
            }

            Object result = aiAppExecutor.execute(userId, appId, params);
            return Result.success(result);

        } catch (IllegalArgumentException e) {
            log.warn("参数校验失败 - appId: {}, error: {}", appId, e.getMessage());
            return Result.fail("参数错误: " + e.getMessage());
        } catch (Exception e) {
            log.error("执行AI应用失败 - appId: {}", appId, e);
            return Result.fail("执行失败: " + e.getMessage());
        }
    }

    /**
     * 查询执行记录详情
     * <p>
     * 用于异步任务完成后查询结果
     * </p>
     *
     * @param recordId 记录ID
     * @return 执行记录详情
     */
    @GetMapping("/record/{recordId}")
    @Operation(summary = "查询执行记录", description = "查询AI应用执行记录详情")
    public Result<Object> getRecord(
        @Parameter(description = "记录ID") @PathVariable String recordId) {
        // 复用 AiAppRecordController 的逻辑，这里可以直接调用 Service
        // 暂时返回简单实现，后续可以扩展
        return Result.success("请使用 /ai-app-record/{id} 接口查询");
    }
}