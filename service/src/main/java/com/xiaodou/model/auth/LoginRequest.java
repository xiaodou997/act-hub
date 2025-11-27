package com.xiaodou.model.auth;

import lombok.Data;

/**
 * 管理员登录请求对象（Request Object）
 * <p>
 * 用于封装管理员登录所需的认证信息，包含用户名和密码字段。
 * 该对象作为登录接口的请求体（Request Body），在身份验证流程中使用。
 * </p>
 * <p><b>使用说明：</b></p>
 * <ul>
 * <li>所有字段均为必填项</li>
 * <li>密码字段在传输过程中必须使用HTTPS加密</li>
 * <li>建议前端在传输前对密码进行二次加密（如RSA加密）</li>
 * </ul>
 * <p><b>安全注意事项：</b></p>
 * <ul>
 * <li>禁止在日志中记录密码明文</li>
 * <li>后端接收到密码后应立即进行哈希处理</li>
 * <li>密码传输应使用HTTPS协议</li>
 * </ul>
 *
 * @author xiaodou
 * @version 1.0
 * @since 2025/6/18
 */
@Data
public class LoginRequest {
    /**
     * 登录用户名
     * <ul>
     * <li>业务含义：用于标识管理员身份的唯一标识符</li>
     * <li>约束条件：
     *   <ul>
     *   <li>必填字段</li>
     *   <li>长度限制：4-50个字符</li>
     *   <li>格式要求：支持字母、数字、下划线，不允许特殊字符</li>
     *   </ul>
     * </li>
     * <li>业务规则：
     *   <ul>
     *   <li>通常为邮箱或手机号格式</li>
     *   <li>不区分大小写</li>
     *   </ul>
     * </li>
     * <li>示例值：admin@example.com 或 13800138000</li>
     * </ul>
     */
    private String username;

    /**
     * 登录密码
     * <ul>
     * <li>业务含义：用于验证管理员身份的凭证</li>
     * <li>约束条件：
     *   <ul>
     *   <li>必填字段</li>
     *   <li>长度限制：8-20个字符</li>
     *   <li>复杂度要求：必须包含字母和数字，支持特殊字符!@#$%^&*</li>
     *   </ul>
     * </li>
     * <li>安全要求：
     *   <ul>
     *   <li>传输时必须使用HTTPS加密</li>
     *   <li>建议前端使用RSA加密后传输</li>
     *   <li>后端应立即进行BCrypt哈希处理</li>
     *   </ul>
     * </li>
     * <li>示例值：Admin123!（实际传输时为加密后的密文）</li>
     * </ul>
     */
    private String password;
}
