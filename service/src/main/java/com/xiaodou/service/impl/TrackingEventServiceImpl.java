package com.xiaodou.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaodou.mapper.TrackingEventMapper;
import com.xiaodou.model.TrackingEvent;
import com.xiaodou.service.TrackingEventService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 埋点事件原始记录表 服务实现类
 * </p>
 *
 * @author luoxiaodou
 * @since 2025-11-19
 */
@Service
public class TrackingEventServiceImpl extends ServiceImpl<TrackingEventMapper, TrackingEvent> implements TrackingEventService {

}