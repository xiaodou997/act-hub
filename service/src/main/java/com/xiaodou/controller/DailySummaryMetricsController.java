package com.xiaodou.controller;

import com.xiaodou.model.DailySummaryMetrics;
import com.xiaodou.result.Result;
import com.xiaodou.service.DailySummaryMetricsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * <p>
 * 每日关键指标聚合表 前端控制器
 * </p>
 *
 * @author luoxiaodou
 * @since 2025-11-19
 */
@Tag(name = "后台-数据报表", description = "查询聚合后的统计报表数据")
@RestController
@RequestMapping("/admin/metrics")
@RequiredArgsConstructor
public class DailySummaryMetricsController {

    private final DailySummaryMetricsService dailySummaryMetricsService;

    @Operation(summary = "查询每日关键指标", description = "根据日期范围查询聚合后的每日关键指标")
    @GetMapping("/summary")
    public Result<List<DailySummaryMetrics>> getSummaryMetrics(
        @Parameter(description = "开始日期", required = true, example = "2025-11-01") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @Parameter(description = "结束日期", required = true, example = "2025-11-18") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        List<DailySummaryMetrics> metrics = dailySummaryMetricsService.lambdaQuery()
            .between(DailySummaryMetrics::getMetricDate, startDate, endDate)
            .orderByAsc(DailySummaryMetrics::getMetricDate)
            .list();
        return Result.success(metrics);
    }
}
