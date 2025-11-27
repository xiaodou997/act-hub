package com.xiaodou.model.dto.tracking;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

/**
 * 在消息队列中传递的埋点事件消息体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    private String eventName;
    private String userId;
    private String tenantId;
    private Map<String, Object> properties;
}
