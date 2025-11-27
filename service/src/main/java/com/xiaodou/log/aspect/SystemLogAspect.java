package com.xiaodou.log.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xiaodou.log.annotation.InjectLogger;
import com.xiaodou.log.api.LogBuilder;
import com.xiaodou.log.api.SystemLogFactory;
import com.xiaodou.log.api.SystemLogger;
import com.xiaodou.log.annotation.SystemLog;
import com.xiaodou.log.model.LogLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * 系统日志切面 (System Log Aspect)。
 * <p>
 * 这是一个基于 Spring AOP 的切面，用于实现声明式的审计日志功能。
 * 它的核心职责是拦截所有标记了 {@link SystemLog} 注解的方法，
 * 并在方法执行前后自动收集信息，最终完成日志的记录。
 * </p>
 * <p><b>核心功能：</b></p>
 * <ul>
 *     <li><b>自动拦截:</b> 通过 {@link Pointcut} 精准定位到需要记录日志的目标方法。</li>
 *     <li><b>信息收集:</b> 在方法执行期间，自动收集模块名、操作名、请求参数、返回结果、执行耗时及异常信息。</li>
 *     <li><b>SpEL支持:</b> 支持使用 Spring Expression Language (SpEL) 动态地从方法参数或返回值中提取业务ID。</li>
 *     <li><b>异步记录:</b> 最终调用 {@link SystemLogger} 的能力，将日志记录委托给事件驱动的异步处理机制，对业务线程无阻塞。</li>
 * </ul>
 *
 * @author xiaodou V=>dddou117
 * @see SystemLog
 * @see com.xiaodou.log.api.SystemLogFactory
 * @since 2025/7/1
 */
@Slf4j // Lombok注解，自动生成日志对象
@Aspect // 标识这是一个切面类
@Component // 将该类注册为Spring组件
@RequiredArgsConstructor // Lombok注解，自动生成包含所有final字段的构造函数
public class SystemLogAspect {
    private final SystemLogFactory systemLogFactory;
    private final ObjectMapper objectMapper;
    private final ExpressionParser expressionParser = new SpelExpressionParser();

    /**
     * 定义切入点 (Pointcut)。
     * <p>
     * 该切入点匹配所有被 {@link com.xiaodou.log.annotation.SystemLog} 注解标记的方法。
     * AOP 框架将基于此切入点来应用下面的环绕通知。
     * </p>
     */
    @Pointcut("@annotation(com.xiaodou.log.annotation.SystemLog)")
    public void logPointcut() {
        // 切入点方法体通常为空，它仅作为标识符存在。
    }

    /**
     * 环绕通知 (Around Advice)。
     * <p>
     * 这是切面的核心逻辑所在。它“环绕”在目标方法的执行周围，
     * 允许我们在方法执行前、执行后、或抛出异常时执行自定义逻辑。
     * </p>
     *
     * @param joinPoint 连接点，代表了被拦截的方法。通过它可以获取方法签名、参数等信息，并能调用 {@code joinPoint.proceed()} 来执行原始方法。
     * @return 目标方法的原始返回值。
     * @throws Throwable 如果目标方法抛出异常，此通知会捕获并重新抛出，以确保不破坏原有的业务异常处理流程。
     */
    @Around("logPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取注解信息
        MethodSignature signature = (MethodSignature)joinPoint.getSignature();
        Method method = signature.getMethod();
        SystemLog systemLog = method.getAnnotation(SystemLog.class);

        if (systemLog == null) {
            return joinPoint.proceed();
        }

        // 🔥 解析最终模块名：方法 > 类 > 报错
        String actualModule = resolveModule(joinPoint, systemLog.module());
        // 获取日志记录器
        SystemLogger logger = systemLogFactory.getLogger(actualModule);

        // 记录方法开始执行的时间
        long startTime = System.currentTimeMillis();
        Object result = null;
        Throwable exception = null;

        try {
            // 执行目标方法
            result = joinPoint.proceed();
            return result;
        } catch (Exception e) {
            // 捕获目标方法抛出的异常
            exception = e;
            // 重新抛出异常，不影响原有业务逻辑
            throw e;
        } finally {
            // 无论方法执行成功还是失败，都记录日志
            recordLog(joinPoint, systemLog, logger, result, exception, startTime);
        }
    }

    private String resolveModule(ProceedingJoinPoint joinPoint, String methodModule) {
        // 1. 方法显式指定
        if (methodModule != null && !methodModule.trim()
            .isEmpty()) {
            return methodModule;
        }

        // 2. 从目标类获取（处理代理）
        Class<?> targetClass = AopProxyUtils.ultimateTargetClass(joinPoint.getTarget());
        InjectLogger classAnn = targetClass.getAnnotation(InjectLogger.class);
        if (classAnn != null && !classAnn.value()
            .trim()
            .isEmpty()) {
            return classAnn.value();
        }

        // 3. 无法确定 → 抛出明确异常（避免日志混乱）
        throw new IllegalStateException(
            "日志模块名未指定！请在 @SystemLog(module=...) 或类上添加 @InjectLogger(\"模块名\")");
    }

