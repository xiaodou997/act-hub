package com.xiaodou.model.vo;

import com.xiaodou.model.TaskParticipation;
import com.xiaodou.utils.DateTimeUtils;
import lombok.Data;
import org.springframework.beans.BeanUtils;

/**
 * 任务参与记录视图对象
 */
@Data
public class TaskParticipationVO {

    private String id;

    private String taskId;

    private String userId; // 参与用户ID

    private String userName; // 参与用户昵称 (需要联表查询或额外设置)

    private Byte status; // 参与状态

    private String statusDesc; // 参与状态描述

    private String submittedLink;

    private String submittedContent;

    private Long submittedAt;

    private String auditNotes;

    private String auditorId;

    private Long auditedAt;

    private String snapshotUrl;

    private Byte rewardStatus;

    private String rewardStatusDesc; // 奖励状态描述

    private Long createdAt;

    private Long updatedAt;

    public static TaskParticipationVO fromEntity(TaskParticipation participation) {
        if (participation == null) {
            return null;
        }
        TaskParticipationVO vo = new TaskParticipationVO();
        BeanUtils.copyProperties(participation, vo);

        // 转换时间为时间戳
        vo.setSubmittedAt(DateTimeUtils.toTimestampAtUTC8(participation.getSubmittedAt()));
        vo.setAuditedAt(DateTimeUtils.toTimestampAtUTC8(participation.getAuditedAt()));
        vo.setCreatedAt(DateTimeUtils.toTimestampAtUTC8(participation.getCreatedAt()));
        vo.setUpdatedAt(DateTimeUtils.toTimestampAtUTC8(participation.getUpdatedAt()));

        // 填充描述字段
        vo.populateDescriptions();

        return vo;
    }

    /**
     * 根据 code 填充 statusDesc 和 rewardStatusDesc 描述字段
     */
    public void populateDescriptions() {
        // 转换参与状态描述
        if (this.getStatus() != null) {
            for (TaskParticipation.ParticipationStatus ps : TaskParticipation.ParticipationStatus.values()) {
                if (ps.getCode().equals(this.getStatus())) {
                    this.setStatusDesc(ps.getDesc());
                    break;
                }
            }
        }

        // 转换奖励状态描述
        if (this.getRewardStatus() != null) {
            for (TaskParticipation.RewardStatus rs : TaskParticipation.RewardStatus.values()) {
                if (rs.getCode().equals(this.getRewardStatus())) {
                    this.setRewardStatusDesc(rs.getDesc());
                    break;
                }
            }
        }
    }
}
