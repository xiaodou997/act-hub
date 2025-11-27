package com.xiaodou.log.core;

import com.xiaodou.model.SystemLog;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 系统日志事件 (System Log Event)
 * <p>
 * 当需要记录系统日志时，由 DefaultSystemLogger 发布此事件。
 * 该事件携带了已经构建好的 SystemLog 实体。
 * 使用事件机制可以实现日志记录与业务逻辑的完全解耦和异步化。
 * </p>
 *
 * @author xiaodou V>dddou117
 * @see SystemLogEventListener
 * @since 1.2
 */
@Getter
public class SystemLogEvent extends ApplicationEvent {

    private final SystemLog systemLog;

    /**
     * 构造一个新的 SystemLogEvent.
     *
     * @param source 事件的来源 (通常是发布事件的对象)
     * @param systemLog 包含所有日志信息的实体对象
     */
    public SystemLogEvent(Object source, SystemLog systemLog) {
        super(source);
        this.systemLog = systemLog;
    }

}