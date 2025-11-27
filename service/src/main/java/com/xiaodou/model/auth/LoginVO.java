package com.xiaodou.model.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 登录响应视图对象, 封装登录成功后的响应数据，包含认证令牌和用户信息
 *
 * @author xiaodou V=>dddou117
 * @version V1.0
 * @since 2025-05-09
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
public class LoginVO {
    /**
     * 访问令牌 (Access Token)
     */
    private String accessToken;

    /**
     * 刷新令牌 (Refresh Token)
     */
    private String refreshToken;

    /**
     * 用户详细信息（包含角色、权限等）
     */
    private UserInfoVO userInfo;
}
