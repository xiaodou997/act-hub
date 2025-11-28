# AI 应用执行记录功能 - 设计方案

## 一、需求概述

AI 应用执行记录模块用于管理和监控 AI 应用的执行情况，包括：

- **RECORD-01**: 执行记录查询管理
- **RECORD-02**: 执行状态跟踪
- **RECORD-03**: 执行结果存储
- **RECORD-04**: 用户执行历史

## 二、数据库设计

### 1. ai_app_record - AI 应用执行记录表

| 字段                 | 类型         | 说明                                 |
| -------------------- | ------------ | ------------------------------------ |
| id                   | varchar(32)  | 记录唯一 ID                          |
| user_id              | varchar(32)  | 发起任务的用户 ID                    |
| ai_application_id    | varchar(32)  | AI 应用 ID（关联 ai_application 表） |
| credit_cost          | int          | 本次消耗积分                         |
| execute_id           | varchar(100) | 异步执行的事件 ID                    |
| status               | int          | 状态：1 成功 2 失败 3 进行中         |
| input_params         | text         | 输入参数 JSON 字符串                 |
| display_input_params | text         | 输入参数 JSON 字符串（展示给用户）   |
| output_result        | text         | 执行结果 JSON 字符串                 |
| error_message        | text         | 执行失败时的错误信息                 |
| execution_time       | bigint       | 任务执行消耗的时间，单位是毫秒       |
| handler_bean         | varchar(100) | 执行器 Bean 名称                     |
| is_deleted           | int          | 软删除标记(0-未删除,1-已删除)        |
| delete_time          | datetime     | 删除时间                             |
| created_at           | datetime     | 记录创建时间                         |
| updated_at           | datetime     | 记录更新时间                         |

## 三、接口设计

### RECORD-01: 执行记录查询接口

#### AiAppRecordController

| 接口                                 | 方法 | 说明                       |
| ------------------------------------ | ---- | -------------------------- |
| `/ai-app-record/page`                | GET  | 分页查询执行记录（管理员） |
| `/ai-app-record/my`                  | GET  | 查询当前用户的执行记录     |
| `/ai-app-record/{id}`                | GET  | 获取执行记录详情           |
| `/ai-app-record/execute/{executeId}` | GET  | 根据执行 ID 查询记录       |
| `/ai-app-record/export`              | GET  | 导出执行记录               |

### RECORD-02: 状态跟踪接口

| 接口                         | 方法 | 说明             |
| ---------------------------- | ---- | ---------------- |
| `/ai-app-record/{id}/status` | GET  | 获取记录状态     |
| `/ai-app-record/{id}/retry`  | POST | 重试失败的执行   |
| `/ai-app-record/{id}/cancel` | POST | 取消进行中的执行 |

### RECORD-03: 批量操作接口

| 接口                          | 方法   | 说明                 |
| ----------------------------- | ------ | -------------------- |
| `/ai-app-record/batch/delete` | DELETE | 批量删除记录         |
| `/ai-app-record/batch/cancel` | POST   | 批量取消进行中的任务 |
| `/ai-app-record/cleanup`      | DELETE | 清理过期记录         |

## 四、后端代码实现

### 涉及文件清单

#### Controller 层

- `AiAppRecordController.java` - AI 应用记录控制器

#### Service 层

- `AiAppRecordService.java` - AI 应用记录服务接口

#### 实现类

- `AiAppRecordServiceImpl.java` - AI 应用记录服务实现

#### 视图对象

- `AiAppRecordVO.java` - AI 应用记录视图对象

#### 实体类

- `AiAppRecord.java` - AI 应用记录实体

#### Mapper 接口

- `AiAppRecordMapper.java` - AI 应用记录数据访问接口

### 核心代码示例

#### AiAppRecordController 关键方法

```java
@RestController
@RequestMapping("/ai-app-record")
public class AiAppRecordController {

    @GetMapping("/my")
    public Result<IPage<AiAppRecordVO>> myRecords(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String aiApplicationId,
            @RequestParam(required = false) Integer status) {

        String currentUserId = UserContextHolder.getUserId();
        Page<AiAppRecord> page = new Page<>(pageNum, pageSize);
        IPage<AiAppRecord> result = aiAppRecordService.pageByUser(page, currentUserId, aiApplicationId, status);
        IPage<AiAppRecordVO> voPage = result.convert(AiAppRecordVO::fromEntity);
        return Result.success(voPage);
    }

    @GetMapping("/execute/{executeId}")
    public Result<AiAppRecordVO> getByExecuteId(@PathVariable String executeId) {
        AiAppRecord aiAppRecord = aiAppRecordService.getByExecuteId(executeId);
        // 权限控制逻辑
        String currentUserId = UserContextHolder.getUserId();
        boolean isSuperAdmin = UserContextHolder.isSuperAdmin();
        if (!isSuperAdmin && !currentUserId.equals(aiAppRecord.getUserId())) {
            throw new AppException(ResultCodeEnum.FORBIDDEN, "无权查看该AI应用记录");
        }
        return Result.success(AiAppRecordVO.fromEntity(aiAppRecord));
    }
}
```

