package com.xiaodou.utils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

/**
 *
 * @Description: LocalDateTime 时间戳转换工具类
 * @Author: xiaodou V=>dddou117
 * @Date: 2025/5/24
 * @Version: V1.0
 * @JDK: JDK21
 */

public final class DateTimeUtils {

    private DateTimeUtils() {} // 禁止实例化

    // 默认时区（可配置）
    private static final ZoneId DEFAULT_ZONE = ZoneId.systemDefault();

    /**
     * LocalDateTime 转时间戳（毫秒，使用默认时区）
     * @param dateTime 时间对象（为null时返回0）
     */
    public static long toTimestamp(LocalDateTime dateTime) {
        return toTimestamp(dateTime, DEFAULT_ZONE);
    }

    /**
     * LocalDateTime 转时间戳（毫秒，指定时区）
     * @param dateTime 时间对象（为null时返回0）
     * @param zoneId   时区，如 ZoneId.of("Asia/Shanghai")
     */
    public static long toTimestamp(LocalDateTime dateTime, ZoneId zoneId) {
        if (dateTime == null) return 0L;
        return dateTime.atZone(zoneId).toInstant().toEpochMilli();
    }

    /**
     * 时间戳（毫秒）转 LocalDateTime（使用默认时区）
     * @param timestamp 时间戳（毫秒）
     */
    public static LocalDateTime toLocalDateTime(long timestamp) {
        return toLocalDateTime(timestamp, DEFAULT_ZONE);
    }

    /**
     * 时间戳（毫秒）转 LocalDateTime（指定时区）
     * @param timestamp 时间戳（毫秒）
     * @param zoneId    时区，如 ZoneId.of("Asia/Shanghai")
     */
    public static LocalDateTime toLocalDateTime(long timestamp, ZoneId zoneId) {
        return Instant.ofEpochMilli(timestamp).atZone(zoneId).toLocalDateTime();
    }

    // ---------- 常用时区快捷方法 ----------
    /**
     * LocalDateTime 转时间戳（强制东八区）
     */
    public static long toTimestampAtUTC8(LocalDateTime dateTime) {
        return toTimestamp(dateTime, ZoneId.of("Asia/Shanghai"));
    }

    /**
     * 时间戳转 LocalDateTime（强制东八区）
     */
    public static LocalDateTime toLocalDateTimeAtUTC8(long timestamp) {
        return toLocalDateTime(timestamp, ZoneId.of("Asia/Shanghai"));
    }

    /**
     * LocalDateTime 转时间戳（强制UTC）
     */
    public static long toTimestampAtUTC(LocalDateTime dateTime) {
        return toTimestamp(dateTime, ZoneOffset.UTC);
    }

    /**
     * 时间戳转 LocalDateTime（强制UTC）
     */
    public static LocalDateTime toLocalDateTimeAtUTC(long timestamp) {
        return toLocalDateTime(timestamp, ZoneOffset.UTC);
    }
}
