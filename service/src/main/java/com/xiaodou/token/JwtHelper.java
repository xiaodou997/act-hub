package com.xiaodou.token;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.xiaodou.model.LoginUser;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * JWT 工具类，用于处理 JWT Token 的创建、验证和解析
 * 支持 Access Token 和 Refresh Token 两种类型
 *
 * @author luoxiaodou
 * @since 2025-11-25
 */
@Slf4j
@Component
public class JwtHelper {

    /**
     * JWT 加密算法
     */
    private final Algorithm algorithm;
    /**
     * API Token 校验器，用于校验 Access Token
     */
    private final JWTVerifier apiVerifier;
    /**
     * 刷新 Token 校验器，用于校验 Refresh Token
     */
    private final JWTVerifier refreshVerifier;
    /**
     * Access Token 过期时间（毫秒）
     */
    private final long accessTokenExpireMillis;
    /**
     * Refresh Token 过期时间（毫秒）
     */
    private final long refreshTokenExpireMillis;

    /**
     * Audience (受众)，用于区分 Token 类型
     */
    private static final String CLAIM_AUDIENCE = "aud";
    /**
     * 用户ID (User ID) 的 Claim 键名
     */
    private static final String CLAIM_USER_ID = "uid";
    /**
     * 用户名 (Username) 的 Claim 键名
     */
    private static final String CLAIM_USERNAME = "unm";
    /**
     * 租户ID (Tenant ID) 的 Claim 键名
     */
    private static final String CLAIM_TENANT_ID = "tid";
    /**
     * 客户端类型 (Client Type) 的 Claim 键名
     */
    private static final String CLAIM_CLIENT_TYPE = "cty";
    /**
     * 角色列表 (Roles) 的 Claim 键名
     */
    private static final String CLAIM_ROLES = "rol";

    /**
     * Access Token 的 Audience 值
     */
    private static final String AUDIENCE_ACCESS = "api";
    /**
     * Refresh Token 的 Audience 值
     */
    private static final String AUDIENCE_REFRESH = "refresh";

    /**
     * 构造函数，初始化 JWT 工具类
     *
     * @param properties JWT 配置属性 {@link JwtProperties}
     */
    public JwtHelper(JwtProperties properties) {
        this.algorithm = Algorithm.HMAC256(properties.getSecret());
        this.apiVerifier = JWT.require(this.algorithm).withAudience(AUDIENCE_ACCESS).build();
        this.refreshVerifier = JWT.require(this.algorithm).withAudience(AUDIENCE_REFRESH).build();
        this.accessTokenExpireMillis = TimeUnit.MINUTES.toMillis(properties.getAccessTokenExpiration());
        this.refreshTokenExpireMillis = TimeUnit.DAYS.toMillis(properties.getRefreshTokenExpiration());
    }

    /**
     * 创建 Access Token
     *
     * @param payload JWT 载荷信息 {@link JwtPayload}
     * @return 生成的 Access Token 字符串，如果创建失败则返回 null
     */
    public String createAccessToken(JwtPayload payload) {
        return createToken(payload, accessTokenExpireMillis, AUDIENCE_ACCESS);
    }

    /**
     * 创建 Refresh Token
     *
     * @param payload JWT 载荷信息 {@link JwtPayload}
     * @return 生成的 Refresh Token 字符串，如果创建失败则返回 null
     */
    public String createRefreshToken(JwtPayload payload) {
        return createToken(payload, refreshTokenExpireMillis, AUDIENCE_REFRESH);
    }

