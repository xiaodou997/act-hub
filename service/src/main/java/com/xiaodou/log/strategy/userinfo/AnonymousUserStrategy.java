package com.xiaodou.log.strategy.userinfo;

import com.xiaodou.log.model.LogUser;

/**
 * 匿名用户信息策略
 *
 * @author xiaodou V=>dddou117
 * @version V1.0
 * @since 2025/10/9
 */
public class AnonymousUserStrategy implements UserInfoStrategy {

    @Override
    public LogUser resolveUser() {
        return LogUser.anonymous();
    }

    @Override
    public String getStrategyName() {
        return "ANONYMOUS";
    }
}
