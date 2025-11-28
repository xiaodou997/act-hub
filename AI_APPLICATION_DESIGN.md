# AI 应用管理功能 - 设计方案

## 一、需求概述

AI 应用管理模块用于管理 AI 应用的配置、执行和监控，包括：

- **AI-APP-01**: AI 应用 CRUD 管理
- **AI-APP-02**: 应用配置管理
- **AI-APP-03**: 应用执行控制
- **AI-APP-04**: 应用监控统计

## 二、数据库设计

### 1. ai_application - AI 应用配置主表

| 字段          | 类型          | 说明                    |
| ------------- | ------------- | ----------------------- |
| id            | bigint        | 主键 ID（自增）         |
| name          | varchar(200)  | 应用名称                |
| description   | text          | 应用描述                |
| type_id       | varchar(32)   | 应用类型 ID             |
| handler_bean  | varchar(100)  | 执行器 Bean 名称        |
| param_schema  | text          | 输入参数 JSON Schema    |
| result_schema | text          | 输出结果 JSON Schema    |
| price         | decimal(10,2) | 使用价格/积分           |
| enabled       | tinyint       | 启用状态：1-启用,0-禁用 |
| timeout_ms    | bigint        | 超时时间（毫秒）        |
| created_at    | datetime      | 创建时间                |
| updated_at    | datetime      | 更新时间                |

## 三、接口设计

### AI-APP-01: AI 应用管理接口

#### AiApplicationController

| 接口                   | 方法   | 说明                 |
| ---------------------- | ------ | -------------------- |
| `/ai-application`      | GET    | 分页查询 AI 应用列表 |
| `/ai-application/{id}` | GET    | 获取 AI 应用详情     |
| `/ai-application`      | POST   | 创建 AI 应用         |
| `/ai-application/{id}` | PUT    | 更新 AI 应用         |
| `/ai-application/{id}` | DELETE | 删除 AI 应用         |

### AI-APP-02: 应用配置接口

| 接口                          | 方法 | 说明                |
| ----------------------------- | ---- | ------------------- |
| `/ai-application/{id}/schema` | GET  | 获取应用参数 Schema |
| `/ai-application/{id}/config` | PUT  | 更新应用配置        |
| `/ai-application/{id}/test`   | POST | 测试应用配置        |

### AI-APP-03: 应用执行接口

| 接口                            | 方法 | 说明             |
| ------------------------------- | ---- | ---------------- |
| `/ai-application/v1/run/{id}`   | POST | 执行 AI 应用     |
| `/ai-application/{id}/validate` | POST | 验证输入参数     |
| `/ai-application/{id}/timeout`  | PUT  | 设置执行超时时间 |

### AI-APP-04: 统计监控接口

| 接口                         | 方法 | 说明             |
| ---------------------------- | ---- | ---------------- |
| `/ai-application/statistics` | GET  | 获取应用统计信息 |
| `/ai-application/{id}/usage` | GET  | 获取应用使用情况 |

## 四、后端代码实现

### 涉及文件清单

#### Controller 层

- `AiApplicationController.java` - AI 应用管理控制器

#### Service 层

- `AiApplicationService.java` - AI 应用服务接口

#### 实现类

- `AiApplicationServiceImpl.java` - AI 应用服务实现

#### 数据传输对象

- `AiApplicationUpdateDTO.java` - AI 应用更新 DTO

#### 视图对象

- `AiApplicationVO.java` - AI 应用视图对象

#### 实体类

- `AiApplication.java` - AI 应用实体

#### Mapper 接口

- `AiApplicationMapper.java` - AI 应用数据访问接口

### 核心代码示例

#### AiApplicationController 关键方法

```java
@RestController
@RequestMapping("/ai-application")
public class AiApplicationController {

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<IPage<AiApplicationVO>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String typeId,
            @RequestParam(required = false) Byte enabled) {

        Page<AiApplication> page = aiApplicationService.pageApplications(pageNum, pageSize, name, typeId, enabled);
        IPage<AiApplicationVO> voPage = page.convert(AiApplicationVO::fromEntity);
        return Result.success(voPage);
    }

    @PostMapping({"/v1/run/{appId}"})
    public Result<Object> runAiApp(@PathVariable Long appId, @RequestBody Map<String, Object> params) {
        try {
            String userId = UserContextHolder.getUserId();
            AiApplication application = aiApplicationService.getById(appId);
            if (application == null || application.getEnabled() != 1) {
                return Result.fail("应用不存在或已禁用");
            }
            Object result = aiAppExecutor.execute(userId, appId, params);
            return Result.success(result);
        } catch (Exception e) {
            return Result.fail("执行失败: " + e.getMessage());
        }
    }
}
```

