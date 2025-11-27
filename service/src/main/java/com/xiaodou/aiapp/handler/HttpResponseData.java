package com.xiaodou.aiapp.handler;

import lombok.Getter;

import java.util.Map;

/**
 * HTTP 响应数据封装类
 *
 * @author luoxiaodou
 * @author xiaodou V=>dddou117
 * @version V1.0
 * @since 2025/10/29
 */
@Getter
public class HttpResponseData {
    private final int statusCode;
    private final String body;
    private final Object bodyObject;
    private final Map<String, String> headers;

    public HttpResponseData(int statusCode, String body, Map<String, String> headers) {
        this.statusCode = statusCode;
        this.body = body;
        this.bodyObject = null;
        this.headers = headers;
    }

    public HttpResponseData(int statusCode, String body, Object bodyObject, Map<String, String> headers) {
        this.statusCode = statusCode;
        this.body = body;
        this.bodyObject = bodyObject;
        this.headers = headers;
    }
}