package com.xiaodou.aiapp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xiaodou.aiapp.handler.HttpResponseData;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

/**
 * 高性能HTTP客户端工具类（单例模式）
 * <p>基于JDK 21内置HTTP Client实现</p>
 * <p>优化特点：</p>
 * <ul>
 *   <li>单例HTTP客户端实例，复用连接池</li>
 *   <li>支持HTTP/2协议</li>
 *   <li>完善的超时和重定向处理</li>
 * </ul>
 *
 * @author luoxiaodou
 * @version 1.3
 * @since JDK 21
 */
public final class HttpClientUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    /**
     * HTTP客户端单例实例
     * <p>配置说明：</p>
     * <ul>
     *   <li>HTTP/2协议优先</li>
     *   <li>10秒连接超时</li>
     *   <li>跟随正常重定向</li>
     * </ul>
     */
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
        .version(HttpClient.Version.HTTP_2)
        .connectTimeout(Duration.ofSeconds(10))
        .followRedirects(HttpClient.Redirect.NORMAL)
        .build();

    /**
     * 私有构造函数防止实例化
     */
    private HttpClientUtil() {
    }

    /**
     * 发送 GET 请求并返回完整响应数据
     */
    public static HttpResponseData sendGetFull(String url, Map<String, String> headers, Map<String, String> params)
        throws URISyntaxException, IOException, InterruptedException {

        return sendRequestFull("GET", url, headers, params, null, false);
    }

    /**
     * 发送POST请求并返回完整响应数据
     */
    public static HttpResponseData sendPostFull(String url, Map<String, String> headers, String body)
        throws URISyntaxException, IOException, InterruptedException {

        return sendRequestFull("POST", url, headers, null, body, true);
    }

    /**
     * 统一的请求处理方法
     */
    private static HttpResponseData sendRequestFull(String method, String url, Map<String, String> headers,
        Map<String, String> params, String body, boolean isJsonBody)
        throws URISyntaxException, IOException, InterruptedException {

        // 处理查询参数（GET请求）
        if (params != null && !params.isEmpty() && "GET".equalsIgnoreCase(method)) {
            StringJoiner joiner = new StringJoiner("&");
            params.forEach((key, value) -> {
                String encodedKey = URLEncoder.encode(key, StandardCharsets.UTF_8);
                String encodedValue = URLEncoder.encode(value, StandardCharsets.UTF_8);
                joiner.add(encodedKey + "=" + encodedValue);
            });
            url += "?" + joiner;
        }

        HttpRequest.Builder builder = HttpRequest.newBuilder()
            .uri(new URI(url));

        // 设置请求方法和body
        switch (method.toUpperCase()) {
            case "GET":
                builder.GET();
                break;
            case "POST":
                HttpRequest.BodyPublisher bodyPublisher =
                    (body != null) ? HttpRequest.BodyPublishers.ofString(body) : HttpRequest.BodyPublishers.noBody();
                builder.POST(bodyPublisher);

                // 如果是POST请求且没有指定Content-Type，默认设置为application/json
                if (isJsonBody && (headers == null || !headers.containsKey("Content-Type"))) {
                    builder.header("Content-Type", "application/json");
                }
                break;
            default:
                throw new IllegalArgumentException("不支持的HTTP方法: " + method);
        }

        addHeaders(builder, headers);
        HttpRequest request = builder.build();

        HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

        // 提取响应头
        Map<String, String> responseHeaders = extractHeaders(response);

        // 尝试解析JSON响应体为对象
        Object bodyObject = tryParseJsonBody(response.body(), responseHeaders);

        return new HttpResponseData(response.statusCode(), response.body(), bodyObject, responseHeaders);
    }

    /**
     * 尝试将响应体解析为JSON对象
     */
    private static Object tryParseJsonBody(String body, Map<String, String> headers) {
        if (body == null || body.trim()
            .isEmpty()) {
            return null;
        }

        // 检查Content-Type是否包含json
        try {
            return objectMapper.readValue(body, Object.class);
        } catch (Exception e) {
            // 解析失败时返回null，不抛出异常
            return null;
        }
    }

    /**
     * 提取响应头
     */
    private static Map<String, String> extractHeaders(HttpResponse<String> response) {
        Map<String, String> responseHeaders = new HashMap<>();
        response.headers()
            .map()
            .forEach((name, values) -> {
                if (!values.isEmpty()) {
                    responseHeaders.put(name, String.join(", ", values));
                }
            });
        return responseHeaders;
    }

    /**
     * 发送GET请求
     *
     * @param url 请求URL（调用方保证不为空）
     * @param headers 请求头Map，可为null
     * @return 响应体字符串
     * @throws URISyntaxException 如果URL格式不正确
     * @throws IOException 如果发生I/O错误
     * @throws InterruptedException 如果请求被中断
     */
    public static String sendGet(String url, Map<String, String> headers)
        throws URISyntaxException, IOException, InterruptedException {
        return sendGet(url, headers, null);
    }

    /**
     * 发送带查询参数的GET请求
     *
     * @param url 请求URL（调用方保证不为空）
     * @param headers 请求头Map，可为null
     * @param params 查询参数Map，可为null
     * @return 响应体字符串
     * @throws URISyntaxException 如果URL格式不正确
     * @throws IOException 如果发生I/O错误
     * @throws InterruptedException 如果请求被中断
     */
    public static String sendGet(String url, Map<String, String> headers, Map<String, String> params)
        throws URISyntaxException, IOException, InterruptedException {

        // 处理查询参数
        if (params != null && !params.isEmpty()) {
            StringJoiner joiner = new StringJoiner("&");
            params.forEach((key, value) -> {
                String encodedKey = URLEncoder.encode(key, StandardCharsets.UTF_8);
                String encodedValue = URLEncoder.encode(value, StandardCharsets.UTF_8);
                joiner.add(encodedKey + "=" + encodedValue);
            });
            url += "?" + joiner;
        }

        HttpRequest.Builder builder = HttpRequest.newBuilder()
            .uri(new URI(url))
            .GET();

        addHeaders(builder, headers);

        HttpRequest request = builder.build();

        HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

        return response.body();
    }

    /**
     * 发送POST请求
     *
     * @param url 请求URL（调用方保证不为空）
     * @param headers 请求头Map，可为null
     * @param body 请求体内容，可为null
     * @return 响应体字符串
     * @throws URISyntaxException 如果URL格式不正确
     * @throws IOException 如果发生I/O错误
     * @throws InterruptedException 如果请求被中断
     */
    public static String sendPost(String url, Map<String, String> headers, String body)
        throws URISyntaxException, IOException, InterruptedException {

        HttpRequest.Builder builder = HttpRequest.newBuilder()
            .uri(new URI(url))
            .POST(body != null ? HttpRequest.BodyPublishers.ofString(body) : HttpRequest.BodyPublishers.noBody())
            .header("Content-Type", "application/json");

        addHeaders(builder, headers);

        HttpRequest request = builder.build();

        HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

        return response.body();
    }

    /**
     * 发送PUT请求
     *
     * @param url 请求URL（调用方保证不为空）
     * @param headers 请求头Map，可为null
     * @param body 请求体内容，可为null
     * @return 响应体字符串
     * @throws URISyntaxException 如果URL格式不正确
     * @throws IOException 如果发生I/O错误
     * @throws InterruptedException 如果请求被中断
     */
    public static String sendPut(String url, Map<String, String> headers, String body)
        throws URISyntaxException, IOException, InterruptedException {

        HttpRequest.Builder builder = HttpRequest.newBuilder()
            .uri(new URI(url))
            .PUT(body != null ? HttpRequest.BodyPublishers.ofString(body) : HttpRequest.BodyPublishers.noBody())
            .header("Content-Type", "application/json");

        addHeaders(builder, headers);

        HttpRequest request = builder.build();

        HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

        return response.body();
    }

    /**
     * 发送DELETE请求
     *
     * @param url 请求URL（调用方保证不为空）
     * @param headers 请求头Map，可为null
     * @return 响应体字符串
     * @throws URISyntaxException 如果URL格式不正确
     * @throws IOException 如果发生I/O错误
     * @throws InterruptedException 如果请求被中断
     */
    public static String sendDelete(String url, Map<String, String> headers)
        throws URISyntaxException, IOException, InterruptedException {

        HttpRequest.Builder builder = HttpRequest.newBuilder()
            .uri(new URI(url))
            .DELETE();

        addHeaders(builder, headers);

        HttpRequest request = builder.build();

        HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

        return response.body();
    }

    /**
     * 添加请求头到请求构建器
     *
     * @param builder 请求构建器
     * @param headers 请求头Map
     */
    private static void addHeaders(HttpRequest.Builder builder, Map<String, String> headers) {
        if (headers != null) {
            headers.forEach((key, value) -> {
                if (key != null && value != null) {
                    builder.header(key, value);
                }
            });
        }
    }

    /**
     * 获取HTTP客户端实例
     *
     * @return 单例HTTP客户端实例
     */
    public static HttpClient getHttpClient() {
        return HTTP_CLIENT;
    }
}
