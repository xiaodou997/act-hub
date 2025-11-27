package com.xiaodou.log.api;

import com.xiaodou.log.model.LogContext;
import com.xiaodou.log.model.LogLevel;
import com.xiaodou.log.model.UserType;

import java.util.Map;

/**
 * 日志构建器接口
 *
 * @author xiaodou V=>dddou117
 * @version V1.0
 * @since 2025/10/9
 */
public interface LogBuilder {

    LogBuilder action(String action);

    LogBuilder description(String description);

    LogBuilder target(String targetType, String targetId);

    LogBuilder level(LogLevel level);

    LogBuilder user(String userId, String tenantId, UserType userType);

    LogBuilder user(String userId, String tenantId);

    LogBuilder user(String userId);

    LogBuilder context(LogContext context);

    LogBuilder detail(String key, Object value);

    LogBuilder details(Map<String, Object> details);

    LogBuilder tag(String key, Object value);

    LogBuilder costTime(long costTimeMs);

    // ========== 执行方法 ==========

    void info();

    void warn();

    void error(Throwable throwable);

    void audit();
}
