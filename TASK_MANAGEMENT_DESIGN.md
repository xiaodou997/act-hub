# 任务管理功能 - 设计方案

## 一、需求概述

任务管理模块用于运营管理者创建和管理分发任务，包括：

- **TASK-01**: 创建分发任务
- **TASK-02**: 任务监控与审核
- **TASK-03**: 奖励下发

## 二、数据库设计

### 现有表（已设计）

#### 1. task - 分发任务表

| 字段            | 类型          | 说明                                |
| --------------- | ------------- | ----------------------------------- |
| id              | varchar(32)   | 主键 ID                             |
| tenant_id       | varchar(32)   | 租户 ID                             |
| task_name       | varchar(200)  | 任务名称                            |
| start_time      | datetime      | 任务开始时间                        |
| end_time        | datetime      | 任务结束时间                        |
| platforms       | text          | 发布平台 JSON 数组                  |
| requirements    | text          | 补充要求（富文本）                  |
| images          | text          | 任务图片 URL JSON 数组              |
| reward_amount   | decimal(10,2) | 单条任务奖励金额                    |
| total_quota     | int           | 任务总量                            |
| claimed_count   | int           | 已领取人数                          |
| completed_count | int           | 已完成人数                          |
| is_targeted     | tinyint(1)    | 是否定向任务                        |
| sort_order      | int           | 任务权重                            |
| status          | tinyint       | 状态：0 草稿,1 上线,2 下线,3 已结束 |
| creator_id      | varchar(32)   | 创建人 ID                           |
| created_at      | datetime      | 创建时间                            |
| updated_at      | datetime      | 更新时间                            |

#### 2. task_participation - 任务参与记录表

| 字段               | 类型         | 说明                                          |
| ------------------ | ------------ | --------------------------------------------- |
| id                 | varchar(32)  | 主键 ID                                       |
| tenant_id          | varchar(32)  | 租户 ID                                       |
| task_id            | varchar(32)  | 任务 ID                                       |
| user_id            | varchar(32)  | 参与用户 ID                                   |
| status             | tinyint      | 状态：1 已领取,2 已提交,3 审核通过,4 审核拒绝 |
| submitted_link     | varchar(500) | 提交的链接                                    |
| submitted_content  | text         | 提交的内容                                    |
| submitted_at       | datetime     | 提交时间                                      |
| audit_notes        | text         | 审核备注                                      |
| auditor_id         | varchar(32)  | 审核人 ID                                     |
| audited_at         | datetime     | 审核时间                                      |
| snapshot_url       | varchar(500) | 页面快照 URL                                  |
| submission_history | text         | 提交历史 JSON                                 |
| reward_status      | tinyint      | 奖励状态：0 待发放,1 已发放,2 发放失败        |
| created_at         | datetime     | 创建时间                                      |
| updated_at         | datetime     | 更新时间                                      |

#### 3. task_target_user - 任务定向用户表

| 字段         | 类型        | 说明    |
| ------------ | ----------- | ------- |
| id           | varchar(32) | 主键 ID |
| tenant_id    | varchar(32) | 租户 ID |
| task_id      | varchar(32) | 任务 ID |
| user_id      | varchar(32) | 用户 ID |
| phone_number | varchar(20) | 手机号  |

### 新增表（需要创建）

#### 4. task_reward_config - 任务奖励配置表

