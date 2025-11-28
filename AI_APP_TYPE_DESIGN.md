# AI 应用类型管理功能 - 设计方案

## 一、需求概述

AI 应用类型管理模块用于管理 AI 应用的分类，为 AI 应用提供组织结构，包括：

- **APP-TYPE-01**: 应用类型 CRUD 管理
- **APP-TYPE-02**: 类型状态控制
- **APP-TYPE-03**: 类型关联应用统计

## 二、数据库设计

### 1. ai_app_type - AI 应用类型表

| 字段        | 类型         | 说明                |
| ----------- | ------------ | ------------------- |
| id          | varchar(32)  | 主键 ID             |
| tenant_id   | varchar(32)  | 租户 ID             |
| name        | varchar(100) | 类型名称            |
| description | text         | 类型描述            |
| status      | tinyint      | 状态：1-启用,0-禁用 |
| creator_id  | varchar(32)  | 创建人 ID           |
| created_at  | datetime     | 创建时间            |
| updated_at  | datetime     | 更新时间            |

## 三、接口设计

### APP-TYPE-01: 应用类型管理接口

#### AiAppTypeController

| 接口                | 方法   | 说明                 |
| ------------------- | ------ | -------------------- |
| `/ai-app-type/page` | GET    | 分页查询应用类型列表 |
| `/ai-app-type/{id}` | GET    | 获取应用类型详情     |
| `/ai-app-type`      | POST   | 创建应用类型         |
| `/ai-app-type`      | PUT    | 更新应用类型         |
| `/ai-app-type/{id}` | DELETE | 删除应用类型         |

### APP-TYPE-02: 类型状态控制接口

| 接口                        | 方法 | 说明         |
| --------------------------- | ---- | ------------ |
| `/ai-app-type/{id}/enable`  | PUT  | 启用应用类型 |
| `/ai-app-type/{id}/disable` | PUT  | 禁用应用类型 |

### APP-TYPE-03: 统计查询接口

| 接口                          | 方法 | 说明               |
| ----------------------------- | ---- | ------------------ |
| `/ai-app-type/statistics`     | GET  | 获取类型统计信息   |
| `/ai-app-type/{id}/app-count` | GET  | 获取类型下应用数量 |

## 四、后端代码实现

### 涉及文件清单

#### Controller 层

- `AiAppTypeController.java` - AI 应用类型控制器

#### Service 层

- `AiAppTypeService.java` - AI 应用类型服务接口

#### 实现类

- `AiAppTypeServiceImpl.java` - AI 应用类型服务实现

#### 数据传输对象

- `dto/aitype/AiAppTypeCreateDTO.java` - AI 应用类型创建 DTO
- `dto/aitype/AiAppTypeUpdateDTO.java` - AI 应用类型更新 DTO

#### 视图对象

- `vo/AiAppTypeVO.java` - AI 应用类型视图对象

#### 实体类

- `model/AiAppType.java` - AI 应用类型实体

#### Mapper 接口

- `mapper/AiAppTypeMapper.java` - AI 应用类型数据访问接口

### 核心代码示例

#### AiAppTypeController 关键方法

```java
@RestController
@RequestMapping("/ai-app-type")
public class AiAppTypeController {

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<String> create(@RequestBody @Validated AiAppTypeCreateDTO dto) {
        AiAppType appType = new AiAppType();
        appType.setName(dto.getName());
        appType.setDescription(dto.getDescription());
        appType.setStatus(dto.getStatus());
        aiAppTypeService.save(appType);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('TEAM_ADMIN','SUPER_ADMIN')")
    public Result<String> delete(@PathVariable String id) {
        aiAppTypeService.safeDelete(id);
        return Result.success();
    }
}
```

#### AiAppTypeService 关键方法

```java
public interface AiAppTypeService extends IService<AiAppType> {

    IPage<AiAppTypeVO> pageList(Page<AiAppType> page, String name, String tenantId, Byte status);

    boolean isDeletable(String typeId);

    void safeDelete(String typeId);

    void updateStatus(String typeId, Byte status);
}
```

