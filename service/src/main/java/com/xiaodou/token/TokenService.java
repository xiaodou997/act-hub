package com.xiaodou.token;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.xiaodou.mapper.PermissionMapper;
import com.xiaodou.model.LoginUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Token 服务，负责：
 * 1. Refresh Token 在 Redis 中的存储和校验。
 * 2. Access Token 黑名单的管理。
 * 3. 用户权限缓存管理。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenService {

    private final StringRedisTemplate redisTemplate;
    private final JwtHelper jwtHelper;
    private final JwtProperties jwtProperties;
    private final PermissionMapper permissionMapper;

    private static final String REFRESH_TOKEN_KEY_PREFIX = "refresh_tokens:";
    private static final String ACCESS_TOKEN_BLACKLIST_PREFIX = "blacklist:access_tokens:";
    private static final String USER_PERMISSIONS_PREFIX = "user:permissions:";

    /**
     * 将 Access Token 加入黑名单。
     * <p>
     * 用于主动使某个 Access Token 失效，例如用户修改密码或被强制下线后。
     * 黑名单中的 Token 会被设置一个与 Access Token 自身过期时间相同的 TTL。
     * </p>
     *
     * @param accessToken 要拉黑的 Access Token
     */
    public void addToBlacklist(String accessToken) {
        String actualToken = jwtHelper.removeBearerPrefix(accessToken);
        String key = ACCESS_TOKEN_BLACKLIST_PREFIX + actualToken;
        long duration = jwtProperties.getAccessTokenExpiration();
        redisTemplate.opsForValue().set(key, "1", duration, TimeUnit.MINUTES);
    }

    /**
     * 检查 Access Token 是否在黑名单中。
     *
     * @param accessToken 待检查的 Access Token
     * @return 如果在黑名单中，返回 {@code true}
     */
    public boolean isBlacklisted(String accessToken) {
        String actualToken = jwtHelper.removeBearerPrefix(accessToken);
        String key = ACCESS_TOKEN_BLACKLIST_PREFIX + actualToken;
        return redisTemplate.hasKey(key);
    }

    /**
     * 保存 Refresh Token 到 Redis。
     */
    public void saveRefreshToken(JwtHelper.JwtPayload payload, String refreshToken) {
        String key = buildRefreshTokenKey(payload.getUserId(), payload.getClientType());
        long duration = jwtProperties.getRefreshTokenExpiration();
        redisTemplate.opsForValue().set(key, refreshToken, duration, TimeUnit.DAYS);
    }

    /**
     * 使 Refresh Token 失效（用于登出）。
     */
    public void invalidateRefreshToken(String userId, String clientType) {
        String key = buildRefreshTokenKey(userId, clientType);
        redisTemplate.delete(key);
    }

    /**
     * 校验 Refresh Token 是否有效。
     */
    public JwtHelper.JwtPayload validateAndParseRefreshToken(String refreshToken) {
        // 1. 使用专用的 Refresh Token 校验器进行校验和解析
        DecodedJWT jwt = jwtHelper.verifyAndParseRefreshToken(refreshToken);
        if (jwt == null) {
            return null;
        }

        // 2. 从已解析的 JWT 中提取 LoginUser 信息（避免使用 Access Token 校验器重复校验）
        LoginUser user = jwtHelper.extractLoginUser(jwt);
        if (user == null || user.getUserId() == null || user.getClientType() == null) {
            return null;
        }

        // 3. 从 Redis 中获取
        String key = buildRefreshTokenKey(user.getUserId(), user.getClientType());
        String storedToken = redisTemplate.opsForValue().get(key);

        // 4. 对比
        if (refreshToken.equals(storedToken)) {
            return JwtHelper.JwtPayload.fromLoginUser(user, user.getClientType());
        }

        return null;
    }

    private String buildRefreshTokenKey(String userId, String clientType) {
        return REFRESH_TOKEN_KEY_PREFIX + userId + ":" + clientType;
    }

    // ==================== 用户权限缓存 ====================

    /**
     * 保存用户权限列表到 Redis
     * <p>
     * 权限缓存的过期时间与 Refresh Token 一致，
     * 确保在 Refresh Token 有效期内权限信息始终可用。
     * </p>
     *
     * @param userId      用户ID
     * @param permissions 权限code列表
     */
    public void saveUserPermissions(String userId, List<String> permissions) {
        String key = buildUserPermissionsKey(userId);
        long duration = jwtProperties.getRefreshTokenExpiration();
        // 使用逗号分隔存储权限列表
        String value = permissions != null ? String.join(",", permissions) : "";
        redisTemplate.opsForValue().set(key, value, duration, TimeUnit.DAYS);
    }

    /**
     * 从 Redis 获取用户权限列表
     *
     * @param userId 用户ID
     * @return 权限code列表，如果不存在返回空列表
     */
    public List<String> getUserPermissions(String userId) {
        String key = buildUserPermissionsKey(userId);
        String value = redisTemplate.opsForValue().get(key);
        if (value == null || value.isEmpty()) {
            return Collections.emptyList();
        }
        return List.of(value.split(","));
    }

    /**
     * 获取用户权限列表（带降级方案）
     * <p>
     * 优先从 Redis 缓存获取，如果缓存不存在（如过期），则从数据库重新加载并缓存。
     * 这种机制确保即使缓存过期，也能自动恢复权限数据。
     * </p>
     *
     * @param userId 用户ID
     * @return 权限code列表
     */
    public List<String> getUserPermissionsWithFallback(String userId) {
        // 1. 尝试从缓存获取
        List<String> permissions = getUserPermissions(userId);

        // 2. 如果缓存为空，从数据库重新加载
        if (permissions.isEmpty()) {
            log.info("用户 {} 权限缓存不存在，从数据库重新加载", userId);
            permissions = permissionMapper.selectPermissionCodesByUserId(userId);

            // 3. 重新缓存权限
            if (permissions != null && !permissions.isEmpty()) {
                saveUserPermissions(userId, permissions);
                log.debug("用户 {} 权限已重新缓存，共 {} 个权限", userId, permissions.size());
            } else {
                permissions = Collections.emptyList();
            }
        }

        return permissions;
    }

    /**
     * 重新加载并缓存用户权限
     * <p>
     * 用于刷新 Token 时主动更新权限缓存，确保权限数据最新。
     * </p>
     *
     * @param userId 用户ID
     */
    public void reloadAndCacheUserPermissions(String userId) {
        log.info("重新加载用户 {} 的权限", userId);
        List<String> permissions = permissionMapper.selectPermissionCodesByUserId(userId);
        saveUserPermissions(userId, permissions);
        log.debug("用户 {} 权限已更新，共 {} 个权限", userId, permissions.size());
    }

    /**
     * 删除用户权限缓存
     * <p>
     * 用于权限变更后主动清除缓存，使新权限生效。
     * 下次请求时会通过 getUserPermissionsWithFallback 自动重新加载。
     * </p>
     *
     * @param userId 用户ID
     */
    public void clearUserPermissions(String userId) {
        String key = buildUserPermissionsKey(userId);
        redisTemplate.delete(key);
        log.info("已清除用户 {} 的权限缓存", userId);
    }

    private String buildUserPermissionsKey(String userId) {
        return USER_PERMISSIONS_PREFIX + userId;
    }
}