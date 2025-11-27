package com.xiaodou.auth.strategy;

import com.xiaodou.auth.exception.AuthenticationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 认证策略工厂
 *
 * @author xiaodou V=>dddou117
 * @version V1.0
 * @since 2025/9/8
 */
@Slf4j
@Component
public class AuthenticationStrategyFactory {

    private final List<AuthenticationStrategy> strategies;

    public AuthenticationStrategyFactory(List<AuthenticationStrategy> strategies) {
        this.strategies = strategies;
        log.info("📋 注册认证策略: {}", strategies.stream()
            .map(AuthenticationStrategy::getStrategyType)
            .toList());
    }

    /**
     * 根据上下文获取合适的认证策略
     */
    public AuthenticationStrategy getStrategy(AuthenticationContext context) {
        return strategies.stream()
            .filter(strategy -> strategy.supports(context))
            .findFirst()
            .orElseThrow(
                () -> new AuthenticationException("没有找到合适的认证策略", context.method(), context.requestUri()));
    }
}