### 执行流程图

```mermaid
flowchart TD
  EX[执行AI应用] --> CR[创建记录 createAiAppRecord]
  CR --> ST[状态=进行中]
  EX -->|成功| SU[updateAiAppSuccess]
  EX -->|失败| FA[updateAiAppFailed]
  SU --> ST1[状态=成功]
  FA --> ST2[状态=失败]
  U[用户] --> MY[我的记录 /ai-app-record/my]
  A[管理员] --> PG[分页查询 /ai-app-record/page]
  U --> EID[按执行ID查询 /execute/{executeId}]
  A --> CLN[清理过期 /cleanup]
  A --> BDEL[批量删除 /batch/delete]
```

#### AiAppRecordService 关键方法

```java
public interface AiAppRecordService extends IService<AiAppRecord> {

    AiAppRecord createAiAppRecord(String userId, String aiApplicationId, String handlerBean,
                                  Map<String, Object> inputParams, String executeId);

    void updateAiAppSuccess(String aiAppRecordId, Map<String, Object> outputResult, Long executionTime);

    void updateAiAppFailed(String aiAppRecordId, String errorMessage, Long executionTime);

    IPage<AiAppRecord> pageByUser(Page<AiAppRecord> page, String userId, String aiApplicationId, Integer status);

    AiAppRecord getByExecuteId(String executeId);

    void retryExecution(String id);

    void cancelExecution(String id);

    void batchDelete(List<String> ids);

    int cleanupExpiredRecords(Integer days);
}
```

### 2. AiAppRecordService.java

```java
package com.xiaodou.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaodou.model.AiAppRecord;

import java.util.List;
import java.util.Map;

/**
 * AI应用任务执行记录表 服务类
 *
 * @author luoxiaodou
 * @since 2025-11-27
 */
public interface AiAppRecordService extends IService<AiAppRecord> {

    /**
     * 创建AI应用记录（状态为进行中）
     *
     * @param userId           用户ID
     * @param aiApplicationId  AI应用ID
     * @param handlerBean      执行器Bean名称
     * @param inputParams      输入参数
     * @param executeId        异步执行ID（可选）
     * @return AI应用记录
     */
    AiAppRecord createAiAppRecord(String userId, String aiApplicationId, String handlerBean,
                                  Map<String, Object> inputParams, String executeId);

    /**
     * 更新AI应用记录为成功状态
     *
     * @param aiAppRecordId    AI应用记录ID
     * @param outputResult     输出结果
     * @param executionTime    执行时间（毫秒）
     */
    void updateAiAppSuccess(String aiAppRecordId, Map<String, Object> outputResult, Long executionTime);

    /**
     * 更新AI应用记录为失败状态
     *
     * @param aiAppRecordId    AI应用记录ID
     * @param errorMessage     错误信息
     * @param executionTime    执行时间（毫秒）
     */
    void updateAiAppFailed(String aiAppRecordId, String errorMessage, Long executionTime);

    /**
     * 分页查询用户的AI应用记录
     *
     * @param page             分页对象
     * @param userId           用户ID
     * @param aiApplicationId  AI应用ID（可选）
     * @param status           状态（可选）
     * @return 分页结果
     */
    IPage<AiAppRecord> pageByUser(Page<AiAppRecord> page, String userId, String aiApplicationId, Integer status);

    /**
     * 分页查询AI应用记录（管理员）
     *
     * @param page             分页对象
     * @param userId           用户ID（可选）
     * @param aiApplicationId  AI应用ID（可选）
     * @param status           状态（可选）
     * @return 分页结果
     */
    IPage<AiAppRecord> pageByCondition(Page<AiAppRecord> page, String userId, String aiApplicationId, Integer status);

    /**
     * 根据执行ID查询AI应用记录
     *
     * @param executeId        执行ID
     * @return AI应用记录
     */
    AiAppRecord getByExecuteId(String executeId);

    /**
     * 重试失败的执行
     *
     * @param id               记录ID
     */
    void retryExecution(String id);

    /**
     * 取消进行中的执行
     *
     * @param id               记录ID
     */
    void cancelExecution(String id);

    /**
     * 批量删除记录
     *
     * @param ids              记录ID列表
     */
    void batchDelete(List<String> ids);

    /**
     * 清理过期记录
     *
     * @param days             保留天数
     * @return 清理的记录数量
     */
    int cleanupExpiredRecords(Integer days);
}
```

