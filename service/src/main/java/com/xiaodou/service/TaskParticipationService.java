package com.xiaodou.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaodou.model.TaskParticipation;
import com.xiaodou.model.dto.task.TaskParticipationAuditDTO;
import com.xiaodou.model.query.TaskParticipationQuery;
import com.xiaodou.model.vo.TaskParticipationVO;

/**
 * <p>
 * 任务参与记录表 服务类
 * </p>
 *
 * @author luoxiaodou
 * @since 2025-11-17
 */
public interface TaskParticipationService extends IService<TaskParticipation> {

    /**
     * 分页查询任务参与记录
     * @param query 查询条件
     * @return 分页数据
     */
    IPage<TaskParticipationVO> pageListParticipations(TaskParticipationQuery query);

    /**
     * 审核任务
     * @param participationId 参与记录ID
     * @param auditDTO 审核数据
     * @param auditorId 审核人ID
     */
    void auditParticipation(String participationId, TaskParticipationAuditDTO auditDTO, String auditorId);
}