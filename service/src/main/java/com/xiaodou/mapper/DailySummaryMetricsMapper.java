package com.xiaodou.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiaodou.model.DailySummaryMetrics;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 每日关键指标聚合表 Mapper 接口
 * </p>
 *
 * @author luoxiaodou
 * @since 2025-11-19
 */
public interface DailySummaryMetricsMapper extends BaseMapper<DailySummaryMetrics> {

    /**
     * 插入或更新每日指标数据
     * @param metrics 指标实体
     */
    void upsert(@Param("metrics") DailySummaryMetrics metrics);
}
