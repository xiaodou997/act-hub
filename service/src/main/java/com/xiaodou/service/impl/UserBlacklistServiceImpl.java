package com.xiaodou.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaodou.mapper.UserBlacklistMapper;
import com.xiaodou.model.UserBlacklist;
import com.xiaodou.service.UserBlacklistService;
import org.springframework.stereotype.Service;

@Service
public class UserBlacklistServiceImpl extends ServiceImpl<UserBlacklistMapper, UserBlacklist> implements UserBlacklistService {
    @Override
    public IPage<UserBlacklist> pageList(Page<UserBlacklist> page, String userId, String phone, String nickname) {
        LambdaQueryWrapper<UserBlacklist> q = new LambdaQueryWrapper<>();
        if (userId != null && !userId.trim().isEmpty()) q.eq(UserBlacklist::getUserId, userId.trim());
        if (phone != null && !phone.trim().isEmpty()) q.eq(UserBlacklist::getPhone, phone.trim());
        if (nickname != null && !nickname.trim().isEmpty()) q.like(UserBlacklist::getNickname, nickname.trim());
        q.orderByDesc(UserBlacklist::getCreatedAt);
        return this.page(page, q);
    }

    @Override
    public boolean isBlacklisted(String userId) {
        if (userId == null || userId.trim().isEmpty()) return false;
        return this.count(new LambdaQueryWrapper<UserBlacklist>().eq(UserBlacklist::getUserId, userId.trim())) > 0;
    }
}

