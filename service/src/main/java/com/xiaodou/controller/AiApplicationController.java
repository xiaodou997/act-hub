package com.xiaodou.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiaodou.aiapp.AiAppExecutor;
import com.xiaodou.auth.util.UserContextHolder;
import com.xiaodou.model.AiApplication;
import com.xiaodou.model.AiApplicationUpdateDTO;
import com.xiaodou.model.vo.AiApplicationVO;
import com.xiaodou.result.Result;
import com.xiaodou.service.AiApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * <p>
 * AI 应用配置主表 前端控制器
 * </p>
 *
 * @author luoxiaodou
 * @since 2025-11-17
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/ai-application")
public class AiApplicationController {
    private final AiApplicationService aiApplicationService;
    private final AiAppExecutor aiAppExecutor;

    /**
     * 分页查询AI应用列表（带筛选条件）
     *
     * @param pageNum  页码，默认为1
     * @param pageSize 每页大小，默认为10
     * @param name     应用名称筛选条件，可选
     * @param typeId   应用类型ID筛选条件，可选
     * @param enabled  启用状态筛选条件，可选
     * @return 分页查询结果，包含AI应用信息列表 {@link IPage<AiApplicationVO>}
     */
    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<IPage<AiApplicationVO>> list(@RequestParam(defaultValue = "1") Integer pageNum,
        @RequestParam(defaultValue = "10") Integer pageSize, @RequestParam(required = false) String name,
        @RequestParam(required = false) String typeId, @RequestParam(required = false) Byte enabled) {
        log.info("page list pageNum:{},pageSize:{},name:{},category:{},enabled:{}", pageNum, pageSize, name, typeId, enabled);
        Page<AiApplication> page = aiApplicationService.pageApplications(pageNum, pageSize, name, typeId, enabled);
        IPage<AiApplicationVO> voPage = page.convert(AiApplicationVO::fromEntity);
        return Result.success(voPage);
    }

    /**
     * 查询AI应用详情
     *
     * @param id AI应用ID
     * @return AI应用详情信息 {@link AiApplicationVO}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<AiApplicationVO> detail(@PathVariable Long id) {
        AiApplication app = aiApplicationService.getById(id);
        if (app == null) {
            log.error("id {} not found", id);
            return Result.success(null);
        }
        return Result.success(AiApplicationVO.fromEntity(app));
    }

    /**
     * 创建AI应用
     *
     * @param app AI应用创建信息 {@link AiApplication}
     * @return 创建结果，包含创建的AI应用信息 {@link AiApplicationVO}
     */
    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<AiApplicationVO> create(@RequestBody AiApplication app) {
        log.info("create app:{}", app);
        app.setHandlerBean("syncWorkflowHandler");
        aiApplicationService.save(app);
        return Result.success(AiApplicationVO.fromEntity(app));
    }

    /**
     * 更新AI应用信息
     *
     * @param id  AI应用ID
     * @param app AI应用更新信息 {@link AiApplicationUpdateDTO}
     * @return 更新结果
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<Void> update(@PathVariable Long id, @RequestBody AiApplicationUpdateDTO app) {
        log.info("update app:{}", app);
        AiApplication application = aiApplicationService.getById(id);
        if (application == null) {
            log.error("update id {} not found", id);
            return Result.success(null);
        }

        application.setName(app.getName());
        application.setDescription(app.getDescription());
        // 当前默认是同步执行器
        application.setHandlerBean("syncWorkflowHandler");
        application.setParamSchema(app.getParamSchema());
        application.setPrice(app.getPrice());
        application.setEnabled(app.getEnabled());
        application.setTypeId(app.getTypeId());
        application.setTimeoutMs(app.getTimeoutMs());

        aiApplicationService.updateById(application);
        return Result.success();
    }

    /**
     * 删除AI应用
     *
     * @param id AI应用ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        log.info("delete app:{}", id);
        aiApplicationService.removeById(id);
        return Result.success();
    }

    /**
     * 执行AI应用
     *
     * @param appId  AI应用ID
     * @param params AI应用执行参数，以Map形式传递
     * @return 执行结果
     */
    @PostMapping({"/v1/run/{appId}"})
    public Result<Object> runAiApp(@PathVariable Long appId, @RequestBody Map<String, Object> params) {
        try {
            String userId = UserContextHolder.getUserId(); // 从上下文获取

            AiApplication application = aiApplicationService.getById(appId);
            if (application == null) {
                log.error("run app id {} not found", appId);
                return Result.fail("app not found");
            }

            Object result = aiAppExecutor.execute(userId, appId, params);

            return Result.success(result);
        } catch (Exception e) {
            log.error("执行 AI 应用失败，appId={}, userId={}", appId, UserContextHolder.getUserId(), e);
            return Result.fail("执行失败: " + e.getMessage());
        }
    }
}
