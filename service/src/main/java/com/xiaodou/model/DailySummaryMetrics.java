package com.xiaodou.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * <p>
 * 每日关键指标聚合表
 * </p>
 *
 * @author luoxiaodou
 * @since 2025-11-19
 */
@Getter
@Setter
@ToString
@TableName("daily_summary_metrics")
public class DailySummaryMetrics implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 统计日期
     */
    private LocalDate metricDate;

    /**
     * 租户ID
     */
    private String tenantId;

    /**
     * 日活跃用户数（DAU）
     */
    private Integer dailyActiveUsers;

    /**
     * 任务完成总数
     */
    private Integer taskCompletions;

    /**
     * 创作文章总数
     */
    private Integer articlesCreated;

    /**
     * 发放奖励总数
     */
    private Integer rewardsPaidOut;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
