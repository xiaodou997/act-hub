package com.xiaodou.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaodou.mapper.UserAddressMapper;
import com.xiaodou.model.UserAddress;
import com.xiaodou.service.UserAddressService;
import org.springframework.stereotype.Service;

@Service
public class UserAddressServiceImpl extends ServiceImpl<UserAddressMapper, UserAddress> implements UserAddressService {
    @Override
    public IPage<UserAddress> pageByUser(Page<UserAddress> page, String userId) {
        LambdaQueryWrapper<UserAddress> q = new LambdaQueryWrapper<>();
        q.eq(UserAddress::getUserId, userId).orderByDesc(UserAddress::getUpdatedAt);
        return this.page(page, q);
    }
}

