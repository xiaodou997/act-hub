package com.xiaodou.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaodou.mapper.SystemNotificationMapper;
import com.xiaodou.model.SystemNotification;
import com.xiaodou.service.SystemNotificationService;
import org.springframework.stereotype.Service;

@Service
public class SystemNotificationServiceImpl extends ServiceImpl<SystemNotificationMapper, SystemNotification> implements SystemNotificationService {
    @Override
    public IPage<SystemNotification> pageByUser(Page<SystemNotification> page, String userId) {
        LambdaQueryWrapper<SystemNotification> q = new LambdaQueryWrapper<>();
        q.eq(SystemNotification::getUserId, userId).orderByDesc(SystemNotification::getCreatedAt);
        return this.page(page, q);
    }

    @Override
    public void markRead(String id, String userId) {
        SystemNotification n = this.getById(id);
        if (n == null || !userId.equals(n.getUserId())) return;
        SystemNotification u = new SystemNotification();
        u.setId(id);
        u.setRead((byte)1);
        this.updateById(u);
    }
}

