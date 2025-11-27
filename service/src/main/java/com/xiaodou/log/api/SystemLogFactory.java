package com.xiaodou.log.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xiaodou.log.core.DefaultSystemLogger;
import com.xiaodou.log.strategy.context.EmptyContextStrategy;
import com.xiaodou.log.strategy.context.HttpRequestContextStrategy;
import com.xiaodou.log.strategy.context.LogContextStrategy;
import com.xiaodou.log.model.LogContext;
import com.xiaodou.log.model.UserType;
import com.xiaodou.log.strategy.userinfo.AnonymousUserStrategy;
import com.xiaodou.log.strategy.userinfo.ManualUserStrategy;
import com.xiaodou.log.strategy.userinfo.SecurityContextUserStrategy;
import com.xiaodou.log.strategy.userinfo.UserInfoStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Role;
import org.springframework.stereotype.Component;

/**
 * 系统日志工厂类
 * 负责创建各种类型的日志记录器实例
 *
 * @author xiaodou V>dddou117
 * @since 1.2 (Refactored)
 */

/**
 * 系统日志工厂类
 * 负责创建各种类型的日志记录器实例
 */
@Component
@Slf4j
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
public class SystemLogFactory {

    private final ApplicationEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;
    private final SecurityContextUserStrategy securityContextUserStrategy;
    private final HttpRequestContextStrategy httpRequestContextStrategy;

    public SystemLogFactory(ApplicationEventPublisher eventPublisher, ObjectMapper objectMapper,
        SecurityContextUserStrategy securityContextUserStrategy,
        HttpRequestContextStrategy httpRequestContextStrategy) {
        this.eventPublisher = eventPublisher;
        this.objectMapper = objectMapper;
        this.securityContextUserStrategy = securityContextUserStrategy;
        this.httpRequestContextStrategy = httpRequestContextStrategy;
    }

    // ========== 主要工厂方法 ==========

    /**
     * 获取默认日志记录器（使用安全上下文策略）
     */
    public SystemLogger getLogger(String module) {
        return new DefaultSystemLogger(module, eventPublisher, objectMapper, securityContextUserStrategy,
            httpRequestContextStrategy);
    }

    /**
     * 获取手动用户日志记录器
     */
    public SystemLogger getLogger(String module, String userId, String tenantId) {
        UserInfoStrategy manualStrategy = new ManualUserStrategy(userId, tenantId);
        return new DefaultSystemLogger(module, eventPublisher, objectMapper, manualStrategy, httpRequestContextStrategy);
    }

    /**
     * 获取手动用户日志记录器（指定用户类型）
     */
    public SystemLogger getLogger(String module, String userId, String tenantId, UserType userType) {
        UserInfoStrategy manualStrategy = new ManualUserStrategy(userId, tenantId, userType);
        return new DefaultSystemLogger(module, eventPublisher, objectMapper, manualStrategy, httpRequestContextStrategy);
    }

    /**
     * 获取匿名用户日志记录器
     */
    public SystemLogger getAnonymousLogger(String module) {
        UserInfoStrategy anonymousStrategy = new AnonymousUserStrategy();
        return new DefaultSystemLogger(module, eventPublisher, objectMapper, anonymousStrategy, httpRequestContextStrategy);
    }

    /**
     * 获取自定义策略日志记录器
     */
    public SystemLogger getLogger(String module, UserInfoStrategy userInfoStrategy) {
        return new DefaultSystemLogger(module, eventPublisher, objectMapper, userInfoStrategy, httpRequestContextStrategy);
    }

    /**
     * 获取自定义上下文日志记录器
     */
    public SystemLogger getLogger(String module, LogContext customContext) {
        UserInfoStrategy strategy =
            new ManualUserStrategy(customContext != null ? "custom" : "unknown", null, UserType.SYSTEM);
        LogContextStrategy contextStrategy = () -> customContext != null ? customContext : LogContext.empty();

        return new DefaultSystemLogger(module, eventPublisher, objectMapper, strategy, contextStrategy);
    }

    // ========== 便捷方法 ==========

    /**
     * 基于类名获取日志记录器
     */
    public SystemLogger getLogger(Class<?> clazz) {
        return getLogger(clazz.getSimpleName());
    }

    /**
     * 基于类名获取匿名日志记录器
     */
    public SystemLogger getAnonymousLogger(Class<?> clazz) {
        return getAnonymousLogger(clazz.getSimpleName());
    }

    /**
     * 获取批处理任务日志记录器
     */
    public SystemLogger getBatchJobLogger(String module, String jobId) {
        UserInfoStrategy batchStrategy = new ManualUserStrategy(jobId, "batch", UserType.BATCH_JOB);
        return new DefaultSystemLogger(module, eventPublisher, objectMapper, batchStrategy, new EmptyContextStrategy());
    }

    /**
     * 获取外部系统日志记录器
     */
    public SystemLogger getExternalSystemLogger(String module, String systemId) {
        UserInfoStrategy externalStrategy = new ManualUserStrategy(systemId, "external", UserType.EXTERNAL_SYSTEM);
        return new DefaultSystemLogger(module, eventPublisher, objectMapper, externalStrategy, new EmptyContextStrategy());
    }
}