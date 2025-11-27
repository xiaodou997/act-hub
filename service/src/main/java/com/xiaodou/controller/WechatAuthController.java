package com.xiaodou.controller;

import com.xiaodou.model.WechatUser;
import com.xiaodou.model.auth.LoginVO;
import com.xiaodou.model.dto.WechatLoginRequestDTO;
import com.xiaodou.result.Result;
import com.xiaodou.service.WechatApiService;
import com.xiaodou.service.WechatUserService;
import com.xiaodou.token.JwtHelper;
import com.xiaodou.token.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;

/**
 * 微信小程序认证控制器
 */
@RestController
@RequestMapping("/wx")
@RequiredArgsConstructor
public class WechatAuthController {

    private final WechatApiService wechatApiService;
    private final WechatUserService wechatUserService;
    private final JwtHelper jwtHelper;
    private final TokenService tokenService;

    /**
     * 微信小程序登录接口
     */
    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody WechatLoginRequestDTO loginRequest) {
        // 1. 用 code 换取 openid 和 session_key
        WechatApiService.WechatSessionVO sessionVO = wechatApiService.code2Session(loginRequest.getCode());
        if (sessionVO == null) {
            return Result.fail(500, "微信登录失败，请稍后重试");
        }

        // 2. 根据 openid 登录或注册用户
        WechatUser user = wechatUserService.loginOrRegister(sessionVO);

        // 3. 检查用户状态
        if (user.getStatus() == 0) {
            return Result.fail(403, "您的账户已被禁用");
        }

        // 4. 为该用户生成系统 Token
        // 注意：这里的 JwtPayload 构造相对简单，因为小程序用户没有复杂的角色和租户
        JwtHelper.JwtPayload payload = new JwtHelper.JwtPayload(
                String.valueOf(user.getId()),
                user.getNickname(), // 可选，也可以用 openid
                null, // 小程序用户无租户
                "WMP", // 客户端类型：Wechat Mini Program
                Collections.singletonList("ROLE_CUSTOMER") // 给所有小程序用户一个基础角色
        );

        String accessToken = jwtHelper.createAccessToken(payload);
        String refreshToken = jwtHelper.createRefreshToken(payload);

        // 5. 存储 Refresh Token
        tokenService.saveRefreshToken(payload, refreshToken);

        // 6. 返回双 Token 给小程序端
        return Result.success(new LoginVO(accessToken, refreshToken, null));
    }
}
