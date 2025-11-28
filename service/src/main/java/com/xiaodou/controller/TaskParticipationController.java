package com.xiaodou.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xiaodou.auth.util.UserContextHolder;
import com.xiaodou.exception.AppException;
import com.xiaodou.log.annotation.SystemLog;
import com.xiaodou.model.TaskParticipation;
import com.xiaodou.model.dto.task.TaskParticipationAuditDTO;
import com.xiaodou.model.query.TaskParticipationQuery;
import com.xiaodou.model.vo.TaskParticipationVO;
import com.xiaodou.result.Result;
import com.xiaodou.result.ResultCodeEnum;
import com.xiaodou.service.TaskParticipationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 任务参与管理 前端控制器
 * <p>
 * 提供任务参与记录的查询和审核功能：
 * 1. 参与记录列表查询（带分页和筛选）
 * 2. 参与记录详情查询
 * 3. 审核参与记录（通过/不通过）
 * 4. 导出参与记录
 * </p>
 *
 * @author luoxiaodou
 * @since 2025-11-28
 */
@Slf4j
@RestController
@RequestMapping("/task-participation")
@RequiredArgsConstructor
@Tag(name = "任务参与管理", description = "任务参与记录管理相关接口")
public class TaskParticipationController {

    private final TaskParticipationService taskParticipationService;

    /**
     * 分页查询参与记录
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询参与记录", description = "支持按任务ID、用户ID、状态筛选")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<IPage<TaskParticipationVO>> page(TaskParticipationQuery query) {
        log.info("分页查询参与记录 - query: {}", query);
        IPage<TaskParticipationVO> result = taskParticipationService.pageListParticipations(query);
        return Result.success(result);
    }

    /**
     * 获取参与记录详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取参与记录详情", description = "根据ID获取参与记录的详细信息")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<TaskParticipationVO> getById(@PathVariable String id) {
        log.info("获取参与记录详情 - id: {}", id);
        TaskParticipation participation = taskParticipationService.getById(id);
        if (participation == null) {
            throw new AppException(ResultCodeEnum.DATA_NOT_FOUND, "参与记录不存在");
        }
        return Result.success(TaskParticipationVO.fromEntity(participation));
    }

    /**
     * 审核参与记录
     * <p>
     * 审核状态说明：
     * - 3: 审核通过（任务完成，触发奖励发放流程）
     * - 4: 审核拒绝（需填写拒绝理由，用户可重新提交）
     * </p>
     */
    @PostMapping("/{id}/audit")
    @Operation(summary = "审核参与记录", description = "审核用户提交的任务完成证明")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @SystemLog(module = "任务参与管理", action = "审核参与记录", recordResponse = true)
    public Result<Void> audit(
        @PathVariable String id,
        @Valid @RequestBody TaskParticipationAuditDTO auditDTO) {
        log.info("审核参与记录 - id: {}, auditDTO: {}", id, auditDTO);

        // 验证审核状态
        if (auditDTO.getAuditStatus() == null ||
            (auditDTO.getAuditStatus() != 3 && auditDTO.getAuditStatus() != 4)) {
            throw new AppException(ResultCodeEnum.BAD_REQUEST, "审核状态无效，只能是3(通过)或4(拒绝)");
        }

        // 如果是拒绝，必须填写理由
        if (auditDTO.getAuditStatus() == 4 &&
            (auditDTO.getAuditNotes() == null || auditDTO.getAuditNotes().trim().isEmpty())) {
            throw new AppException(ResultCodeEnum.BAD_REQUEST, "拒绝时必须填写拒绝理由");
        }

        String auditorId = UserContextHolder.getUserId();
        taskParticipationService.auditParticipation(id, auditDTO, auditorId);
        return Result.success();
    }

