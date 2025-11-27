package com.xiaodou.log.strategy.context;

import com.xiaodou.log.model.LogContext;
import com.xiaodou.service.Ip2RegionService;
import com.xiaodou.utils.IpUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Role;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * HTTP请求上下文解析策略
 *
 * @author xiaodou V=>dddou117
 * @version V1.0
 * @since 2025/10/9
 */
@Component
@Slf4j
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
public class HttpRequestContextStrategy implements LogContextStrategy {

    private final Ip2RegionService ip2RegionService;

    public HttpRequestContextStrategy(Ip2RegionService ip2RegionService) {
        this.ip2RegionService = ip2RegionService;
    }

    @Override
    public LogContext resolveContext() {
        HttpServletRequest request = getCurrentRequest();
        if (request == null) {
            return LogContext.empty();
        }

        return new LogContext(resolveTraceId(request), resolveSpanId(request), resolveIpAddress(request),
            resolveUserAgent(request), resolveHeaders(request));
    }

    private HttpServletRequest getCurrentRequest() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
            return attributes != null ? attributes.getRequest() : null;
        } catch (Exception e) {
            log.debug("获取当前请求失败: {}", e.getMessage());
            return null;
        }
    }

    private String resolveTraceId(HttpServletRequest request) {
        String traceId = request.getHeader("X-Trace-Id");
        if (StringUtils.hasText(traceId)) {
            return traceId;
        }

        traceId = MDC.get("traceId");
        if (StringUtils.hasText(traceId)) {
            return traceId;
        }

        return generateTraceId();
    }

    private String resolveSpanId(HttpServletRequest request) {
        String spanId = request.getHeader("X-Span-Id");
        if (StringUtils.hasText(spanId)) {
            return spanId;
        }

        spanId = MDC.get("spanId");
        if (StringUtils.hasText(spanId)) {
            return spanId;
        }

        return generateSpanId();
    }

    private String resolveIpAddress(HttpServletRequest request) {
        try {

            String ipAddress = IpUtil.getIpAddress(request);
            String location = ip2RegionService.getLocation(ipAddress);
            return ipAddress + "," + location;
        } catch (Exception e) {
            log.debug("解析IP地址失败: {}", e.getMessage());
            return "unknown";
        }
    }

    private String resolveUserAgent(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        return StringUtils.hasText(userAgent) ? userAgent : "unknown";
    }

    private Map<String, String> resolveHeaders(HttpServletRequest request) {
        Map<String, String> headers = new HashMap<>();

        String[] importantHeaders =
            {"X-Forwarded-For", "X-Real-IP", "Referer", "Origin", "Content-Type", "Accept", "Authorization"};

        for (String headerName : importantHeaders) {
            String headerValue = request.getHeader(headerName);
            if (StringUtils.hasText(headerValue)) {
                headers.put(headerName, maskSensitiveHeader(headerName, headerValue));
            }
        }

        return headers;
    }

    private String maskSensitiveHeader(String headerName, String headerValue) {
        if ("Authorization".equalsIgnoreCase(headerName)) {
            if (headerValue.startsWith("Bearer ")) {
                return "Bearer ***";
            } else if (headerValue.startsWith("Basic ")) {
                return "Basic ***";
            }
        }
        return headerValue;
    }

    private String generateTraceId() {
        return UUID.randomUUID()
            .toString()
            .replace("-", "")
            .substring(0, 16);
    }

    private String generateSpanId() {
        return UUID.randomUUID()
            .toString()
            .replace("-", "")
            .substring(0, 8);
    }

    @Override
    public String getStrategyName() {
        return "HTTP_REQUEST";
    }
}
