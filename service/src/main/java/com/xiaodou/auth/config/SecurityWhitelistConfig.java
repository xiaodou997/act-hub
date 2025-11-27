package com.xiaodou.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

/**
 *
 * @author xiaodou V=>dddou117
 * @version V1.0
 * @since 2025/9/8
 */
@Configuration
public class SecurityWhitelistConfig {

    @Bean("publicEndpoints")
    public RequestMatcher publicEndpoints() {
        return new OrRequestMatcher(
            // CORS预检请求 - 所有路径的OPTIONS请求
            new AntPathRequestMatcher("/**", "OPTIONS"),

            // 认证接口
            new AntPathRequestMatcher("/admin/auth/login"), // 登录账号
            new AntPathRequestMatcher("/admin/auth/refresh"), // 登录账号

            // 插件的接口
            new AntPathRequestMatcher("/api/plugin/**"),

            // 文档接口
            new AntPathRequestMatcher("/v3/api-docs/**"), new AntPathRequestMatcher("/swagger-ui.html"),
            new AntPathRequestMatcher("/swagger-ui/**"), new AntPathRequestMatcher("/webjars/**"));

    }
}