    /**
     * 批量审核参与记录
     */
    @PostMapping("/batch-audit")
    @Operation(summary = "批量审核参与记录", description = "批量审核多条参与记录")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @SystemLog(module = "任务参与管理", action = "批量审核参与记录", recordResponse = true)
    public Result<Void> batchAudit(
        @Parameter(description = "参与记录ID列表") @RequestBody List<String> ids,
        @Parameter(description = "审核状态") @RequestParam Byte auditStatus,
        @Parameter(description = "审核备注") @RequestParam(required = false) String auditNotes) {
        log.info("批量审核参与记录 - ids: {}, auditStatus: {}", ids, auditStatus);

        if (ids == null || ids.isEmpty()) {
            throw new AppException(ResultCodeEnum.BAD_REQUEST, "请选择要审核的记录");
        }

        // 验证审核状态
        if (auditStatus != 3 && auditStatus != 4) {
            throw new AppException(ResultCodeEnum.BAD_REQUEST, "审核状态无效");
        }

        // 如果是拒绝，必须填写理由
        if (auditStatus == 4 && (auditNotes == null || auditNotes.trim().isEmpty())) {
            throw new AppException(ResultCodeEnum.BAD_REQUEST, "拒绝时必须填写拒绝理由");
        }

        String auditorId = UserContextHolder.getUserId();
        TaskParticipationAuditDTO auditDTO = new TaskParticipationAuditDTO();
        auditDTO.setAuditStatus(auditStatus);
        auditDTO.setAuditNotes(auditNotes);

        for (String id : ids) {
            try {
                taskParticipationService.auditParticipation(id, auditDTO, auditorId);
            } catch (Exception e) {
                log.error("批量审核失败 - id: {}, error: {}", id, e.getMessage());
            }
        }

        return Result.success();
    }

    /**
     * 导出参与记录
     */
    @GetMapping("/export")
    @Operation(summary = "导出参与记录", description = "导出参与记录为CSV文件")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @SystemLog(module = "任务参与管理", action = "导出参与记录")
    public void export(TaskParticipationQuery query, HttpServletResponse response) throws IOException {
        log.info("导出参与记录 - query: {}", query);

        // 设置最大查询数量，防止数据过大
        query.setPageNum(1);
        query.setPageSize(10000);

        IPage<TaskParticipationVO> result = taskParticipationService.pageListParticipations(query);
        List<TaskParticipationVO> records = result.getRecords();

        // 设置响应头
        String fileName = "任务参与记录_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ".csv";
        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition",
            "attachment;filename=" + URLEncoder.encode(fileName, StandardCharsets.UTF_8));

        // 写入CSV内容
        try (PrintWriter writer = response.getWriter()) {
            // 写入BOM，解决Excel打开乱码问题
            writer.write('\ufeff');

            // 写入表头
            writer.println("ID,任务ID,用户ID,用户昵称,提交链接,提交时间,状态,审核备注,审核人,审核时间,奖励状态");

            // 写入数据行
            for (TaskParticipationVO record : records) {
                writer.println(String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s",
                    escapeCsv(record.getId()),
                    escapeCsv(record.getTaskId()),
                    escapeCsv(record.getUserId()),
                    escapeCsv(record.getUserName()),
                    escapeCsv(record.getSubmittedLink()),
                    record.getSubmittedAt() != null ? record.getSubmittedAt() : "",
                    escapeCsv(record.getStatusDesc()),
                    escapeCsv(record.getAuditNotes()),
                    escapeCsv(record.getAuditorId()),
                    record.getAuditedAt() != null ? record.getAuditedAt() : "",
                    escapeCsv(record.getRewardStatusDesc())
                ));
            }
        }
    }

    /**
     * 获取任务统计信息
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取任务参与统计", description = "获取指定任务的参与统计信息")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<TaskParticipationStatistics> getStatistics(
        @Parameter(description = "任务ID") @RequestParam String taskId) {
        log.info("获取任务参与统计 - taskId: {}", taskId);

        // 这里可以实现统计逻辑
        // 统计各状态的数量
        TaskParticipationStatistics statistics = new TaskParticipationStatistics();
        // TODO: 实现统计查询

        return Result.success(statistics);
    }

    /**
     * CSV转义处理
     */
    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        // 如果包含逗号、引号或换行，需要用引号包裹并转义引号
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    /**
     * 任务参与统计信息
     */
    @lombok.Data
    public static class TaskParticipationStatistics {
        private Long total;           // 总数
        private Long claimedCount;    // 已领取
        private Long submittedCount;  // 已提交
        private Long approvedCount;   // 审核通过
        private Long rejectedCount;   // 审核拒绝
        private Long pendingCount;    // 待审核
    }
}