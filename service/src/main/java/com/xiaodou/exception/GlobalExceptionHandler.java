package com.xiaodou.exception;

import com.xiaodou.result.Result;
import com.xiaodou.result.ResultCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 全局异常处理器，统一处理控制层抛出的各类异常
 *
 * @author xiaodou
 * @version 1.0
 * @since 2025/5/9
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理自定义业务异常
     *
     * @param e AppException 异常
     * @return 返回通用失败响应
     */
    @ExceptionHandler(AppException.class)
    public Result<Void> handleAppException(AppException e) {
        log.error("业务异常: code={}, message={}", e.getCode(), e.getMessage(), e);
        return Result.fail(e.getCode(), e.getMessage());
    }

    /**
     * 处理参数校验失败（@Valid/@Validated）
     *
     * @param e 参数校验异常
     * @return 返回参数错误提示
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(FieldError::getDefaultMessage)
            .collect(Collectors.joining("; "));
        log.warn("参数校验失败: {}", message, e);
        return Result.fail(ResultCodeEnum.PARAM_VALID_ERROR.getCode(), message);
    }

    /**
     * 处理不支持的请求方法（如 GET 请求了只能 POST 的接口）
     *
     * @param e 异常信息
     * @return 返回错误响应
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Result<Void> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        log.warn("请求方法不支持: {}", e.getMessage(), e);
        return Result.fail(ResultCodeEnum.METHOD_NOT_ALLOWED);
    }

    /**
     * 处理访问被拒绝（权限不足）
     *
     * @param e 权限拒绝异常（Spring Security）
     * @return 返回 403 权限不足
     */
    @ExceptionHandler(AccessDeniedException.class)
    public Result<Void> handleAccessDeniedException(AccessDeniedException e) {
        log.warn("权限不足: {}", e.getMessage(), e);
        return Result.fail(ResultCodeEnum.FORBIDDEN.getCode(), "权限不足，无法访问该资源");
    }

    /**
     * 处理 Spring Security 6+ 授权拒绝异常
     *
     * @param e 授权失败异常
     * @return 返回 403 权限不足
     */
    @ExceptionHandler(AuthorizationDeniedException.class)
    public Result<Void> handleAuthorizationDeniedException(AuthorizationDeniedException e) {
        log.warn("授权失败: {}", e.getMessage(), e);
        return Result.fail(ResultCodeEnum.FORBIDDEN.getCode(), "您没有权限执行此操作");
    }

    /**
     * 兜底异常处理，捕获所有未明确处理的异常
     *
     * @param e 未捕获的异常
     * @return 返回 500 错误提示
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常: ", e);
        return Result.fail(ResultCodeEnum.INTERNAL_SERVER_ERROR);
    }
}
