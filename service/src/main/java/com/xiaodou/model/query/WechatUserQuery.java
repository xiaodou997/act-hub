package com.xiaodou.model.query;

import com.xiaodou.model.vo.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 微信用户分页查询对象
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class WechatUserQuery extends PageQuery {

    /**
     * 用户昵称 (支持模糊查询)
     */
    private String nickname;

    /**
     * 手机号 (支持模糊查询)
     */
    private String phoneNumber;

    /**
     * 用户状态（1:正常, 0:禁用）
     */
    private Byte status;
}