### 执行流程图

```mermaid
flowchart TD
  U[用户] --> L[列表查询 /ai-application]
  U --> D[查看详情 /ai-application/{id}]
  D --> S[获取参数Schema /{id}/schema]
  U --> V[参数验证 /{id}/validate]
  V -->|通过| R[执行应用 /v1/run/{id}]
  R --> H[选择执行器 handlerBean]
  H -->|SyncWorkflowHandler/HttpAiApplicationHandler/TikHubHandler| X[执行]
  X --> RR[记录服务 AiAppRecordService]
  RR --> DB[(ai_app_record)]
  X --> OUT[返回结果]
```

#### AiApplicationService 关键方法

```java
public interface AiApplicationService extends IService<AiApplication> {

    Page<AiApplication> pageApplications(Integer current, Integer size, String name, String typeId, Byte enabled);

    Map<String, Object> getStatistics();

    List<AiApplication> getApplicationsByTypeId(String typeId);
}
```

```java
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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * AI应用配置主表 前端控制器
 *
 * @author luoxiaodou
 * @since 2025-11-17
 */
@Slf4j
@RestController
@RequestMapping("/ai-application")
@RequiredArgsConstructor
@Tag(name = "AI应用管理", description = "AI应用管理相关接口")
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
    @Operation(summary = "分页查询AI应用列表")
    public Result<IPage<AiApplicationVO>> list(
        @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
        @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer pageSize,
        @Parameter(description = "应用名称") @RequestParam(required = false) String name,
        @Parameter(description = "应用类型ID") @RequestParam(required = false) String typeId,
        @Parameter(description = "启用状态") @RequestParam(required = false) Byte enabled) {

        log.info("分页查询AI应用列表 - pageNum:{}, pageSize:{}, name:{}, typeId:{}, enabled:{}",
                 pageNum, pageSize, name, typeId, enabled);

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
    @Operation(summary = "获取AI应用详情")
    public Result<AiApplicationVO> detail(@PathVariable Long id) {
        log.info("获取AI应用详情 - id: {}", id);
        AiApplication app = aiApplicationService.getById(id);
        if (app == null) {
            log.error("AI应用不存在 - id: {}", id);
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
    @Operation(summary = "创建AI应用")
    public Result<AiApplicationVO> create(@RequestBody AiApplication app) {
        log.info("创建AI应用 - app: {}", app);

        // 设置默认处理器
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
    @Operation(summary = "更新AI应用")
    public Result<Void> update(@PathVariable Long id, @RequestBody AiApplicationUpdateDTO app) {
        log.info("更新AI应用 - id: {}, app: {}", id, app);

        AiApplication application = aiApplicationService.getById(id);
        if (application == null) {
            log.error("更新的AI应用不存在 - id: {}", id);
            return Result.success(null);
        }

        // 更新字段
        application.setName(app.getName());
        application.setDescription(app.getDescription());
        application.setHandlerBean("syncWorkflowHandler"); // 当前默认是同步执行器
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
    @Operation(summary = "删除AI应用")
    public Result<Void> delete(@PathVariable Long id) {
        log.info("删除AI应用 - id: {}", id);
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
    @Operation(summary = "执行AI应用")
    public Result<Object> runAiApp(@PathVariable Long appId, @RequestBody Map<String, Object> params) {
        try {
            String userId = UserContextHolder.getUserId(); // 从上下文获取

            AiApplication application = aiApplicationService.getById(appId);
            if (application == null) {
                log.error("执行AI应用失败，应用不存在 - appId: {}", appId);
                return Result.fail("应用不存在");
            }

            if (application.getEnabled() != 1) {
                log.warn("执行AI应用失败，应用已禁用 - appId: {}", appId);
                return Result.fail("该应用已禁用");
            }

            Object result = aiAppExecutor.execute(userId, appId, params);
            return Result.success(result);

        } catch (Exception e) {
            log.error("执行AI应用失败 - appId: {}, userId: {}", appId, UserContextHolder.getUserId(), e);
            return Result.fail("执行失败: " + e.getMessage());
        }
    }

    /**
     * 获取应用参数Schema
     */
    @GetMapping("/{id}/schema")
    @Operation(summary = "获取应用参数Schema")
    public Result<Object> getSchema(@PathVariable Long id) {
        log.info("获取应用参数Schema - id: {}", id);
        AiApplication app = aiApplicationService.getById(id);
        if (app == null) {
            return Result.fail("应用不存在");
        }
        return Result.success(app.getParamSchema());
    }

    /**
     * 验证输入参数
     */
    @PostMapping("/{id}/validate")
    @Operation(summary = "验证输入参数")
    public Result<String> validateParams(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        log.info("验证输入参数 - id: {}, params: {}", id, params);
        try {
            AiApplication app = aiApplicationService.getById(id);
            if (app == null) {
                return Result.fail("应用不存在");
            }

            // 使用JSON Schema验证参数
            JsonSchemaUtils.validate(params, app.getParamSchema());
            return Result.success("参数验证通过");
        } catch (Exception e) {
            return Result.fail("参数验证失败: " + e.getMessage());
        }
    }

    /**
     * 获取应用统计信息
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取应用统计信息")
    public Result<Map<String, Object>> getStatistics() {
        log.info("获取应用统计信息");
        Map<String, Object> statistics = aiApplicationService.getStatistics();
        return Result.success(statistics);
    }
}
```

