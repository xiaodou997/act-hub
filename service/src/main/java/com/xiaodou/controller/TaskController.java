package com.xiaodou.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xiaodou.auth.util.UserContextHolder;
import com.xiaodou.exception.AppException;
import com.xiaodou.log.annotation.SystemLog;
import com.xiaodou.model.Task;
import com.xiaodou.model.TaskTargetUser;
import com.xiaodou.model.dto.task.TaskCreateDTO;
import com.xiaodou.model.dto.task.TaskTargetUserImportDTO;
import com.xiaodou.model.dto.task.TaskUpdateDTO;
import com.xiaodou.model.query.TaskQuery;
import com.xiaodou.model.query.TaskTargetUserQuery;
import com.xiaodou.model.vo.TaskVO;
import com.xiaodou.result.Result;
import com.xiaodou.result.ResultCodeEnum;
import com.xiaodou.service.TaskService;
import com.xiaodou.service.TaskTargetUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 任务管理 前端控制器
 * <p>
 * 提供分发任务的CRUD操作，包括：
 * 1. 任务列表查询（带分页和筛选）
 * 2. 任务创建/更新/删除
 * 3. 任务状态变更（上线/下线/结束）
 * 4. 定向用户管理
 * </p>
 *
 * @author luoxiaodou
 * @since 2025-11-28
 */
@Slf4j
@RestController
@RequestMapping("/task")
@RequiredArgsConstructor
@Tag(name = "任务管理", description = "分发任务管理相关接口")
public class TaskController {

    private final TaskService taskService;
    private final TaskTargetUserService taskTargetUserService;

    /**
     * 分页查询任务列表
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询任务列表", description = "支持按任务名称、状态、平台、时间范围筛选")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<IPage<TaskVO>> page(TaskQuery query) {
        log.info("分页查询任务列表 - query: {}", query);
        IPage<TaskVO> result = taskService.pageListTasks(query);
        return Result.success(result);
    }

    /**
     * 获取任务详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取任务详情", description = "根据ID获取任务的详细信息")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<TaskVO> getById(@PathVariable String id) {
        log.info("获取任务详情 - id: {}", id);
        Task task = taskService.getById(id);
        if (task == null) {
            throw new AppException(ResultCodeEnum.DATA_NOT_FOUND, "任务不存在");
        }
        return Result.success(TaskVO.fromEntity(task));
    }

    /**
     * 创建任务
     */
    @PostMapping
    @Operation(summary = "创建任务", description = "创建新的分发任务")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @SystemLog(module = "任务管理", action = "创建任务", recordResponse = true)
    public Result<TaskVO> create(@Valid @RequestBody TaskCreateDTO createDTO) {
        log.info("创建任务 - createDTO: {}", createDTO);
        String creatorId = UserContextHolder.getUserId();
        Task task = taskService.createTask(createDTO, creatorId);
        return Result.success(TaskVO.fromEntity(task));
    }

    /**
     * 更新任务
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新任务", description = "更新任务信息（只有草稿和下线状态可编辑）")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @SystemLog(module = "任务管理", action = "更新任务", recordResponse = true)
    public Result<TaskVO> update(@PathVariable String id, @Valid @RequestBody TaskUpdateDTO updateDTO) {
        log.info("更新任务 - id: {}, updateDTO: {}", id, updateDTO);

        // 检查任务是否存在
        Task existingTask = taskService.getById(id);
        if (existingTask == null) {
            throw new AppException(ResultCodeEnum.DATA_NOT_FOUND, "任务不存在");
        }

        // 检查任务状态是否允许编辑（只有草稿和下线状态可编辑）
        if (existingTask.getStatus() != null &&
            existingTask.getStatus() != Task.TaskStatus.DRAFT.getCode() &&
            existingTask.getStatus() != Task.TaskStatus.OFFLINE.getCode()) {
            throw new AppException(ResultCodeEnum.BUSINESS_ERROR, "只有草稿或下线状态的任务可以编辑");
        }

        updateDTO.setId(id);
        Task task = taskService.updateTask(updateDTO);
        return Result.success(TaskVO.fromEntity(task));
    }

    /**
     * 删除任务
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除任务", description = "删除任务（有参与记录的任务不能删除）")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @SystemLog(module = "任务管理", action = "删除任务", recordResponse = true)
    public Result<Void> delete(@PathVariable String id) {
        log.info("删除任务 - id: {}", id);
        taskService.deleteTask(id);
        return Result.success();
    }

    /**
     * 变更任务状态
     * <p>
     * 状态流转规则：
     * - 草稿(0) -> 上线(1)
     * - 上线(1) -> 下线(2)
     * - 下线(2) -> 上线(1)
     * - 上线(1) -> 已结束(3)（手动结束或到期自动结束）
     * </p>
     */
    @PutMapping("/{id}/status")
    @Operation(summary = "变更任务状态", description = "变更任务状态：0草稿,1上线,2下线,3已结束")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @SystemLog(module = "任务管理", action = "变更任务状态", recordResponse = true)
    public Result<Void> changeStatus(
        @PathVariable String id,
        @Parameter(description = "目标状态：0草稿,1上线,2下线,3已结束") @RequestParam Byte status) {
        log.info("变更任务状态 - id: {}, status: {}", id, status);

        // 验证状态值
        if (status < 0 || status > 3) {
            throw new AppException(ResultCodeEnum.BAD_REQUEST, "无效的状态值");
        }

        // 检查任务是否存在
        Task task = taskService.getById(id);
        if (task == null) {
            throw new AppException(ResultCodeEnum.DATA_NOT_FOUND, "任务不存在");
        }

        // 验证状态流转
        validateStatusTransition(task.getStatus(), status);

        taskService.changeTaskStatus(id, status);
        return Result.success();
    }