### 3. AiAppRecordServiceImpl.java

```java
package com.xiaodou.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xiaodou.exception.AppException;
import com.xiaodou.mapper.AiAppRecordMapper;
import com.xiaodou.model.AiAppRecord;
import com.xiaodou.service.AiAppRecordService;
import com.xiaodou.service.AiApplicationService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * AI应用任务执行记录表 服务实现类
 *
 * @author luoxiaodou
 * @since 2025-11-27
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiAppRecordServiceImpl extends ServiceImpl<AiAppRecordMapper, AiAppRecord>
        implements AiAppRecordService {

    private final ObjectMapper objectMapper;
    private final AiApplicationService aiApplicationService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AiAppRecord createAiAppRecord(String userId, String aiApplicationId, String handlerBean,
                                         Map<String, Object> inputParams, String executeId) {
        AiAppRecord record = new AiAppRecord();
        record.setUserId(userId);
        record.setAiApplicationId(aiApplicationId);
        record.setHandlerBean(handlerBean);
        record.setExecuteId(executeId);
        record.setStatus(3); // 3-进行中

        try {
            // 将输入参数转换为JSON字符串
            String inputParamsJson = objectMapper.writeValueAsString(inputParams);
            record.setInputParams(inputParamsJson);

            // TODO: 这里可以实现参数脱敏逻辑，生成 displayInputParams
            record.setDisplayInputParams(inputParamsJson);

        } catch (Exception e) {
            log.error("序列化输入参数失败", e);
            record.setInputParams("{}");
            record.setDisplayInputParams("{}");
        }

        this.save(record);
        log.info("创建AI应用记录成功 - aiAppRecordId: {}, aiApplicationId: {}, handlerBean: {}",
                record.getId(), aiApplicationId, handlerBean);
        return record;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAiAppSuccess(String aiAppRecordId, Map<String, Object> outputResult, Long executionTime) {
        AiAppRecord record = new AiAppRecord();
        record.setId(aiAppRecordId);
        record.setStatus(1); // 1-成功
        record.setExecutionTime(executionTime);

        try {
            // 将输出结果转换为JSON字符串
            String outputResultJson = objectMapper.writeValueAsString(outputResult);
            record.setOutputResult(outputResultJson);
        } catch (Exception e) {
            log.error("序列化输出结果失败", e);
            record.setOutputResult("{}");
        }

        record.setUpdatedAt(LocalDateTime.now());
        this.updateById(record);
        log.info("更新AI应用记录为成功状态 - aiAppRecordId: {}, executionTime: {}ms", aiAppRecordId, executionTime);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAiAppFailed(String aiAppRecordId, String errorMessage, Long executionTime) {
        AiAppRecord record = new AiAppRecord();
        record.setId(aiAppRecordId);
        record.setStatus(2); // 2-失败
        record.setErrorMessage(errorMessage);
        record.setExecutionTime(executionTime);
        record.setUpdatedAt(LocalDateTime.now());

        this.updateById(record);
        log.warn("更新AI应用记录为失败状态 - aiAppRecordId: {}, error: {}", aiAppRecordId, errorMessage);
    }

    @Override
    public IPage<AiAppRecord> pageByUser(Page<AiAppRecord> page, String userId, String aiApplicationId, Integer status) {
        LambdaQueryWrapper<AiAppRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiAppRecord::getUserId, userId);

        if (aiApplicationId != null && !aiApplicationId.trim().isEmpty()) {
            wrapper.eq(AiAppRecord::getAiApplicationId, aiApplicationId);
        }
        if (status != null) {
            wrapper.eq(AiAppRecord::getStatus, status);
        }

        wrapper.orderByDesc(AiAppRecord::getCreatedAt);
        return this.page(page, wrapper);
    }

    @Override
    public IPage<AiAppRecord> pageByCondition(Page<AiAppRecord> page, String userId, String aiApplicationId, Integer status) {
        LambdaQueryWrapper<AiAppRecord> wrapper = new LambdaQueryWrapper<>();

        if (userId != null && !userId.trim().isEmpty()) {
            wrapper.eq(AiAppRecord::getUserId, userId);
        }
        if (aiApplicationId != null && !aiApplicationId.trim().isEmpty()) {
            wrapper.eq(AiAppRecord::getAiApplicationId, aiApplicationId);
        }
        if (status != null) {
            wrapper.eq(AiAppRecord::getStatus, status);
        }

        wrapper.orderByDesc(AiAppRecord::getCreatedAt);
        return this.page(page, wrapper);
    }

    @Override
    public AiAppRecord getByExecuteId(String executeId) {
        LambdaQueryWrapper<AiAppRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiAppRecord::getExecuteId, executeId);
        return this.getOne(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void retryExecution(String id) {
        AiAppRecord record = getById(id);
        if (record == null) {
            throw new AppException("记录不存在");
        }

        if (record.getStatus() != 2) {
            throw new AppException("只能重试失败的执行");
        }

        // TODO: 实现重试逻辑
        log.info("重试执行记录 - id: {}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelExecution(String id) {
        AiAppRecord record = getById(id);
        if (record == null) {
            throw new AppException("记录不存在");
        }

        if (record.getStatus() != 3) {
            throw new AppException("只能取消进行中的执行");
        }

        // TODO: 实现取消逻辑
        log.info("取消执行记录 - id: {}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<String> ids) {
        LambdaQueryWrapper<AiAppRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(AiAppRecord::getId, ids);
        this.remove(wrapper);
        log.info("批量删除AI应用记录 - count: {}", ids.size());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int cleanupExpiredRecords(Integer days) {
        LocalDateTime cutoffTime = LocalDateTime.now().minusDays(days);
        LambdaQueryWrapper<AiAppRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.lt(AiAppRecord::getCreatedAt, cutoffTime);

        int count = this.count(wrapper);
        this.remove(wrapper);
        log.info("清理过期AI应用记录 - count: {}, days: {}", count, days);
        return count;
    }
}
```

