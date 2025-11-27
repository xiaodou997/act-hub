package com.xiaodou.log.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xiaodou.log.api.LogBuilder;
import com.xiaodou.log.api.SystemLogger;
import com.xiaodou.log.api.TimeRecorder;
import com.xiaodou.log.model.LogContext;
import com.xiaodou.log.model.LogLevel;
import com.xiaodou.log.model.LogUser;
import com.xiaodou.log.model.UserType;
import com.xiaodou.log.strategy.context.LogContextStrategy;
import com.xiaodou.log.strategy.userinfo.UserInfoStrategy;
import com.xiaodou.model.SystemLog;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.context.ApplicationEventPublisher;

import java.util.HashMap;
import java.util.Map;

/**
 * 系统日志记录器核心实现 ({@link com.xiaodou.log.api.SystemLogger} 的默认实现)。
 * <p>
 * 该类是日志模块的“心脏”，它不直接执行日志的持久化操作，而是采用了一种更现代、高效的设计模式：
 * <ol>
 *     <li><b>策略模式 (Strategy Pattern)</b>: 通过组合 {@link UserInfoStrategy} 和 {@link LogContextStrategy}
 *     接口，将“如何获取用户信息”和“如何获取环境上下文”的逻辑从核心记录器中解耦，实现了高度的灵活性和可扩展性。</li>
 *     <li><b>事件驱动 (Event-Driven)</b>: 当一条日志被构建完成后，本类会将其封装成一个 {@link SystemLogEvent}
 *     并发布到 Spring 的应用上下文中。这种异步化设计将日志记录的耗时操作从主业务线程中剥离，避免了对业务性能的影响。</li>
 * </ol>
 * <p>
 * 开发者通常不直接与该类交互，而是通过 {@link com.xiaodou.log.api.SystemLogFactory} 获取 {@link com.xiaodou.log.api.SystemLogger} 接口的实例。
 *
 * @author xiaodou V>dddou117
 * @see com.xiaodou.log.api.SystemLogFactory
 * @see SystemLogEvent
 * @see com.xiaodou.log.core.SystemLogEventListener
 * @since 1.2 (Refactored to be event-driven)
 */
