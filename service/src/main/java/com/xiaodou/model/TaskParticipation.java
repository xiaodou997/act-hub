package com.xiaodou.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 任务参与记录表
 * </p>
 *
 * @author luoxiaodou
 * @since 2025-11-17
 */
@Getter
@Setter
@ToString
@TableName("task_participation")
public class TaskParticipation implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 租户ID
     */
    private String tenantId;

    /**
     * 任务ID
     */
    private String taskId;

    /**
     * 参与用户ID（关联wechat_user表）
     */
    private String userId;

    /**
     * 参与状态（1:已领取, 2:已提交, 3:审核通过, 4:审核拒绝）
     */
    private Byte status;

    /**
     * 用户提交的内容链接
     */
    private String submittedLink;

    /**
     * 用户提交的文字内容（可选）
     */
    private String submittedContent;

    /**
     * 提交时间
     */
    private LocalDateTime submittedAt;

    /**
     * 审核备注（特别是拒绝理由）
     */
    private String auditNotes;

    /**
     * 审核人ID（关联user表）
     */
    private String auditorId;

    /**
     * 审核时间
     */
    private LocalDateTime auditedAt;

    /**
     * 提交链接的页面快照URL
     */
    private String snapshotUrl;

    /**
     * 历史提交与审核记录 (JSON数组)
     */
    private String submissionHistory;

    /**
     * 奖励状态（0:待发放, 1:已发放, 2:发放失败）
     */
    private Byte rewardStatus;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    // 参与状态枚举
    @Getter
    public enum ParticipationStatus {
        CLAIMED((byte) 1, "已领取"),
        SUBMITTED((byte) 2, "已提交"),
        APPROVED((byte) 3, "审核通过"),
        REJECTED((byte) 4, "审核拒绝");

        private final Byte code;
        private final String desc;

        ParticipationStatus(Byte code, String desc) {
            this.code = code;
            this.desc = desc;
        }

    }

    // 奖励状态枚举
    @Getter
    public enum RewardStatus {
        PENDING((byte) 0, "待发放"),
        PAID((byte) 1, "已发放"),
        FAILED((byte) 2, "发放失败");

        private final Byte code;
        private final String desc;

        RewardStatus(Byte code, String desc) {
            this.code = code;
            this.desc = desc;
        }

    }

    /**
     * 提交历史记录条目
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubmissionHistoryEntry implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
        private String submittedLink;
        private LocalDateTime submittedAt;
        private Byte auditStatus; // 审核结果：3-通过, 4-拒绝
        private String auditNotes;
        private String auditorId;
        private LocalDateTime auditedAt;
    }
}
