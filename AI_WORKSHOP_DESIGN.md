# AI 工坊功能 - 设计方案

## 一、需求概述

AI 工坊模块为用户提供 AI 应用的使用界面，支持分类浏览、应用选择、参数输入和结果展示，包括：

- **WORKSHOP-01**: 应用分类展示
- **WORKSHOP-02**: 应用选择与详情查看
- **WORKSHOP-03**: 动态参数表单
- **WORKSHOP-04**: 应用执行与结果展示
- **WORKSHOP-05**: 执行历史记录

## 二、数据库设计

AI 工坊主要依赖已存在的表结构，不需要额外数据表：

- **ai_app_type** - 应用类型表
- **ai_application** - 应用配置表
- **ai_app_record** - 执行记录表

## 三、接口设计

### WORKSHOP-01: 应用分类接口

#### AiWorkshopController

| 接口                                 | 方法 | 说明                 |
| ------------------------------------ | ---- | -------------------- |
| `/ai-workshop/categories`            | GET  | 获取 AI 应用分类列表 |
| `/ai-workshop/categories/statistics` | GET  | 获取分类统计信息     |

### WORKSHOP-02: 应用选择接口

| 接口                                      | 方法 | 说明                     |
| ----------------------------------------- | ---- | ------------------------ |
| `/ai-workshop/applications`               | GET  | 根据分类获取 AI 应用列表 |
| `/ai-workshop/application/{appId}`        | GET  | 获取 AI 应用详情         |
| `/ai-workshop/application/{appId}/schema` | GET  | 获取应用参数 Schema      |

### WORKSHOP-03: 参数验证接口

| 接口                                        | 方法 | 说明         |
| ------------------------------------------- | ---- | ------------ |
| `/ai-workshop/application/{appId}/validate` | POST | 验证输入参数 |

### WORKSHOP-04: 应用执行接口

| 接口                                   | 方法 | 说明             |
| -------------------------------------- | ---- | ---------------- |
| `/ai-workshop/execute/{appId}`         | POST | 执行 AI 应用     |
| `/ai-workshop/execute/async/{appId}`   | POST | 异步执行 AI 应用 |
| `/ai-workshop/execute/{appId}/preview` | POST | 预览执行结果     |

### WORKSHOP-05: 历史记录接口

| 接口                                   | 方法 | 说明             |
| -------------------------------------- | ---- | ---------------- |
| `/ai-workshop/my-records`              | GET  | 获取我的执行记录 |
| `/ai-workshop/record/{recordId}`       | GET  | 获取执行记录详情 |
| `/ai-workshop/record/{recordId}/share` | POST | 分享执行结果     |

## 四、后端代码实现

### 涉及文件清单

#### Controller 层

- `AiWorkshopController.java` - AI 工坊控制器

#### 前端组件

- `AiWorkshopPage.vue` - AI 工坊首页
- `AiApplicationDetail.vue` - 应用详情页
- `ExecutionResult.vue` - 执行结果页
- `MyExecutionRecords.vue` - 我的记录页

#### 通用组件

- `DynamicParamForm.vue` - 动态参数表单
- `ResultDisplay.vue` - 结果展示组件

### 核心代码示例

#### AiWorkshopController 关键方法

```java
@RestController
@RequestMapping("/ai-workshop")
public class AiWorkshopController {

    @GetMapping("/categories")
    public Result<List<AiAppTypeVO>> getCategories() {
        LambdaQueryWrapper<AiAppType> query = new LambdaQueryWrapper<>();
        query.eq(AiAppType::getStatus, 1)
            .orderByAsc(AiAppType::getCreatedAt);

        List<AiAppType> types = aiAppTypeService.list(query);
        List<AiAppTypeVO> voList = types.stream()
            .map(AiAppTypeVO::fromEntity)
            .collect(Collectors.toList());
        return Result.success(voList);
    }

    @PostMapping("/execute/{appId}")
    public Result<Object> execute(@PathVariable Long appId, @RequestBody Map<String, Object> params) {
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
  U[用户] -->|浏览分类| C[获取分类 /ai-workshop/categories]
  U -->|选择应用| D[获取应用详情 /ai-workshop/application/{appId}]
  D --> S[获取参数Schema /application/{appId}/schema]
  U -->|填写参数| V[参数验证 /application/{appId}/validate]
  V -->|通过| E[执行应用 /execute/{appId}]
  E --> R[生成记录 ai_app_record]
  R --> X[结果展示组件 ResultDisplay.vue]
  U --> H[我的记录 /ai-workshop/my-records]
```

#### DynamicParamForm.vue 关键功能

