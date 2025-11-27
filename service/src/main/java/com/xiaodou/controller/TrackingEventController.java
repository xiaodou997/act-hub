package com.xiaodou.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiaodou.auth.util.UserContextHolder;
import com.xiaodou.config.rabbitmq.TrackingRabbitMQConfig;
import com.xiaodou.model.TrackingEvent;
import com.xiaodou.model.dto.tracking.EventMessage;
import com.xiaodou.model.dto.tracking.TrackingEventDTO;
import com.xiaodou.result.Result;
import com.xiaodou.service.TrackingEventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 埋点事件前端控制器
 * </p>
 *
 * @author luoxiaodou
 * @since 2025-11-19
 */
@Tag(name = "数据埋点", description = "C端上报接口与后台查询接口")
@RestController
@RequiredArgsConstructor
public class TrackingEventController {

    private final RabbitTemplate rabbitTemplate;
    private final TrackingEventService trackingEventService;

    @Operation(summary = "C端-上报埋点事件", description = "接收前端发送的埋点事件，并异步处理")
    @PostMapping("/api/track")
    public Result<Void> trackEvent(@Valid @RequestBody TrackingEventDTO eventDTO) {
        String userId = UserContextHolder.getUserId();
        String tenantId = UserContextHolder.getTenantId();
        EventMessage message = new EventMessage(eventDTO.getEventName(), userId, tenantId, eventDTO.getProperties());
        rabbitTemplate.convertAndSend(TrackingRabbitMQConfig.EXCHANGE_NAME, TrackingRabbitMQConfig.ROUTING_KEY, message);
        return Result.success();
    }

    @Operation(summary = "后台-查询原始埋点事件", description = "分页查询原始埋点事件明细")
    @GetMapping("/admin/tracking-event/page")
    public Result<IPage<TrackingEvent>> pageListEvents(
        @RequestParam(defaultValue = "1") int pageNum,
        @RequestParam(defaultValue = "10") int pageSize) {
        // 此处可扩展更复杂的查询条件
        IPage<TrackingEvent> page = trackingEventService.lambdaQuery()
            .orderByDesc(TrackingEvent::getCreatedAt)
            .page(new Page<>(pageNum, pageSize));
        return Result.success(page);
    }
}
