package com.xiaodou.aiapp.handler;

import com.xiaodou.model.AiApplication;

import java.util.Map;

/**
 * AI 应用执行器接口
 * 所有具体的 AI 应用逻辑必须实现此接口，并注册为 Spring Bean。
 * 系统通过 {@link AiApplication#getHandlerBean()} 动态获取对应 Bean 并执行。
 *
 * @author xiaodou V=>dddou117
 * @version V1.0
 * @since 2025/10/28
 */
public interface AiApplicationHandler {

    /**
     * 执行 AI 应用的核心逻辑
     *
     * @param input 用户传入的参数，已通过 {@link AiApplication#getParamSchema()} 校验，
     *     结构为 Map<String, Object>，支持嵌套 JSON。
     * @return 执行结果，必须符合 {@link AiApplication#getResultSchema()} 的结构，
     *     通常为 Map<String, Object> 或简单值（如 String、Number）。
     * @throws Exception 执行过程中抛出的任何异常将被全局捕获并返回错误响应
     */
    Object execute(Map<String, Object> input) throws Exception;
}
