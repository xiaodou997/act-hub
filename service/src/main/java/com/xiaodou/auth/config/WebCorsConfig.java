package com.xiaodou.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * 跨域资源共享(CORS)配置类
 * <p>
 * 配置全局CORS策略，允许跨域请求访问后端API接口。
 * 默认配置为开发环境宽松策略，生产环境需调整安全设置。
 * </p>
 *
 * @author xiaodou V=>dddou117
 * @version V1.0
 * @since 2025/5/15
 */
@Configuration
public class WebCorsConfig {

    /**
     * 配置CORS策略源
     * <p>
     * 当前配置说明：
     * 1. 允许携带凭证（cookies等）
     * 2. 允许所有来源（开发环境方便调试）
     * 3. 允许所有请求头
     * 4. 允许所有HTTP方法
     * 安全建议：
     * 1. 生产环境应将addAllowedOriginPattern("*")替换为具体的前端域名
     * 2. 可根据业务需要限制允许的HTTP方法
     * 3. 可配置exposedHeaders暴露特定响应头
     * </p>
     *
     * @return CorsConfigurationSource 跨域配置源
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // 允许发送认证信息（如cookies）
        config.setAllowCredentials(true);
        // 允许所有来源（生产环境应替换为具体域名）
        config.addAllowedOriginPattern("*");
        // 允许所有请求头
        config.addAllowedHeader("*");
        // 允许所有HTTP方法（GET/POST/PUT/DELETE等）
        config.addAllowedMethod("*");

        // 注册CORS配置，应用到所有路径
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}