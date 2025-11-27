package com.xiaodou.model.auth;

import lombok.Data;

/**
 * 刷新令牌请求 DTO
 */
@Data
public class RefreshRequest {
    /**
     * 长期有效的刷新令牌 (Refresh Token)
     */
    private String refreshToken;
}
