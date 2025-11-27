package com.xiaodou.log.core;

import com.xiaodou.mapper.SystemLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 系统日志事件监听器
 * 负责异步处理日志入库操作
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SystemLogEventListener {

    private final SystemLogMapper systemLogMapper;

    /**
     * 异步处理日志事件
     * 使用 @TransactionalEventListener 确保在事务提交后执行
     */
    @Async("logTaskExecutor")
    @TransactionalEventListener(
        classes = SystemLogEvent.class,
        phase = TransactionPhase.AFTER_COMMIT,
        fallbackExecution = true // 如果没有事务，也执行
    )
    public void handleSystemLog(SystemLogEvent event) {
        try {
            systemLogMapper.insert(event.getSystemLog());
            log.debug("系统日志异步入库成功，模块：{}，操作：{}",
                event.getSystemLog().getModule(),
                event.getSystemLog().getAction());
        } catch (Exception e) {
            log.error("系统日志入库失败，模块：{}，操作：{}，错误：{}",
                event.getSystemLog().getModule(),
                event.getSystemLog().getAction(),
                e.getMessage(), e);
        }
    }
}