```sql
CREATE TABLE `task_reward_config` (
  `id` varchar(32) NOT NULL COMMENT '主键ID',
  `tenant_id` varchar(32) DEFAULT NULL COMMENT '租户ID',
  `task_id` varchar(32) NOT NULL COMMENT '任务ID',
  `reward_type` tinyint NOT NULL DEFAULT '1' COMMENT '奖励类型：1基础奖,2进阶奖,3人气奖',
  `reward_name` varchar(100) NOT NULL COMMENT '奖励名称',
  `reward_form` tinyint NOT NULL DEFAULT '1' COMMENT '奖励形式：1现金,2实物,3券码',
  `reward_value` decimal(10,2) DEFAULT NULL COMMENT '奖励价值/金额',
  `reward_item_id` varchar(100) DEFAULT NULL COMMENT '奖品ID（关联奖品库）',
  `condition_desc` varchar(500) DEFAULT NULL COMMENT '获得条件描述',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_task_id` (`task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务奖励配置表';
```

#### 5. reward_record - 奖励发放记录表

```sql
CREATE TABLE `reward_record` (
  `id` varchar(32) NOT NULL COMMENT '主键ID',
  `tenant_id` varchar(32) DEFAULT NULL COMMENT '租户ID',
  `task_id` varchar(32) NOT NULL COMMENT '任务ID',
  `participation_id` varchar(32) NOT NULL COMMENT '参与记录ID',
  `user_id` varchar(32) NOT NULL COMMENT '用户ID',
  `reward_config_id` varchar(32) NOT NULL COMMENT '奖励配置ID',
  `reward_type` tinyint NOT NULL COMMENT '奖励类型',
  `reward_name` varchar(100) NOT NULL COMMENT '奖励名称',
  `reward_form` tinyint NOT NULL COMMENT '奖励形式',
  `reward_value` decimal(10,2) DEFAULT NULL COMMENT '奖励价值',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态：0待发放,1已发放,2发放失败',
  `issued_at` datetime DEFAULT NULL COMMENT '发放时间',
  `issued_by` varchar(32) DEFAULT NULL COMMENT '发放人',
  `fail_reason` varchar(500) DEFAULT NULL COMMENT '失败原因',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_task_id` (`task_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_participation_id` (`participation_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='奖励发放记录表';
```

## 三、接口设计

### TASK-01: 任务管理接口

#### TaskController

| 接口                      | 方法   | 说明                           |
| ------------------------- | ------ | ------------------------------ |
| `/task/page`              | GET    | 分页查询任务列表               |
| `/task/{id}`              | GET    | 获取任务详情                   |
| `/task`                   | POST   | 创建任务                       |
| `/task/{id}`              | PUT    | 更新任务                       |
| `/task/{id}`              | DELETE | 删除任务                       |
| `/task/{id}/status`       | PUT    | 变更任务状态（上线/下线/结束） |
| `/task/{id}/target-users` | POST   | 导入定向用户                   |
| `/task/{id}/target-users` | GET    | 获取定向用户列表               |

### TASK-02: 任务参与审核接口

#### TaskParticipationController

| 接口                                | 方法 | 说明             |
| ----------------------------------- | ---- | ---------------- |
| `/task-participation/page`          | GET  | 分页查询参与记录 |
| `/task-participation/{id}`          | GET  | 获取参与记录详情 |
| `/task-participation/{id}/audit`    | POST | 审核参与记录     |
| `/task-participation/export`        | GET  | 导出参与记录     |
| `/task-participation/{id}/snapshot` | GET  | 获取页面快照     |

### TASK-03: 奖励发放接口

#### RewardController

| 接口                            | 方法 | 说明             |
| ------------------------------- | ---- | ---------------- |
| `/reward/task/{taskId}/config`  | GET  | 获取任务奖励配置 |
| `/reward/task/{taskId}/config`  | POST | 配置任务奖励     |
| `/reward/task/{taskId}/issue`   | POST | 发放奖励         |
| `/reward/task/{taskId}/records` | GET  | 查询发放记录     |
| `/reward/record/{id}`           | GET  | 获取发放记录详情 |

## 四、后端代码实现

### 涉及文件清单

#### Controller 层

- `TaskController.java` - 任务管理控制器
- `TaskParticipationController.java` - 任务参与控制器
- `RewardController.java` - 奖励发放控制器
- `TaskTargetUserController.java` - 定向用户控制器

#### Service 层

- `TaskService.java` - 任务服务接口
- `TaskParticipationService.java` - 任务参与服务接口
- `TaskTargetUserService.java` - 定向用户服务接口
- `RewardService.java` - 奖励服务接口

#### 实体与视图

- `Task.java` - 分发任务实体
- `TaskParticipation.java` - 任务参与记录实体
- `TaskTargetUser.java` - 任务定向用户实体
- `RewardRecord.java` - 奖励发放记录实体
- `TaskRewardConfig.java` - 奖励配置实体
- `TaskVO.java` - 任务视图对象
- `TaskParticipationVO.java` - 参与记录视图对象

#### Mapper 接口

- `TaskMapper.java`
- `TaskParticipationMapper.java`
- `TaskTargetUserMapper.java`
- `RewardMapper.java`

#### 前端页面

- `TaskManagement.vue` - 任务列表
- `TaskDetail.vue` - 任务详情
- `TaskForm.vue` - 创建/编辑任务
- `TaskParticipations.vue` - 参与记录与审核
- `TaskRewards.vue` - 奖励配置与发放

### 1. TaskController.java

```java
package com.xiaodou.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xiaodou.auth.util.UserContextHolder;
import com.xiaodou.log.annotation.SystemLog;
import com.xiaodou.model.Task;
import com.xiaodou.model.dto.task.TaskCreateDTO;
import com.xiaodou.model.dto.task.TaskTargetUserImportDTO;
import com.xiaodou.model.dto.task.TaskUpdateDTO;
import com.xiaodou.model.query.TaskQuery;
import com.xiaodou.model.vo.TaskVO;
import com.xiaodou.result.Result;
import com.xiaodou.service.TaskService;
import com.xiaodou.service.TaskTargetUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/task")
@RequiredArgsConstructor
@Tag(name = "任务管理", description = "分发任务管理相关接口")
public class TaskController {

    private final TaskService taskService;
    private final TaskTargetUserService taskTargetUserService;

    @GetMapping("/page")
    @Operation(summary = "分页查询任务列表")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<IPage<TaskVO>> page(TaskQuery query) {
        log.info("分页查询任务列表 - query: {}", query);
        return Result.success(taskService.pageListTasks(query));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取任务详情")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<TaskVO> getById(@PathVariable String id) {
        log.info("获取任务详情 - id: {}", id);
        Task task = taskService.getById(id);
        return Result.success(TaskVO.fromEntity(task));
    }

    @PostMapping
    @Operation(summary = "创建任务")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @SystemLog(module = "任务管理", action = "创建任务", recordResponse = true)
    public Result<TaskVO> create(@Valid @RequestBody TaskCreateDTO createDTO) {
        log.info("创建任务 - createDTO: {}", createDTO);
        String creatorId = UserContextHolder.getUserId();
        Task task = taskService.createTask(createDTO, creatorId);
        return Result.success(TaskVO.fromEntity(task));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新任务")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @SystemLog(module = "任务管理", action = "更新任务", recordResponse = true)
    public Result<TaskVO> update(@PathVariable String id, @Valid @RequestBody TaskUpdateDTO updateDTO) {
        log.info("更新任务 - id: {}, updateDTO: {}", id, updateDTO);
        updateDTO.setId(id);
        Task task = taskService.updateTask(updateDTO);
        return Result.success(TaskVO.fromEntity(task));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除任务")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @SystemLog(module = "任务管理", action = "删除任务", recordResponse = true)
    public Result<Void> delete(@PathVariable String id) {
        log.info("删除任务 - id: {}", id);
        taskService.deleteTask(id);
        return Result.success();
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "变更任务状态")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @SystemLog(module = "任务管理", action = "变更任务状态", recordResponse = true)
    public Result<Void> changeStatus(@PathVariable String id, @RequestParam Byte status) {
        log.info("变更任务状态 - id: {}, status: {}", id, status);
        taskService.changeTaskStatus(id, status);
        return Result.success();
    }

    @PostMapping("/{id}/target-users")
    @Operation(summary = "导入定向用户")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @SystemLog(module = "任务管理", action = "导入定向用户", recordResponse = true)
    public Result<Void> importTargetUsers(@PathVariable String id,
                                          @Valid @RequestBody TaskTargetUserImportDTO importDTO) {
        log.info("导入定向用户 - taskId: {}, count: {}", id, importDTO.getUserIdentifiers().size());
        importDTO.setTaskId(id);
        taskTargetUserService.importTargetUsers(importDTO);
        return Result.success();
    }
}
```

### 2. TaskParticipationController.java

```java
package com.xiaodou.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xiaodou.auth.util.UserContextHolder;
import com.xiaodou.log.annotation.SystemLog;
import com.xiaodou.model.dto.task.TaskParticipationAuditDTO;
import com.xiaodou.model.query.TaskParticipationQuery;
import com.xiaodou.model.vo.TaskParticipationVO;
import com.xiaodou.result.Result;
import com.xiaodou.service.TaskParticipationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/task-participation")
@RequiredArgsConstructor
@Tag(name = "任务参与管理", description = "任务参与记录管理相关接口")
public class TaskParticipationController {

    private final TaskParticipationService taskParticipationService;

    @GetMapping("/page")
    @Operation(summary = "分页查询参与记录")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<IPage<TaskParticipationVO>> page(TaskParticipationQuery query) {
        log.info("分页查询参与记录 - query: {}", query);
        return Result.success(taskParticipationService.pageListParticipations(query));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取参与记录详情")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<TaskParticipationVO> getById(@PathVariable String id) {
        log.info("获取参与记录详情 - id: {}", id);
        return Result.success(TaskParticipationVO.fromEntity(
            taskParticipationService.getById(id)));
    }

    @PostMapping("/{id}/audit")
    @Operation(summary = "审核参与记录")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @SystemLog(module = "任务参与管理", action = "审核参与记录", recordResponse = true)
    public Result<Void> audit(@PathVariable String id,
                              @Valid @RequestBody TaskParticipationAuditDTO auditDTO) {
        log.info("审核参与记录 - id: {}, auditDTO: {}", id, auditDTO);
        String auditorId = UserContextHolder.getUserId();
        taskParticipationService.auditParticipation(id, auditDTO, auditorId);
        return Result.success();
    }
}
```

### 任务流程图

```mermaid
flowchart TD
  A[创建任务 /task POST] --> B[任务上线 /task/{id}/status]
  B --> C{是否定向}
  C -- 是 --> TU[导入定向用户 /task/{id}/target-users]
  C -- 否 --> PUB[公开任务]
  U[用户] --> CLM[领取任务]
  CLM --> SUB[提交内容 /task-participation/{id}/audit 前的提交]
  AUD[管理员审核 /task-participation/{id}/audit] --> RES{审核结果}
  RES -- 通过 --> RWCFG[配置奖励 /reward/task/{taskId}/config]
  RWCFG --> ISSUE[发放奖励 /reward/task/{taskId}/issue]
  ISSUE --> RWRCD[记录生成 reward_record]
  RES -- 拒绝 --> ENDR[结束并记录拒绝]
  MON[参与记录分页 /task-participation/page] --> AUD
```

## 五、前端页面设计

### 页面清单

| 页面     | 路由                     | 组件名             | 说明               |
| -------- | ------------------------ | ------------------ | ------------------ |
| 任务列表 | /task                    | TaskManagement     | 任务列表、创建入口 |
| 任务详情 | /task/:id                | TaskDetail         | 任务详情、参与记录 |
| 创建任务 | /task/create             | TaskForm           | 创建/编辑任务表单  |
| 参与记录 | /task/:id/participations | TaskParticipations | 参与记录列表、审核 |
| 奖励发放 | /task/:id/rewards        | TaskRewards        | 奖励配置、发放     |

### 任务列表页面效果

```
┌─────────────────────────────────────────────────────────────────────────────┐
│ 任务管理                                              [+ 创建任务]          │
├─────────────────────────────────────────────────────────────────────────────┤
│ 任务名称: [__________]  状态: [全部 ▼]  平台: [全部 ▼]  [搜索] [重置]       │
├─────────────────────────────────────────────────────────────────────────────┤
│ # │ 任务名称              │ 状态  │ 平台       │ 进度      │ 奖励  │ 操作    │
│───┼───────────────────────┼───────┼────────────┼───────────┼───────┼─────────│
│ 1 │ 海信电视以旧换新活动  │ 上线  │ 小红书     │ 15/20    │ 5元   │ 详情... │
│ 2 │ 双11好物推荐任务      │ 草稿  │ 抖音,小红书 │ 0/100    │ 10元  │ 详情... │
│ 3 │ 新品试用体验任务      │ 已结束│ 小红书     │ 50/50    │ 8元   │ 详情... │
├─────────────────────────────────────────────────────────────────────────────┤
│                              [1] [2] [3] ... [10]    共 98 条               │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 任务创建表单

```
┌─────────────────────────────────────────────────────────────────────────────┐
│ 创建分发任务                                                                │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  基本信息                                                                   │
│  ├─ 任务名称 *    [海信电视以旧换新，最高赢300购物卡________________]      │
│  ├─ 任务时间 *    [2025-09-01 09:00] 至 [2025-09-30 23:59]                 │
│  └─ 发布平台 *    ☑ 小红书  ☐ 抖音  ☐ 快手                                 │
│                                                                             │
│  任务要求                                                                   │
│  └─ 补充要求      [富文本编辑器：必带话题#海信电视...                 ]     │
│                   [上传图片]                                                │
│                                                                             │
│  奖励设置                                                                   │
│  ├─ 任务奖励 *    [5] 元/条                                                │
│  └─ 任务总量 *    [1500] 人                                                │
│                                                                             │
│  高级设置                                                                   │
│  ├─ 任务定向      ○ 否  ● 是 → [导入用户列表]                              │
│  └─ 任务权重      [10] (数字越大优先级越高)                                │
│                                                                             │
│                                           [取消]  [保存草稿]  [发布任务]    │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 参与记录与审核页面

```
┌─────────────────────────────────────────────────────────────────────────────┐
│ 任务参与记录 - "海信电视以旧换新活动"                    [导出] [返回]      │
├─────────────────────────────────────────────────────────────────────────────┤
│ 状态: [全部 ▼]                                                              │
├─────────────────────────────────────────────────────────────────────────────┤
│ # │ 用户       │ 提交链接                    │ 提交时间    │ 状态    │ 操作 │
│───┼────────────┼─────────────────────────────┼─────────────┼─────────┼──────│
│ 1 │ 用户A      │ https://xhs.com/xxx         │ 2025-09-05  │ 待审核  │ 审核 │
│ 2 │ 用户B      │ https://douyin.com/xxx      │ 2025-09-04  │ 已通过  │ 详情 │
│ 3 │ 用户C      │ https://xhs.com/yyy         │ 2025-09-03  │ 未通过  │ 审核 │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│ 审核 - 用户A                                                     [×]       │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  提交链接: https://xiaohongshu.com/discovery/item/xxx                      │
│  提交时间: 2025-09-05 14:30:25                                             │
│                                                                             │
│  ┌─────────────────────────────┐  ┌─────────────────────────────┐          │
│  │       页面快照预览          │  │        审核操作              │          │
│  │                             │  │                             │          │
│  │    [自动抓取的截图]         │  │  ○ 通过                     │          │
│  │                             │  │  ○ 不通过                   │          │
│  │                             │  │                             │          │
│  │                             │  │  不通过理由:                │          │
│  │                             │  │  [________________________] │          │
│  │                             │  │                             │          │
│  └─────────────────────────────┘  └─────────────────────────────┘          │
│                                                                             │
│                                               [取消]  [确认提交]            │
└─────────────────────────────────────────────────────────────────────────────┘
```

## 六、菜单配置

| 菜单名称 | 路由地址     | 组件键名       | 权限标识    | 图标     |
| -------- | ------------ | -------------- | ----------- | -------- |
| 任务管理 | /task        | TaskManagement | task:view   | List     |
| 创建任务 | /task/create | TaskForm       | task:create | Plus     |
| 任务详情 | /task/:id    | TaskDetail     | task:view   | - (隐藏) |

## 七、开发计划

### 第一阶段：基础 CRUD（预计 2 天）

- [ ] TaskController 完善
- [ ] TaskParticipationController 完善
- [ ] 前端任务列表页面
- [ ] 前端任务表单页面

### 第二阶段：审核功能（预计 2 天）

- [ ] 参与记录列表页面
- [ ] 审核弹窗组件
- [ ] 页面快照功能（可选，需要第三方服务）
- [ ] 导出功能

### 第三阶段：奖励发放（预计 2 天）

- [ ] 新增奖励相关表
- [ ] 奖励配置接口
- [ ] 奖励发放接口
- [ ] 奖励发放页面

## 八、待确认问题

1. **页面快照功能**：是否需要实现自动抓取？如果需要，建议使用第三方截图服务（如 Puppeteer 或 截图 API），
   答：不实用页面快照功能，用户自己上传截图，管理员审核截图

2. **奖励发放流程**：具体发放方式是什么？现金直接转账还是生成券码？
   两种方式：1、是寄快递，显示快递单号，2 是生成券码，显示券码列表

3. **C 端用户接口**：是否需要在本阶段实现 C 端用户领取任务、提交链接的接口？
   答：不需要

4. **通知机制**：审核不通过时如何通知用户重新提交？短信/微信/站内信？
   答：审核不通过时，状态会变为未通过，审核人会收到通知，审核人点击详情按钮可以查看未通过理由