## 五、数据传输对象

### 1. AiAppTypeCreateDTO.java

```java
package com.xiaodou.model.dto.aitype;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * AI应用类型创建数据传输对象
 *
 * @author xiaodou V=>dddou117
 * @since 2025/5/9
 * @version 1.0
 */
@Data
@Schema(description = "创建智能体类型请求")
@JsonIgnoreProperties(ignoreUnknown = true)
public class AiAppTypeCreateDTO {

    /**
     * 类型名称，不能为空
     */
    @NotBlank(message = "类型名称不能为空")
    @Schema(description = "类型名称", example = "文本处理")
    private String name;

    /**
     * 类型描述信息
     */
    @Schema(description = "类型描述", example = "处理文本相关的AI智能体")
    private String description;

    /**
     * 状态：1-启用，0-禁用，默认为启用状态
     */
    @Schema(description = "状态：1-启用，0-禁用", example = "1")
    private Byte status = 1;
}
```

### 2. AiAppTypeUpdateDTO.java

```java
package com.xiaodou.model.dto.aitype;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * AI应用类型更新数据传输对象
 *
 * @author xiaodou V=>dddou117
 * @since 2025/5/9
 * @version 1.0
 */
@Data
@Schema(description = "更新智能体类型请求")
@JsonIgnoreProperties(ignoreUnknown = true)
public class AiAppTypeUpdateDTO {

    /**
     * 类型ID，不能为空
     */
    @NotBlank(message = "类型ID不能为空")
    @Schema(description = "类型ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private String id;

    /**
     * 类型名称
     */
    @Schema(description = "类型名称", example = "文本处理")
    private String name;

    /**
     * 类型描述信息
     */
    @Schema(description = "类型描述", example = "处理文本相关的AI智能体")
    private String description;

    /**
     * 状态：1-启用，0-禁用，默认为启用状态
     */
    @Schema(description = "状态：1-启用，0-禁用", example = "1")
    private Byte status = 1;
}
```

### 3. AiAppTypeVO.java

```java
package com.xiaodou.model.vo;

import com.xiaodou.model.AiAppType;
import com.xiaodou.utils.DateTimeUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.beans.BeanUtils;

/**
 * 工作流类型视图对象（View Object）
 * <p>
 * 用于封装工作流类型的核心信息，包含类型标识、名称、描述、状态等关键属性。
 * 主要应用于以下场景：
 * <ul>
 * <li>工作流类型管理界面展示</li>
 * <li>工作流创建时的类型选择</li>
 * <li>工作流类型权限控制</li>
 * </ul>
 * </p>
 *
 * @author xiaodou V=>dddou117
 * @version V1.0
 * @since 2025/5/9
 */
@Data
@Schema(description = "智能体类型响应")
@Accessors(chain = true)
public class AiAppTypeVO {

    /**
     * 工作流类型唯一标识
     */
    @Schema(description = "类型ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private String id;

    /**
     * 工作流类型名称
     */
    @Schema(description = "类型名称", example = "文本处理")
    private String name;

    /**
     * 工作流类型描述
     */
    @Schema(description = "类型描述", example = "处理文本相关的AI智能体")
    private String description;

    /**
     * 工作流类型状态
     */
    @Schema(description = "状态：1-启用，0-禁用", example = "1")
    private Byte status;

    /**
     * 关联的团队ID
     */
    @Schema(description = "关联的租户ID")
    private String tenantId;

    /**
     * 创建人ID
     */
    @Schema(description = "创建人ID", example = "user123")
    private String creatorId;

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
     * 该类型下的工作流数量
     */
    @Schema(description = "该类型下的工作流数量")
    private Integer workflowCount;

    /**
     * 将工作流类型实体转换为视图对象
     *
     * @param type 工作流类型实体，不能为null
     * @return 转换后的视图对象，当输入为null时返回null
     */
    public static AiAppTypeVO fromEntity(AiAppType type) {
        if (type == null) {
            return null;
        }
        AiAppTypeVO vo = new AiAppTypeVO();
        BeanUtils.copyProperties(type, vo);
        vo.setCreatedAt(DateTimeUtils.toTimestampAtUTC8(type.getCreatedAt()));
        vo.setUpdatedAt(DateTimeUtils.toTimestampAtUTC8(type.getUpdatedAt()));
        return vo;
    }
}
```

