package com.xiaodou.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xiaodou.mapper.AiAppRecordMapper;
import com.xiaodou.model.AiAppRecord;
import com.xiaodou.service.AiAppRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * <p>
 * AI应用任务执行记录表 服务实现类
 * </p>
 *
 * @author luoxiaodou
 * @since 2025-11-27
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiAppRecordServiceImpl extends ServiceImpl<AiAppRecordMapper, AiAppRecord>
        implements AiAppRecordService {

    private final ObjectMapper objectMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AiAppRecord createAiAppRecord(String userId, String aiApplicationId, String handlerBean,
                                         Map<String, Object> inputParams, String executeId) {
        AiAppRecord record = new AiAppRecord();
        record.setUserId(userId);
        record.setAiApplicationId(aiApplicationId);
        record.setHandlerBean(handlerBean);
        record.setExecuteId(executeId);
        record.setStatus(3); // 3-进行中

        try {
            // 将输入参数转换为JSON字符串
            String inputParamsJson = objectMapper.writeValueAsString(inputParams);
            record.setInputParams(inputParamsJson);

            // TODO: 这里可以实现参数脱敏逻辑，生成 displayInputParams
            record.setDisplayInputParams(inputParamsJson);

        } catch (Exception e) {
            log.error("序列化输入参数失败", e);
            record.setInputParams("{}");
            record.setDisplayInputParams("{}");
        }

        this.save(record);
        log.info("创建AI应用记录成功 - aiAppRecordId: {}, aiApplicationId: {}, handlerBean: {}",
                record.getId(), aiApplicationId, handlerBean);
        return record;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAiAppSuccess(String aiAppRecordId, Map<String, Object> outputResult, Long executionTime) {
        AiAppRecord record = new AiAppRecord();
        record.setId(aiAppRecordId);
        record.setStatus(1); // 1-成功
        record.setExecutionTime(executionTime);

        try {
            // 将输出结果转换为JSON字符串
            String outputResultJson = objectMapper.writeValueAsString(outputResult);
            record.setOutputResult(outputResultJson);
        } catch (Exception e) {
            log.error("序列化输出结果失败", e);
            record.setOutputResult("{}");
        }

        record.setUpdatedAt(LocalDateTime.now());
        this.updateById(record);
        log.info("更新AI应用记录为成功状态 - aiAppRecordId: {}, executionTime: {}ms", aiAppRecordId, executionTime);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAiAppFailed(String aiAppRecordId, String errorMessage, Long executionTime) {
        AiAppRecord record = new AiAppRecord();
        record.setId(aiAppRecordId);
        record.setStatus(2); // 2-失败
        record.setErrorMessage(errorMessage);
        record.setExecutionTime(executionTime);
        record.setUpdatedAt(LocalDateTime.now());

        this.updateById(record);
        log.warn("更新AI应用记录为失败状态 - aiAppRecordId: {}, error: {}", aiAppRecordId, errorMessage);
    }

    @Override
    public IPage<AiAppRecord> pageByUser(Page<AiAppRecord> page, String userId, String aiApplicationId, Integer status) {
        LambdaQueryWrapper<AiAppRecord> query = new LambdaQueryWrapper<>();

        if (userId != null && !userId.trim().isEmpty()) {
            query.eq(AiAppRecord::getUserId, userId.trim());
        }

        if (aiApplicationId != null && !aiApplicationId.trim().isEmpty()) {
            query.eq(AiAppRecord::getAiApplicationId, aiApplicationId.trim());
        }

        if (status != null) {
            query.eq(AiAppRecord::getStatus, status);
        }

        query.orderByDesc(AiAppRecord::getCreatedAt);

        return this.page(page, query);
    }

    @Override
    public AiAppRecord getByExecuteId(String executeId) {
        if (executeId == null || executeId.trim().isEmpty()) {
            return null;
        }

        LambdaQueryWrapper<AiAppRecord> query = new LambdaQueryWrapper<>();
        query.eq(AiAppRecord::getExecuteId, executeId.trim());
        query.last("LIMIT 1");

        return this.getOne(query);
    }
}
