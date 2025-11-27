package com.xiaodou.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xiaodou.auth.util.UserContextHolder;
import com.xiaodou.model.Task;
import com.xiaodou.model.dto.task.TaskCreateDTO;
import com.xiaodou.model.dto.task.TaskUpdateDTO;
import com.xiaodou.model.query.TaskQuery;
import com.xiaodou.model.vo.TaskVO;
import com.xiaodou.result.Result;
import com.xiaodou.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 任务管理后台控制器
 */
@RestController
@RequestMapping("/admin/tasks")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')") // 整个控制器都需要管理员权限
public class TaskManagementController {

    private final TaskService taskService;

    /**
     * 创建任务
     */
    @PostMapping
    public Result<TaskVO> createTask(@RequestBody TaskCreateDTO createDTO) {
        Task task = taskService.createTask(createDTO, UserContextHolder.getUserId());
        return Result.success(TaskVO.fromEntity(task));
    }

    /**
     * 更新任务
     */
    @PutMapping("/{id}")
    public Result<TaskVO> updateTask(@PathVariable String id, @RequestBody TaskUpdateDTO updateDTO) {
        updateDTO.setId(id);
        Task task = taskService.updateTask(updateDTO);
        return Result.success(TaskVO.fromEntity(task));
    }

    /**
     * 删除任务
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteTask(@PathVariable String id) {
        taskService.deleteTask(id);
        return Result.success();
    }

    /**
     * 获取任务详情
     */
    @GetMapping("/{id}")
    public Result<TaskVO> getTaskDetail(@PathVariable String id) {
        Task task = taskService.getById(id);
        return Result.success(TaskVO.fromEntity(task));
    }

    /**
     * 分页查询任务列表
     */
    @GetMapping
    public Result<IPage<TaskVO>> listTasks(TaskQuery query) {
        IPage<TaskVO> pageResult = taskService.pageListTasks(query);
        return Result.success(pageResult);
    }

    /**
     * 更改任务状态（上线/下线）
     */
    @PatchMapping("/{id}/status/{status}")
    public Result<Void> changeTaskStatus(@PathVariable String id, @PathVariable Byte status) {
        taskService.changeTaskStatus(id, status);
        return Result.success();
    }
}
