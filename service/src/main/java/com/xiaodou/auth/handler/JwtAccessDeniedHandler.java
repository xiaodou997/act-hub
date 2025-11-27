package com.xiaodou.auth.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xiaodou.result.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * JWT 认证权限不足处理器
 * <p>
 * 当已认证用户访问受保护资源但权限不足时，该处理器将被触发，返回403 Forbidden响应
 * </p>
 *
 * @ClassName: JwtAccessDeniedHandler
 * @Description: 处理Spring Security权限不足异常，返回标准JSON格式错误响应
 * @Author: xiaodou V=>dddou117
 * @Date: 2025/5/15
 * @Version: V1.0
 * @JDK: JDK21
 */
@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    /**
     * Jackson JSON对象映射器，用于将Java对象转换为JSON字符串
     */
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 处理权限不足异常
     * <p>
     * 设置响应状态为403(Forbidden)，并返回标准错误信息JSON
     * </p>
     *
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @param accessDeniedException 权限不足异常对象
     * @throws IOException 当响应输出流写入失败时抛出
     */
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
        AccessDeniedException accessDeniedException) throws IOException {
        // 设置响应内容类型为JSON，字符编码为UTF-8
        response.setContentType("application/json;charset=UTF-8");
        // 设置HTTP状态码为403 Forbidden
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        // 将错误信息转换为JSON并写入响应输出流
        response.getWriter()
            .write(objectMapper.writeValueAsString(Result.fail(403, "权限不足，禁止访问")));
    }
}