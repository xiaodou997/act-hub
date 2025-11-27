package com.xiaodou.result;

import lombok.Getter;

import java.util.Arrays;

/**
 * 统一结果状态码枚举（成功为0，错误为非0）
 * <p>
 * 状态码设计原则：
 * <ul>
 * <li>0: 操作成功</li>
 * <li>1xxx: 通用错误（参数、请求等）</li>
 * <li>2xxx: 业务逻辑错误</li>
 * <li>3xxx: 用户/认证相关错误</li>
 * <li>4xxx: 文件/资源错误</li>
 * <li>5xxx: AI服务错误</li>
 * <li>9999: 系统内部错误（兜底）</li>
 * </ul>
 */
@Getter
public enum ResultCodeEnum {

    /* ========== 成功 ========== */
    SUCCESS(0, "操作成功"),

    /* ========== 通用错误 1xxx ========== */
    BAD_REQUEST(1000, "请求参数错误"),
    PARAM_VALID_ERROR(1001, "参数校验失败"),
    PARAM_MISSING(1002, "参数缺失"),
    PARAM_TYPE_ERROR(1003, "参数类型错误"),
    PARAM_FORMAT_ERROR(1004, "参数格式错误"),
    METHOD_NOT_ALLOWED(1005, "请求方法不支持"),
    TOO_MANY_REQUESTS(1006, "请求过于频繁，请稍后再试"),

    /* ========== 业务错误 2xxx ========== */
    BUSINESS_ERROR(2000, "业务异常"),
    DATA_NOT_FOUND(2001, "数据不存在"),
    DATA_EXISTS(2002, "数据已存在"),
    DATA_ACCESS_ERROR(2003, "数据访问异常"),
    OPERATION_FAILED(2004, "操作失败"),

    /* ========== 用户/认证/授权相关 3xxx ========== */
    // UNAUTHORIZED(3000, "未认证"), // 使用更具体的错误码
    /**
     * 用户未登录（请求头中不包含令牌）
     */
    USER_NOT_LOGIN(3001, "用户未登录"),
    USER_ACCOUNT_NOT_EXIST(3002, "账号不存在"),
    USER_ACCOUNT_ALREADY_EXIST(3003, "账号已存在"),
    USER_CREDENTIALS_ERROR(3004, "密码错误"),
    USER_CREDENTIALS_EXPIRED(3005, "密码过期"),
    USER_ACCOUNT_EXPIRED(3006, "账号已过期"),
    USER_ACCOUNT_DISABLE(3007, "账号不可用"),
    USER_ACCOUNT_LOCKED(3008, "账号被锁定"),
    /**
     * 无权限访问（用户已登录，但权限不足）
     */
    FORBIDDEN(3009, "无权限访问"),
    /**
     * 令牌无效或已过期（请求头中包含令牌，但令牌校验失败）
     */
    TOKEN_INVALID(3010, "令牌无效或已过期"),

    /* ========== 文件/资源 4xxx ========== */
    FILE_UPLOAD_ERROR(4001, "文件上传失败"),
    FILE_DOWNLOAD_ERROR(4002, "文件下载失败"),
    FILE_DELETE_ERROR(4003, "文件删除失败"),
    FILE_TOO_LARGE(4004, "文件过大"),
    FILE_TYPE_ERROR(4005, "文件类型错误"),

    /* ========== AI 服务 5xxx ========== */
    AI_MODEL_NOT_FOUND(5001, "AI模型不存在"),
    AI_MODEL_ERROR(5002, "AI模型错误"),
    AI_PROCESSING_ERROR(5003, "AI处理错误"),
    AI_API_LIMIT(5004, "AI API调用限制"),
    AI_API_ERROR(5005, "AI API调用错误"),

    /* ========== 系统错误 ========== */
    INTERNAL_SERVER_ERROR(9999, "服务器内部错误");

    private final Integer code;
    private final String message;

    ResultCodeEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * 根据状态码获取对应的枚举常量
     *
     * @param code 状态码
     * @return 匹配的枚举常量，未找到时返回 {@link #INTERNAL_SERVER_ERROR}
     */
    public static ResultCodeEnum getByCode(Integer code) {
        return Arrays.stream(values())
            .filter(result -> result.getCode().equals(code))
            .findFirst()
            .orElse(INTERNAL_SERVER_ERROR);
    }
}