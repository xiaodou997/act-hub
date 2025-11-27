package com.xiaodou.aiapp;

import com.xiaodou.aiapp.handler.AiApplicationHandler;
import com.xiaodou.aiapp.model.AiAppExecutionContext;
import com.xiaodou.mapper.UserMapper;
import com.xiaodou.model.AiApplication;
import com.xiaodou.service.AiApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 核心执行器
 *
 * @author xiaodou V=>dddou117
 * @version V1.0
 * @since 2025/10/29
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiAppExecutor {
    private final AiApplicationService aiApplicationService;
    private final AiApplicationHandlerFactory handlerFactory;
    private final UserMapper userMapper;
    // protected PointAssignService pointAssignService;

    // 可选：积分服务、权限服务等
    // @Autowired
    // private PointService pointService;

    /**
     * 同步执行 AI 应用（主入口）
     */
    public Object execute(String userId, Long appId, Map<String, Object> inputParams) throws Exception {
        // 1. 获取应用配置
        AiApplication app = aiApplicationService.getById(appId);

        // 2. 权限与积分校验（可扩展为校验链）
        // if (!checkPermission(userId, appId, app)) {
        //     throw new SecurityException("用户无权限执行该 AI 应用");
        // }

        // 3. 冻结积分（预留，可后续实现）
        // freezePoints(userId, app.getCostPoints());

        Map<String, Object> params = JsonSchemaUtils.validateAndApplyDefaults(inputParams, app.getParamSchema());

        // 4. 构建上下文
        AiAppExecutionContext context = new AiAppExecutionContext();
        context.setUserId(userId);
        context.setAppId(appId);
        context.setInputParams(inputParams);
        context.setAiApplication(app);

        // 5. 获取 Handler 并执行
        AiApplicationHandler handler = handlerFactory.getHandler(app.getHandlerBean());
        Object result = handler.execute(params);

        context.setResult(result);
        return result;
    }
}
