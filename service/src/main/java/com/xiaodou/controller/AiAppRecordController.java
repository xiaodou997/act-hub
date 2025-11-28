package com.xiaodou.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiaodou.auth.util.UserContextHolder;
import com.xiaodou.exception.AppException;
import com.xiaodou.log.annotation.SystemLog;
import com.xiaodou.model.AiAppRecord;
import com.xiaodou.model.vo.AiAppRecordVO;
import com.xiaodou.result.Result;
import com.xiaodou.result.ResultCodeEnum;
import com.xiaodou.service.AiAppRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AI应用执行记录 前端控制器
 *
 * @author luoxiaodou
 * @since 2025-11-27
 */
@Slf4j
@RestController
@RequestMapping("/ai-app-record")
@RequiredArgsConstructor
@Tag(name = "AI应用记录管理", description = "AI应用执行记录相关接口")
public class AiAppRecordController {

    private final AiAppRecordService aiAppRecordService;

    /**
     * 分页查询AI应用记录
     * <p>
     * 权限控制：
     * - 超级管理员可以查询所有用户的AI应用记录
     * - 普通用户只能查询自己的AI应用记录
     * </p>
     */
    @GetMapping("/page")
    @SystemLog(module = "AI应用记录管理", action = "分页查询AI应用记录")
    @Operation(summary = "分页查询AI应用记录", description = "支持按用户ID、AI应用ID、状态筛选")
    public Result<IPage<AiAppRecordVO>> page(
        @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
        @Parameter(description = "每页条数") @RequestParam(defaultValue = "20") Integer pageSize,
        @Parameter(description = "用户ID（超管可选，普通用户忽略此参数）") @RequestParam(required = false) String userId,
        @Parameter(description = "AI应用ID") @RequestParam(required = false) String aiApplicationId,
        @Parameter(description = "状态（1-成功,2-失败,3-进行中）") @RequestParam(required = false) Integer status) {

        // 获取当前登录用户
        String currentUserId = UserContextHolder.getUserId();
        boolean isSuperAdmin = UserContextHolder.isSuperAdmin();

        // 权限控制：非超管只能查询自己的记录
        String queryUserId;
        if (isSuperAdmin) {
            // 超管可以查询指定用户的记录，如果未指定则查询所有
            queryUserId = userId;
            log.info("超管分页查询AI应用记录 - pageNum: {}, pageSize: {}, userId: {}, aiApplicationId: {}, status: {}",
                pageNum, pageSize, userId, aiApplicationId, status);
        } else {
            // 普通用户只能查询自己的记录
            queryUserId = currentUserId;
            log.info("用户分页查询AI应用记录 - pageNum: {}, pageSize: {}, userId: {}, aiApplicationId: {}, status: {}",
                pageNum, pageSize, currentUserId, aiApplicationId, status);
        }

        Page<AiAppRecord> page = new Page<>(pageNum, pageSize);
        IPage<AiAppRecord> result = aiAppRecordService.pageByUser(page, queryUserId, aiApplicationId, status);

        // 转换为 VO
        IPage<AiAppRecordVO> voPage = result.convert(AiAppRecordVO::fromEntity);
        return Result.success(voPage);
    }

    /**
     * 查询当前用户的AI应用记录（简化版）
     * <p>
     * 用于AI工坊等场景，只返回当前用户自己的记录
     * </p>
     */
    @GetMapping("/my")
    @SystemLog(module = "AI应用记录管理", action = "查询我的AI应用记录")
    @Operation(summary = "查询我的AI应用记录", description = "获取当前登录用户的AI应用执行记录")
    public Result<IPage<AiAppRecordVO>> myRecords(
        @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
        @Parameter(description = "每页条数") @RequestParam(defaultValue = "20") Integer pageSize,
        @Parameter(description = "AI应用ID") @RequestParam(required = false) String aiApplicationId,
        @Parameter(description = "状态（1-成功,2-失败,3-进行中）") @RequestParam(required = false) Integer status) {

        String currentUserId = UserContextHolder.getUserId();
        log.info("查询我的AI应用记录 - pageNum: {}, pageSize: {}, userId: {}, aiApplicationId: {}, status: {}",
            pageNum, pageSize, currentUserId, aiApplicationId, status);

        Page<AiAppRecord> page = new Page<>(pageNum, pageSize);
        IPage<AiAppRecord> result = aiAppRecordService.pageByUser(page, currentUserId, aiApplicationId, status);

        // 转换为 VO
        IPage<AiAppRecordVO> voPage = result.convert(AiAppRecordVO::fromEntity);
        return Result.success(voPage);
    }