```vue
<template>
  <div class="dynamic-param-form">
    <div
      v-for="(field, key) in schema.properties"
      :key="key"
      class="form-field"
    >
      <!-- 动态渲染不同类型的输入控件 -->
      <el-input v-if="field.type === 'string'" v-model="formData[key]" />
      <el-select v-else-if="field.enum" v-model="formData[key]" />
      <el-input-number
        v-else-if="field.type === 'number'"
        v-model="formData[key]"
      />
    </div>
  </div>
</template>
```

## 五、前端页面设计

### 页面清单

| 页面        | 路由                          | 组件名              | 说明               |
| ----------- | ----------------------------- | ------------------- | ------------------ |
| AI 工坊首页 | /ai-workshop                  | AiWorkshopPage      | 分类展示、应用浏览 |
| 应用详情页  | /ai-workshop/app/:id          | AiApplicationDetail | 应用详情、参数输入 |
| 执行结果页  | /ai-workshop/result/:recordId | ExecutionResult     | 执行结果展示       |
| 我的记录    | /ai-workshop/records          | MyExecutionRecords  | 个人执行历史       |

### AI 工坊首页效果

```
┌─────────────────────────────────────────────────────────────────────────────┐
│ AI工坊                                                                      │
├─────────────────────────────────────────────────────────────────────────────┤
│ 搜索框: [搜索AI应用...]                                                  [🔍] │
├─────────────────────────────────────────────────────────────────────────────┤
│ ┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐ │
│ │   文本处理      │ │   图像处理      │ │   数据分析      │ │   语音处理      │ │
│ │                 │ │                 │ │                 │ │                 │ │
│ │ 📝              │ │ 🖼️              │ │ 📊              │ │ 🎤              │ │
│ │                 │ │                 │ │                 │ │                 │ │
│ │ 15个应用        │ │ 12个应用        │ │ 8个应用         │ │ 6个应用         │ │
│ │                 │ │                 │ │                 │ │                 │ │
│ │ 进入分类 →      │ │ 进入分类 →      │ │ 进入分类 →      │ │ 进入分类 →      │ │
│ └─────────────────┘ └─────────────────┘ └─────────────────┘ └─────────────────┘ │
│                                                                             │
│ ┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐ │
│ │   内容创作      │ │   翻译服务      │ │   代码生成      │ │   其他工具      │ │
│ │                 │ │                 │ │                 │ │                 │ │
│ │ ✍️              │ │ 🌐              │ │ 💻              │ │ 🛠️              │ │
│ │                 │ │                 │ │                 │ │                 │ │
│ │ 10个应用        │ │ 7个应用         │ │ 9个应用         │ │ 5个应用         │ │
│ │                 │ │                 │ │                 │ │                 │ │
│ │ 进入分类 →      │ │ 进入分类 →      │ │ 进入分类 →      │ │ 进入分类 →      │ │
│ └─────────────────┘ └─────────────────┘ └─────────────────┘ └─────────────────┘ │
│                                                                             │
├─────────────────────────────────────────────────────────────────────────────┤
│ 热门应用                                                                    │
│ ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────────┐               │
│ │ 文本摘要    │ │ 图像识别    │ │ 数据分析    │ │ 语音转文字  │               │
│ │             │ │             │ │             │ │             │               │
│ │ ⭐⭐⭐⭐⭐     │ │ ⭐⭐⭐⭐      │ │ ⭐⭐⭐⭐⭐     │ │ ⭐⭐⭐       │               │
│ │ 5积分/次     │ │ 10积分/次   │ │ 8积分/次    │ │ 6积分/次    │               │
│ │             │ │             │ │             │ │             │               │
│ │ [立即使用]   │ │ [立即使用]   │ │ [立即使用]   │ │ [立即使用]   │               │
│ └─────────────┘ └─────────────┘ └─────────────┘ └─────────────┘               │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 应用详情页面效果

```
┌─────────────────────────────────────────────────────────────────────────────┐
│ AI工坊 > 文本处理 > 文本摘要                                                │
├─────────────────────────────────────────────────────────────────────────────┤
│ ┌─────────────────────────────────────────┐ ┌─────────────────────────────┐ │
│ │              应用信息                   │ │         使用统计              │ │
│ │                                     │ │                             │ │
│ │ 📝 文本摘要                          │ │ 📊 使用次数：1,234次          │ │
│ │                                     │ │ ⭐ 用户评分：4.8分            │ │
│ │ 自动生成文本摘要，支持中英文，          │ │ 🕒 平均耗时：2.3秒            │ │
│ │ 可自定义摘要长度和语言偏好。            │ │ 💰 积分消耗：5积分/次          │ │
│ │                                     │ │                             │ │
│ │ 🏷️ 标签：文本处理、智能摘要、NLP      │ │ 📅 最近使用：刚刚             │ │
│ │                                     │ │                             │ │
│ │ ⚡ 执行器：syncWorkflowHandler        │ │                             │ │
│ │ ⏱️  超时时间：30秒                    │ │                             │ │
│ │                                     │ │                             │ │
│ └─────────────────────────────────────────┘ └─────────────────────────────┘ │
│                                                                             │
│ 参数配置                                                                    │
│ ┌─────────────────────────────────────────────────────────────────────────┐ │
│ │                                                                         │ │
│ │ 输入文本 *                                                              │ │
│ │ ┌─────────────────────────────────────────────────────────────────────┐ │ │
│ │ │ 请输入需要生成摘要的文本内容...                                      │ │ │
│ │ │                                                                 │ │ │
│ │ │                                                                 │ │ │
│ │ └─────────────────────────────────────────────────────────────────────┘ │ │
│ │                                                                         │ │
│ │ 摘要长度                                                                │ │ │
│ │ [ 100 ] 字符    (最小: 50, 最大: 500)                                  │ │
│ │                                                                         │ │
│ │ 输出语言                                                                │ │ │
│ │ ○ 中文  ● 英文  ○ 中英混合                                              │ │
│ │                                                                         │ │
│ │ 高级选项 ▼                                                              │ │
│ │ ┌─────────────────────────────────────────────────────────────────────┐ │ │
│ │ │ • 提取关键词  • 句子相似度  • 重点标记                                │ │ │
│ │ ☑ 提取关键词    ☐ 句子相似度    ☐ 重点标记                            │ │ │
│ └─────────────────────────────────────────────────────────────────────┘ │ │
│ └─────────────────────────────────────────────────────────────────────────┘ │
│                                                                             │
│                                     [参数验证]  [生成摘要]  [保存配置]         │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 执行结果页面效果

