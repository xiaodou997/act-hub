# 概览
按你确认的方案启动首版实现（JS + Vue3 + Vite + Element Plus，接口前缀 `/act`，前端静态菜单 `menu.js` + `v-auth` 指令，Mock 开发，ECharts 仪表盘最后阶段）。`UserInfoVO` 增加 `menuKeys: string[]`，用于高亮/默认展开；菜单的实际访问控制基于 `permissions`。

## 路由与菜单
- 静态菜单（`menu.js`）使用 `key/title/path/icon/permissions/children/hidden` 字段。
- 登录后根据 `permissions` 过滤菜单与路由，403 拦截无权访问。
- 示例菜单键：
  - `dashboard`（`/dashboard`，`['dashboard:view']`）
  - `system:user`（`/users`，`['user:list']`）
  - `system:role`（`/roles`，`['role:list']`）
  - `system:menu`（`/menus`，`['menu:list']`，只读展示）
  - `system:logs`（`/logs`，`['log:list']`）
  - `task`（`/tasks`，`['task:list']`）
  - `resource`（`/resources`，`['resource:list']`）
  - `schedule`（`/schedules`，`['schedule:list']`）

## 权限点（默认集合）
- 通用：`dashboard:view`, `dict:view`
- 用户：`user:list`, `user:view`, `user:create`, `user:update`, `user:disable`, `user:resetPwd`, `user:export`
- 角色：`role:list`, `role:view`, `role:create`, `role:update`, `role:delete`, `role:assignPerm`
- 菜单：`menu:list`, `menu:view`, `menu:update`
- 日志：`log:list`, `log:view`
- 任务：`task:list`, `task:view`, `task:create`, `task:update`, `task:assign`, `task:finish`, `task:archive`, `task:notify`, `task:export`
- 资源：`resource:list`, `resource:view`, `resource:create`, `resource:update`, `resource:uploadImage`, `resource:export`
- 调度：`schedule:list`, `schedule:view`, `schedule:create`, `schedule:update`
- 上传/通知：`upload:image`, `notification:send`

## 接口清单（前缀均为 `/act`）
### 鉴权
- `POST /act/auth/login`：登录（账号/密码）
  - 请求：`{ username, password }`
  - 响应：`{ token, userInfo: UserInfoVO }`（新增 `menuKeys: string[]`）
- `POST /act/auth/logout`（可选）：登出
- `GET /act/users/me`：当前用户信息

### 字典（分模块，返回统一字典）
- `GET /act/dict/system`
- `GET /act/dict/task`
- `GET /act/dict/resource`

### 用户
- `GET /act/users`：列表
  - 查询：`page, pageSize(默认20), keyword?, status?, roleId?`
  - 响应：分页结构：`{ list, page, pageSize, total }`
- `GET /act/users/{id}`：详情
- `POST /act/users`：新增
- `PUT /act/users/{id}`：编辑
- `PATCH /act/users/{id}/disable`：启用/禁用（`{ disable: boolean }`）
- `POST /act/users/{id}/reset-password`：重置密码

### 角色
- `GET /act/roles`：列表
- `GET /act/roles/{id}`：详情
- `POST /act/roles`：新增
- `PUT /act/roles/{id}`：编辑
- `DELETE /act/roles/{id}`：删除
- `POST /act/roles/{id}/permissions`：分配权限点（`{ permissions: string[] }`）
- `GET /act/permissions`：权限点清单（可选，便于前端渲染选择项）

### 菜单（当前仅静态，以下为预留）
- `GET /act/menus`（可选）：菜单树（`id/parentId/title/code/path/permissions/...`）

### 日志中心
- `GET /act/logs`：列表/筛选
  - 查询：`page, pageSize, level?, module?, operatorUserId?, success?, startAt?, endAt?`
- `GET /act/logs/{id}`：详情

### 任务
- `GET /act/tasks`：列表/筛选
  - 查询：`page, pageSize, status?, assigneeId?, keyword?, startAt?, endAt?`
- `GET /act/tasks/{id}`：详情
- `POST /act/tasks`：创建（`{ title, assignees, priority, startAt?, endAt?, description?, tags? }`）
- `PUT /act/tasks/{id}`：编辑
- `POST /act/tasks/{id}/assign`：分配（`{ assigneeIds: string[] }`）
- `POST /act/tasks/{id}/status`：流转（`{ status: 'DRAFT'|'IN_PROGRESS'|'DONE'|'ARCHIVED' }`）
- `POST /act/notifications/task`：通知（`{ taskId, type: 'ASSIGN'|'STATUS_CHANGE', receivers: string[], message? }`）

### 资源（商品/物料）
- `GET /act/resources`：列表/筛选（`page, pageSize, category?, status?, keyword?`）
- `GET /act/resources/{id}`：详情
- `POST /act/resources`：新增（含 `images[]`）
- `PUT /act/resources/{id}`：编辑
- `DELETE /act/resources/{id}`（可选）
- 上传图片相关：
  - `POST /act/upload/image`（`multipart/form-data`，字段 `file`，≤10MB）
    - 响应：`{ id, url, width, height, size, mimeType, hash }`
  - `GET /act/upload/image/{id}`：获取图片信息
  - `DELETE /act/upload/image/{id}`：删除

### 调度中心
- `GET /act/schedules`
- `GET /act/schedules/{id}`
- `POST /act/schedules`
- `PUT /act/schedules/{id}`
- `DELETE /act/schedules/{id}`（可选）

## 响应与错误码
- 统一响应：`{ code: number, message?: string, data?: T }`
- 分页响应：`{ list: T[], page: number, pageSize: number, total: number }`
- 成功约定：`code === 0`；失败弹出统一错误提示（沿用后端错误码规范）。

## `v-auth` 指令
- 用法：`v-auth="'user:create'"` 或 `v-auth="['user:create','user:update']"`
- 行为：默认隐藏；可在指令参数中支持修饰符设置 `disabled`。
- 数据源：`permissionStore.permissions`；与路由 `meta.permissions` 联动。

## Mock 方案（默认）
- 优先使用 `vite-plugin-mock`（JS 文件定义路由与返回体，开发环境生效，统一前缀 `/act`）。
- 备选：Mock Service Worker（MSW），如不想引入插件也可用纯 JS 拦截。

## 首版交付内容
- 项目基座：Vite + Vue3 + JS，Element Plus 主题浅蓝，Axios 拦截器（注入 `Authorization: Bearer token`），`/act` 基地址。
- 鉴权：登录页；`POST /act/auth/login` 与 `GET /act/users/me` 接入；登录态与跳转。
- RBAC：`menu.js` 静态菜单、路由守卫、菜单过滤、`v-auth` 指令；403 无权页。
- Mock：为以上接口提供基础 Mock 数据，确保首版可运行。

## 需要你确认的点
- `UserInfoVO.menuKeys: string[]` 字段名确认。
- 是否返回 `permissions` 与 `menuKeys` 一并在 `GET /act/users/me` 中提供，以便刷新登录态。
- Mock 采用 `vite-plugin-mock` 是否可以；若需改用 MSW 请说明。

> 确认后我将按该计划开始实现，并交付首个可运行版本（基座 + 鉴权 + RBAC + Mock）。