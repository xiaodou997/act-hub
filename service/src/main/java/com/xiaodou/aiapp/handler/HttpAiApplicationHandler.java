package com.xiaodou.aiapp.handler;

import com.xiaodou.aiapp.HttpClientUtil;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

/**
 * 基础 HTTP 请求 AI 应用执行器
 * 支持 GET / POST 请求，用于验证 AI 应用执行器机制是否可行。
 *
 * 输入参数示例：
 * {
 * "method": "GET",                     // 必填，支持 GET / POST
 * "url": "https://api.example.com/data",
 * "headers": { "Authorization": "Bearer xxx" }, // 可选
 * "params": { "q": "test" },           // 仅 GET 有效
 * "body": "{ \"key\": \"value\" }"     // 仅 POST 有效
 * }
 *
 * 输出结果示例：
 * {
 * "status": 200,
 * "body": "...",
 * "headers": { ... }
 * }
 *
 * @author xiaodou V=>dddou117
 * @version V1.0
 * @since 2025/10/29
 */
@Component("httpRequestHandler") // Bean 名称需与 AiApplication.handlerBean 一致
public class HttpAiApplicationHandler implements AiApplicationHandler {

    @Override
    public Object execute(Map<String, Object> input) throws Exception {
        // 参数校验
        String method = getString(input, "method", true);
        String url = getString(input, "url", true);

        if (!"GET".equalsIgnoreCase(method) && !"POST".equalsIgnoreCase(method)) {
            throw new IllegalArgumentException("仅支持 GET 或 POST 方法");
        }

        @SuppressWarnings("unchecked") Map<String, String> headers = (Map<String, String>)input.get("headers");
        @SuppressWarnings("unchecked") Map<String, String> params = (Map<String, String>)input.get("params");
        String body = getString(input, "body", false);

        try {
            HttpResponseData response;
            if ("GET".equalsIgnoreCase(method)) {
                response = HttpClientUtil.sendGetFull(url, headers, params);
            } else { // POST
                response = HttpClientUtil.sendPostFull(url, headers, body);
            }

            // 构造标准响应（你也可以根据实际需求调整结构）
            Map<String, Object> result = new HashMap<>();
            result.put("statusCode", response.getStatusCode());
            result.put("body", response.getBody());
            result.put("bodyObject", response.getBodyObject()); // 解析后的对象（如果是JSON）
            result.put("headers", response.getHeaders());

            return result;

        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("URL 格式错误: " + e.getMessage(), e);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("HTTP 请求失败: " + e.getMessage(), e);
        }
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
        return (String)value;
    }
}
