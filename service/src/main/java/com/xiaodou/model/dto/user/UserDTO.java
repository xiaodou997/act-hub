package com.xiaodou.model.dto.user;

import com.xiaodou.model.User;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 用户视图对象（View Object）
 * <p>
 * 用于封装用户核心信息，提供给前端展示用户列表、用户详情等场景。
 * 包含用户基本信息（ID、用户名、邮箱等）、状态信息、时间戳等字段。
 * 通过静态方法 fromUser 实现与实体类 User 的转换。
 * </p>
 *
 * @author xiaodou V=>dddou117
 * @version V1.0
 * @since 2025/5/11
 */
@Data
@Accessors(chain = true)
public class UserDTO {
    /**
     * 用户唯一标识
     * <p>
     * 系统生成的UUID格式字符串，长度为36字符。
     * 非空字段，创建后不可修改。
     * </p>
     */
    private String id;

    /**
     * 租户唯一标识符
     */
    private String tenantId;

    /**
     * 租户显示名称
     * <p>用于前端展示的团队名称，非唯一标识</p>
     */
    private String tenantName;

    /**
     * 用户名
     * <p>
     * 用户登录系统的唯一名称，长度限制为4-50字符。
     * 必须以字母开头，只能包含字母、数字、下划线。
     * 非空字段，创建后不可修改。
     * </p>
     */
    private String username;

    /**
     * 用户邮箱
     * <p>
     * 用于系统通知和密码重置等功能。
     * 必须符合标准邮箱格式（如 user@example.com）。
     * 非空字段，创建后可修改。
     * </p>
     */
    private String email;

    /**
     * 用户状态
     * <p>
     * 表示用户账户的当前状态：
     * <ul>
     *   <li>0 - 禁用（账户被冻结，无法登录）</li>
     *   <li>1 - 正常（账户可正常使用）</li>
     *   <li>2 - 锁定（不可以使用）</li>
     * </ul>
     * 非空字段，默认值为1（正常）。
     * </p>
     */
    private Byte status;

    /**
     * 用户备注信息
     * <p>
     * 用于记录用户的特殊标识、部门信息等备注内容。
     * 可为空，最大长度限制为200字符。
     * </p>
     */
    private String remark;

    /**
     * 最后登录时间
     * <p>
     * 记录用户最后一次成功登录系统的时间。
     * 可为空（表示从未登录过）。
     * 时间格式为ISO-8601标准（yyyy-MM-ddTHH:mm:ss）。
     * </p>
     */
    private LocalDateTime lastLoginAt;

    /**
     * 创建时间
     * <p>
     * 记录用户账户创建的时间。
     * 由系统自动生成，非空字段。
     * 时间格式为ISO-8601标准（yyyy-MM-ddTHH:mm:ss）。
     * </p>
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     * <p>
     * 记录用户信息最后一次修改的时间。
     * 由系统自动维护，非空字段。
     * 时间格式为ISO-8601标准（yyyy-MM-ddTHH:mm:ss）。
     * </p>
     */
    private LocalDateTime updatedAt;

    /**
     * 将实体类 User 转换为视图对象 UserVO
     * <p>
     * 实现从数据实体到视图对象的转换，忽略敏感字段（如密码）。
     * 转换过程中会复制所有非敏感字段：
     * <ul>
     *   <li>id - 用户ID</li>
     *   <li>username - 用户名</li>
     *   <li>email - 邮箱</li>
     *   <li>status - 用户状态</li>
     *   <li>remark - 备注</li>
     *   <li>lastLoginTime - 最后登录时间</li>
     *   <li>createdAt - 创建时间</li>
     *   <li>updatedAt - 更新时间</li>
     * </ul>
     * 注意：此方法不会转换 avatar 字段（需要单独处理）
     * </p>
     *
     * @param user 用户实体对象，不能为null
     * @return 转换后的用户视图对象
     */
    public static UserDTO fromUser(User user) {
        if (user == null) {
            return null;
        }

        UserDTO userVO = new UserDTO();
        userVO.setId(user.getId());
        userVO.setUsername(user.getUsername());
        userVO.setEmail(user.getEmail());
        userVO.setStatus(user.getStatus());
        userVO.setRemark(user.getRemark());
        userVO.setLastLoginAt(user.getLastLoginAt());
        userVO.setCreatedAt(user.getCreatedAt());
        userVO.setUpdatedAt(user.getUpdatedAt());
        return userVO;
    }
}

