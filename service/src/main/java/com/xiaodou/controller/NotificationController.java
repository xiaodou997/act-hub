package com.xiaodou.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiaodou.auth.util.UserContextHolder;
import com.xiaodou.model.SystemNotification;
import com.xiaodou.result.Result;
import com.xiaodou.service.SystemNotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "系统通知", description = "消息通知查询与标记")
@RestController
@RequestMapping("/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final SystemNotificationService notificationService;

    @GetMapping("/page")
    @Operation(summary = "分页查询我的通知")
    public Result<IPage<SystemNotification>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        String userId = UserContextHolder.getUserId();
        IPage<SystemNotification> page = notificationService.pageByUser(new Page<>(pageNum, pageSize), userId);
        return Result.success(page);
    }

    @PostMapping("/{id}/read")
    @Operation(summary = "标记已读")
    public Result<Void> markRead(@PathVariable String id) {
        String userId = UserContextHolder.getUserId();
        notificationService.markRead(id, userId);
        return Result.success();
    }
}

