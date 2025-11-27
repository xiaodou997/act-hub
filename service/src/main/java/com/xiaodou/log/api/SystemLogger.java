package com.xiaodou.log.api;


/**
 * 系统日志记录器接口
 */
public interface SystemLogger {

    // ========== 快速日志方法 ==========

    void info(String description);

    void info(String action, String description);

    void warn(String description);

    void warn(String action, String description);

    void error(String description, Throwable throwable);

    void error(String action, String description, Throwable throwable);

    void audit(String action, String description);

    // ========== 构建器模式 ==========

    LogBuilder builder();

    // ========== 耗时记录 ==========

    TimeRecorder startTimer();
}