```
┌─────────────────────────────────────────────────────────────────────────────┐
│ AI工坊 > 执行结果                                                             │
├─────────────────────────────────────────────────────────────────────────────┤
│ ┌─────────────────────────────────────────┐ ┌─────────────────────────────┐ │
│ │              执行信息                   │ │         操作按钮              │ │
│ │                                     │ │                             │ │
│ │ ✅ 执行成功                          │ │ 📋 复制结果                  │ │
│ │                                     │ │ 📤 导出结果                  │ │
│ │ ⏱️ 耗时：2.3秒                       │ │ 🔗 分享链接                  │ │
│ │ 💰 消耗：5积分                       │ │ 🔄 重新生成                  │ │
│ │ 📅 时间：2025-01-01 14:30:25        │ │ ⭐ 评价应用                  │ │
│ │                                     │ │                             │ │
│ │ 🆔 任务ID：task_123456789          │ │                             │ │
│ └─────────────────────────────────────────┘ └─────────────────────────────┘ │
│                                                                             │
│ 原始文本                                                                    │
│ ┌─────────────────────────────────────────────────────────────────────────┐ │
│ │ 这是一段关于人工智能发展的长文本内容，包含了人工智能的历史、现状和未来   │ │
│ │ 发展趋势的详细描述。文本涵盖了机器学习、深度学习、自然语言处理等多个领域 │ │
│ │ 的技术发展和应用案例...                                                  │ │
│ │                                                                         │ │
│ │ [查看全文 ▼]                                                            │ │
│ └─────────────────────────────────────────────────────────────────────────┘ │
│                                                                             │
│ 生成摘要                                                                    │
│ ┌─────────────────────────────────────────────────────────────────────────┐ │
│ │ 📝 人工智能经历了从理论到实践的快速发展，涵盖了机器学习、深度学习等      │ │
│ │ 核心技术。目前AI已在自然语言处理、计算机视觉等领域取得突破性进展，未来   │ │
│ │ 将在更多行业发挥重要作用。                                                │ │
│ │                                                                         │ │
│ │ 📊 原文长度：1,245字  │  摘要长度：89字  │  压缩率：92.8%              │ │
│ └─────────────────────────────────────────────────────────────────────────┘ │
│                                                                             │
│ 提取的关键词                                                                │
│ ┌─────────────────────────────────────────────────────────────────────────┐ │
│ │ #人工智能 #机器学习 #深度学习 #自然语言处理 #计算机视觉 #技术发展          │ │
│ └─────────────────────────────────────────────────────────────────────────┘ │
│                                                                             │
│ 执行历史                                                                    │
│ ┌─────────────────────────────────────────────────────────────────────────┐ │
│ │ • 2025-01-01 14:25:10 - 成功生成摘要 (89字)                             │ │
│ │ • 2025-01-01 14:20:30 - 成功生成摘要 (95字)                             │ │
│ │ • 2025-01-01 14:15:45 - 成功生成摘要 (87字)                             │ │
│ │ [查看更多历史...]                                                       │ │
│ └─────────────────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────────────────┘
```

