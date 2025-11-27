package com.xiaodou.aiapp.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xiaodou.aiapp.HttpClientUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

/**
 * 牛马网站工作流 AI 应用执行器
 * 支持运行工作流和查询任务结果
 *
 * 输入参数示例：
 * {
 *   "action": "run",                           // 必填，支持 run（运行工作流）/ query（查询结果）
 *   "workflowId": "workflow_123",              // run 时必填
 *   "taskId": "task_456",                      // query 时必填
 *   "apiKey": "Bearer xxx",                    // 必填，用于认证
 *   "params": {                                // run 时必填，工作流参数
 *     "key1": "value1",
 *     "key2": "value2"
 *   }
 * }
 *
 * 运行工作流输出示例：
 * {
 *   "taskId": "task_456",
 *   "message": "任务提交成功"
 * }
 *
 * 查询结果输出示例：
 * {
 *   "taskId": "task_456",
 *   "status": 2,
 *   "data": { ... },
 *   "message": "查询成功"
 * }
 *
 * @author xiaodou V=>dddou117
 * @version V1.0
 * @since 2025/11/27
 */
@Slf4j
@Component("workflowHandler")
@RequiredArgsConstructor
public class WorkflowAiApplicationHandler implements AiApplicationHandler {

    private final ObjectMapper objectMapper;

    // 牛马网站 API 基础地址
    private static final String BASE_URL = "https://team-agent.luoxiaodou.cn/ai-team/api";

    @Override
    public Object execute(Map<String, Object> input) throws Exception {
        // 参数校验
        String action = getString(input, "action", true);
        String apiKey = getString(input, "apiKey", true);

        // 根据 action 执行不同的操作
        if ("run".equalsIgnoreCase(action)) {
            return runWorkflow(input, apiKey);
        } else if ("query".equalsIgnoreCase(action)) {
            return queryTaskResult(input, apiKey);
        } else {
            throw new IllegalArgumentException("action 参数必须是 'run' 或 'query'");
        }
    }

    /**
     * 运行工作流
     */
    private Object runWorkflow(Map<String, Object> input, String apiKey)
            throws URISyntaxException, IOException, InterruptedException {
        // 获取必要参数
        String workflowId = getString(input, "workflowId", true);
        Object paramsObj = input.get("params");

        if (paramsObj == null) {
            throw new IllegalArgumentException("缺少必填参数: params");
        }

        // 构建请求 URL
        String url = BASE_URL + "/workflow/v2/run/" + workflowId;

        // 构建请求头
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", apiKey);
        headers.put("X-Client-Type", "fs-filed");
        headers.put("Content-Type", "application/json");

        // 将 params 转换为 JSON 字符串
        String body = objectMapper.writeValueAsString(paramsObj);

        log.info("运行工作流 - workflowId: {}, url: {}", workflowId, url);

        // 发送 POST 请求
        HttpResponseData response = HttpClientUtil.sendPostFull(url, headers, body);

        // 解析响应
        return parseRunWorkflowResponse(response);
    }

    /**
     * 查询任务结果
     */
    private Object queryTaskResult(Map<String, Object> input, String apiKey)
            throws URISyntaxException, IOException, InterruptedException {
        // 获取必要参数
        String taskId = getString(input, "taskId", true);

        // 构建请求 URL
        String url = BASE_URL + "/task-record/result/" + taskId;

        // 构建请求头
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", apiKey);
        headers.put("X-Client-Type", "fs-filed");

        log.info("查询任务结果 - taskId: {}, url: {}", taskId, url);

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
        }

        return result;
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
