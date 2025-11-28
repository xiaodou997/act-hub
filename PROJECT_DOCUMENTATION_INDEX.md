# Act-Hub 项目文档索引

## 项目概述

Act-Hub 是一个基于 Spring Boot + Vue.js 的 AI 应用管理平台，提供完整的 RBAC 权限管理、AI 应用配置、执行监控和用户工坊等功能。

## 技术栈

### 后端技术栈

- **框架**: Spring Boot 3.x
- **数据库**: MySQL 8.0
- **ORM**: MyBatis Plus
- **安全**: Spring Security + JWT
- **文档**: OpenAPI 3 (Swagger)
- **日志**: Logback
- **缓存**: Redis
- **消息队列**: RabbitMQ

### 前端技术栈

- **框架**: Vue 3 + Composition API
- **UI 库**: Element Plus
- **构建工具**: Vite
- **状态管理**: Pinia
- **路由**: Vue Router
- **HTTP 客户端**: Axios
- **样式**: SCSS

## 功能模块文档

### 1. RBAC 权限管理

- **文档**: [RBAC_MANAGEMENT_DESIGN.md](./RBAC_MANAGEMENT_DESIGN.md)
- **功能**: 用户管理、角色管理、权限管理、菜单管理
- **核心特性**: 基于角色的访问控制、细粒度权限、动态菜单加载
- **相关页面**: 用户列表、角色列表、权限列表、菜单管理

### 2. AI 应用类型管理

- **文档**: [AI_APP_TYPE_DESIGN.md](./AI_APP_TYPE_DESIGN.md)
- **功能**: 应用类型 CRUD、状态控制、关联应用统计
- **核心特性**: 类型分类、启用禁用、关联检查
- **相关页面**: 应用类型列表、类型编辑

### 3. AI 应用管理

- **文档**: [AI_APPLICATION_DESIGN.md](./AI_APPLICATION_DESIGN.md)
- **功能**: 应用配置管理、参数 Schema 定义、执行器配置
- **核心特性**: JSON Schema 参数验证、多执行器支持、应用测试
- **相关页面**: 应用列表、应用配置、参数编辑、测试工具

### 4. AI 应用执行记录

- **文档**: [AI_APP_RECORD_DESIGN.md](./AI_APP_RECORD_DESIGN.md)
- **功能**: 执行记录查询、状态跟踪、结果存储
- **核心特性**: 生命周期管理、权限控制、批量操作、数据清理
- **相关页面**: 执行记录列表、记录详情、统计分析

### 5. 任务管理

- **文档**: [TASK_MANAGEMENT_DESIGN.md](./TASK_MANAGEMENT_DESIGN.md)
- **功能**: 分发任务管理、参与审核、奖励发放
- **核心特性**: 任务创建、用户参与、审核流程、奖励系统
- **相关页面**: 任务列表、任务详情、参与记录、奖励管理

### 6. AI 工坊

- **文档**: [AI_WORKSHOP_DESIGN.md](./AI_WORKSHOP_DESIGN.md)
- **功能**: 应用分类展示、动态表单、执行结果展示
- **核心特性**: 分类导航、智能搜索、实时验证、结果分享
- **相关页面**: AI 工坊首页、应用详情、执行结果、个人记录

### 7. 奖励模块

- **文档**: [REWARD_MODULE_DESIGN.md](./REWARD_MODULE_DESIGN.md)
- **功能**: 奖品配置、库存管理、奖励发放
- **相关页面**: 奖品管理、库存管理、发放中心

### 8. 黑名单管理

- **文档**: [BLACKLIST_DESIGN.md](./BLACKLIST_DESIGN.md)
- **功能**: 拉黑、查询、移除
- **相关页面**: 黑名单管理

### 9. 收货地址管理

- **文档**: [ADDRESS_MANAGEMENT_DESIGN.md](./ADDRESS_MANAGEMENT_DESIGN.md)
- **功能**: 用户收货地址维护
- **相关页面**: 收货地址管理

### 10. 创作者学院

- **文档**: [CREATOR_ACADEMY_DESIGN.md](./CREATOR_ACADEMY_DESIGN.md)
- **功能**: 文章与分类管理
- **相关页面**: 文章管理、分类管理

### 11. 消息通知

- **文档**: [NOTIFICATION_DESIGN.md](./NOTIFICATION_DESIGN.md)
- **功能**: 系统消息查看与标记
- **相关页面**: 消息通知

### 12. 数据埋点

