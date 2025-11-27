package com.xiaodou.aiapp.handler;

import com.xiaodou.aiapp.WorkflowApiClient;
import com.xiaodou.service.AiAppRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 同步工作流执行器（牛马网站专用）
 * <p>
 * 同步模式：执行工作流 → 轮询查询结果 → 返回最终结果 → 保存到数据库
 * </p>
 *
 * 输入参数示例：
 * {
 *   "userId": "user_123",              // 必填，用户ID
 *   "aiApplicationId": "app_456",      // 必填，AI应用ID
 *   "workflowId": "workflow_123",      // 必填，工作流ID
 *   "apiKey": "Bearer xxx",            // 必填，API密钥
 *   "params": {                        // 必填，工作流参数
 *     "key1": "value1",
 *     "key2": "value2"
 *   }
 * }
 *
 * 输出示例：
 * {
 *   "aiAppRecordId": "record_789",      // AI应用记录ID
 *   "taskId": "task_456",              // 牛马网站的任务ID
 *   "status": 1,                       // 最终状态
 *   "data": { ... },                   // 执行结果数据
 *   "message": "执行成功",
 *   "executionTime": 5230,             // 总耗时（毫秒）
 *   "retryCount": 3                    // 实际轮询次数
 * }
 *
 * @author xiaodou V=>dddou117
 * @version V1.0
 * @since 2025/11/27
 */
@Slf4j
@Component("syncWorkflowHandler")
@RequiredArgsConstructor
public class SyncWorkflowHandler implements AiApplicationHandler {

    private final WorkflowApiClient workflowApiClient;
    private final AiAppRecordService aiAppRecordService;

    // 牛马网站工作流配置（固化在代码中）
    private static final int MAX_RETRY_TIMES = 30;        // 最大轮询次数
    private static final long RETRY_INTERVAL_MS = 2000;   // 轮询间隔（毫秒）

    @Override
    public Object execute(Map<String, Object> input) throws Exception {
        long startTime = System.currentTimeMillis();

        // 1. 参数校验
        String userId = getString(input, "userId", true);
        String aiApplicationId = getString(input, "aiApplicationId", true);
        String workflowId = getString(input, "workflowId", true);
        String apiKey = getString(input, "apiKey", true);

        @SuppressWarnings("unchecked")
        Map<String, Object> params = (Map<String, Object>) input.get("params");
        if (params == null) {
            throw new IllegalArgumentException("缺少必填参数: params");
        }

        // 2. 运行工作流
        log.info("同步执行工作流 - workflowId: {}, userId: {}, aiApplicationId: {}",
                workflowId, userId, aiApplicationId);

        Map<String, Object> runResult = workflowApiClient.runWorkflow(workflowId, apiKey, params);
        String taskId = (String) runResult.get("taskId");

        if (taskId == null || taskId.trim().isEmpty()) {
            throw new RuntimeException("工作流启动失败，未返回 taskId: " + runResult.get("message"));
        }

        // 3. 创建AI应用记录（状态：进行中）
        String aiAppRecordId = aiAppRecordService.createAiAppRecord(
                userId, aiApplicationId, "syncWorkflowHandler", input, taskId
        ).getId();

        // 4. 轮询查询结果
        Map<String, Object> finalResult = pollTaskResult(taskId, apiKey);

        // 5. 更新AI应用记录
        long executionTime = System.currentTimeMillis() - startTime;
        Integer status = (Integer) finalResult.get("status");

        if (workflowApiClient.isTaskSuccess(status)) {
            // 成功
            aiAppRecordService.updateAiAppSuccess(aiAppRecordId, finalResult, executionTime);
        } else {
            // 失败或超时
            String errorMessage = (String) finalResult.getOrDefault("message", "任务执行失败");
            aiAppRecordService.updateAiAppFailed(aiAppRecordId, errorMessage, executionTime);
        }

        // 6. 构造返回结果
        Map<String, Object> result = new HashMap<>(finalResult);
        result.put("aiAppRecordId", aiAppRecordId);
        result.put("executionTime", executionTime);

        log.info("同步工作流执行完成 - aiAppRecordId: {}, status: {}, executionTime: {}ms",
                aiAppRecordId, status, executionTime);

        return result;
    }

    /**
     * 轮询查询任务结果
     */
    private Map<String, Object> pollTaskResult(String taskId, String apiKey) throws Exception {
        int retryCount = 0;

        while (retryCount < MAX_RETRY_TIMES) {
            try {
                // 等待一段时间后再查询
                if (retryCount > 0) {
                    Thread.sleep(RETRY_INTERVAL_MS);
                }

                // 查询任务状态
                Map<String, Object> queryResult = workflowApiClient.queryTaskResult(taskId, apiKey);
                Integer status = (Integer) queryResult.get("status");

                retryCount++;
                queryResult.put("retryCount", retryCount);

                // 检查是否完成
                if (workflowApiClient.isTaskFinished(status)) {
                    log.info("任务执行完成 - taskId: {}, status: {}, retryCount: {}", taskId, status, retryCount);
                    return queryResult;
                }

                log.debug("任务进行中 - taskId: {}, status: {}, retryCount: {}/{}",
                        taskId, status, retryCount, MAX_RETRY_TIMES);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("轮询任务被中断", e);
            } catch (Exception e) {
                log.warn("查询任务结果失败 - taskId: {}, retryCount: {}, error: {}",
                        taskId, retryCount, e.getMessage());
                // 如果是最后一次尝试，抛出异常
                if (retryCount >= MAX_RETRY_TIMES) {
                    throw e;
                }
            }
        }

        // 超过最大轮询次数
        Map<String, Object> timeoutResult = new HashMap<>();
        timeoutResult.put("taskId", taskId);
        timeoutResult.put("status", 2); // 失败
        timeoutResult.put("message", "任务执行超时，已达最大轮询次数: " + MAX_RETRY_TIMES);
        timeoutResult.put("retryCount", retryCount);

        return timeoutResult;
    }

    /**
     * 从 input 中安全获取字符串字段
     */
    private String getString(Map<String, Object> input, String key, boolean required) {
        Object value = input.get(key);
        if (value == null) {
            if (required) {
                throw new IllegalArgumentException("缺少必填参数: " + key);
            }
            return null;
        }
        if (!(value instanceof String)) {
            throw new IllegalArgumentException("参数 '" + key + "' 必须是字符串类型");
        }
        return (String) value;
    }
}
