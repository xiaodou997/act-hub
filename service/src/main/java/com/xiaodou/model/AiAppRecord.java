package com.xiaodou.model;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * Ai应用执行记录表
 * </p>
 *
 * @author luoxiaodou
 * @since 2025-05-19
 */
@Getter
@Setter
@ToString
@TableName("ai_app_record")
public class AiAppRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 任务记录唯一ID(雪花ID)
     */
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 发起任务的用户ID
     */
    private String userId;

    /**
     * AI应用ID（关联 ai_application 表）
     */
    private String aiApplicationId;

    /**
     * 本次消耗积分
     */
    private Integer creditCost;

    /**
     * 异步执行的事件 ID
     */
    private String executeId;

    /**
     * 状态：1成功 2失败 3进行中
     */
    private Integer status;

    /**
     * 输入参数JSON字符串，系统调用工作流使用
     */
    private String inputParams;

    /**
     * 输入参数JSON字符串,展示给用户查看的，key映射之后的值
     */
    private String displayInputParams;

    /**
     * 执行结果JSON字符串
     */
    private String outputResult;

    /**
     * 执行失败时的错误信息
     */
    private String errorMessage;

    /**
     * 任务执行消耗的时间，单位是毫秒
     */
    private Long executionTime;

    /**
     * 执行器Bean名称（如 workflowHandler, tikHubHandler, httpRequestHandler）
     */
    private String handlerBean;

    /**
     * 软删除标记(0-未删除,1-已删除)
     */
    @TableLogic
    private Integer isDeleted = 0; // 添加默认值

    /**
     * 删除时间
     */
    private LocalDateTime deleteTime;

    /**
     * 记录创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    /**
     * 记录更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
