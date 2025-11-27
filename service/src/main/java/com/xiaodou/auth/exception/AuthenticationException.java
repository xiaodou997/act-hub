package com.xiaodou.auth.exception;

import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;

/**
 * 自定义认证异常
 *
 * @author xiaodou V=>dddou117
 * @version V1.0
 * @since 2025/9/8
 */

public class AuthenticationException extends AuthenticationCredentialsNotFoundException {

    private final String method;
    private final String requestUri;

    public AuthenticationException(String message, String method, String requestUri) {
        super(message);
        this.method = method;
        this.requestUri = requestUri;
    }

    public String getMethod() {
        return method;
    }

    public String getRequestUri() {
        return requestUri;
    }
}