### 2. AiApplicationService.java

```java
package com.xiaodou.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaodou.model.AiApplication;

import java.util.Map;

/**
 * AI应用配置主表 服务接口
 *
 * @author luoxiaodou
 * @since 2025-11-17
 */
public interface AiApplicationService extends IService<AiApplication> {

    /**
     * 分页查询AI应用
     *
     * @param current  当前页
     * @param size     每页大小
     * @param name     应用名称（模糊查询）
     * @param typeId   应用类型ID
     * @param enabled  启用状态
     * @return 分页结果
     */
    Page<AiApplication> pageApplications(Integer current, Integer size, String name, String typeId, Byte enabled);

    /**
     * 获取应用统计信息
     *
     * @return 统计信息
     */
    Map<String, Object> getStatistics();

    /**
     * 根据类型ID获取应用列表
     *
     * @param typeId 应用类型ID
     * @return 应用列表
     */
    java.util.List<AiApplication> getApplicationsByTypeId(String typeId);
}
```

### 3. AiApplicationServiceImpl.java

```java
package com.xiaodou.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiaodou.mapper.AiApplicationMapper;
import com.xiaodou.model.AiApplication;
import com.xiaodou.service.AiApplicationService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI应用配置主表 服务实现类
 *
 * @author luoxiaodou
 * @since 2025-11-17
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiApplicationServiceImpl extends ServiceImpl<AiApplicationMapper, AiApplication>
    implements AiApplicationService {

    @Override
    public Page<AiApplication> pageApplications(Integer current, Integer size, String name, String typeId,
        Byte enabled) {
        LambdaQueryWrapper<AiApplication> query = new LambdaQueryWrapper<>();

        if (name != null && !name.trim().isEmpty()) {
            query.like(AiApplication::getName, name.trim());
        }
        if (typeId != null && !typeId.trim().isEmpty()) {
            query.eq(AiApplication::getTypeId, typeId.trim());
        }
        if (enabled != null) {
            query.eq(AiApplication::getEnabled, enabled);
        }

        query.orderByDesc(AiApplication::getUpdatedAt);

        return this.page(new Page<>(current, size), query);
    }

    @Override
    public Map<String, Object> getStatistics() {
        Map<String, Object> statistics = new HashMap<>();

        // 总应用数量
        Long totalCount = count();
        statistics.put("totalCount", totalCount);

        // 启用数量
        Long enabledCount = count(new LambdaQueryWrapper<AiApplication>()
            .eq(AiApplication::getEnabled, 1));
        statistics.put("enabledCount", enabledCount);

        // 禁用数量
        Long disabledCount = count(new LambdaQueryWrapper<AiApplication>()
            .eq(AiApplication::getEnabled, 0));
        statistics.put("disabledCount", disabledCount);

        return statistics;
    }

    @Override
    public List<AiApplication> getApplicationsByTypeId(String typeId) {
        LambdaQueryWrapper<AiApplication> query = new LambdaQueryWrapper<>();
        query.eq(AiApplication::getTypeId, typeId)
            .eq(AiApplication::getEnabled, 1)
            .orderByDesc(AiApplication::getUpdatedAt);
        return list(query);
    }
}
```

## 五、数据传输对象

### 1. AiApplication.java

