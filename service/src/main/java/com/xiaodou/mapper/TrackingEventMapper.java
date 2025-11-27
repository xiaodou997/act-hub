package com.xiaodou.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiaodou.model.TrackingEvent;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

/**
 * <p>
 * 埋点事件原始记录表 Mapper 接口
 * </p>
 *
 * @author luoxiaodou
 * @since 2025-11-19
 */
public interface TrackingEventMapper extends BaseMapper<TrackingEvent> {

    /**
     * 统计指定时间范围内的日活跃用户数 (DAU)
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param tenantId 租户ID (可选)
     * @return 独立用户数
     */
    Integer countDistinctUsersByTimeRange(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime, @Param("tenantId") String tenantId);

    /**
     * 统计指定时间范围内特定事件的发生次数
     * @param eventName 事件名称
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param tenantId 租户ID (可选)
     * @return 事件发生次数
     */
    Integer countEventsByNameAndTimeRange(@Param("eventName") String eventName, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime, @Param("tenantId") String tenantId);
}
