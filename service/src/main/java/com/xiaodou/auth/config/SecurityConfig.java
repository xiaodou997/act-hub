package com.xiaodou.auth.config;

import com.xiaodou.auth.handler.JwtAccessDeniedHandler;
import com.xiaodou.auth.filter.JwtAuthenticationEntryPoint;
import com.xiaodou.auth.filter.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;

import static org.springframework.security.config.Customizer.withDefaults;

/**
 * Spring Security 核心配置类
 * <p>
 * 配置应用安全策略，包括：
 * 1. JWT认证过滤器配置
 * 2. 权限控制规则
 * 3. 异常处理机制
 * 4. 静态资源放行规则
 * </p>
 *
 * @author xiaodou
 * @version V1.1
 * @since 2025/5/15
 */
@Configuration
@EnableWebSecurity // 启用Spring Security
@EnableMethodSecurity // 启用方法级安全控制
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final RequestMatcher publicEndpoints;

    /**
     * 构造方法注入依赖组件
     *
     * @param jwtAuthenticationFilter JWT认证过滤器
     * @param jwtAccessDeniedHandler 权限拒绝处理器
     * @param jwtAuthenticationEntryPoint 认证入口点
     */
    @Autowired
    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
        JwtAccessDeniedHandler jwtAccessDeniedHandler, JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
        @Qualifier("publicEndpoints") RequestMatcher publicEndpoints) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.jwtAccessDeniedHandler = jwtAccessDeniedHandler;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
        this.publicEndpoints = publicEndpoints;
    }

    /**
     * 配置认证管理器
     *
     * @param authConfig 认证配置
     * @return AuthenticationManager 认证管理器实例
     * @throws Exception 配置异常
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * 安全过滤器链配置
     * <p>
     * 主要配置：
     * 1. 启用CORS支持
     * 2. 禁用CSRF防护（因使用JWT）
     * 3. 配置无状态会话管理
     * 4. 设置认证和授权异常处理器
     * 5. 配置路径访问权限规则
     * 6. 添加JWT认证过滤器
     * </p>
     *
     * @param http HttpSecurity配置器
     * @return SecurityFilterChain 安全过滤器链
     * @throws Exception 配置异常
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 启用CORS支持（使用默认配置）
            .cors(withDefaults())
            // 禁用CSRF防护（因使用无状态JWT认证）
            .csrf(AbstractHttpConfigurer::disable)
            // 配置无状态会话（不使用Session）
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // 配置异常处理
            .exceptionHandling(exception -> exception
                // 认证失败处理
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                // 权限不足处理
                .accessDeniedHandler(jwtAccessDeniedHandler))
            // 配置请求授权规则
            .authorizeHttpRequests(auth -> auth

                // 使用统一的白名单配置（已包含OPTIONS请求）
                .requestMatchers(publicEndpoints)
                .permitAll()

                // 其他所有请求需要认证
                .anyRequest()
                .authenticated())
            // 添加JWT认证过滤器
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            // 禁用缓存控制
            .headers(headers -> headers.cacheControl(HeadersConfigurer.CacheControlConfig::disable));

        return http.build();
    }
}