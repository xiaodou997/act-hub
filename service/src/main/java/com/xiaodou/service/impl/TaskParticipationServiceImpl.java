package com.xiaodou.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xiaodou.exception.AppException;
import com.xiaodou.mapper.TaskParticipationMapper;
import com.xiaodou.model.Task;
import com.xiaodou.model.TaskParticipation;
import com.xiaodou.model.dto.task.TaskParticipationAuditDTO;
import com.xiaodou.model.query.TaskParticipationQuery;
import com.xiaodou.model.vo.TaskParticipationVO;
import com.xiaodou.result.ResultCodeEnum;
import com.xiaodou.service.TaskParticipationService;
import com.xiaodou.service.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 任务参与记录表 服务实现类
 * </p>
 *
 * @author luoxiaodou
 * @since 2025-11-17
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TaskParticipationServiceImpl extends ServiceImpl<TaskParticipationMapper, TaskParticipation> implements TaskParticipationService {

    private final TaskService taskService;
    private final TaskParticipationMapper taskParticipationMapper;
    private final ObjectMapper objectMapper; // 注入ObjectMapper

    @Override
    public IPage<TaskParticipationVO> pageListParticipations(TaskParticipationQuery query) {
        Page<TaskParticipationVO> page = new Page<>(query.getPageNum(), query.getPageSize());
        IPage<TaskParticipationVO> pageResult = taskParticipationMapper.selectPageWithDetails(page, query);
        
        // 调用VO自己的方法来填充派生字段，消除重复代码
        pageResult.getRecords().forEach(TaskParticipationVO::populateDescriptions);
        
        return pageResult;
    }

    @Override
    @Transactional
    public void auditParticipation(String participationId, TaskParticipationAuditDTO auditDTO, String auditorId) {
        TaskParticipation participation = this.getById(participationId);
        if (participation == null) {
            throw new AppException(ResultCodeEnum.DATA_NOT_FOUND, "参与记录不存在");
        }
        if (!TaskParticipation.ParticipationStatus.SUBMITTED.getCode().equals(participation.getStatus())) {
            throw new AppException(ResultCodeEnum.BUSINESS_ERROR, "该记录不是“已提交”状态，无法审核");
        }

        // 1. 读取并反序列化现有的 submissionHistory
        List<TaskParticipation.SubmissionHistoryEntry> history = new ArrayList<>();
        if (StringUtils.hasText(participation.getSubmissionHistory())) {
            try {
                history = objectMapper.readValue(participation.getSubmissionHistory(), new TypeReference<List<TaskParticipation.SubmissionHistoryEntry>>() {});
            } catch (JsonProcessingException e) {
                log.error("反序列化提交历史失败，participationId: {}", participationId, e);
                // 异常处理：可以抛出业务异常，或者记录日志并初始化为空列表
            }
        }

        // 2. 创建新的历史条目，记录本次提交和审核结果
        TaskParticipation.SubmissionHistoryEntry currentEntry = new TaskParticipation.SubmissionHistoryEntry();
        currentEntry.setSubmittedLink(participation.getSubmittedLink());
        currentEntry.setSubmittedAt(participation.getSubmittedAt());
        currentEntry.setAuditStatus(auditDTO.getAuditStatus());
        currentEntry.setAuditNotes(auditDTO.getAuditNotes());
        currentEntry.setAuditorId(auditorId);
        currentEntry.setAuditedAt(LocalDateTime.now());
        history.add(currentEntry);

        // 3. 更新主参与记录
        TaskParticipation update = new TaskParticipation();
        update.setId(participationId);
        update.setStatus(auditDTO.getAuditStatus());
        update.setAuditNotes(auditDTO.getAuditNotes()); // 记录本次审核的备注
        update.setAuditorId(auditorId);
        update.setAuditedAt(LocalDateTime.now());

        try {
            update.setSubmissionHistory(objectMapper.writeValueAsString(history));
        } catch (JsonProcessingException e) {
            log.error("序列化提交历史失败，participationId: {}", participationId, e);
            throw new AppException(ResultCodeEnum.INTERNAL_SERVER_ERROR, "保存审核历史失败");
        }

        // 如果审核结果是拒绝，清空 submittedLink 和 submittedAt，等待用户重新提交
        if (TaskParticipation.ParticipationStatus.REJECTED.getCode().equals(auditDTO.getAuditStatus())) {
            update.setSubmittedLink(null);
            update.setSubmittedAt(null);
        }

        this.updateById(update);

        // 4. 如果审核通过，增加任务的“已完成”计数
        if (TaskParticipation.ParticipationStatus.APPROVED.getCode().equals(auditDTO.getAuditStatus())) {
            Task task = taskService.getById(participation.getTaskId());
            if (task != null) {
                task.setCompletedCount(task.getCompletedCount() + 1);
                taskService.updateById(task);
            }
        }
    }
}