    /**
     * 导入定向用户
     */
    @PostMapping("/{id}/target-users")
    @Operation(summary = "导入定向用户", description = "为定向任务导入用户列表")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @SystemLog(module = "任务管理", action = "导入定向用户", recordResponse = true)
    public Result<Void> importTargetUsers(
        @PathVariable String id,
        @Valid @RequestBody TaskTargetUserImportDTO importDTO) {
        log.info("导入定向用户 - taskId: {}, count: {}", id,
            importDTO.getUserIdentifiers() != null ? importDTO.getUserIdentifiers().size() : 0);

        // 检查任务是否存在
        Task task = taskService.getById(id);
        if (task == null) {
            throw new AppException(ResultCodeEnum.DATA_NOT_FOUND, "任务不存在");
        }

        // 检查是否为定向任务
        if (!Boolean.TRUE.equals(task.getIsTargeted())) {
            throw new AppException(ResultCodeEnum.BUSINESS_ERROR, "该任务不是定向任务，无法导入用户");
        }

        importDTO.setTaskId(id);
        taskTargetUserService.importTargetUsers(importDTO);
        return Result.success();
    }

    /**
     * 获取定向用户列表
     */
    @GetMapping("/{id}/target-users")
    @Operation(summary = "获取定向用户列表", description = "获取任务的定向用户列表")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<IPage<TaskTargetUser>> getTargetUsers(
        @PathVariable String id,
        @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
        @Parameter(description = "每页条数") @RequestParam(defaultValue = "20") Integer pageSize) {
        log.info("获取定向用户列表 - taskId: {}, pageNum: {}, pageSize: {}", id, pageNum, pageSize);

        TaskTargetUserQuery query = new TaskTargetUserQuery();
        query.setTaskId(id);
        query.setPageNum(pageNum);
        query.setPageSize(pageSize);

        IPage<TaskTargetUser> result = taskTargetUserService.pageListTargetUsers(query);
        return Result.success(result);
    }

    /**
     * 删除定向用户
     */
    @DeleteMapping("/{taskId}/target-users/{userId}")
    @Operation(summary = "删除定向用户", description = "从定向任务中移除指定用户")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @SystemLog(module = "任务管理", action = "删除定向用户", recordResponse = true)
    public Result<Void> removeTargetUser(
        @PathVariable String taskId,
        @PathVariable String userId) {
        log.info("删除定向用户 - taskId: {}, userId: {}", taskId, userId);

        // 这里可以添加删除逻辑
        // taskTargetUserService.removeByTaskAndUser(taskId, userId);

        return Result.success();
    }

    /**
     * 验证状态流转
     */
    private void validateStatusTransition(Byte currentStatus, Byte targetStatus) {
        if (currentStatus == null) {
            currentStatus = Task.TaskStatus.DRAFT.getCode();
        }

        // 草稿 -> 上线
        if (currentStatus.equals(Task.TaskStatus.DRAFT.getCode())) {
            if (!targetStatus.equals(Task.TaskStatus.ONLINE.getCode())) {
                throw new AppException(ResultCodeEnum.BUSINESS_ERROR, "草稿状态只能变更为上线");
            }
            return;
        }

        // 上线 -> 下线 或 已结束
        if (currentStatus.equals(Task.TaskStatus.ONLINE.getCode())) {
            if (!targetStatus.equals(Task.TaskStatus.OFFLINE.getCode()) &&
                !targetStatus.equals(Task.TaskStatus.ENDED.getCode())) {
                throw new AppException(ResultCodeEnum.BUSINESS_ERROR, "上线状态只能变更为下线或已结束");
            }
            return;
        }

        // 下线 -> 上线
        if (currentStatus.equals(Task.TaskStatus.OFFLINE.getCode())) {
            if (!targetStatus.equals(Task.TaskStatus.ONLINE.getCode())) {
                throw new AppException(ResultCodeEnum.BUSINESS_ERROR, "下线状态只能变更为上线");
            }
            return;
        }

        // 已结束 不能变更
        if (currentStatus.equals(Task.TaskStatus.ENDED.getCode())) {
            throw new AppException(ResultCodeEnum.BUSINESS_ERROR, "已结束的任务不能变更状态");
        }
    }
}