package com.xiaodou.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xiaodou.auth.util.UserContextHolder;
import com.xiaodou.model.dto.task.TaskParticipationAuditDTO;
import com.xiaodou.model.query.TaskParticipationQuery;
import com.xiaodou.model.vo.TaskParticipationVO;
import com.xiaodou.result.Result;
import com.xiaodou.service.TaskParticipationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 任务参与管理后台控制器
 */
@RestController
@RequestMapping("/admin/task-participations")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')") // 整个控制器都需要管理员权限
public class TaskParticipationController {

    private final TaskParticipationService taskParticipationService;

    /**
     * 分页查询任务参与记录
     */
    @GetMapping
    public Result<IPage<TaskParticipationVO>> listParticipations(TaskParticipationQuery query) {
        IPage<TaskParticipationVO> pageResult = taskParticipationService.pageListParticipations(query);
        return Result.success(pageResult);
    }

    /**
     * 审核任务提交
     */
    @PatchMapping("/{id}/audit")
    public Result<Void> auditParticipation(@PathVariable String id,
                                           @Validated({TaskParticipationAuditDTO.RejectValidation.class}) @RequestBody TaskParticipationAuditDTO auditDTO) {
        taskParticipationService.auditParticipation(id, auditDTO, UserContextHolder.getUserId());
        return Result.success();
    }
}
