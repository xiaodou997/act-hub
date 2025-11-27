package com.xiaodou.auth.strategy;

import com.xiaodou.auth.exception.AuthenticationException;
import com.xiaodou.model.LoginUser;
import com.xiaodou.token.JwtHelper;
import com.xiaodou.token.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * JWT Token 认证策略
 *
 * @author xiaodou V=>dddou117
 * @version 2.0 (Refactored to use JwtHelper)
 * @since 2025/9/8
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenAuthenticationStrategy implements AuthenticationStrategy {

    private final TokenService tokenService;
    private final JwtHelper jwtHelper;

    @Override
    public boolean supports(AuthenticationContext context) {
        // JWT策略作为默认策略，处理非API Key的所有请求
        return true;
    }

    @Override
    public LoginUser authenticate(AuthenticationContext context) {
        String requestUri = context.requestUri();
        String method = context.method();
        log.debug("🎫 执行 JWT Token 认证: {}", requestUri);

        // 验证JWT签名和有效期
        if (!jwtHelper.verify(context.actualToken())) {
            log.warn("❌ JWT令牌无效或已过期: {}", requestUri);
            throw new AuthenticationException("无效的身份凭证或已过期", method, requestUri);
        }

        // 增加黑名单校验
        if (tokenService.isBlacklisted(context.actualToken())) {
            log.warn("❌ JWT令牌已被拉黑: {}", requestUri);
            throw new AuthenticationException("令牌已失效", method, requestUri);
        }

        // 解析用户信息
        LoginUser loginUser = jwtHelper.getLoginUser(context.actualToken());
        if (loginUser == null) {
            log.error("❌ 无法从有效令牌中解析出用户信息: {}", requestUri);
            throw new AuthenticationException("无效的用户信息", method, requestUri);
        }

        // 从 Redis 缓存获取用户权限列表，如果缓存不存在则自动从数据库重新加载
        List<String> permissionList = tokenService.getUserPermissionsWithFallback(loginUser.getUserId());
        loginUser.setPermissionList(permissionList);
        log.debug("用户 {} 权限列表: {}", loginUser.getUsername(), permissionList);

        // 验证客户端类型
        validateClientType(context, loginUser);

        return loginUser;
    }

    @Override
    public void setSecurityContext(HttpServletRequest request, LoginUser loginUser) {
        var authentication = new UsernamePasswordAuthenticationToken(loginUser, null, loginUser.getAuthorities());
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext()
            .setAuthentication(authentication);
    }

    @Override
    public String getStrategyType() {
        return "JWT_TOKEN";
    }

    private void validateClientType(AuthenticationContext context, LoginUser loginUser) {
        if (!context.clientType()
            .equalsIgnoreCase(loginUser.getClientType())) {
            log.warn("❌ 客户端类型不匹配 [{}] {} - 令牌中[{}], 请求头中[{}]", context.method(), context.requestUri(),
                loginUser.getClientType(), context.clientType());
            throw new AuthenticationException("客户端类型不匹配", context.method(), context.requestUri());
        }
    }
}
