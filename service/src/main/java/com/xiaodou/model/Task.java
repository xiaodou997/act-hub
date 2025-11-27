package com.xiaodou.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * 分发任务表
 * </p>
 *
 * @author luoxiaodou
 * @since 2025-11-17
 */
@Getter
@Setter
@ToString
@TableName("task")
public class Task implements Serializable {

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
     * 任务名称
     */
    private String taskName;

    /**
     * 任务开始时间
     */
    private LocalDateTime startTime;

    /**
     * 任务结束时间
     */
    private LocalDateTime endTime;

    /**
     * 发布平台, 存储为 JSON 字符串，例如 ["XIAOHONGSHU", "DOUYIN"]
     */
    private String platforms; // 存储JSON字符串

    /**
     * 补充要求及规则，支持富文本
     */
    private String requirements;

    /**
     * 任务相关图片URL列表，存储为 JSON 字符串
     */
    private String images; // 存储JSON字符串

    /**
     * 单条任务完成奖励金额
     */
    private BigDecimal rewardAmount;

    /**
     * 任务总量（总参与人数）
     */
    private Integer totalQuota;

    /**
     * 已领取人数（缓存计数，提高性能）
     */
    private Integer claimedCount;

    /**
     * 已完成人数（缓存计数，提高性能）
     */
    private Integer completedCount;

    /**
     * 是否为定向任务（0:否, 1:是）
     */
    private Boolean isTargeted;

    /**
     * 任务权重（排序值），数字越大越靠前
     */
    private Integer sortOrder;

    /**
     * 任务状态（0:草稿, 1:上线, 2:下线, 3:已结束）
     */
    private Byte status;

    /**
     * 创建人ID（关联user表）
     */
    private String creatorId;

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

    // 任务状态枚举
    @Getter
    public enum TaskStatus {
        DRAFT((byte) 0, "草稿"),
        ONLINE((byte) 1, "上线"),
        OFFLINE((byte) 2, "下线"),
        ENDED((byte) 3, "已结束");

        private final Byte code;
        private final String desc;

        TaskStatus(Byte code, String desc) {
            this.code = code;
            this.desc = desc;
        }

    }
}
