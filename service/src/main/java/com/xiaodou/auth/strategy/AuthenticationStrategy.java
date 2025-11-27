package com.xiaodou.auth.strategy;

import com.xiaodou.model.LoginUser;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 认证策略接口
 * @author xiaodou V=>dddou117
 * @version V1.0
 * @since 2025/9/8
 */
public interface AuthenticationStrategy {

    /**
     * 判断是否支持当前认证上下文
     */
    boolean supports(AuthenticationContext context);

    /**
     * 执行认证
     */
    LoginUser authenticate(AuthenticationContext context);

    /**
     * 设置安全上下文
     */
    void setSecurityContext(HttpServletRequest request, LoginUser loginUser);

    /**
     * 获取策略类型（用于日志和调试）
     */
    String getStrategyType();
}
