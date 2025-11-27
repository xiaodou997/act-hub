package com.xiaodou.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiaodou.model.WechatUser;
import com.xiaodou.mapper.WechatUserMapper;
import com.xiaodou.model.query.WechatUserQuery;
import com.xiaodou.service.WechatApiService;
import com.xiaodou.service.WechatUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * <p>
 * 微信小程序用户表 服务实现类
 * </p>
 *
 * @author luoxiaodou
 * @since 2025-11-16
 */
@Service
public class WechatUserServiceImpl extends ServiceImpl<WechatUserMapper, WechatUser> implements WechatUserService {

    @Override
    public Page<WechatUser> listUsersByPage(WechatUserQuery query) {
        Page<WechatUser> page = new Page<>(query.getPageNum(), query.getPageSize());
        QueryWrapper<WechatUser> wrapper = new QueryWrapper<>();

        // 构建动态查询条件
        wrapper.like(StringUtils.hasText(query.getNickname()), "nickname", query.getNickname());
        wrapper.like(StringUtils.hasText(query.getPhoneNumber()), "phone_number", query.getPhoneNumber());
        wrapper.eq(query.getStatus() != null, "status", query.getStatus());

        // 按创建时间倒序
        wrapper.orderByDesc("created_at");

        return this.page(page, wrapper);
    }

    @Override
    public WechatUser loginOrRegister(WechatApiService.WechatSessionVO sessionVO) {
        // 1. 根据 openid 查询用户
        WechatUser user = this.getOne(new QueryWrapper<WechatUser>().eq("openid", sessionVO.getOpenid()));

        if (user != null) {
            // 2. 如果用户存在，更新最后登录时间并返回
            user.setLastLoginAt(LocalDateTime.now());
            // 可选：如果 unionid 为空，此时可以尝试更新
            if (user.getUnionid() == null && sessionVO.getUnionid() != null) {
                user.setUnionid(sessionVO.getUnionid());
            }
            this.updateById(user);
            return user;
        } else {
            // 3. 如果用户不存在，创建新用户
            WechatUser newUser = new WechatUser();
            newUser.setOpenid(sessionVO.getOpenid());
            newUser.setUnionid(sessionVO.getUnionid());
            newUser.setStatus((byte) 1); // 默认状态正常
            newUser.setLastLoginAt(LocalDateTime.now());
            this.save(newUser);
            return newUser;
        }
    }
}