```java
package com.xiaodou.model;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * AI 应用配置主表
 * </p>
 *
 * @author luoxiaodou
 * @since 2025-11-17
 */
@Getter
@Setter
@ToString
@TableName("ai_application")
public class AiApplication implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 应用名称
     */
    private String name;

    /**
     * 应用描述
     */
    private String description;

    /**
     * 应用类型ID
     */
    private String typeId;

    /**
     * 执行器Bean名称
     */
    private String handlerBean;

    /**
     * 输入参数JSON Schema
     */
    private String paramSchema;

    /**
     * 输出结果JSON Schema
     */
    private String resultSchema;

    /**
     * 使用价格/积分
     */
    private java.math.BigDecimal price;

    /**
     * 启用状态：1-启用，0-禁用
     */
    private Byte enabled;

    /**
     * 超时时间（毫秒）
     */
    private Long timeoutMs;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
```

### 2. AiApplicationVO.java

```java
package com.xiaodou.model.vo;

import com.xiaodou.model.AiApplication;
import com.xiaodou.utils.DateTimeUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.util.Map;

/**
 * AI应用视图对象
 *
 * @author luoxiaodou
 * @since 2025-11-17
 */
@Data
@Schema(description = "AI应用响应")
public class AiApplicationVO {

    /**
     * 应用ID
     */
    @Schema(description = "应用ID")
    private Long id;

    /**
     * 应用名称
     */
    @Schema(description = "应用名称")
    private String name;

    /**
     * 应用描述
     */
    @Schema(description = "应用描述")
    private String description;

    /**
     * 应用类型ID
     */
    @Schema(description = "应用类型ID")
    private String typeId;

    /**
     * 执行器Bean名称
     */
    @Schema(description = "执行器Bean名称")
    private String handlerBean;

    /**
     * 输入参数JSON Schema
     */
    @Schema(description = "输入参数JSON Schema")
    private Map<String, Object> paramSchema;

    /**
     * 输出结果JSON Schema
     */
    @Schema(description = "输出结果JSON Schema")
    private Map<String, Object> resultSchema;

    /**
     * 使用价格/积分
     */
    @Schema(description = "使用价格/积分")
    private java.math.BigDecimal price;

    /**
     * 启用状态：1-启用，0-禁用
     */
    @Schema(description = "启用状态：1-启用，0-禁用")
    private Byte enabled;

    /**
     * 超时时间（毫秒）
     */
    @Schema(description = "超时时间（毫秒）")
    private Long timeoutMs;

    /**
     * 创建时间（毫秒时间戳）
     */
    @Schema(description = "创建时间")
    private Long createdAt;

    /**
     * 更新时间（毫秒时间戳）
     */
    @Schema(description = "更新时间")
    private Long updatedAt;

    /**
     * 从实体对象转换
     */
    public static AiApplicationVO fromEntity(AiApplication application) {
        if (application == null) {
            return null;
        }
        AiApplicationVO vo = new AiApplicationVO();
        BeanUtils.copyProperties(application, vo);
        vo.setCreatedAt(DateTimeUtils.toTimestampAtUTC8(application.getCreatedAt()));
        vo.setUpdatedAt(DateTimeUtils.toTimestampAtUTC8(application.getUpdatedAt()));

        // 解析JSON Schema
        try {
            if (application.getParamSchema() != null) {
                vo.setParamSchema(com.fasterxml.jackson.databind.ObjectMapper().readTree(application.getParamSchema()));
            }
            if (application.getResultSchema() != null) {
                vo.setResultSchema(com.fasterxml.jackson.databind.ObjectMapper().readTree(application.getResultSchema()));
            }
        } catch (Exception e) {
            log.warn("解析JSON Schema失败", e);
        }

        return vo;
    }
}
```

## 六、前端页面设计

### 页面清单

| 页面        | 路由                | 组件名                  | 说明           |
| ----------- | ------------------- | ----------------------- | -------------- |
| AI 应用管理 | /ai-app/application | AiApplicationManagement | 应用列表、管理 |

### AI 应用管理页面效果

