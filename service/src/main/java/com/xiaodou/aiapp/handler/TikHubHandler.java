package com.xiaodou.aiapp.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xiaodou.aiapp.HttpClientUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author xiaodou V=>dddou117
 * @version V1.0
 * @since 2025/11/5
 */
@Slf4j
@Component("tikHubHandler")
@RequiredArgsConstructor
public class TikHubHandler implements AiApplicationHandler {
    private final RedisTemplate<String, Object> redisTemplate;

    // 需要注入ObjectMapper
    private final ObjectMapper objectMapper;

    // 缓存过期时间，根据业务需求调整
    private static final long CACHE_EXPIRE_HOURS = 12; // 12小时

    @Override
    public Object execute(Map<String, Object> input) throws Exception {
        // 生成缓存key
        String cacheKey = generateCacheKey(input);

        // 先尝试从缓存获取
        Object cachedResult = redisTemplate.opsForValue()
            .get(cacheKey);
        if (cachedResult != null) {
            log.info("get cache result from cache key: {}", cacheKey);
            return cachedResult;
        }

        // 缓存未命中，执行原有逻辑
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

            Map<String, Object> result = new HashMap<>();
            result.put("statusCode", response.getStatusCode());
            result.put("body", response.getBody());
            result.put("bodyObject", response.getBodyObject());
            result.put("headers", response.getHeaders());

            // 将结果存入缓存
            redisTemplate.opsForValue()
                .set(cacheKey, result, CACHE_EXPIRE_HOURS, TimeUnit.HOURS);

            return result;

        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("URL 格式错误: " + e.getMessage(), e);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("HTTP 请求失败: " + e.getMessage(), e);
        }
    }

    /**
     * 生成缓存key - 核心逻辑
     */
    private String generateCacheKey(Map<String, Object> input) {
        try {
            // 方法1: 使用JSON序列化后计算MD5
            String jsonString = objectMapper.writeValueAsString(input);
            String md5 = DigestUtils.md5DigestAsHex(jsonString.getBytes());
            return "tikHub:request:" + md5;

        } catch (Exception e) {
            // 方法2: 手动构建key（备选方案）
            return buildManualCacheKey(input);
        }
    }

    /**
     * 备选方案：手动构建缓存key
     */
    private String buildManualCacheKey(Map<String, Object> input) {
        StringBuilder keyBuilder = new StringBuilder("tikHub:request:");

        // 添加基本参数
        keyBuilder.append(getString(input, "method", true))
            .append(":");
        keyBuilder.append(getString(input, "url", true))
            .append(":");

        // 处理headers
        Map<String, String> headers = (Map<String, String>)input.get("headers");
        if (headers != null) {
            headers.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> keyBuilder.append(entry.getKey())
                    .append("=")
                    .append(entry.getValue())
                    .append(";"));
        }
        keyBuilder.append(":");

        // 处理params
        Map<String, String> params = (Map<String, String>)input.get("params");
        if (params != null) {
            params.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> keyBuilder.append(entry.getKey())
                    .append("=")
                    .append(entry.getValue())
                    .append(";"));
        }
        keyBuilder.append(":");

        // 处理body
        String body = getString(input, "body", false);
        if (body != null) {
            keyBuilder.append(body);
        }

        // 计算hash避免key过长
        String keyString = keyBuilder.toString();
        return "tikHub:request:" + DigestUtils.md5DigestAsHex(keyString.getBytes());
    }

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