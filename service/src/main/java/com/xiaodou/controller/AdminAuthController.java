package com.xiaodou.controller;

import com.xiaodou.auth.util.UserContextHolder;
import com.xiaodou.log.api.SystemLogFactory;
import com.xiaodou.log.model.LogLevel;
import com.xiaodou.model.LoginUser;
import com.xiaodou.model.User;
import com.xiaodou.model.auth.LoginVO;
import com.xiaodou.model.auth.LoginRequest;
import com.xiaodou.model.auth.RefreshRequest;
import com.xiaodou.model.auth.UserInfoVO;
import com.xiaodou.result.Result;
import com.xiaodou.service.UserService;
import com.xiaodou.token.JwtHelper;
import com.xiaodou.token.TokenService;
import com.xiaodou.utils.IpUtil;
import com.xiaodou.utils.RoleUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.util.StringUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 认证控制器（V3 - 双Token机制版）
 */
@Slf4j
@RestController
@RequestMapping("/admin/auth")
@RequiredArgsConstructor
public class AdminAuthController {
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final SystemLogFactory logFactory;
    private final JwtHelper jwtHelper;
    private final TokenService tokenService;

    private static final String CLIENT_TYPE_HEADER = "X-Client-Type";

    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        long start = System.currentTimeMillis();
        String username = loginRequest.getUsername();
        String clientType = request.getHeader(CLIENT_TYPE_HEADER);

        if (!StringUtils.hasText(clientType)) {
            log.warn("登录失败：缺少 X-Client-Type 请求头，username={}", username);
            logFactory.getAnonymousLogger("认证模块").error("登录失败", "缺少 X-Client-Type 请求头，username:" + username, null);
            return Result.fail(400, "缺少完整的请求头信息");
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, loginRequest.getPassword()));

            LoginUser user = (LoginUser) authentication.getPrincipal();
            List<String> roles = user.getRoles();
            String userType;

            if (RoleUtils.isAdmin(roles)) {
                userType = "管理员";
            } else if (RoleUtils.isTenant(roles)) {
                userType = "租户";
            } else {
                log.warn("登录失败：用户 {} 角色不正确，当前角色: {}", username, roles);
                logFactory.getLogger("认证模块", user.getUserId(), user.getTenantId())
                    .error("登录失败", "用户角色不正确", null);
                return Result.fail(403, "无权登录该系统");
            }

            return processSuccessfulLogin(user, clientType, userType, start, request);

        } catch (BadCredentialsException e) {
            log.warn("登录失败：用户名或密码错误，username={}", username);
            logFactory.getAnonymousLogger("认证模块").error("登录失败", "用户名或密码错误，username:" + username, e);
            return Result.fail(400, "用户名或密码错误");
        } catch (DisabledException e) {
            log.warn("登录失败：账户被禁用，username={}", username);
            logFactory.getAnonymousLogger("认证模块").error("登录失败", "账户被禁用，username:" + username, e);
            return Result.fail(400, "账户被禁用，请联系管理员");
        } catch (Exception e) {
            log.error("登录发生未知错误，username={}", username, e);
            logFactory.getAnonymousLogger("认证模块").error("登录失败", "未知错误，username:" + username, e);
            return Result.fail(500, "登录失败");
        }
    }

    private Result<LoginVO> processSuccessfulLogin(LoginUser user, String clientType, String userType, long startTime, HttpServletRequest request) {
        String ip = IpUtil.getIpAddress(request);
        
        UserInfoVO userInfoVO = UserInfoVO.builder()
            .userId(user.getUserId())
            .username(user.getUsername())
            .tenantId(user.getTenantId())
            .roles(user.getRoles())
            .build();

        JwtHelper.JwtPayload payload = JwtHelper.JwtPayload.fromLoginUser(user, clientType);
        String accessToken = jwtHelper.createAccessToken(payload);
        String refreshToken = jwtHelper.createRefreshToken(payload);

        tokenService.saveRefreshToken(payload, refreshToken);

        User dbUser = new User();
        dbUser.setId(user.getUserId());
        dbUser.setLastLoginAt(LocalDateTime.now());
        userService.updateById(dbUser);

        logFactory.getLogger("认证模块", user.getUserId(), user.getTenantId()).builder()
            .action("用户登录")
            .description("登录成功")
            .level(LogLevel.INFO)
            .detail("username", user.getUsername())
            .detail("ip", ip)
            .detail("clientType", clientType)
            .detail("userType", userType)
            .costTime(System.currentTimeMillis() - startTime)
            .info();
        
        // userInfoVO.setTenantId(null);

        return Result.success(new LoginVO(accessToken, refreshToken, userInfoVO));
    }

    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        String tokenHeader = request.getHeader("Authorization");
        String clientType = request.getHeader(CLIENT_TYPE_HEADER);

        if (!StringUtils.hasText(clientType)) {
            return Result.fail(400, "缺少X-Client-Type请求头");
        }
        
        try {
            String accessToken = jwtHelper.removeBearerPrefix(tokenHeader);
            String userId = UserContextHolder.getUserId();

            if (userId != null) {
                // 1. 使 Refresh Token 失效
                tokenService.invalidateRefreshToken(userId, clientType);
                // 2. 将当前 Access Token 加入黑名单，实现立即下线
                tokenService.addToBlacklist(accessToken);
                log.info("用户 {} 成功登出，Client: {}", userId, clientType);
            }
            return Result.success();
        } catch (Exception e) {
            log.error("登出失败，Client: {}", clientType, e);
            return Result.fail(500, "登出失败");
        }
    }

    @PostMapping("/refresh")
    public Result<LoginVO> refresh(@RequestBody RefreshRequest refreshRequest) {
        String refreshToken = refreshRequest.getRefreshToken();

        JwtHelper.JwtPayload payload = tokenService.validateAndParseRefreshToken(refreshToken);
        if (payload == null) {
            return Result.fail(401, "Refresh Token 无效或已过期");
        }

        // 刷新 Token 时重新加载并缓存用户权限
        tokenService.reloadAndCacheUserPermissions(payload.getUserId());

        String newAccessToken = jwtHelper.createAccessToken(payload);
        String newRefreshToken = jwtHelper.createRefreshToken(payload);
        tokenService.saveRefreshToken(payload, newRefreshToken);

        return Result.success(new LoginVO(newAccessToken, newRefreshToken, null));
    }

    /**
     * 新增：强制用户下线接口（仅管理员可用）
     */
    @PostMapping("/kick/{userId}")
    @PreAuthorize("hasRole('ADMIN')") // 假设有ADMIN角色
    public Result<Void> kickUser(@PathVariable String userId) {
        // 使该用户在所有客户端的 Refresh Token 都失效
        // 注意：这里需要一种方式获取用户所有可能的 clientType，或者改造 invalidateRefreshToken 支持通配符
        // 为简化，我们先假设 clientType 是已知的，例如 "web", "app"
        // 在实际项目中，你可能需要一个更复杂的逻辑来处理多端登录
        List<String> clientTypes = List.of("web", "app", "miniapp"); // 示例
        for (String clientType : clientTypes) {
            tokenService.invalidateRefreshToken(userId, clientType);
        }
        log.info("管理员操作：强制用户 {} 下线。", userId);
        return Result.success();
    }
}