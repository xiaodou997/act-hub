package com.xiaodou.utils;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.StringUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * IP 地址工具类
 * <p>
 * 提供获取客户端IP、格式化IP地址、获取网关IP等功能
 * </p>
 *
 * @author 作者名
 * @version 1.0
 * @since 2023-11-20
 */
@Slf4j
public class IpUtil {
    /** 代理相关HTTP头集合 */
    private static final Set<String> PROXY_HEADERS = Set.of(
        "X-Forwarded-For", "Proxy-Client-IP", "WL-Proxy-Client-IP",
        "HTTP_CLIENT_IP", "HTTP_X_FORWARDED_FOR", "X-Real-IP"
    );

    /** 本地IP信息记录类 */
    private record LocalIpInfo(String ip, Instant timestamp) {}

    /** 缓存的本地IP信息 */
    private static volatile LocalIpInfo cachedLocalIp = null;

    /**
     * 从HttpServletRequest中提取客户端IP地址
     * <p>
     * 按照以下顺序尝试获取IP地址：
     * 1. x-forwarded-for 头
     * 2. Proxy-Client-IP 头
     * 3. WL-Proxy-Client-IP 头
     * 4. 远程地址
     * 如果是本地地址(127.0.0.1)，则尝试获取本机实际IP
     * </p>
     *
     * @param request HttpServletRequest对象
     * @return 客户端IP地址，获取失败时返回空字符串
     */
    public static String getIpAddress(HttpServletRequest request) {
        try {
            String ip = Stream.of("x-forwarded-for", "Proxy-Client-IP", "WL-Proxy-Client-IP")
                .map(request::getHeader)
                .filter(IpUtil::isValidIp)
                .findFirst()
                .orElseGet(() -> {
                    String remoteAddr = request.getRemoteAddr();
                    return "127.0.0.1".equals(remoteAddr) ? getLocalIp() : remoteAddr;
                });

            int commaIndex = ip.indexOf(',');
            return commaIndex > 0 ? ip.substring(0, commaIndex).trim() : ip;
        } catch (Exception e) {
            log.error("Error extracting IP address", e);
            return "";
        }
    }

    /**
     * 从ServerHttpRequest中提取客户端IP地址
     * <p>
     * 优先从代理头中获取，如果没有则获取远程地址
     * </p>
     *
     * @param request ServerHttpRequest对象
     * @return 客户端IP地址，获取失败时返回空字符串
     */
    public static String getClientIp(ServerHttpRequest request) {
        return Optional.of(getFirstNonUnknownHeader(request.getHeaders()))
            .orElseGet(() -> Optional.ofNullable(request.getRemoteAddress())
                .map(remoteAddress -> remoteAddress.getAddress().getHostAddress())
                .orElse(""));
    }

    /**
     * 格式化IP地址
     * <p>
     * 去除IP中的点(.)和冒号(:)，IPv4会去掉点，IPv6会将冒号替换为横杠
     * </p>
     *
     * @param ip 原始IP地址
     * @return 格式化后的IP地址
     */
    public static String formatIpAddress(String ip) {
        return ip.replace(".", "")
            .replace(":", "-");
    }

    /**
     * 获取网关IP地址
     * <p>
     * 从请求头中提取网关IP地址，主要用于反向代理场景
     * </p>
     *
     * @param request ServerHttpRequest对象
     * @return 网关IP地址，获取失败时返回空字符串
     */
    public static String getGatewayIpAddress(ServerHttpRequest request) {
        return Optional.of(getFirstNonUnknownHeader(request.getHeaders()))
            .orElseGet(() -> Optional.ofNullable(request.getRemoteAddress())
                .map(remoteAddress -> remoteAddress.getAddress().getHostAddress())
                .orElse(""));
    }

    /**
     * 从请求中获取User-Agent
     *
     * @param request HttpServletRequest对象
     * @return User-Agent字符串，可能为null
     */
    public static String getUserAgent(HttpServletRequest request) {
        return request.getHeader("User-Agent");
    }

    /**
     * 获取第一个非unknown的header值
     *
     * @param headers HttpHeaders对象
     * @return 第一个有效的header值，如果没有则返回null
     */
    private static String getFirstNonUnknownHeader(HttpHeaders headers) {
        return Objects.requireNonNull(IpUtil.PROXY_HEADERS.stream()
            .map(headers::getFirst)
            .filter(Objects::nonNull)
            .filter(IpUtil::isValidIp)
            .findFirst()
            .orElse(null));
    }

    /**
     * 验证IP地址是否有效
     * <p>
     * 有效的IP地址应满足：
     * 1. 非空
     * 2. 不是"unknown"(不区分大小写)
     * </p>
     *
     * @param ip 要验证的IP地址
     * @return 如果有效返回true，否则返回false
     */
    private static boolean isValidIp(String ip) {
        return StringUtils.hasText(ip) && !"unknown".equalsIgnoreCase(ip);
    }

    /**
     * 获取本地IP地址
     * <p>
     * 会缓存获取到的IP地址，默认10分钟刷新一次
     * 如果获取失败，返回127.0.0.1
     * </p>
     *
     * @return 本地IP地址
     */
    private static String getLocalIp() {
        if (cachedLocalIp == null || Duration.between(cachedLocalIp.timestamp(), Instant.now()).toMinutes() > 10) {
            try {
                String ip = InetAddress.getLocalHost().getHostAddress();
                cachedLocalIp = new LocalIpInfo(ip, Instant.now());
            } catch (UnknownHostException e) {
                log.error("Failed to get localhost IP address", e);
                return "127.0.0.1";
            }
        }
        return cachedLocalIp.ip();
    }
}