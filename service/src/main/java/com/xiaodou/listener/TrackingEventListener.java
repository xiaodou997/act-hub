package com.xiaodou.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xiaodou.config.rabbitmq.TrackingRabbitMQConfig;
import com.xiaodou.model.TrackingEvent;
import com.xiaodou.model.dto.tracking.EventMessage;
import com.xiaodou.service.TrackingEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Slf4j
@Component
@RequiredArgsConstructor
public class TrackingEventListener {

    private final TrackingEventService trackingEventService;
    private final ObjectMapper objectMapper;

    @RabbitListener(queues = TrackingRabbitMQConfig.QUEUE_NAME)
    public void handleTrackingEvent(EventMessage message) {
        log.info("接收到埋点事件: {}", message);
        try {
            TrackingEvent event = new TrackingEvent();
            event.setEventName(message.getEventName());
            event.setUserId(message.getUserId());
            event.setTenantId(message.getTenantId());

            if (!CollectionUtils.isEmpty(message.getProperties())) {
                try {
                    event.setProperties(objectMapper.writeValueAsString(message.getProperties()));
                } catch (JsonProcessingException e) {
                    log.error("序列化埋点事件属性失败: {}", message.getProperties(), e);
                    // 可以选择记录一个错误标记，或者丢弃该属性
                    event.setProperties("{\"error\":\"properties serialization failed\"}");
                }
            }
            trackingEventService.save(event);
        } catch (Exception e) {
            log.error("处理埋点事件失败: {}", message, e);
            // 此处可以加入重试或死信队列逻辑
        }
    }
}