- **文档**: [DATA_TRACKING_DESIGN.md](./DATA_TRACKING_DESIGN.md)
- **功能**: C端上报与后台查询
- **相关页面**: 埋点事件查询

## 项目结构

```
act-hub/
├── service/                          # 后端服务
│   ├── src/main/java/com/xiaodou/
│   │   ├── controller/               # 控制器层
│   │   ├── service/                  # 服务层
│   │   ├── mapper/                   # 数据访问层
│   │   ├── model/                    # 实体类
│   │   ├── dto/                      # 数据传输对象
│   │   ├── vo/                       # 视图对象
│   │   ├── config/                   # 配置类
│   │   ├── auth/                     # 认证授权
│   │   ├── aiapp/                    # AI应用核心
│   │   └── utils/                    # 工具类
│   └── src/main/resources/           # 配置文件
├── vue-font/                         # 前端应用
│   ├── src/
│   │   ├── api/                      # API接口
│   │   ├── components/               # 公共组件
│   │   ├── views/                    # 页面组件
│   │   ├── router/                   # 路由配置
│   │   ├── stores/                   # 状态管理
│   │   ├── utils/                    # 工具函数
│   │   └── styles/                   # 样式文件
│   └── public/                       # 静态资源
└── docs/                             # 项目文档
    ├── RBAC_MANAGEMENT_DESIGN.md
    ├── AI_APP_TYPE_DESIGN.md
    ├── AI_APPLICATION_DESIGN.md
    ├── AI_APP_RECORD_DESIGN.md
    ├── AI_WORKSHOP_DESIGN.md
    └── PROJECT_DOCUMENTATION_INDEX.md
```

## 数据库设计

### 核心表结构

1. **权限管理表**

   - `user` - 用户表
   - `role` - 角色表
   - `permission` - 权限表
   - `user_role` - 用户角色关联表
   - `role_permission` - 角色权限关联表
   - `menu` - 菜单权限表

2. **AI 应用表**

   - `ai_app_type` - AI 应用类型表
   - `ai_application` - AI 应用配置表
   - `ai_app_record` - AI 应用执行记录表

3. **任务管理表**
   - `task` - 分发任务表
   - `task_participation` - 任务参与记录表
   - `task_target_user` - 任务定向用户表
   - `task_reward_config` - 任务奖励配置表
   - `reward_record` - 奖励发放记录表

## 接口文档

### 权限管理接口

- 用户管理: `/user/*`
- 角色管理: `/role/*`
- 权限管理: `/permission/*`
- 菜单管理: `/menu/*`

### AI 应用接口

- 应用类型: `/ai-app-type/*`
- 应用管理: `/ai-application/*`
- 执行记录: `/ai-app-record/*`
- AI 工坊: `/ai-workshop/*`

### 任务管理接口

- 任务管理: `/task/*`
- 任务参与: `/task-participation/*`
- 奖励管理: `/reward/*`

## 部署说明

### 环境要求

- JDK 17+
- Node.js 18+
- MySQL 8.0+
- Redis 6.0+
- RabbitMQ 3.9+

### 启动步骤

1. 后端启动: `cd service && mvn spring-boot:run`
2. 前端启动: `cd vue-font && npm run dev`
3. 访问地址: `http://localhost:3000`

### 配置说明

- 数据库配置: `service/src/main/resources/application.yml`
- 前端配置: `vue-font/vite.config.js`
- 环境变量: `.env` 文件

## 开发规范

### 代码规范

- 后端: 遵循阿里巴巴 Java 开发手册
- 前端: 使用 ESLint + Prettier
- 注释: 重要的业务逻辑必须添加注释

### 提交规范

- feat: 新功能
- fix: 修复 bug
- docs: 文档更新
- style: 代码格式调整
- refactor: 重构
- test: 测试相关
- chore: 构建过程或辅助工具的变动

### 分支管理

- master: 主分支，用于生产环境
- develop: 开发分支
- feature/\*: 功能分支
- hotfix/\*: 热修复分支

## 文档约定

- 文件清单仅列出文件名，不包含完整路径，便于快速定位。
- 展示关键代码片段，聚焦核心业务逻辑与接口用法。
- 复杂流程以流程图说明，采用 Mermaid 语法，便于阅读与分享。
- 后端与前端目录分别为 `service` 与 `vue-font`，文件名在对应目录下可唯一定位。

## 通用接口规范