## 六、前端页面设计

### 页面清单

| 页面         | 路由         | 组件名              | 说明           |
| ------------ | ------------ | ------------------- | -------------- |
| 应用类型管理 | /ai-app-type | AiAppTypeManagement | 类型列表、管理 |

### 应用类型管理页面效果

```
┌─────────────────────────────────────────────────────────────────────────────┐
│ AI应用类型管理                                      [+ 新建类型]            │
├─────────────────────────────────────────────────────────────────────────────┤
│ 类型名称: [__________]  状态: [全部 ▼]  [搜索] [重置]                        │
├─────────────────────────────────────────────────────────────────────────────┤
│ 统计信息：总计 5 个类型 | 启用 4 个 | 禁用 1 个                               │
├─────────────────────────────────────────────────────────────────────────────┤
│ # │ 类型名称      │ 描述                  │ 状态  │ 应用数量 │ 操作        │
│───┼───────────────┼──────────────────────┼───────┼──────────┼─────────────│
│ 1 │ 文本处理      │ 处理文本相关的AI应用  │ 启用  │ 3        │ 编辑 删除   │
│ 2 │ 图像处理      │ 图像识别和处理应用    │ 启用  │ 5        │ 编辑 删除   │
│ 3 │ 数据分析      │ 数据分析统计应用      │ 禁用  │ 0        │ 编辑 删除   │
│ 4 │ 语音处理      │ 语音识别合成应用      │ 启用  │ 2        │ 编辑 删除   │
├─────────────────────────────────────────────────────────────────────────────┤
│                              [1] [2] [3] ... [10]    共 5 条                │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│ 新建AI应用类型                                                 [×]           │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  基本信息                                                                   │
│  ├─ 类型名称 *    [文本处理_________________________________]            │
│  ├─ 类型描述      [处理文本相关的AI智能体应用                       ]    │
│  └─ 状态          ○ 禁用  ● 启用                                      │
│                                                                             │
│                                           [取消]  [保存草稿]  [确定创建]      │
└─────────────────────────────────────────────────────────────────────────────┘
```

## 七、核心特性

### 1. 类型管理

- **CRUD 操作**：支持应用类型的创建、查询、更新、删除
- **状态控制**：支持类型的启用和禁用状态切换
- **关联检查**：删除前检查是否有应用关联，防止误删

### 2. 权限控制

- **角色权限**：不同角色有不同的操作权限
- **租户隔离**：支持多租户环境下的类型隔离

### 3. 数据统计

- **实时统计**：提供类型数量、启用状态等统计信息
- **应用关联**：统计每个类型下的应用数量

## 八、菜单配置

| 菜单名称    | 路由地址     | 组件键名            | 权限标识         | 图标  |
| ----------- | ------------ | ------------------- | ---------------- | ----- |
| AI 应用管理 | /ai-app      | -                   | -                | Robot |
| 应用类型    | /ai-app/type | AiAppTypeManagement | ai-app-type:view | Grid  |

## 九、开发计划

### 第一阶段：基础功能（预计 1 天）

- [ ] 应用类型 CRUD 接口实现
- [ ] 前端列表页面开发
- [ ] 表单页面开发

### 第二阶段：高级功能（预计 0.5 天）

- [ ] 状态管理功能
- [ ] 统计信息展示
- [ ] 关联检查功能

## 十、待确认问题

1. **类型层级**：是否需要支持多级类型分类？ 答：不需要
2. **排序功能**：类型是否需要支持手动排序？ 答：不需要
3. **图标配置**：类型是否支持配置图标？ 答：不需要
4. **导入导出**：是否需要支持类型的批量导入导出？ 答：不需要
