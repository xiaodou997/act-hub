package com.xiaodou.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaodou.model.SystemNotification;

public interface SystemNotificationService extends IService<SystemNotification> {
    IPage<SystemNotification> pageByUser(Page<SystemNotification> page, String userId);
    void markRead(String id, String userId);
}

