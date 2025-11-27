package com.xiaodou.model.vo;

import com.xiaodou.model.User;
import com.xiaodou.utils.DateTimeUtils;
import lombok.Data;

/**
 * 用户视图对象（View Object）
 * <p>
 * 用于封装用户信息返回给前端，时间字段使用毫秒时间戳格式
 * </p>
 *
 * @author xiaodou
 * @since 2025-11-25
 */
@Data
public class UserVO {
    /**
     * 用户唯一标识
     */
    private String id;

    /**
     * 租户唯一标识符
     */
    private String tenantId;

    /**
     * 租户显示名称
     */
    private String tenantName;

    /**
     * 用户名
     */
    private String username;

    /**
     * 用户邮箱
     */
    private String email;

    /**
     * 用户状态：0-禁用, 1-正常, 2-锁定
     */
    private Byte status;

    /**
     * 用户备注信息
     */
    private String remark;

    /**
     * 最后登录时间（毫秒时间戳）
     */
    private Long lastLoginAt;

    /**
     * 创建时间（毫秒时间戳）
     */
    private Long createdAt;

    /**
     * 更新时间（毫秒时间戳）
     */
    private Long updatedAt;

    /**
     * 将实体类 User 转换为视图对象 UserVO
     *
     * @param user 用户实体对象
     * @return 转换后的用户视图对象
     */
    public static UserVO fromEntity(User user) {
        if (user == null) {
            return null;
        }

        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setTenantId(user.getTenantId());
        vo.setUsername(user.getUsername());
        vo.setEmail(user.getEmail());
        vo.setStatus(user.getStatus());
        vo.setRemark(user.getRemark());
        // 使用 DateTimeUtils 转换时间字段为毫秒时间戳
        vo.setLastLoginAt(DateTimeUtils.toTimestamp(user.getLastLoginAt()));
        vo.setCreatedAt(DateTimeUtils.toTimestamp(user.getCreatedAt()));
        vo.setUpdatedAt(DateTimeUtils.toTimestamp(user.getUpdatedAt()));
        return vo;
    }
}