## 六、核心组件

### 1. DynamicParamForm.vue - 动态参数表单

```vue
<template>
  <div class="dynamic-param-form">
    <div
      v-for="(field, key) in schema.properties"
      :key="key"
      class="form-field"
    >
      <label :for="key" class="field-label">
        {{ field.title || key }}
        <span v-if="schema.required?.includes(key)" class="required">*</span>
      </label>

      <!-- 文本输入 -->
      <el-input
        v-if="field.type === 'string' && !field.enum"
        :id="key"
        v-model="formData[key]"
        :placeholder="field.description"
        :maxlength="field.maxLength"
        @input="validateField(key)"
      />

      <!-- 下拉选择 -->
      <el-select
        v-else-if="field.enum"
        :id="key"
        v-model="formData[key]"
        :placeholder="`请选择${field.title || key}`"
      >
        <el-option
          v-for="option in field.enum"
          :key="option"
          :label="option"
          :value="option"
        />
      </el-select>

      <!-- 数字输入 -->
      <el-input-number
        v-else-if="field.type === 'number' || field.type === 'integer'"
        :id="key"
        v-model="formData[key]"
        :min="field.minimum"
        :max="field.maximum"
        :step="field.type === 'integer' ? 1 : 0.1"
      />

      <!-- 错误提示 -->
      <div v-if="errors[key]" class="error-message">
        {{ errors[key] }}
      </div>
    </div>
  </div>
</template>
```

### 2. ResultDisplay.vue - 结果展示组件

```vue
<template>
  <div class="result-display">
    <div class="result-header">
      <div class="status-indicator" :class="statusClass">
        <el-icon
          ><SuccessFilled v-if="status === 1" /><CircleCloseFilled v-else
        /></el-icon>
        {{ statusText }}
      </div>
      <div class="execution-info">
        <span>耗时：{{ executionTime }}ms</span>
        <span>消耗：{{ creditCost }}积分</span>
      </div>
    </div>

    <div class="result-content">
      <pre v-if="result">{{ formatResult(result) }}</pre>
      <div v-else class="no-result">暂无结果</div>
    </div>

    <div class="result-actions">
      <el-button @click="copyResult">📋 复制</el-button>
      <el-button @click="exportResult">📤 导出</el-button>
      <el-button @click="shareResult">🔗 分享</el-button>
    </div>
  </div>
</template>
```

## 七、核心特性

### 1. 用户体验

- **分类导航**：清晰的分类结构，便于用户快速找到所需应用
- **智能搜索**：支持应用名称、描述、标签的全文搜索
- **动态表单**：根据应用的 JSON Schema 动态生成参数输入表单
- **实时验证**：输入参数的实时验证和错误提示

### 2. 执行管理

- **同步/异步**：支持同步和异步两种执行模式
- **进度跟踪**：实时显示执行进度和状态
- **结果展示**：美观的结果展示和格式化
- **历史记录**：个人执行历史和收藏功能

### 3. 社交功能

- **结果分享**：生成分享链接，便于传播
- **用户评价**：对应用进行评分和评价
- **使用统计**：展示应用的使用次数和用户反馈

## 八、菜单配置

| 菜单名称 | 路由地址             | 组件键名           | 权限标识        | 图标    |
| -------- | -------------------- | ------------------ | --------------- | ------- |
| AI 工坊  | /ai-workshop         | AiWorkshopPage     | workshop:view   | Robot   |
| 我的记录 | /ai-workshop/records | MyExecutionRecords | workshop:record | History |

## 九、开发计划

### 第一阶段：基础页面（预计 2 天）

- [ ] AI 工坊首页开发
- [ ] 分类展示功能
- [ ] 应用列表页面
- [ ] 应用详情页面

### 第二阶段：核心功能（预计 2 天）

- [ ] 动态参数表单组件
- [ ] 应用执行功能
- [ ] 结果展示组件
- [ ] 参数验证机制

### 第三阶段：高级功能（预计 2 天）

- [ ] 异步执行支持
- [ ] 历史记录功能
- [ ] 分享功能
- [ ] 搜索功能

## 十、待确认问题

1. **积分系统**：是否需要实现完整的积分消耗和充值系统？ 答：不需要
2. **应用推荐**：是否需要基于用户行为的智能推荐？ 答：不需要
3. **收藏功能**：是否需要支持应用收藏和自定义分类？ 答：不需要
4. **多语言**：是否需要支持多语言界面？ 答：不需要
