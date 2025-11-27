package com.xiaodou.log.strategy.userinfo;

import com.xiaodou.log.model.LogUser;

/**
 * 用户信息解析策略
 */
public interface UserInfoStrategy {

    /**
     * 解析用户信息
     */
    LogUser resolveUser();

    /**
     * 策略名称
     */
    String getStrategyName();
}
