package com.xiaodou.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaodou.model.UserBlacklist;

public interface UserBlacklistService extends IService<UserBlacklist> {
    IPage<UserBlacklist> pageList(Page<UserBlacklist> page, String userId, String phone, String nickname);
    boolean isBlacklisted(String userId);
}

