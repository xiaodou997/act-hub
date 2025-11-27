package com.xiaodou.result;

import org.slf4j.MDC;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * 全局响应处理器，用于在返回的 Result 对象中自动注入 traceId。
 * <p>
 * 该处理器通过实现 {@link ResponseBodyAdvice} 接口，在 Spring MVC 将响应体写入 HTTP 响应之前，
 * 对返回结果进行拦截和修改。
 * </p>
 * <p>
 * 核心逻辑：
 * <ol>
 *     <li>通过 {@code @RestControllerAdvice} 注解，将该逻辑应用到所有 {@code @RestController} 或带有 {@code @ResponseBody} 的方法。</li>
 *     <li>{@code supports} 方法判断返回类型是否为 {@link Result}，确保只处理我们自定义的全局返回对象。</li>
 *     <li>{@code beforeBodyWrite} 方法在写入前执行，从 SLF4J 的 MDC (Mapped Diagnostic Context) 中获取由
 *     Micrometer Tracing 自动放入的 traceId，并设置到 Result 对象中。</li>
 * </ol>
 * 这种方式实现了 traceId 注入的自动化和非侵入性，业务代码无需关心 traceId 的传递。
 *
 * @author xiaodou
 * @see Result
 */
@RestControllerAdvice(basePackages = "com.xiaodou.controller")
public class TraceIdResponseAdvice implements ResponseBodyAdvice<Result<?>> {

    /**
     * 判断是否需要对响应进行处理。
     *
     * @param returnType    Controller 方法的返回类型
     * @param converterType 选择的 HTTP 消息转换器
     * @return 如果返回类型是 Result 的子类或本身，则返回 true
     */
    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return Result.class.isAssignableFrom(returnType.getParameterType());
    }

    /**
     * 在响应体写入 HTTP 响应之前的处理逻辑。
     *
     * @param body                  原始的响应体对象 (Result 对象)
     * @param returnType            返回类型
     * @param selectedContentType   选择的内容类型
     * @param selectedConverterType 选择的转换器
     * @param request               当前请求
     * @param response              当前响应
     * @return 修改后的响应体对象
     */
    @Override
    public Result<?> beforeBodyWrite(Result<?> body, MethodParameter returnType, MediaType selectedContentType,
                                     Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                     ServerHttpRequest request, ServerHttpResponse response) {
        // 从 MDC 获取 traceId
        String traceId = MDC.get("traceId");
        if (body != null && traceId != null) {
            // 将 traceId 设置到 Result 对象中
            body.setTraceId(traceId);
        }
        return body;
    }
}
