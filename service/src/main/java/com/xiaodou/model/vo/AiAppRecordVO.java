package com.xiaodou.model.vo;

import com.xiaodou.model.AiAppRecord;
import com.xiaodou.utils.DateTimeUtils;
import lombok.Data;

/**
 * AI应用执行记录视图对象 (View Object)
 * <p>
 * 用于向前端返回AI应用执行记录，时间字段转换为毫秒时间戳。
 * </p>
 *
 * @author xiaodou
 * @since 2025-11-27
 */
@Data
public class AiAppRecordVO {

    /**
     * AI应用记录唯一ID
     */
    private String id;

    /**
     * 发起AI应用的用户ID
     */
    private String userId;

    /**
     * AI应用ID
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
     * 输入参数JSON字符串（展示给用户的）
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
     * AI应用执行消耗的时间，单位是毫秒
     */
    private Long executionTime;

    /**
     * 执行器Bean名称
     */
    private String handlerBean;

    /**
     * 创建时间（毫秒时间戳）
     */
    private Long createdAt;

    /**
     * 更新时间（毫秒时间戳）
     */
    private Long updatedAt;

    /**
     * 从实体对象转换
     */
    public static AiAppRecordVO fromEntity(AiAppRecord record) {
        AiAppRecordVO vo = new AiAppRecordVO();
        vo.setId(record.getId());
        vo.setUserId(record.getUserId());
        vo.setAiApplicationId(record.getAiApplicationId());
        vo.setCreditCost(record.getCreditCost());
        vo.setExecuteId(record.getExecuteId());
        vo.setStatus(record.getStatus());
        vo.setDisplayInputParams(record.getDisplayInputParams());
        vo.setOutputResult(record.getOutputResult());
        vo.setErrorMessage(record.getErrorMessage());
        vo.setExecutionTime(record.getExecutionTime());
        vo.setHandlerBean(record.getHandlerBean());
        vo.setCreatedAt(DateTimeUtils.toTimestamp(record.getCreatedAt()));
        vo.setUpdatedAt(DateTimeUtils.toTimestamp(record.getUpdatedAt()));
        return vo;
    }
}
