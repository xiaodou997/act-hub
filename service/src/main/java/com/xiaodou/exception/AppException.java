package com.xiaodou.exception;

import com.xiaodou.result.Result;
import com.xiaodou.result.ResultCodeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 业务异常类（Business Exception）
 * <p>
 * 封装AI代理服务相关的业务异常，提供统一的错误处理机制。
 * 包含错误码、错误信息和附加数据，支持直接转换为标准响应对象。
 * </p>
 * <p><b>核心特性：</b></p>
 * <ul>
 * <li>标准化错误处理：与ResultCodeEnum结合使用，确保错误码一致性</li>
 * <li>灵活数据携带：支持附加业务数据（transient修饰，不参与序列化）</li>
 * <li>快速响应转换：提供toResult()方法直接生成标准响应对象</li>
 * <li>多构造方式：支持错误码枚举、自定义错误码等多种构造方式</li>
 * </ul>
 * <p><b>使用场景：</b></p>
 * <ul>
 * <li>业务流程控制异常</li>
 * <li>统一异常处理器捕获</li>
 * </ul>
 *
 * @author xiaodou V=>dddou117
 * @version V1.0
 * @see ResultCodeEnum
 * @see Result
 * @since 2025/5/9
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AppException extends RuntimeException {
    /**
     * 错误码
     * <p>
     * 标识异常类型的数字代码，与ResultCodeEnum中的错误码保持一致。
     * 使用final修饰确保不可变性。
     * </p>
     */
    private final Integer code;

    /**
     * 错误信息
     * <p>
     * 描述异常详情的文本信息，支持自定义错误描述。
     * 使用final修饰确保不可变性。
     * </p>
     */
    private final String message;

    /**
     * 附加业务数据
     * <p>
     * 携带异常相关的业务数据，用于前端展示或日志记录。
     * 使用transient修饰表示不参与序列化过程。
     * </p>
     */
    private final transient Object data;

    /**
     * 使用错误码枚举构造异常
     * <p>
     * 使用ResultCodeEnum中的预定义错误码和默认错误信息构造异常。
     * </p>
     *
     * @param resultCode 错误码枚举，包含预定义的错误码和错误信息
     */
    public AppException(ResultCodeEnum resultCode) {
        this(resultCode, resultCode.getMessage(), null);
    }

    /**
     * 使用错误码枚举和自定义错误信息构造异常
     * <p>
     * 使用ResultCodeEnum中的错误码，但覆盖默认错误信息。
     * </p>
     *
     * @param resultCode 错误码枚举，包含预定义的错误码
     * @param message 自定义错误信息，覆盖枚举中的默认信息
     */
    public AppException(ResultCodeEnum resultCode, String message) {
        this(resultCode, message, null);
    }

    /**
     * 使用错误码枚举、自定义错误信息和附加数据构造异常
     * <p>
     * 完整构造方式，可自定义错误信息并携带业务数据。
     * </p>
     *
     * @param resultCode 错误码枚举，包含预定义的错误码
     * @param message 自定义错误信息，覆盖枚举中的默认信息
     * @param data 附加业务数据，可为null
     */
    public AppException(ResultCodeEnum resultCode, String message, Object data) {
        super(message);
        this.code = resultCode.getCode();
        this.message = message;
        this.data = data;
    }

    /**
     * 使用自定义错误码和错误信息构造异常
     * <p>
     * 完全自定义错误码和错误信息，不依赖ResultCodeEnum。
     * </p>
     *
     * @param code 自定义错误码
     * @param message 自定义错误信息
     */
    public AppException(Integer code, String message) {
        this(code, message, null);
    }

    /**
     * 使用自定义错误码、错误信息和附加数据构造异常
     * <p>
     * 完全自定义的完整构造方式，可指定所有字段。
     * </p>
     *
     * @param code 自定义错误码
     * @param message 自定义错误信息
     * @param data 附加业务数据，可为null
     */
    public AppException(Integer code, String message, Object data) {
        super(message);
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * 获取错误信息
     * <p>
     * 重写父类方法，返回自定义的错误信息。
     * </p>
     *
     * @return 错误信息，不可为null
     */
    @Override
    public String getMessage() {
        return message;
    }

    /**
     * 转换为标准响应对象
     * <p>
     * 将异常对象转换为统一的Result响应对象，用于API响应。
     * 仅包含错误码和错误信息，不包含附加数据。
     * </p>
     *
     * @param <T> 响应数据类型
     * @return 包含错误码和错误信息的Result对象
     */
    public <T> Result<T> toResult() {
        return Result.fail(code, message);
    }
}
