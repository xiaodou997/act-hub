package com.xiaodou.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiaodou.auth.util.UserContextHolder;
import com.xiaodou.exception.AppException;
import com.xiaodou.model.AiAppType;
import com.xiaodou.model.dto.aitype.AiAppTypeCreateDTO;
import com.xiaodou.model.dto.aitype.AiAppTypeUpdateDTO;
import com.xiaodou.model.vo.AiAppTypeVO;
import com.xiaodou.result.Result;
import com.xiaodou.service.AiAppTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * AI应用类型管理控制器
 * 提供AI应用类型的创建、更新、删除、查询等操作
 *
 * @author luoxiaodou
 * @since 2025-11-17
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/ai-app-type")
public class AiAppTypeController {

    /**
     * AI应用类型服务
     */
    private final AiAppTypeService aiAppTypeService;

    /**
     * 创建AI应用类型
     *
     * @param dto AI应用类型创建信息 {@link AiAppTypeCreateDTO}
     * @return 操作结果，成功返回空字符串，失败返回错误信息
     */
    @PostMapping
    @Operation(summary = "创建智能体类型")
    public Result<String> create(@RequestBody @Validated AiAppTypeCreateDTO dto) {
        String userId = UserContextHolder.getUserId();
        log.info("user:{}, start create agentType : {}", userId, dto);

        AiAppType workflowType = new AiAppType();
        workflowType.setName(dto.getName());
        workflowType.setDescription(dto.getDescription());
        workflowType.setStatus(dto.getStatus());

        aiAppTypeService.save(workflowType);
        log.info("create agentType success");
        return Result.success();
    }

    /**
     * 更新AI应用类型
     *
     * @param dto AI应用类型更新信息 {@link AiAppTypeUpdateDTO}
     * @return 操作结果，成功返回空字符串，失败返回错误信息
     */
    @PutMapping
    @Operation(summary = "更新智能体类型")
    public Result<String> update(@RequestBody @Validated AiAppTypeUpdateDTO dto) {
        String userId = UserContextHolder.getUserId();
        log.info("user:{}, start update agentType : {}", userId, dto);
        AiAppType workflowType = new AiAppType();
        workflowType.setId(dto.getId());
        workflowType.setName(dto.getName());
        workflowType.setDescription(dto.getDescription());
        workflowType.setStatus(dto.getStatus());
        aiAppTypeService.updateById(workflowType);
        log.info("update agentType success");
        return Result.success();
    }

    /**
     * 删除AI应用类型
     *
     * @param id AI应用类型ID
     * @return 操作结果，成功返回空字符串，失败返回错误信息
     */
    @PreAuthorize("hasAnyRole('TEAM_ADMIN','SUPER_ADMIN')")
    @DeleteMapping("/{id}")
    @Operation(summary = "删除智能体类型")
    public Result<String> delete(@Parameter(description = "类型ID", required = true) @PathVariable String id) {
        log.info("start delete agentType : {}, userId: {}", id, UserContextHolder.getUserId());
        try {
            aiAppTypeService.safeDelete(id);
            return Result.success();
        } catch (AppException e) {
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 获取AI应用类型详情
     *
     * @param id AI应用类型ID
     * @return AI应用类型详情信息 {@link AiAppTypeVO}
     */
    @PreAuthorize("hasAnyRole('TEAM_ADMIN','SUPER_ADMIN')")
    @GetMapping("/{id}")
    @Operation(summary = "获取智能体类型详情")
    public Result<AiAppTypeVO> getById(@Parameter(description = "类型ID", required = true) @PathVariable String id) {
        log.info("start get agentType : {}", id);
        try {
            return Result.success(AiAppTypeVO.fromEntity(aiAppTypeService.getById(id)));
        } catch (Exception e) {
            log.error("get agentType error", e);
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 分页查询AI应用类型列表
     *
     * @param pageNum  页码，默认为1
     * @param pageSize 每页数量，默认为10
     * @param name     类型名称筛选条件，可选
     * @param status   状态筛选条件，可选
     * @return 分页查询结果，包含AI应用类型信息列表 {@link IPage<AiAppTypeVO>}
     */
    // @PreAuthorize("hasAnyRole('TEAM_ADMIN','SUPER_ADMIN')")
    @GetMapping("/page")
    @Operation(summary = "分页查询智能体类型")
    public Result<IPage<AiAppTypeVO>> pageList(
        @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") int pageNum,
        @Parameter(description = "每页数量", example = "10") @RequestParam(defaultValue = "10") int pageSize,
        @Parameter(description = "类型名称") @RequestParam(required = false) String name,
        @Parameter(description = "状态") @RequestParam(required = false) Byte status) {
        Page<AiAppType> page = new Page<>(pageNum, pageSize);
        String tenantId = UserContextHolder.getTenantId();
        log.info("start page list : {}, team id : {}", page, tenantId);
        // 调用Service获取分页数据
        IPage<AiAppTypeVO> agentTypePage = aiAppTypeService.pageList(page, name, tenantId, status);
        return Result.success(agentTypePage);
    }
}
