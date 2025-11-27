package com.xiaodou.log.api;

/**
 * 时间记录器接口
 *
 * @author xiaodou V=>dddou117
 * @version V1.0
 * @since 2025/10/9
 */

/**
 * 时间记录器接口
 */
public interface TimeRecorder {

    void record(String action, String description);

    void record(String description);

    void recordError(String action, String description, Throwable throwable);

    void recordError(String description, Throwable throwable);
}
