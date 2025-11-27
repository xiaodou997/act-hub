package com.xiaodou.auth.filter;

import com.xiaodou.auth.strategy.AuthenticationContext;
import com.xiaodou.auth.strategy.AuthenticationStrategy;
import com.xiaodou.auth.strategy.AuthenticationStrategyFactory;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 认证过滤器 - 基于策略模式的现代化实现
 * <p>
 * 使用策略模式支持多种认证方式：
 * - JWT Token 认证
 * - API Key 认证
 * - 可扩展支持更多认证类型
 * </p>
 *
 * @author xiaodou V=>dddou117
 * @version V2.0
 * @since 2025/5/15
 */
@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final RequestMatcher publicEndpoints;
    private final AuthenticationStrategyFactory strategyFactory;

    /**
     * 构造方法
     */
    @Autowired
    public JwtAuthenticationFilter(@Qualifier("publicEndpoints") RequestMatcher publicEndpoints,
        AuthenticationStrategyFactory strategyFactory) {
        this.publicEndpoints = publicEndpoints;
        this.strategyFactory = strategyFactory;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        // OPTIONS请求直接跳过
        // if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
        //     return true;
        // }

        boolean shouldSkip = publicEndpoints.matches(request);
        if (shouldSkip) {
            log.debug("⏭️ JWT过滤器跳过: [{}] {}", request.getMethod(), request.getRequestURI());
        }
        return shouldSkip; // 返回true表示完全跳过这个过滤器
    }

    /**
     * 过滤器核心方法
     * <p>
     * 处理流程：
     * 2. 从请求头获取Token和客户端类型
     * 3. 验证Token格式和有效性
     * 4. 检查客户端类型一致性
     * 5. 验证Redis中的Token状态
     * 6. 构建认证对象并设置安全上下文
     * </p>
     *
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @param filterChain 过滤器链
     * @throws ServletException 当Servlet处理发生错误时抛出
     * @throws IOException 当I/O操作发生错误时抛出
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        var requestInfo = RequestInfo.of(request);
        log.debug("➡️ JWT过滤器 -> [{}] {}", requestInfo.method(), requestInfo.uri());

        try {
            // 构建认证上下文
            var authContext = AuthenticationContext.from(request);

            // 获取认证策略并执行认证
            AuthenticationStrategy strategy = strategyFactory.getStrategy(authContext);
            var loginUser = strategy.authenticate(authContext);

            // 设置安全上下文
            strategy.setSecurityContext(request, loginUser);

            log.debug("✅ [{}] {} 认证通过, 用户: {}, 类型: {}", requestInfo.method(), requestInfo.uri(),
                loginUser.getDisplayName(), loginUser.getClientType());

        } catch (AuthenticationCredentialsNotFoundException e) {
            // 清除安全上下文并重新抛出异常
            SecurityContextHolder.clearContext();
            log.warn("🚫 [{}] {} 认证失败: {}", requestInfo.method(), requestInfo.uri(), e.getMessage());
            throw e;
        }

        // 继续过滤器链
        filterChain.doFilter(request, response);
    }

    /**
     * 请求信息记录 - 使用 JDK 21 的 Record
     */
    private record RequestInfo(String method, String uri) {
        static RequestInfo of(HttpServletRequest request) {
            return new RequestInfo(request.getMethod(), request.getRequestURI());
        }
    }
}