```
┌─────────────────────────────────────────────────────────────────────────────┐
│ AI应用管理                                          [+ 新建应用]            │
├─────────────────────────────────────────────────────────────────────────────┤
│ 应用名称: [__________]  类型: [全部 ▼]  状态: [全部 ▼]  [搜索] [重置]        │
├─────────────────────────────────────────────────────────────────────────────┤
│ 统计信息：总计 12 个应用 | 启用 10 个 | 禁用 2 个                              │
├─────────────────────────────────────────────────────────────────────────────┤
│ # │ 应用名称      │ 类型    │ 状态  │ 价格  │ 超时   │ 操作                │
│───┼───────────────┼─────────┼───────┼───────┼────────┼─────────────────────│
│ 1 │ 文本摘要      │ 文本处理│ 启用  │ 5积分  │ 30秒   │ 编辑 删除 测试 运行  │
│ 2 │ 图像识别      │ 图像处理│ 启用  │ 10积分 │ 60秒   │ 编辑 删除 测试 运行  │
│ 3 │ 数据分析      │ 数据分析│ 禁用  │ 8积分  │ 45秒   │ 编辑 删除 测试       │
│ 4 │ 语音转文字    │ 语音处理│ 启用  │ 6积分  │ 90秒   │ 编辑 删除 测试 运行  │
├─────────────────────────────────────────────────────────────────────────────┤
│                              [1] [2] [3] ... [10]    共 12 条               │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│ 新建AI应用                                                   [×]               │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  基本信息                                                                   │
│  ├─ 应用名称 *    [文本摘要应用_________________________________]        │
│  ├─ 应用类型 *    [文本处理 ▼]                                        │
│  ├─ 应用描述      [自动生成文本摘要的AI应用                      ]      │
│  └─ 执行器        [syncWorkflowHandler ▼]                           │
│                                                                             │
│  配置信息                                                                   │
│  ├─ 参数Schema    [JSON编辑器...]                                   │
│  ├─ 结果Schema    [JSON编辑器...]                                   │
│  ├─ 使用价格      [5] 积分                                          │
│  ├─ 超时时间      [30] 秒                                           │
│  └─ 启用状态      ○ 禁用  ● 启用                                      │
│                                                                             │
│                                           [取消]  [保存]  [保存并测试]        │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│ 测试AI应用 - "文本摘要"                                     [×]               │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  测试参数                                                                   │
│  ├─ 输入文本 *    [这是一段需要摘要的文本内容...]                       │
│  ├─ 摘要长度      [200] 字符                                            │
│  └─ 语言类型      [中文 ▼]                                             │
│                                                                             │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                         测试结果                                   │   │
│  │                                                                     │   │
│  │  [测试结果显示区域]                                                   │   │
│  │                                                                     │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                                                             │
│                                               [取消]  [运行测试]            │
└─────────────────────────────────────────────────────────────────────────────┘
```

## 七、核心特性

### 1. 应用配置

- **Schema 定义**：支持 JSON Schema 定义输入输出参数
- **多执行器**：支持不同类型的执行器（同步、异步、HTTP 等）
- **参数验证**：自动根据 Schema 验证输入参数
- **超时控制**：支持设置应用执行超时时间

### 2. 应用执行

- **统一执行入口**：通过 AiAppExecutor 统一管理应用执行
- **参数预处理**：支持参数转换和默认值设置
- **执行监控**：记录执行时间和结果
- **异常处理**：统一的异常处理和错误返回

### 3. 权限控制

- **操作权限**：不同角色有不同的操作权限
- **使用权限**：控制用户对应用的访问权限

## 八、菜单配置

| 菜单名称    | 路由地址            | 组件键名                | 权限标识      | 图标  |
| ----------- | ------------------- | ----------------------- | ------------- | ----- |
| AI 应用管理 | /ai-app/application | AiApplicationManagement | ai-app:view   | Robot |
| 新建应用    | /ai-app/create      | AiApplicationForm       | ai-app:create | Plus  |
| 应用详情    | /ai-app/:id         | AiApplicationDetail     | ai-app:view   | -     |

## 九、开发计划

### 第一阶段：基础功能（预计 2 天）

- [ ] AI 应用 CRUD 接口实现
- [ ] 前端列表页面开发
- [ ] 应用配置表单开发
- [ ] JSON Schema 编辑器集成

### 第二阶段：执行功能（预计 1 天）

- [ ] 应用执行接口实现
- [ ] 参数验证功能
- [ ] 测试功能开发

### 第三阶段：高级功能（预计 1 天）

- [ ] 统计监控功能
- [ ] 应用分类管理
- [ ] 权限控制完善

## 十、待确认问题

1. **执行器扩展**：是否需要支持插件式的执行器扩展？ 答：不需要
2. **版本管理**：应用是否需要支持版本控制？ 答：不需要
3. **应用市场**：是否需要开发应用市场和分享功能？ 答：不需要
4. **计费模式**：是否需要支持复杂的计费策略？ 答：不需要
