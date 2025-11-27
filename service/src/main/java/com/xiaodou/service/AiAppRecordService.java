package com.xiaodou.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaodou.model.AiAppRecord;

import java.util.Map;

/**
 * <p>
 * AI应用任务执行记录表 服务类
 * </p>
 *
 * @author luoxiaodou
 * @since 2025-11-27
 */
public interface AiAppRecordService extends IService<AiAppRecord> {

    /**
     * 创建AI应用记录（状态为进行中）
     *
     * @param userId 用户ID
     * @param aiApplicationId AI应用ID
     * @param handlerBean 执行器Bean名称
     * @param inputParams 输入参数
     * @param executeId 异步执行ID（可选）
     * @return AI应用记录
     */
    AiAppRecord createAiAppRecord(String userId, String aiApplicationId, String handlerBean,
                                  Map<String, Object> inputParams, String executeId);

    /**
     * 更新AI应用记录为成功状态
     *
     * @param aiAppRecordId AI应用记录ID
     * @param outputResult 输出结果
     * @param executionTime 执行时间（毫秒）
     */
    void updateAiAppSuccess(String aiAppRecordId, Map<String, Object> outputResult, Long executionTime);

    /**
     * 更新AI应用记录为失败状态
     *
     * @param aiAppRecordId AI应用记录ID
     * @param errorMessage 错误信息
     * @param executionTime 执行时间（毫秒）
     */
    void updateAiAppFailed(String aiAppRecordId, String errorMessage, Long executionTime);

    /**
     * 分页查询用户的AI应用记录
     *
     * @param page 分页对象
     * @param userId 用户ID
     * @param aiApplicationId AI应用ID（可选）
     * @param status 状态（可选）
     * @return 分页结果
     */
    IPage<AiAppRecord> pageByUser(Page<AiAppRecord> page, String userId, String aiApplicationId, Integer status);

    /**
     * 根据执行ID查询AI应用记录
     *
     * @param executeId 执行ID
     * @return AI应用记录
     */
    AiAppRecord getByExecuteId(String executeId);
}
