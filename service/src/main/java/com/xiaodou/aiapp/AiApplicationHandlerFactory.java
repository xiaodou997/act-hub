package com.xiaodou.aiapp;

import com.xiaodou.aiapp.handler.AiApplicationHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * 工厂类 —— 从 Spring 获取 Handler Bean
 *
 * @author xiaodou V=>dddou117
 * @version V1.0
 * @since 2025/10/29
 */
@RequiredArgsConstructor
@Component
public class AiApplicationHandlerFactory {

    private final ApplicationContext applicationContext;

    /**
     * 根据 handlerBean 名称获取对应的 AiApplicationHandler 实例
     */
    public AiApplicationHandler getHandler(String handlerBeanName) {
        if (handlerBeanName == null || handlerBeanName.trim()
            .isEmpty()) {
            throw new IllegalArgumentException("handlerBeanName 不能为空");
        }
        try {
            return applicationContext.getBean(handlerBeanName, AiApplicationHandler.class);
        } catch (Exception e) {
            throw new RuntimeException("无法找到名为 '" + handlerBeanName + "' 的 AiApplicationHandler Bean", e);
        }
    }
}
