package com.xiaodou.result;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 全局统一返回结果封装
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) // 全局配置：如果字段为null，则不参与序列化
public class Result<T> {
    /**
     * 状态码
     */
    private Integer code;

    /**
     * 返回消息
     */
    private String message;

    /**
     * 返回数据
     */
    private T data;

    /**
     * 时间戳
     */
    private final long timestamp = System.currentTimeMillis();

    /**
     * 链路追踪ID
     */
    private String traceId;

    /**
     * 详细的校验错误列表 (可选)
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY) // 列表为空时不序列化
    private List<ValidationError> errors;

    /**
     * 调试信息 (仅在非生产环境返回)
     */
    private String debugInfo;


    // ========== 成功响应 ==========

    public static <T> Result<T> success() {
        return success(null);
    }

    public static <T> Result<T> success(T data) {
        return Result.<T>builder()
            .code(ResultCodeEnum.SUCCESS.getCode())
            .message(ResultCodeEnum.SUCCESS.getMessage())
            .data(data)
            .build();
    }

    // ========== 失败响应 ==========

    public static <T> Result<T> fail() {
        return fail(ResultCodeEnum.INTERNAL_SERVER_ERROR);
    }

    public static <T> Result<T> fail(String message) {
        return Result.<T>builder()
            .code(ResultCodeEnum.INTERNAL_SERVER_ERROR.getCode())
            .message(message)
            .build();
    }

    public static <T> Result<T> fail(ResultCodeEnum resultCode) {
        return Result.<T>builder()
            .code(resultCode.getCode())
            .message(resultCode.getMessage())
            .build();
    }

    public static <T> Result<T> fail(ResultCodeEnum resultCode, T data) {
        return Result.<T>builder()
            .code(resultCode.getCode())
            .message(resultCode.getMessage())
            .data(data)
            .build();
    }

    public static <T> Result<T> fail(Integer code, String message) {
        return Result.<T>builder()
            .code(code)
            .message(message)
            .build();
    }

    /**
     * 用于参数校验异常的失败响应
     */
    public static <T> Result<T> fail(ResultCodeEnum resultCode, List<ValidationError> errors) {
        return Result.<T>builder()
            .code(resultCode.getCode())
            .message(resultCode.getMessage())
            .errors(errors)
            .build();
    }

    /**
     * 用于包含调试信息的失败响应
     */
    public static <T> Result<T> fail(ResultCodeEnum resultCode, String debugInfo) {
        return Result.<T>builder()
            .code(resultCode.getCode())
            .message(resultCode.getMessage())
            .debugInfo(debugInfo)
            .build();
    }


    /**
     * 内部静态类，用于表示字段校验错误
     */
    @Data
    @AllArgsConstructor
    public static class ValidationError {
        /**
         * 出错的字段名
         */
        private String field;
        /**
         * 具体的错误信息
         */
        private String message;
    }
}
