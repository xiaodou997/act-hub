package com.xiaodou.model.dto;

import lombok.Data;

@Data
public class WechatLoginRequestDTO {
    /**
     * 小程序端通过 wx.login() 获取的临时登录凭证
     */
    private String code;

    // 未来可扩展，用于接收加密的用户信息或手机号
    // private String encryptedData;
    // private String iv;
}
