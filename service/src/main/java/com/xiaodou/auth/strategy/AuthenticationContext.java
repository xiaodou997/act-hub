package com.xiaodou.auth.strategy;

import com.xiaodou.auth.exception.AuthenticationException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;

/**
 * 认证上下文（Authentication Context）
 * <p>
 * 封装一次身份认证过程中所需的所有信息，包括原始凭证、解析后的 Token、客户端类型、请求信息等。
 * 该记录类用于在认证策略链中传递上下文数据，确保线程安全与不可变性。
 * </p>
 *
 * @param rawToken 原始 Token 字符串（来自请求头 "Authorization"，含 "Bearer " 前缀）
 * @param actualToken 实际 Token 字符串（去除 "Bearer " 前缀后的部分）
 * @param clientType 客户端类型（来自请求头 "X-Client-Type"，如 "web", "app", "miniapp" 等）
 * @param requestUri 请求的 URI 路径（不包含查询参数）
 * @param method 请求的 HTTP 方法（如 "GET", "POST"）
 * @param request 原始的 {@link HttpServletRequest} 对象，可用于进一步提取信息
 * @author xiaodou V=>dddou117
 * @version V1.0
 * @since 2025/9/8
 */
public record AuthenticationContext(String rawToken, String actualToken, String clientType, String requestUri,
                                    String method, HttpServletRequest request) {

    private static final String TOKEN_HEADER = "Authorization";
    private static final String CLIENT_TYPE_HEADER = "X-Client-Type";
    private static final String BEARER_PREFIX = "Bearer ";

    /**
     * 从 HTTP 请求中构建 {@link AuthenticationContext} 实例。
     * <p>
     * 该方法从请求头中提取必要信息，进行基本格式校验，并构造上下文对象。
     * 不进行 Token 的签名或有效性验证（由后续认证策略处理）。
     * </p>
     *
     * @param request HTTP 请求对象，不能为 null
     * @return 构建好的认证上下文实例
     * @throws AuthenticationException 如果以下任一情况发生：
     *     <ul>
     *       <li>请求头中缺少 Authorization</li>
     *       <li>Authorization 不以 "Bearer " 开头</li>
     *       <li>Authorization 值仅为 "Bearer "（实际 Token 为空）</li>
     *       <li>请求头中缺少 X-Client-Type</li>
     *     </ul>
     */
    public static AuthenticationContext from(HttpServletRequest request) {
        String rawToken = request.getHeader(TOKEN_HEADER);
        String clientType = request.getHeader(CLIENT_TYPE_HEADER);
        String method = request.getMethod();
        String requestUri = request.getRequestURI();

        // 验证基本格式
        validateHeaders(rawToken, clientType, method, requestUri);

        // 提取实际token
        String actualToken = extractActualToken(rawToken, method, requestUri);

        return new AuthenticationContext(rawToken, actualToken, clientType, requestUri, method, request);
    }

    /**
     * 校验请求头的基本格式。
     * <p>
     * 检查 Token 和 ClientType 是否存在且符合预期格式。
     * </p>
     *
     * @param token Authorization 头值
     * @param clientType X-Client-Type 头值
     * @param method 当前请求方法
     * @param requestUri 当前请求 URI
     * @throws AuthenticationException 若任一条件不满足
     */
    private static void validateHeaders(String token, String clientType, String method, String requestUri) {
        if (!StringUtils.hasText(token)) {
            throw new AuthenticationException("请求头中缺少身份凭证", method, requestUri);
        }

        if (!token.startsWith(BEARER_PREFIX)) {
            throw new AuthenticationException("身份凭证格式错误", method, requestUri);
        }

        if (!StringUtils.hasText(clientType)) {
            throw new AuthenticationException("缺少客户端类型标识", method, requestUri);
        }
    }

    /**
     * 从原始 Token 中提取实际的认证凭证（去除 "Bearer " 前缀）。
     *
     * @param rawToken 原始 Authorization 头值
     * @param method 当前请求方法（用于异常信息）
     * @param requestUri 当前请求 URI（用于异常信息）
     * @return 去除前缀后的实际 Token
     * @throws AuthenticationException 如果原始 Token 长度不足，说明 Token 内容为空
     */
    private static String extractActualToken(String rawToken, String method, String requestUri) {
        if (rawToken.length() <= BEARER_PREFIX.length()) {
            throw new AuthenticationException("身份凭证为空", method, requestUri);
        }
        return rawToken.substring(BEARER_PREFIX.length());
    }
}
