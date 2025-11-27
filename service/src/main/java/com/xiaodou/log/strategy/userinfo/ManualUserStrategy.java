package com.xiaodou.log.strategy.userinfo;

import com.xiaodou.log.model.LogUser;
import com.xiaodou.log.model.UserType;

/**
 * 手动用户信息策略
 *
 * @author xiaodou V=>dddou117
 * @version V1.0
 * @since 2025/10/9
 */
public class ManualUserStrategy implements UserInfoStrategy {

    private final LogUser user;

    public ManualUserStrategy(String userId, String tenantId, UserType userType) {
        this.user = new LogUser(userId, tenantId, userType);
    }

    public ManualUserStrategy(String userId, String tenantId) {
        this(userId, tenantId, UserType.SYSTEM);
    }

    public ManualUserStrategy(String userId) {
        this(userId, null, UserType.SYSTEM);
    }

    @Override
    public LogUser resolveUser() {
        return user;
    }

    @Override
    public String getStrategyName() {
        return "MANUAL";
    }
}