    /**
     * 保存日志信息到数据库
     *
     * @param joinPoint 连接点，包含目标方法的信息
     * @param result 目标方法的执行结果
     */
    private void recordLog(ProceedingJoinPoint joinPoint, SystemLog systemLog, SystemLogger logger, Object result,
        Throwable exception, long startTime) {
        try {
            // 使用构建器模式
            LogBuilder builder = logger.builder()
                .action(systemLog.action())
                .description(systemLog.description())
                .level(systemLog.level());

            // 设置目标对象
            setTargetInfo(joinPoint, systemLog, builder, result);

            // 设置标签和详情信息
            setLogDetails(joinPoint, systemLog, builder, result, exception, startTime);

            // 根据执行结果记录日志
            if (exception != null) {
                builder.error(exception);
            } else {
                if (systemLog.level() == LogLevel.AUDIT) {
                    builder.audit();
                } else if (systemLog.level() == LogLevel.WARN) {
                    builder.warn();
                } else {
                    builder.info();
                }
            }
        } catch (Exception e) {
            log.warn("记录系统日志失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 设置目标对象信息
     */
    private void setTargetInfo(ProceedingJoinPoint joinPoint, SystemLog systemLog, LogBuilder builder, Object result) {
        String targetType = systemLog.targetType();
        if (targetType == null || targetType.isEmpty()) {
            log.debug("target type is empty");
            return;
        }

        String targetId = resolveTargetId(joinPoint, systemLog.targetId(), result);

        if (targetId != null && !targetId.isEmpty()) {
            builder.target(targetType, targetId);
        }

    }

    /**
     * 设置日志详情信息
     */
    private void setLogDetails(ProceedingJoinPoint joinPoint, SystemLog systemLog, LogBuilder builder, Object result,
        Throwable exception, long startTime) {

        // 记录方法信息
        builder.detail("method", joinPoint.getSignature()
            .toShortString());
        builder.detail("class", joinPoint.getTarget()
            .getClass()
            .getName());

        // 记录请求参数
        if (systemLog.recordRequest()) {
            builder.detail("params", getMethodParams(joinPoint));
        }

        // 记录响应结果
        if (systemLog.recordResponse() && result != null) {
            builder.detail("response", serializeObject(result));
        }

        // 记录异常信息
        if (exception != null) {
            builder.detail("exception", exception.getMessage());
            builder.detail("exceptionType", exception.getClass()
                .getName());
        }

        // 记录耗时
        if (systemLog.recordCost()) {
            long costTime = System.currentTimeMillis() - startTime;
            builder.costTime(costTime);
            builder.detail("durationMs", costTime);
        }
    }

    /**
     * 解析目标对象ID（支持SpEL表达式）
     */
    private String resolveTargetId(ProceedingJoinPoint joinPoint, String targetIdExpression, Object result) {
        if (targetIdExpression == null || targetIdExpression.isEmpty()) {
            return null;
        }

        try {
            // 创建评估上下文
            StandardEvaluationContext context = new StandardEvaluationContext();

            // 设置方法参数
            MethodSignature signature = (MethodSignature)joinPoint.getSignature();
            String[] paramNames = signature.getParameterNames();
            Object[] paramValues = joinPoint.getArgs();

            for (int i = 0; i < paramNames.length; i++) {
                context.setVariable(paramNames[i], paramValues[i]);
            }

            // 设置方法返回值（如果有）
            context.setVariable("result", result);

            // 解析表达式
            Expression expression = expressionParser.parseExpression(targetIdExpression);
            Object value = expression.getValue(context);

            return value != null ? value.toString() : null;
        } catch (Exception e) {
            log.debug("解析目标对象ID表达式失败: {}", e.getMessage());
            return null;
        } catch (Throwable e) {
            log.error(e.getMessage());
            return null;
        }
    }

    /**
     * 获取方法参数信息
     */
    private Map<String, Object> getMethodParams(ProceedingJoinPoint joinPoint) {
        String[] paramNames = ((MethodSignature)joinPoint.getSignature()).getParameterNames();
        Object[] paramValues = joinPoint.getArgs();
        Map<String, Object> params = new HashMap<>();

        for (int i = 0; i < paramNames.length; i++) {
            // 过滤敏感参数（可根据需要扩展）
            if (isSensitiveParam(paramNames[i])) {
                params.put(paramNames[i], "***");
            } else {
                params.put(paramNames[i], serializeParam(paramValues[i]));
            }
        }
        return params;
    }

    /**
     * 序列化参数值
     */
    private Object serializeParam(Object param) {
        try {
            // 对于简单类型直接返回
            if (param instanceof String || param instanceof Number || param instanceof Boolean || param == null) {
                return param;
            }

            // 对于复杂对象，限制序列化深度
            return objectMapper.convertValue(param, Map.class);
        } catch (Exception e) {
            return param.toString();
        }
    }

    /**
     * 序列化对象
     */
    private Object serializeObject(Object obj) {
        try {
            if (obj instanceof String || obj instanceof Number || obj instanceof Boolean || obj == null) {
                return obj;
            }
            return objectMapper.convertValue(obj, Map.class);
        } catch (Exception e) {
            log.debug("对象序列化失败: {}", e.getMessage());
            return obj.toString();
        }
    }

    /**
     * 判断是否为敏感参数
     */
    private boolean isSensitiveParam(String paramName) {
        String lowerParamName = paramName.toLowerCase();
        return lowerParamName.contains("password") || lowerParamName.contains("token") || lowerParamName.contains(
            "secret") || lowerParamName.contains("key");
    }
}
