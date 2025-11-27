package com.xiaodou.log.aspect;

import com.xiaodou.log.annotation.InjectLogger;
import com.xiaodou.log.api.SystemLogFactory;
import com.xiaodou.log.api.SystemLogger;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Role;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;

/**
 * 系统日志 Bean 后置处理器，用于自动注入带有 {@link InjectLogger} 注解的类中的日志字段。
 * <p>
 * 该类通过 Spring 的 {@link BeanPostProcessor} 接口，在 Bean 初始化后检查是否标记了 {@link InjectLogger} 注解，
 * 并根据注解配置自动注入 {@link SystemLogger} 实例到指定字段中。
 *
 * @author xiaodou V=>dddou117
 * @version V1.0
 * @since 2025/10/9
 */
@Component
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
public class InjectLoggerBeanPostProcessor implements BeanPostProcessor {

    private final SystemLogFactory logFactory;

    public InjectLoggerBeanPostProcessor(SystemLogFactory logFactory) {
        this.logFactory = logFactory;
    }

    /**
     * 在 Bean 初始化后处理逻辑，检查并注入带有 {@link InjectLogger} 注解的日志字段。
     * <p>
     * 如果 Bean 类标记了 {@link InjectLogger} 注解，则根据注解配置查找并注入 {@link SystemLogger} 实例到指定字段中。
     * 如果字段类型不匹配或字段不存在，将抛出异常。
     *
     * @param bean 当前正在初始化的 Bean 实例
     * @param beanName Bean 的名称
     * @return 处理后的 Bean 实例
     * @throws IllegalStateException 如果字段类型不匹配或字段未声明
     * @throws RuntimeException 如果无法访问字段
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        Class<?> clazz = bean.getClass();
        InjectLogger ann = clazz.getAnnotation(InjectLogger.class);
        if (ann == null)
            return bean;

        String fieldName = ann.field(); // 如 "sysLogger"
        String moduleName = ann.value();

        try {
            Field field = clazz.getDeclaredField(fieldName);
            if (field.getType() != SystemLogger.class) {
                throw new IllegalStateException("字段 " + fieldName + " 类型必须是 SystemLogger");
            }

            field.setAccessible(true);
            // 可选：只在 null 时注入（避免覆盖）
            if (field.get(bean) == null) {
                field.set(bean, logFactory.getLogger(moduleName));
            }
        } catch (NoSuchFieldException e) {
            throw new IllegalStateException(
                "类 " + clazz.getSimpleName() + " 使用了 @InjectLogger，但未声明字段: " + fieldName);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("无法访问字段: " + fieldName, e);
        }

        return bean;
    }
}