    /**
     * 查询AI应用记录详情
     * <p>
     * 权限控制：
     * - 超级管理员可以查询任何AI应用记录
     * - 普通用户只能查询自己的AI应用记录
     * </p>
     */
    @GetMapping("/{id}")
    @SystemLog(module = "AI应用记录管理", action = "查询AI应用记录详情")
    @Operation(summary = "查询AI应用记录详情", description = "根据ID查询AI应用执行记录的详细信息")
    public Result<AiAppRecordVO> getById(@PathVariable String id) {
        log.info("查询AI应用记录详情 - id: {}", id);

        AiAppRecord aiAppRecord = aiAppRecordService.getById(id);
        if (aiAppRecord == null) {
            throw new AppException(ResultCodeEnum.DATA_NOT_FOUND, "AI应用记录不存在");
        }

        // 权限控制：非超管只能查询自己的记录
        String currentUserId = UserContextHolder.getUserId();
        boolean isSuperAdmin = UserContextHolder.isSuperAdmin();

        if (!isSuperAdmin && !currentUserId.equals(aiAppRecord.getUserId())) {
            log.warn("用户尝试查询其他用户的AI应用记录 - currentUserId: {}, recordUserId: {}", currentUserId,
                aiAppRecord.getUserId());
            throw new AppException(ResultCodeEnum.FORBIDDEN, "无权查看该AI应用记录");
        }

        return Result.success(AiAppRecordVO.fromEntity(aiAppRecord));
    }

    /**
     * 根据执行ID查询AI应用记录
     * <p>
     * 主要用于异步任务执行完成后，根据 executeId 查询AI应用状态
     * </p>
     */
    @GetMapping("/execute/{executeId}")
    @SystemLog(module = "AI应用记录管理", action = "根据执行ID查询AI应用记录")
    @Operation(summary = "根据执行ID查询AI应用记录", description = "用于查询异步任务的执行状态")
    public Result<AiAppRecordVO> getByExecuteId(@PathVariable String executeId) {
        log.info("根据执行ID查询AI应用记录 - executeId: {}", executeId);

        AiAppRecord aiAppRecord = aiAppRecordService.getByExecuteId(executeId);
        if (aiAppRecord == null) {
            throw new AppException(ResultCodeEnum.DATA_NOT_FOUND, "AI应用记录不存在");
        }

        // 权限控制：非超管只能查询自己的记录
        String currentUserId = UserContextHolder.getUserId();
        boolean isSuperAdmin = UserContextHolder.isSuperAdmin();

        if (!isSuperAdmin && !currentUserId.equals(aiAppRecord.getUserId())) {
            log.warn("用户尝试查询其他用户的AI应用记录 - currentUserId: {}, recordUserId: {}", currentUserId,
                aiAppRecord.getUserId());
            throw new AppException(ResultCodeEnum.FORBIDDEN, "无权查看该AI应用记录");
        }

        return Result.success(AiAppRecordVO.fromEntity(aiAppRecord));
    }

    /**
     * 删除AI应用记录（软删除）
     * <p>
     * 权限控制：
     * - 超级管理员可以删除任何AI应用记录
     * - 普通用户只能删除自己的AI应用记录
     * </p>
     */
    @DeleteMapping("/{id}")
    @SystemLog(module = "AI应用记录管理", action = "删除AI应用记录", recordResponse = true)
    @Operation(summary = "删除AI应用记录", description = "软删除AI应用执行记录")
    public Result<Void> delete(@PathVariable String id) {
        log.info("删除AI应用记录 - id: {}", id);

        AiAppRecord aiAppRecord = aiAppRecordService.getById(id);
        if (aiAppRecord == null) {
            throw new AppException(ResultCodeEnum.DATA_NOT_FOUND, "AI应用记录不存在");
        }

        // 权限控制：非超管只能删除自己的记录
        String currentUserId = UserContextHolder.getUserId();
        boolean isSuperAdmin = UserContextHolder.isSuperAdmin();

        if (!isSuperAdmin && !currentUserId.equals(aiAppRecord.getUserId())) {
            log.warn("用户尝试删除其他用户的AI应用记录 - currentUserId: {}, recordUserId: {}", currentUserId,
                aiAppRecord.getUserId());
            throw new AppException(ResultCodeEnum.FORBIDDEN, "无权删除该AI应用记录");
        }

        aiAppRecordService.removeById(id);
        return Result.success();
    }

    /**
     * 批量删除AI应用记录（软删除）
     * <p>
     * 权限控制：
     * - 超级管理员可以删除任何AI应用记录
     * - 普通用户只能删除自己的AI应用记录
     * </p>
     */
    @DeleteMapping("/batch")
    @SystemLog(module = "AI应用记录管理", action = "批量删除AI应用记录", recordResponse = true)
    @Operation(summary = "批量删除AI应用记录", description = "批量软删除AI应用执行记录")
    public Result<Void> batchDelete(@RequestBody List<String> ids) {
        log.info("批量删除AI应用记录 - ids: {}", ids);

        if (ids == null || ids.isEmpty()) {
            throw new AppException(ResultCodeEnum.BAD_REQUEST, "请选择要删除的记录");
        }

        String currentUserId = UserContextHolder.getUserId();
        boolean isSuperAdmin = UserContextHolder.isSuperAdmin();

        // 非超管需要校验每条记录的所有权
        if (!isSuperAdmin) {
            for (String id : ids) {
                AiAppRecord record = aiAppRecordService.getById(id);
                if (record != null && !currentUserId.equals(record.getUserId())) {
                    log.warn("用户尝试删除其他用户的AI应用记录 - currentUserId: {}, recordId: {}", currentUserId, id);
                    throw new AppException(ResultCodeEnum.FORBIDDEN, "无权删除部分记录");
                }
            }
        }

        aiAppRecordService.removeByIds(ids);
        return Result.success();
    }
}