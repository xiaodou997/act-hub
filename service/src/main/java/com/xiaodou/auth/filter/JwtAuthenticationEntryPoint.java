package com.xiaodou.auth.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xiaodou.result.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * JWT 认证入口点处理器
 * <p>
 * 当未认证用户尝试访问受保护资源时，该处理器将被触发，返回401 Unauthorized响应
 * </p>
 *
 * @ClassName: JwtAuthenticationEntryPoint
 * @Description: 处理Spring Security认证异常，返回标准JSON格式错误响应
 * @Author: xiaodou V=>dddou117
 * @Date: 2025/5/15
 * @Version: V1.0
 * @JDK: JDK21
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    /**
     * Jackson JSON对象映射器，用于将Java对象转换为JSON字符串
     */
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 处理认证异常
     * <p>
     * 设置响应状态为401(Unauthorized)，并返回标准错误信息JSON
     * </p>
     *
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @param authException 认证异常对象，包含认证失败的具体信息
     * @throws IOException 当响应输出流写入失败时抛出
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
        AuthenticationException authException) throws IOException {
        // 设置响应内容类型为JSON，字符编码为UTF-8
        response.setContentType("application/json;charset=UTF-8");
        // 设置HTTP状态码为401 Unauthorized
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        // 将错误信息转换为JSON并写入响应输出流
        response.getWriter()
            .write(objectMapper.writeValueAsString(Result.fail(401, "未认证，请先登录")));
    }
}

