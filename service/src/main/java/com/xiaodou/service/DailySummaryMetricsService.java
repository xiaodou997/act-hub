package com.xiaodou.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaodou.model.DailySummaryMetrics;

import java.time.LocalDate;

/**
 * <p>
 * 每日关键指标聚合表 服务类
 * </p>
 *
 * @author luoxiaodou
 * @since 2025-11-19
 */
public interface DailySummaryMetricsService extends IService<DailySummaryMetrics> {

    /**
     * 聚合指定日期的埋点数据并保存
     * @param date 统计日期
     */
    void aggregateAndSaveDailyMetrics(LocalDate date);
}