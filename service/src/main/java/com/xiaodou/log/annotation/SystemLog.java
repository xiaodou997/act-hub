package com.xiaodou.log.annotation;


import com.xiaodou.log.model.LogLevel;

import java.lang.annotation.*;

/**
 * 系统操作日志注解（System Operation Log Annotation）
 * <p>
 * 标记需要记录系统操作日志的方法，自动收集操作信息并记录到日志系统。
 * 支持灵活配置记录内容（模块、动作、描述、请求参数、返回结果）。
 * </p>
 * <p><b>核心功能：</b></p>
 * <ul>
 * <li>操作追踪：记录关键业务操作的行为轨迹</li>
 * <li>审计支持：提供操作回溯和责任追溯依据</li>
 * <li>性能监控：通过日志分析接口调用情况</li>
 * <li>安全控制：记录敏感操作行为</li>
 * </ul>
 * <p><b>使用示例：</b></p>
 * <pre>
 * {@code
 * @SystemLog(
 *     module = "用户管理",
 *     action = "创建用户",
 *     description = "管理员创建新用户账号",
 *     recordRequest = true,
 *     recordResponse = false
 * )
 * public void createUser(UserDTO user) {
 *     // 业务逻辑
 * }
 * }
 * </pre>
 *
 * @author xiaodou V>dddou117
 * @see Documented
 * @see Retention
 * @see Target
 * @since 1.0
 */
@Target(ElementType.METHOD)      // 只能用于方法上
@Retention(RetentionPolicy.RUNTIME) // 运行时保留，可通过反射获取
@Documented                     // 包含在JavaDoc中
public @interface SystemLog {

    /**
     * 功能模块名称
     * <p>
     * 标识操作所属的功能模块，建议使用统一的模块命名规范。
     * 示例值："用户管理"、"订单系统"、"权限控制"
     * </p>
     * <p><b>约束：</b></p>
     * <ul>
     * <li>必填属性（无默认值）</li>
     * <li>长度建议：2-20个字符</li>
     * <li>避免使用特殊字符</li>
     * </ul>
     *
     * @return 功能模块名称
     */
    String module() default "";

    ;

    /**
     * 操作动作名称
     * <p>
     * 描述具体执行的操作行为，建议使用动词+名词结构。
     * 示例值："创建用户"、"更新订单"、"删除权限"
     * </p>
     * <p><b>约束：</b></p>
     * <ul>
     * <li>必填属性（无默认值）</li>
     * <li>长度建议：2-20个字符</li>
     * <li>建议使用动宾结构</li>
     * </ul>
     *
     * @return 操作动作名称
     */
    String action();

    /**
     * 操作详细描述
     * <p>
     * 补充说明操作的具体内容、目的或上下文信息。
     * 当 {@code recordRequest=false} 时，此字段尤为重要。
     * </p>
     * <p><b>使用建议：</b></p>
     * <ul>
     * <li>包含关键业务标识（如订单号、用户ID）</li>
     * <li>说明操作的特殊条件或目的</li>
     * <li>长度建议：不超过200字符</li>
     * </ul>
     *
     * @return 操作描述信息，默认为空字符串
     */
    String description() default "";

    /**
     * 是否记录请求参数
     * <p>
     * 控制是否在日志中记录方法调用时的请求参数。
     * 敏感操作建议设置为 {@code false}，避免记录敏感信息。
     * </p>
     * <p><b>安全提示：</b></p>
     * <ul>
     * <li>涉及密码、密钥等敏感数据时必须设为 {@code false}</li>
     * <li>大对象参数建议设为 {@code false} 避免日志过大</li>
     * <li>生产环境建议谨慎开启此选项</li>
     * </ul>
     *
     * @return true-记录请求参数，false-不记录，默认为true
     */
    boolean recordRequest() default true;

    /**
     * 是否记录返回结果
     * <p>
     * 控制是否在日志中记录方法执行后的返回结果。
     * 返回结果可能包含敏感数据或大对象，需谨慎设置。
     * </p>
     * <p><b>使用建议：</b></p>
     * <ul>
     * <li>查询类操作可开启（如查询用户信息）</li>
     * <li>涉及敏感数据的操作必须关闭</li>
     * <li>大结果集操作建议关闭（如导出报表）</li>
     * </ul>
     *
     * @return true-记录返回结果，false-不记录，默认为false
     */
    boolean recordResponse() default false;

    /**
     * 日志级别
     *
     * @return 日志级别，默认为 INFO
     */
    LogLevel level() default LogLevel.INFO;

    /**
     * 是否记录耗时
     *
     * @return true-记录耗时，false-不记录，默认为true
     */
    boolean recordCost() default true;

    /**
     * 目标对象类型（用于关联业务实体）
     *
     * @return 目标对象类型
     */
    String targetType() default "";

    /**
     * 目标对象ID（SpEL表达式，用于动态获取）
     *
     * @return 目标对象ID表达式
     */
    String targetId() default "";
}