    /**
     * 验证 Access Token 的有效性
     *
     * @param token 包含或不包含 "Bearer " 前缀的 JWT Token
     * @return 如果 Token 有效返回 true，否则返回 false
     */
    public boolean verify(String token) {
        try {
            apiVerifier.verify(removeBearerPrefix(token));
            return true;
        } catch (JWTVerificationException e) {
            log.warn("Access Token 校验失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 从 Access Token 中解析并获取 LoginUser 信息
     *
     * @param token 包含或不包含 "Bearer " 前缀的 JWT Token
     * @return 解析出的 LoginUser 对象，如果 Token 无效或解析失败则返回 null
     */
    public LoginUser getLoginUser(String token) {
        try {
            DecodedJWT jwt = verifyAndDecode(token, apiVerifier);
            if (jwt == null) return null;

            String userId = jwt.getClaim(CLAIM_USER_ID).asString();
            String username = jwt.getClaim(CLAIM_USERNAME).asString();
            String tenantId = jwt.getClaim(CLAIM_TENANT_ID).asString();
            String clientType = jwt.getClaim(CLAIM_CLIENT_TYPE).asString();
            List<String> roles = jwt.getClaim(CLAIM_ROLES).asList(String.class);

            LoginUser loginUser = new LoginUser(userId, username, tenantId, null, roles);
            loginUser.setClientType(clientType);
            return loginUser;
        } catch (JWTVerificationException e) {
            log.warn("从 Access Token 解析 LoginUser 失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从已解析的 JWT 中提取 LoginUser 信息
     * 用于从 Refresh Token 中提取用户信息，避免重复校验
     *
     * @param jwt 已解析的 JWT 对象 {@link DecodedJWT}
     * @return 解析出的 LoginUser 对象，如果 JWT 为 null 或解析失败则返回 null
     */
    public LoginUser extractLoginUser(DecodedJWT jwt) {
        if (jwt == null) return null;

        try {
            String userId = jwt.getClaim(CLAIM_USER_ID).asString();
            String username = jwt.getClaim(CLAIM_USERNAME).asString();
            String tenantId = jwt.getClaim(CLAIM_TENANT_ID).asString();
            String clientType = jwt.getClaim(CLAIM_CLIENT_TYPE).asString();
            List<String> roles = jwt.getClaim(CLAIM_ROLES).asList(String.class);

            LoginUser loginUser = new LoginUser(userId, username, tenantId, null, roles);
            loginUser.setClientType(clientType);
            return loginUser;
        } catch (Exception e) {
            log.warn("从 JWT 提取 LoginUser 失败: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 从 Access Token 中提取用户ID
     *
     * @param token 包含或不包含 "Bearer " 前缀的 JWT Token
     * @return 用户ID字符串，如果 Token 无效或解析失败则返回 null
     */
    public String getUserId(String token) {
        try {
            DecodedJWT jwt = verifyAndDecode(token, apiVerifier);
            return jwt != null ? jwt.getClaim(CLAIM_USER_ID).asString() : null;
        } catch (JWTVerificationException e) {
            return null;
        }
    }

    /**
     * 从 Access Token 中提取用户名
     *
     * @param token 包含或不包含 "Bearer " 前缀的 JWT Token
     * @return 用户名字字符串，如果 Token 无效或解析失败则返回 null
     */
    public String getUsername(String token) {
        try {
            DecodedJWT jwt = verifyAndDecode(token, apiVerifier);
            return jwt != null ? jwt.getClaim(CLAIM_USERNAME).asString() : null;
        } catch (JWTVerificationException e) {
            return null;
        }
    }

    /**
     * 验证并解析 Refresh Token
     *
     * @param refreshToken 包含或不包含 "Bearer " 前缀的 Refresh Token
     * @return 解析后的 DecodedJWT 对象，如果 Token 无效或解析失败则返回 null
     */
    public DecodedJWT verifyAndParseRefreshToken(String refreshToken) {
        try {
            return verifyAndDecode(refreshToken, refreshVerifier);
        } catch (JWTVerificationException e) {
            log.warn("Refresh Token 校验失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 移除 Token 中的 "Bearer " 前缀
     *
     * @param token 可能包含 "Bearer " 前缀的 Token 字符串
     * @return 移除前缀后的 Token 字符串，如果原字符串不包含前缀则直接返回原字符串
     */
    public String removeBearerPrefix(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            return token.substring(7);
        }
        return token;
    }

    /**
     * 创建 JWT Token 的内部方法
     *
     * @param payload      JWT 载荷信息
     * @param expireMillis 过期时间（毫秒）
     * @param audience     Audience 值，用于区分 Token 类型
     * @return 生成的 JWT Token 字符串，如果创建失败则返回 null
     */
    private String createToken(JwtPayload payload, long expireMillis, String audience) {
        try {
            Date now = new Date();
            Date expiresAt = new Date(now.getTime() + expireMillis);

            return JWT.create()
                .withAudience(audience)
                .withClaim(CLAIM_USER_ID, payload.getUserId())
                .withClaim(CLAIM_USERNAME, payload.getUsername())
                .withClaim(CLAIM_TENANT_ID, payload.getTenantId())
                .withClaim(CLAIM_CLIENT_TYPE, payload.getClientType())
                .withClaim(CLAIM_ROLES, payload.getRoles())
                .withIssuedAt(now)
                .withExpiresAt(expiresAt)
                .sign(algorithm);

        } catch (Exception e) {
            log.error("JWT Token 创建失败", e);
            return null;
        }
    }

    /**
     * 验证并解码 JWT Token 的内部方法
     *
     * @param token    包含或不包含 "Bearer " 前缀的 Token 字符串
     * @param verifier JWT 校验器
     * @return 解码后的 JWT 对象，如果 Token 无效则抛出 JWTVerificationException
     * @throws JWTVerificationException 如果 Token 校验失败
     */
    private DecodedJWT verifyAndDecode(String token, JWTVerifier verifier) throws JWTVerificationException {
        String actualToken = removeBearerPrefix(token);
        if (!StringUtils.hasText(actualToken)) {
            return null;
        }
        return verifier.verify(actualToken);
    }

    /**
     * JWT 载荷信息类，用于封装创建 JWT 时所需的用户信息
     */
    @Getter
    public static class JwtPayload {
        /**
         * 用户ID
         */
        private final String userId;
        /**
         * 用户名
         */
        private final String username;
        /**
         * 租户ID
         */
        private final String tenantId;
        /**
         * 客户端类型
         */
        private final String clientType;
        /**
         * 角色列表
         */
        private final List<String> roles;

        /**
         * 构造函数
         *
         * @param userId     用户ID
         * @param username   用户名
         * @param tenantId   租户ID
         * @param clientType 客户端类型
         * @param roles      角色列表
         */
        public JwtPayload(String userId, String username, String tenantId, String clientType, List<String> roles) {
            this.userId = userId;
            this.username = username;
            this.tenantId = tenantId;
            this.clientType = clientType;
            this.roles = roles;
        }

        /**
         * 从 LoginUser 对象创建 JwtPayload 对象
         *
         * @param user       登录用户信息 {@link LoginUser}
         * @param clientType 客户端类型
         * @return 创建的 JwtPayload 对象
         */
        public static JwtPayload fromLoginUser(LoginUser user, String clientType) {
            return new JwtPayload(user.getUserId(), user.getUsername(), user.getTenantId(), clientType, user.getRoles());
        }
    }
}