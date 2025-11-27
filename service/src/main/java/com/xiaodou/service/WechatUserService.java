package com.xiaodou.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiaodou.model.WechatUser;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaodou.model.query.WechatUserQuery;

/**
 * <p>
 * 微信小程序用户表 服务类
 * </p>
 *
 * @author luoxiaodou
 * @since 2025-11-16
 */
public interface WechatUserService extends IService<WechatUser> {

    /**
     * 根据条件分页查询微信用户列表
     *
     * @param query 查询条件
     * @return 分页的用户数据
     */
    Page<WechatUser> listUsersByPage(WechatUserQuery query);

    /**
     * 处理微信用户登录或注册
     * <p>
     * 根据微信返回的 openid 查询用户，如果用户不存在则创建新用户。
     *
     * @param sessionVO 包含 openid 和 unionid 的微信会话信息
     * @return 系统中的微信用户实体
     */
    WechatUser loginOrRegister(WechatApiService.WechatSessionVO sessionVO);
}
