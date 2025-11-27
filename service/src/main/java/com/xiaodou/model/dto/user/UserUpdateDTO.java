package com.xiaodou.model.dto.user;

import lombok.Data;

/**
 * @ClassName: UserUpdateDTO
 * @Description: 用户更新信息的请求参数
 * @Author: xiaodou V=>dddou117
 * @Date: 2025/5/11
 * @Version: V1.0
 * @JDK: JDK21
 */
@Data
public class UserUpdateDTO {

    /**
     * 用户ID，主键
     */
    private String id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 绑定的租户ID
     */
    private String tenantId;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 密码哈希值
     */
    private String password;

    /**
     * 状态：0-禁用, 1-正常, 2-锁定
     */
    private Byte status;

    /**
     * 备注信息
     */
    private String remark;
}
