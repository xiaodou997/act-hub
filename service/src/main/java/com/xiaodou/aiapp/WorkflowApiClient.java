package com.xiaodou.aiapp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xiaodou.aiapp.handler.HttpResponseData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 牛马网站工作流 API 客户端
 * 封装运行工作流和查询结果的公共逻辑
 *
 * @author xiaodou V=>dddou117
 * @version V1.0
 * @since 2025/11/27
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WorkflowApiClient {

    private final ObjectMapper objectMapper;

    // 牛马网站 API 基础地址
    private static final String BASE_URL = "https://team-agent.luoxiaodou.cn/ai-team/api";

    /**
     * 运行工作流
     *
     * @param workflowId 工作流ID
     * @param apiKey API密钥
     * @param params 工作流参数
     * @return 包含 taskId 的响应
     * @throws Exception 执行异常
     */
    public Map<String, Object> runWorkflow(String workflowId, String apiKey, Map<String, Object> params)
            throws Exception {
        // 构建请求 URL
        String url = BASE_URL + "/workflow/v2/run/" + workflowId;

        // 构建请求头
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", apiKey);
        headers.put("X-Client-Type", "fs-filed");
        headers.put("Content-Type", "application/json");

        // 将 params 转换为 JSON 字符串
        String body = objectMapper.writeValueAsString(params);

        log.info("运行工作流 - workflowId: {}, url: {}", workflowId, url);

        // 发送 POST 请求
        HttpResponseData response = HttpClientUtil.sendPostFull(url, headers, body);

        // 解析响应
        return parseRunWorkflowResponse(response);
    }

    /**
     * 查询任务结果
     *
     * @param taskId 任务ID
     * @param apiKey API密钥
     * @return 包含任务状态和结果的响应
     * @throws Exception 执行异常
     */
    public Map<String, Object> queryTaskResult(String taskId, String apiKey)
            throws Exception {
        // 构建请求 URL
        String url = BASE_URL + "/task-record/result/" + taskId;

        // 构建请求头
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", apiKey);
        headers.put("X-Client-Type", "fs-filed");

        log.debug("查询任务结果 - taskId: {}, url: {}", taskId, url);

        // 发送 GET 请求
        HttpResponseData response = HttpClientUtil.sendGetFull(url, headers, null);

        // 解析响应
        return parseQueryTaskResponse(response);
    }

    /**
     * 解析运行工作流的响应
     */
    private Map<String, Object> parseRunWorkflowResponse(HttpResponseData response) {
        Map<String, Object> result = new HashMap<>();
        result.put("statusCode", response.getStatusCode());
        result.put("rawBody", response.getBody());

        // 尝试解析 bodyObject
        Object bodyObject = response.getBodyObject();
        if (bodyObject instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> bodyMap = (Map<String, Object>) bodyObject;

            // 提取 data 字段作为 taskId
            Object dataObj = bodyMap.get("data");
            if (dataObj != null) {
                result.put("taskId", dataObj.toString());
            }

            // 提取 message 字段
            Object messageObj = bodyMap.get("message");
            if (messageObj != null) {
                result.put("message", messageObj.toString());
            } else {
                result.put("message", "任务提交成功");
            }

            // 提取 success 字段
            Object successObj = bodyMap.get("success");
            if (successObj != null) {
                result.put("success", successObj);
            }
        }

        return result;
    }

    /**
     * 解析查询任务结果的响应
     */
    private Map<String, Object> parseQueryTaskResponse(HttpResponseData response) {
        Map<String, Object> result = new HashMap<>();
        result.put("statusCode", response.getStatusCode());
        result.put("rawBody", response.getBody());

        // 尝试解析 bodyObject
        Object bodyObject = response.getBodyObject();
        if (bodyObject instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> bodyMap = (Map<String, Object>) bodyObject;

            // 提取 data 字段
            Object dataObj = bodyMap.get("data");
            if (dataObj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> dataMap = (Map<String, Object>) dataObj;

                // 提取任务信息
                result.put("taskId", dataMap.get("taskId"));
                result.put("status", dataMap.get("status"));
                result.put("data", dataMap.get("data"));
            }

            // 提取 message 字段
            Object messageObj = bodyMap.get("message");
            if (messageObj != null) {
                result.put("message", messageObj.toString());
            } else {
                result.put("message", "查询成功");
            }

            // 提取 success 字段
            Object successObj = bodyMap.get("success");
            if (successObj != null) {
                result.put("success", successObj);
            }
        }

        return result;
    }

    /**
     * 检查任务是否完成
     *
     * @param status 任务状态（1:成功, 2:失败, 3:进行中）
     * @return true表示已完成（成功或失败），false表示进行中
     */
    public boolean isTaskFinished(Integer status) {
        return status != null && (status == 1 || status == 2);
    }

    /**
     * 检查任务是否成功
     *
     * @param status 任务状态
     * @return true表示成功
     */
    public boolean isTaskSuccess(Integer status) {
        return status != null && status == 1;
    }
}