@Slf4j
public record DefaultSystemLogger(String module, ApplicationEventPublisher eventPublisher, ObjectMapper objectMapper,
                                  UserInfoStrategy userInfoStrategy, LogContextStrategy logContextStrategy)
    implements SystemLogger {
    // ========== 快速日志方法实现 ==========

    @Override
    public void info(String description) {
        builder().action(module)
            .description(description)
            .info();
    }

    @Override
    public void info(String action, String description) {
        builder().action(action)
            .description(description)
            .info();
    }

    @Override
    public void warn(String description) {
        builder().action(module)
            .description(description)
            .warn();
    }

    @Override
    public void warn(String action, String description) {
        builder().action(action)
            .description(description)
            .warn();
    }

    @Override
    public void error(String description, Throwable throwable) {
        builder().action(module)
            .description(description)
            .error(throwable);
    }

    @Override
    public void error(String action, String description, Throwable throwable) {
        builder().action(action)
            .description(description)
            .error(throwable);
    }

    @Override
    public void audit(String action, String description) {
        builder().action(action)
            .description(description)
            .audit();
    }

    // ========== 构建器模式实现 ==========

    @Override
    public LogBuilder builder() {
        return new DefaultLogBuilder();
    }

    @Override
    public TimeRecorder startTimer() {
        long startTime = System.currentTimeMillis();
        return new DefaultTimeRecorder(startTime);
    }

    // ========== 内部构建器实现 ==========

    private class DefaultLogBuilder implements LogBuilder {
        private String action;
        private String description = "";
        private String targetType;
        private String targetId;
        private LogLevel level = LogLevel.INFO;
        private LogUser user;
        private LogContext context;
        private final Map<String, Object> details = new HashMap<>();
        private final Map<String, Object> tags = new HashMap<>();
        private Long costTimeMs;
        private Throwable throwable;

        @Override
        public LogBuilder action(String action) {
            this.action = action;
            return this;
        }

        @Override
        public LogBuilder description(String description) {
            this.description = description;
            return this;
        }

        @Override
        public LogBuilder target(String targetType, String targetId) {
            this.targetType = targetType;
            this.targetId = targetId;
            return this;
        }

        @Override
        public LogBuilder level(LogLevel level) {
            this.level = level;
            return this;
        }

        @Override
        public LogBuilder user(String userId, String tenantId, UserType userType) {
            this.user = new LogUser(userId, tenantId, userType);
            return this;
        }

        @Override
        public LogBuilder user(String userId, String tenantId) {
            return user(userId, tenantId, UserType.SYSTEM);
        }

        @Override
        public LogBuilder user(String userId) {
            return user(userId, null, UserType.SYSTEM);
        }

        @Override
        public LogBuilder context(LogContext context) {
            this.context = context;
            return this;
        }

        @Override
        public LogBuilder detail(String key, Object value) {
            this.details.put(key, value);
            return this;
        }

        @Override
        public LogBuilder details(Map<String, Object> details) {
            this.details.putAll(details);
            return this;
        }

        @Override
        public LogBuilder tag(String key, Object value) {
            this.tags.put(key, value);
            return this;
        }

        @Override
        public LogBuilder costTime(long costTimeMs) {
            this.costTimeMs = costTimeMs;
            return this;
        }

        @Override
        public void info() {
            publishLog(true, null);
        }

        @Override
        public void warn() {
            this.level = LogLevel.WARN;
            publishLog(true, null);
        }

        @Override
        public void error(Throwable throwable) {
            this.level = LogLevel.ERROR;
            this.throwable = throwable;
            publishLog(false, throwable);
        }

        @Override
        public void audit() {
            this.level = LogLevel.AUDIT;
            publishLog(true, null);
        }

        private void publishLog(boolean success, Throwable throwable) {
            try {
                SystemLog logEntity = buildSystemLog(success, throwable);
                eventPublisher.publishEvent(new SystemLogEvent(this, logEntity));
            } catch (Exception e) {
                log.warn("发布日志事件失败 - 模块: {}, 操作: {}, 错误: {}", module, action, e.getMessage());
                fallbackLogSave(e);
            }
        }

        private SystemLog buildSystemLog(boolean success, Throwable throwable) {
            LogContext resolvedContext = context != null ? context : logContextStrategy.resolveContext();
            LogUser resolvedUser = user != null ? user : userInfoStrategy.resolveUser();

            // 构建详情信息
            Map<String, Object> detailMap = buildDetailMap(resolvedContext);
            String detailJson = serializeToJson(detailMap);

            // 构建标签信息
            Map<String, Object> tagMap = buildTagMap();
            String tagsJson = serializeToJson(tagMap);

            return SystemLog.builder()
                .module(module)
                .action(action != null ? action : module)
                .description(description)
                .targetType(targetType)
                .targetId(targetId)
                .logLevel(level.getValue())
                .operatorUserId(resolvedUser.userId())
                .tenantId(resolvedUser.tenantId())
                .operatorUserType(resolvedUser.userType()
                    .getValue())
                .traceId(MDC.get("traceId")) // 从MDC获取，与Micrometer保持一致
                .spanId(MDC.get("spanId"))   // 从MDC获取，与Micrometer保持一致
                .detail(detailJson)
                .tags(tagsJson)
                .success(success ? 1 : 0)
                .errorCode(extractErrorCode(throwable))
                .errorMessage(extractErrorMessage(throwable))
                .stackTrace(extractStackTrace(throwable))
                .costTimeMs(costTimeMs)
                .ipAddress(resolvedContext.ipAddress())
                .userAgent(resolvedContext.userAgent())
                .build();
        }

        private Map<String, Object> buildDetailMap(LogContext context) {

            // 添加自定义详情
            Map<String, Object> detailMap = new HashMap<>(details);

            // 添加异常信息
            if (throwable != null) {
                detailMap.put("exception", throwable.getMessage());
                detailMap.put("exceptionType", throwable.getClass()
                    .getName());
                detailMap.putIfAbsent("stackTrace", extractStackTrace(throwable));
            }

            // 添加上下文信息到标签
            if (context.headers() != null && !context.headers()
                .isEmpty()) {
                detailMap.put("headers", context.headers());
            }

            return detailMap;
        }

        private Map<String, Object> buildTagMap() {
            Map<String, Object> tagMap = new HashMap<>(tags);
            tagMap.putIfAbsent("logLevel", level.getValue());
            tagMap.putIfAbsent("module", module);
            if (targetType != null) {
                tagMap.putIfAbsent("targetType", targetType);
                tagMap.putIfAbsent("targetId", targetId);
            }
            return tagMap;
        }

        private String serializeToJson(Map<String, Object> map) {
            if (map.isEmpty()) {
                return null;
            }
            try {
                return objectMapper.writeValueAsString(map);
            } catch (Exception e) {
                log.warn("JSON序列化失败: {}", e.getMessage());
                return "{\"serializeError\":\"JSON序列化失败\"}";
            }
        }

        private String extractErrorCode(Throwable throwable) {
            if (throwable == null)
                return null;
            // 这里可以根据实际业务定义错误码提取逻辑
            return throwable.getClass()
                .getSimpleName();
        }

        private String extractErrorMessage(Throwable throwable) {
            if (throwable == null)
                return null;
            return throwable.getMessage();
        }

        private String extractStackTrace(Throwable throwable) {
            if (throwable == null)
                return null;

            StringBuilder sb = new StringBuilder();
            StackTraceElement[] stackTrace = throwable.getStackTrace();
            for (int i = 0; i < Math.min(stackTrace.length, 10); i++) {
                sb.append(stackTrace[i].toString())
                    .append("\n");
            }
            return sb.toString();
        }

        private void fallbackLogSave(Exception publishException) {
            try {
                log.error("日志事件发布失败，执行降级策略 - 模块: {}, 操作: {}", module, action);
                // 这里可以添加同步保存逻辑
            } catch (Exception e) {
                log.error("日志降级保存也失败", e);
            }
        }
    }

    // ========== 内部时间记录器实现 ==========

    private class DefaultTimeRecorder implements TimeRecorder {
        private final long startTime;

        public DefaultTimeRecorder(long startTime) {
            this.startTime = startTime;
        }

        @Override
        public void record(String action, String description) {
            long costTime = System.currentTimeMillis() - startTime;
            builder().action(action)
                .description(description)
                .costTime(costTime)
                .info();
        }

        @Override
        public void record(String description) {
            record(module, description);
        }

        @Override
        public void recordError(String action, String description, Throwable throwable) {
            long costTime = System.currentTimeMillis() - startTime;
            builder().action(action)
                .description(description)
                .costTime(costTime)
                .error(throwable);
        }

        @Override
        public void recordError(String description, Throwable throwable) {
            recordError(module, description, throwable);
        }
    }
}