## 五、数据传输对象

### 1. AiAppRecordVO.java

```java
package com.xiaodou.model.vo;

import com.xiaodou.model.AiAppRecord;
import com.xiaodou.utils.DateTimeUtils;
import lombok.Data;

/**
 * AI应用执行记录视图对象 (View Object)
 * <p>
 * 用于向前端返回AI应用执行记录，时间字段转换为毫秒时间戳。
 * </p>
 *
 * @author xiaodou
 * @since 2025-11-27
 */
@Data
public class AiAppRecordVO {

    /**
     * AI应用记录唯一ID
     */
    private String id;

    /**
     * 发起AI应用的用户ID
     */
    private String userId;

    /**
     * AI应用ID
     */
    private String aiApplicationId;

    /**
     * 本次消耗积分
     */
    private Integer creditCost;

    /**
     * 异步执行的事件 ID
     */
    private String executeId;

    /**
     * 状态：1成功 2失败 3进行中
     */
    private Integer status;

    /**
     * 输入参数JSON字符串（展示给用户的）
     */
    private String displayInputParams;

    /**
     * 执行结果JSON字符串
     */
    private String outputResult;

    /**
     * 执行失败时的错误信息
     */
    private String errorMessage;

    /**
     * AI应用执行消耗的时间，单位是毫秒
     */
    private Long executionTime;

    /**
     * 执行器Bean名称
     */
    private String handlerBean;

    /**
     * 创建时间（毫秒时间戳）
     */
    private Long createdAt;

    /**
     * 更新时间（毫秒时间戳）
     */
    private Long updatedAt;

    /**
     * 从实体对象转换
     */
    public static AiAppRecordVO fromEntity(AiAppRecord record) {
        AiAppRecordVO vo = new AiAppRecordVO();
        vo.setId(record.getId());
        vo.setUserId(record.getUserId());
        vo.setAiApplicationId(record.getAiApplicationId());
        vo.setCreditCost(record.getCreditCost());
        vo.setExecuteId(record.getExecuteId());
        vo.setStatus(record.getStatus());
        vo.setDisplayInputParams(record.getDisplayInputParams());
        vo.setOutputResult(record.getOutputResult());
        vo.setErrorMessage(record.getErrorMessage());
        vo.setExecutionTime(record.getExecutionTime());
        vo.setHandlerBean(record.getHandlerBean());
        vo.setCreatedAt(DateTimeUtils.toTimestamp(record.getCreatedAt()));
        vo.setUpdatedAt(DateTimeUtils.toTimestamp(record.getUpdatedAt()));
        return vo;
    }
}
```

## 六、前端页面设计

### 页面清单

| 页面     | 路由              | 组件名                | 说明           |
| -------- | ----------------- | --------------------- | -------------- |
| 执行记录 | /ai-app-record    | AiAppRecordManagement | 记录列表、管理 |
| 我的记录 | /ai-app-record/my | MyAiAppRecordList     | 用户个人记录   |

### AI 应用执行记录页面效果

