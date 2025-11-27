package com.xiaodou.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xiaodou.model.TaskTargetUser;
import com.xiaodou.model.dto.task.TaskTargetUserImportDTO;
import com.xiaodou.model.query.TaskTargetUserQuery;
import com.xiaodou.result.Result;
import com.xiaodou.service.TaskTargetUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 任务定向用户管理后台控制器
 */
@RestController
@RequestMapping("/admin/task-targets")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')") // 整个控制器都需要管理员权限
public class TaskTargetUserController {

    private final TaskTargetUserService taskTargetUserService;

    /**
     * 导入任务定向用户
     */
    @PostMapping("/import")
    public Result<Void> importTargetUsers(@RequestBody TaskTargetUserImportDTO importDTO) {
        taskTargetUserService.importTargetUsers(importDTO);
        return Result.success();
    }

    /**
     * 获取任务的定向用户列表
     */
    @GetMapping
    public Result<IPage<TaskTargetUser>> listTargetUsers(TaskTargetUserQuery query) {
        IPage<TaskTargetUser> pageResult = taskTargetUserService.pageListTargetUsers(query);
        return Result.success(pageResult);
    }

    /**
     * 删除任务的定向用户
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteTargetUser(@PathVariable String id) {
        taskTargetUserService.removeById(id);
        return Result.success();
    }
}
