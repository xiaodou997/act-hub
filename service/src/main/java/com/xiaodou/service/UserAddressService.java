package com.xiaodou.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaodou.model.UserAddress;

public interface UserAddressService extends IService<UserAddress> {
    IPage<UserAddress> pageByUser(Page<UserAddress> page, String userId);
}

