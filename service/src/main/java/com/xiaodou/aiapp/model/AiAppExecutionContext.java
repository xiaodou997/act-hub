package com.xiaodou.aiapp.model;

import com.xiaodou.model.AiApplication;
import lombok.Data;

import java.util.Map;

/**
 * 定义上下文
 *
 * @author xiaodou V=>dddou117
 * @version V1.0
 * @since 2025/10/29
 */
@Data
public class AiAppExecutionContext {

    private String userId;
    private Long appId;
    private Map<String, Object> inputParams;
    private AiApplication aiApplication;
    private Object result; // 执行结果
}