- 响应包装：统一使用 `Result<T>`，包含 `code`、`message`、`data`。
- 错误码约定：`200` 成功，`400` 参数错误，`401` 未认证，`403` 无权限，`404` 不存在，`500` 服务异常；业务错误码推荐区间 `1000-1999`。
- 认证约定：`Authorization: Bearer <JWT>`，登录后由后台返回并前端存储。
- 分页约定：请求使用 `pageNum`、`pageSize`；响应包含 `total`、`records`。
- 权限约定：后端基于 `@PreAuthorize(...)`；前端通过指令 `v-permission` 控制元素显示。
- 时间约定：后端存储 `LocalDateTime`；VO 转毫秒时间戳（UTC+8）对前端友好。
- 日志约定：关键操作使用 `@SystemLog` 注解记录操作、用户与耗时。

## 状态与枚举对照

- AI 应用启用状态：`enabled`（1-启用，0-禁用）。
- 执行记录状态：`status`（1-成功，2-失败，3-进行中）。
- 任务状态：`status`（0-草稿，1-上线，2-下线，3-已结束）。
- 任务参与状态：`status`（1-已领取，2-已提交，3-审核通过，4-审核拒绝）。
- 奖励状态：`reward_status`（0-待发放，1-已发放，2-发放失败）。

## AI 辅助分析清单

- 本清单不随仓库分发，请在外部文档维护以节约 token。

## 接口示例与联调指南

- 执行 AI 应用
  - 请求：`POST /ai-application/v1/run/{id}`，Header `Authorization: Bearer <JWT>`，Body `{ "text": "..." }`
  - 响应：`{ code: 200, data: { ...result }, message: "success" }`
- 查询我的执行记录
  - 请求：`GET /ai-app-record/my?pageNum=1&pageSize=20`
  - 响应：`{ code: 200, data: { total, records: [...] } }`
- 任务参与审核
  - 请求：`POST /task-participation/{id}/audit`，Body `{ status: 3, auditNotes: "..." }`
  - 响应：`{ code: 200, message: "success" }`
- 联调流程
  - 后端启动：`mvn spring-boot:run`
  - 前端启动：`npm run dev`
  - 登录获取 JWT 后，前端存储并在 `request.js` 统一注入 `Authorization` 头；分页统一传 `pageNum/pageSize`；错误处理统一走 `message`。

## 常见问题

### Q: 如何添加新的 AI 应用类型？

A: 参考文档 [AI_APP_TYPE_DESIGN.md](./AI_APP_TYPE_DESIGN.md) 中的接口说明，通过管理界面或 API 创建新的应用类型。

### Q: 如何自定义执行器？

A: 实现 `AiApplicationHandler` 接口，并将其注册为 Spring Bean，然后在应用配置中指定执行器名称。

### Q: 如何配置权限？

A: 参考 [RBAC_MANAGEMENT_DESIGN.md](./RBAC_MANAGEMENT_DESIGN.md) 中的权限配置说明，通过角色和权限的关联实现访问控制。

## 文档特点

### 📋 完整的结构

每个设计文档都包含：

- **需求概述** - 明确功能目标和范围
- **数据库设计** - 详细的表结构设计
- **接口设计** - RESTful API 规范和说明
- **涉及文件清单** - 按类型分类的文件列表
- **核心代码示例** - 关键业务逻辑的实现
- **前端页面设计** - UI 界面设计图和组件说明
- **核心特性** - 功能亮点和技术特性
- **开发计划** - 分阶段的实施计划
- **待确认问题** - 需要进一步讨论的问题

### 🎨 丰富的示例

- **代码片段** - 精选的核心业务逻辑代码
- **界面设计图** - ASCII 艺术风格的 UI 设计
- **数据结构** - 清晰的表格展示
- **接口文档** - 详细的 API 说明

### 🛠️ 实用的指导

- **开发计划** - 分阶段的开发任务
- **权限控制** - 安全机制设计
- **性能优化** - 缓存和优化建议
- **扩展性** - 模块化和插件化设计

## 更新日志

### v1.1.0 (2025-01-01)

- 优化文档结构，采用文件清单替代完整代码
- 精简代码展示，突出核心业务逻辑
- 统一文档格式和风格
- 完善各模块的文件依赖关系
- 增强前端组件设计说明

### v1.0.0 (2025-01-01)

- 初始版本发布
- 完成 RBAC 权限管理
- 完成 AI 应用基础功能
- 完成 AI 工坊前端界面

## 联系方式

- 项目维护者: luoxiaodou
- 问题反馈: [GitHub Issues](https://github.com/your-repo/issues)
- 技术交流: [讨论区](https://github.com/your-repo/discussions)

---

**注意**: 本文档会随着项目发展持续更新，请关注最新版本。