```
┌─────────────────────────────────────────────────────────────────────────────┐
│ AI应用执行记录                                      [批量删除] [清理过期]    │
├─────────────────────────────────────────────────────────────────────────────┤
│ 用户ID: [__________]  应用ID: [__________]  状态: [全部 ▼]  [搜索] [重置]    │
├─────────────────────────────────────────────────────────────────────────────┤
│ # │ 用户ID   │ 应用ID   │ 状态  │ 执行时间 │ 消耗积分 │ 创建时间  │ 操作    │
│───┼──────────┼─────────┼───────┼──────────┼──────────┼───────────┼─────────│
│ 1 │ user001  │ app001   │ 成功  │ 2.3s     │ 5        │ 14:30:25  │ 详情    │
│ 2 │ user002  │ app002   │ 失败  │ 30.0s    │ 0        │ 14:25:10  │ 详情 重试│
│ 3 │ user003  │ app001   │ 进行中│ -        │ -        │ 14:35:00  │ 详情 取消│
│ 4 │ user001  │ app003   │ 成功  │ 1.8s     │ 8        │ 14:20:15  │ 详情    │
├─────────────────────────────────────────────────────────────────────────────┤
│                              [1] [2] [3] ... [10]    共 156 条              │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│ 执行记录详情 - "文本摘要应用"                                 [×]           │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  基本信息                                                                   │
│  ├─ 记录ID        : rec_123456789                                        │
│  ├─ 用户ID        : user001                                              │
│  ├─ 应用ID        : app001                                               │
│  ├─ 执行器        : syncWorkflowHandler                                  │
│  ├─ 状态          : 成功 (状态码: 1)                                     │
│  ├─ 执行时间      : 2.3 秒                                              │
│  ├─ 消耗积分      : 5 积分                                               │
│  ├─ 创建时间      : 2025-01-01 14:30:25                                  │
│  └─ 更新时间      : 2025-01-01 14:30:27                                  │
│                                                                             │
│  输入参数                                                                   │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │ {                                                                   │   │
│  │   "text": "这是一段需要摘要的文本内容...",                           │   │
│  │   "maxLength": 200,                                                 │   │
│  │   "language": "zh-CN"                                               │   │
│  │ }                                                                   │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                                                             │
│  执行结果                                                                   │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │ {                                                                   │   │
│  │   "summary": "这是文本摘要结果...",                                 │   │
│  │   "originalLength": 156,                                           │   │
│  │   "summaryLength": 45                                               │   │
│  │ }                                                                   │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                                                             │
│                                                     [关闭] [重试] [删除] │
└─────────────────────────────────────────────────────────────────────────────┘
```

## 七、核心特性

### 1. 记录管理

- **完整生命周期**：记录从创建到完成的全过程
- **状态跟踪**：支持进行中、成功、失败三种状态
- **参数存储**：保存输入参数和输出结果
- **错误处理**：记录详细的错误信息

### 2. 权限控制

- **用户隔离**：用户只能查看自己的记录
- **管理员权限**：管理员可以查看所有用户记录
- **操作控制**：不同状态下的操作权限控制

### 3. 数据管理

- **软删除**：支持记录的软删除和恢复
- **批量操作**：支持批量删除、清理等操作
- **数据导出**：支持记录数据的导出功能

## 八、菜单配置

| 菜单名称    | 路由地址          | 组件键名              | 权限标识      | 图标  |
| ----------- | ----------------- | --------------------- | ------------- | ----- |
| AI 应用管理 | /ai-app           | -                     | -             | Robot |
| 执行记录    | /ai-app/record    | AiAppRecordManagement | ai-app:record | List  |
| 我的记录    | /ai-app/record/my | MyAiAppRecordList     | -             | User  |

## 九、开发计划

### 第一阶段：基础功能（预计 2 天）

- [ ] 执行记录 CRUD 接口实现
- [ ] 前端记录列表页面开发
- [ ] 记录详情页面开发
- [ ] 权限控制实现

### 第二阶段：高级功能（预计 1 天）

- [ ] 批量操作功能
- [ ] 数据清理功能
- [ ] 记录导出功能

### 第三阶段：优化功能（预计 1 天）

- [ ] 性能优化
- [ ] 搜索功能增强
- [ ] 统计报表功能

## 十、待确认问题

1. **数据保留策略**：执行记录的默认保留时长？ 答：30 天
2. **数据脱敏**：输入参数中是否包含敏感信息需要脱敏？ 答：不需要
3. **实时监控**：是否需要实时监控执行状态？ 答：不需要
4. **统计分析**：是否需要详细的统计分析功能？ 答：不需要
