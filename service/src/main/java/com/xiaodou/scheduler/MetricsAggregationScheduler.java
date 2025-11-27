package com.xiaodou.scheduler;

import com.xiaodou.service.DailySummaryMetricsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * 指标聚合定时任务
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MetricsAggregationScheduler {

    private final DailySummaryMetricsService dailySummaryMetricsService;

    /**
     * 每天凌晨2点执行，聚合前一天的埋点数据
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void aggregateDailyMetrics() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        log.info("开始执行 {} 的每日指标聚合任务...", yesterday);
        try {
            dailySummaryMetricsService.aggregateAndSaveDailyMetrics(yesterday);
            log.info("成功完成 {} 的每日指标聚合任务。", yesterday);
        } catch (Exception e) {
            log.error("执行 {} 的每日指标聚合任务失败。", yesterday, e);
        }
    }
}
