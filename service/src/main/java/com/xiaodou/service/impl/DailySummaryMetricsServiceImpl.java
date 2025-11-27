package com.xiaodou.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaodou.mapper.DailySummaryMetricsMapper;
import com.xiaodou.mapper.TrackingEventMapper;
import com.xiaodou.model.DailySummaryMetrics;
import com.xiaodou.service.DailySummaryMetricsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
public class DailySummaryMetricsServiceImpl extends ServiceImpl<DailySummaryMetricsMapper, DailySummaryMetrics> implements DailySummaryMetricsService {

    private final TrackingEventMapper trackingEventMapper;
    private final DailySummaryMetricsMapper dailySummaryMetricsMapper;

    @Override
    @Transactional
    public void aggregateAndSaveDailyMetrics(LocalDate date) {
        LocalDateTime startTime = date.atStartOfDay();
        LocalDateTime endTime = date.atTime(LocalTime.MAX);

        // 1. 查询需要聚合的数据
        // 注意：在实际的多租户场景中，这里需要按 tenant_id 进行分组聚合
        Integer dailyActiveUsers = trackingEventMapper.countDistinctUsersByTimeRange(startTime, endTime, null);
        Integer taskCompletions = trackingEventMapper.countEventsByNameAndTimeRange("TaskSubmit", startTime, endTime, null);
        Integer articlesCreated = trackingEventMapper.countEventsByNameAndTimeRange("ArticleCreate", startTime, endTime, null);
        Integer rewardsPaidOut = trackingEventMapper.countEventsByNameAndTimeRange("RewardPayout", startTime, endTime, null);

        // 2. 构造或更新聚合实体
        DailySummaryMetrics metrics = new DailySummaryMetrics();
        metrics.setMetricDate(date);
        // metrics.setTenantId(tenantId); // 在实际多租户场景中设置
        metrics.setDailyActiveUsers(dailyActiveUsers);
        metrics.setTaskCompletions(taskCompletions);
        metrics.setArticlesCreated(articlesCreated);
        metrics.setRewardsPaidOut(rewardsPaidOut);

        // 3. 使用 INSERT ... ON DUPLICATE KEY UPDATE 保存数据
        dailySummaryMetricsMapper.upsert(metrics);
    }
}