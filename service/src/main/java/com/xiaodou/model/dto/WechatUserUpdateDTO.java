package com.xiaodou.model.dto;

import lombok.Data;

/**
 * 微信用户更新 DTO
 * <p>
 * 用于管理员修改微信用户的部分信息。
 * </p>
 */
@Data
public class WechatUserUpdateDTO {

    /**
     * 用户昵称
     */
    private String nickname;

    /**
     * 用户头像URL
     */
    private String avatarUrl;

    /**
     * 性别（0:未知, 1:男, 2:女）
     */
    private Byte gender;

    /**
     * 用户绑定的手机号
     */
    private String phoneNumber;

    /**
     * 备注信息
     */
    private String remark;
}
