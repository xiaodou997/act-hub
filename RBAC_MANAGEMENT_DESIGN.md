# RBAC 权限管理功能 - 设计方案

## 一、需求概述

RBAC（Role-Based Access Control）权限管理模块用于实现基于角色的访问控制，包括：

- **RBAC-01**: 用户管理
- **RBAC-02**: 角色管理
- **RBAC-03**: 权限管理
- **RBAC-04**: 菜单管理

## 二、数据库设计

### 1. user - 用户表

| 字段          | 类型         | 说明                |
| ------------- | ------------ | ------------------- |
| id            | varchar(32)  | 主键 ID             |
| tenant_id     | varchar(32)  | 租户 ID             |
| username      | varchar(100) | 用户名              |
| password      | varchar(255) | 密码（加密存储）    |
| nickname      | varchar(100) | 昵称                |
| avatar        | varchar(500) | 头像 URL            |
| email         | varchar(200) | 邮箱                |
| phone         | varchar(20)  | 手机号              |
| status        | tinyint      | 状态：1-正常,0-禁用 |
| last_login_at | datetime     | 最后登录时间        |
| created_at    | datetime     | 创建时间            |
| updated_at    | datetime     | 更新时间            |

### 2. role - 角色表

| 字段        | 类型         | 说明                |
| ----------- | ------------ | ------------------- |
| id          | varchar(32)  | 主键 ID             |
| tenant_id   | varchar(32)  | 租户 ID             |
| name        | varchar(100) | 角色名称            |
| code        | varchar(50)  | 角色编码（唯一）    |
| description | text         | 角色描述            |
| status      | tinyint      | 状态：1-启用,0-禁用 |
| created_at  | datetime     | 创建时间            |
| updated_at  | datetime     | 更新时间            |

### 3. permission - 权限表

| 字段        | 类型         | 说明                          |
| ----------- | ------------ | ----------------------------- |
| id          | varchar(32)  | 主键 ID                       |
| name        | varchar(100) | 权限名称                      |
| code        | varchar(100) | 权限编码（唯一标识）          |
| type        | tinyint      | 类型：1-菜单,2-操作按钮,3-API |
| description | varchar(500) | 权限描述                      |
| created_at  | datetime     | 创建时间                      |

### 4. user_role - 用户角色关联表

| 字段       | 类型        | 说明     |
| ---------- | ----------- | -------- |
| id         | varchar(32) | 主键 ID  |
| user_id    | varchar(32) | 用户 ID  |
| role_id    | varchar(32) | 角色 ID  |
| created_at | datetime    | 创建时间 |

### 5. role_permission - 角色权限关联表

| 字段          | 类型        | 说明     |
| ------------- | ----------- | -------- |
| id            | varchar(32) | 主键 ID  |
| role_id       | varchar(32) | 角色 ID  |
| permission_id | varchar(32) | 权限 ID  |
| created_at    | datetime    | 创建时间 |

### 6. menu - 菜单权限表

| 字段            | 类型         | 说明                               |
| --------------- | ------------ | ---------------------------------- |
| id              | varchar(32)  | 主键 ID                            |
| parent_id       | varchar(32)  | 父菜单 ID，0 表示为顶级菜单        |
| name            | varchar(100) | 菜单名称                           |
| type            | tinyint      | 菜单类型（0:目录, 1:菜单, 2:按钮） |
| path            | varchar(200) | 路由地址                           |
| component_name  | varchar(100) | 组件键名                           |
| permission_code | varchar(100) | 权限标识                           |
| icon            | varchar(100) | 菜单图标                           |
| sort_order      | int          | 显示排序                           |
| status          | tinyint      | 菜单状态（1:正常, 0:禁用）         |
| is_visible      | tinyint      | 是否可见（1:可见, 0:隐藏）         |
| created_at      | datetime     | 创建时间                           |
| updated_at      | datetime     | 更新时间                           |

## 三、接口设计

### RBAC-01: 用户管理接口

#### UserController

| 接口                | 方法   | 说明             |
| ------------------- | ------ | ---------------- |
| `/user/page`        | GET    | 分页查询用户列表 |
| `/user/{id}`        | GET    | 获取用户详情     |
| `/user`             | POST   | 创建用户         |
| `/user/{id}`        | PUT    | 更新用户         |
| `/user/{id}`        | DELETE | 删除用户         |
| `/user/{id}/status` | PUT    | 变更用户状态     |
| `/user/{id}/roles`  | GET    | 获取用户角色     |
| `/user/{id}/roles`  | POST   | 分配用户角色     |

### RBAC-02: 角色管理接口

#### RoleController

| 接口                     | 方法   | 说明             |
| ------------------------ | ------ | ---------------- |
| `/role/page`             | GET    | 分页查询角色列表 |
| `/role/{id}`             | GET    | 获取角色详情     |
| `/role`                  | POST   | 创建角色         |
| `/role/{id}`             | PUT    | 更新角色         |
| `/role/{id}`             | DELETE | 删除角色         |
| `/role/{id}/permissions` | GET    | 获取角色权限     |
| `/role/permissions`      | POST   | 为角色分配权限   |

### RBAC-03: 权限管理接口

#### PermissionController

| 接口               | 方法   | 说明                       |
| ------------------ | ------ | -------------------------- |
| `/permission/page` | GET    | 分页查询权限列表           |
| `/permission/list` | GET    | 获取所有权限列表（不分页） |
| `/permission/{id}` | GET    | 获取权限详情               |
| `/permission`      | POST   | 创建权限                   |
| `/permission`      | PUT    | 更新权限                   |
| `/permission/{id}` | DELETE | 删除权限                   |

### RBAC-04: 菜单管理接口

#### MenuController

| 接口         | 方法   | 说明           |
| ------------ | ------ | -------------- |
| `/menu/tree` | GET    | 获取菜单树结构 |
| `/menu/{id}` | GET    | 获取菜单详情   |
| `/menu`      | POST   | 创建菜单       |
| `/menu/{id}` | PUT    | 更新菜单       |
| `/menu/{id}` | DELETE | 删除菜单       |

## 四、后端代码实现

### 涉及文件清单

#### Controller 层

- `UserController.java` - 用户管理控制器
- `RoleController.java` - 角色管理控制器
- `PermissionController.java` - 权限管理控制器
- `MenuController.java` - 菜单管理控制器

#### Service 层

- `UserService.java` - 用户服务接口
- `RoleService.java` - 角色服务接口
- `PermissionService.java` - 权限服务接口
- `MenuService.java` - 菜单服务接口

#### 实现类

- `UserServiceImpl.java` - 用户服务实现
- `RoleServiceImpl.java` - 角色服务实现
- `PermissionServiceImpl.java` - 权限服务实现
- `MenuServiceImpl.java` - 菜单服务实现

#### 数据传输对象

- `dto/user/UserCreateDTO.java` - 用户创建 DTO
- `dto/user/UserUpdateDTO.java` - 用户更新 DTO
- `dto/role/RoleCreateDTO.java` - 角色创建 DTO
- `dto/role/RoleUpdateDTO.java` - 角色更新 DTO
- `dto/role/RolePermissionDTO.java` - 角色权限分配 DTO
- `dto/permission/PermissionCreateDTO.java` - 权限创建 DTO
- `dto/permission/PermissionUpdateDTO.java` - 权限更新 DTO

#### 视图对象

- `vo/UserVO.java` - 用户视图对象
- `vo/RoleVO.java` - 角色视图对象
- `vo/PermissionVO.java` - 权限视图对象
- `vo/MenuVO.java` - 菜单视图对象

#### 实体类

- `model/User.java` - 用户实体
- `model/Role.java` - 角色实体
- `model/Permission.java` - 权限实体
- `model/Menu.java` - 菜单实体
- `model/UserRole.java` - 用户角色关联实体
- `model/RolePermission.java` - 角色权限关联实体

#### Mapper 接口

- `mapper/UserMapper.java` - 用户数据访问接口
- `mapper/RoleMapper.java` - 角色数据访问接口
- `mapper/PermissionMapper.java` - 权限数据访问接口
- `mapper/MenuMapper.java` - 菜单数据访问接口

### 核心代码示例

#### UserController 关键方法

```java
@RestController
@RequestMapping("/user")
public class UserController {

    @GetMapping("/page")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<IPage<UserVO>> page(UserQuery query) {
        return Result.success(userService.pageListUsers(query));
    }

    @PostMapping("/{id}/roles")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<Void> assignRoles(@PathVariable String id, @RequestBody List<String> roleIds) {
        userService.assignRoles(id, roleIds);
        return Result.success();
    }
}
```

### 流程图

```mermaid
flowchart TD
  L[登录] --> AUTH[JWT认证]
  AUTH --> LOAD[加载用户角色与权限]
  LOAD --> MENU[动态菜单 /menu/tree]
  MENU --> UI[前端路由与按钮权限]
  UI --> ACT[用户发起操作]
  ACT --> CHECK[后端@PreAuthorize 权限校验]
  CHECK -->|通过| OK[执行接口]
  CHECK -->|拒绝| DENY[403 无权限]

  RA[角色管理] --> RP[分配权限 /role/permissions]
  RP --> CACHE[更新权限映射]
  CACHE --> EFFECT[下次请求生效]
```

#### RoleController 关键方法

```java
@RestController
@RequestMapping("/role")
public class RoleController {

    @PostMapping("/permissions")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<Void> assignPermissions(@RequestBody RolePermissionDTO dto) {
        roleService.assignPermissions(dto.getRoleId(), dto.getPermissionIds());
        return Result.success();
    }
}
```

## 五、前端页面设计

### 页面清单

| 页面     | 路由        | 组件名               | 说明               |
| -------- | ----------- | -------------------- | ------------------ |
| 用户列表 | /user       | UserManagement       | 用户列表、创建入口 |
| 角色列表 | /role       | RoleManagement       | 角色列表、权限分配 |
| 权限列表 | /permission | PermissionManagement | 权限列表、管理     |
| 菜单管理 | /menu       | MenuManagement       | 菜单树、编辑       |

### 用户管理页面效果

```
┌─────────────────────────────────────────────────────────────────────────────┐
│ 用户管理                                              [+ 新建用户]          │
├─────────────────────────────────────────────────────────────────────────────┤
│ 用户名: [__________]  状态: [全部 ▼]  邮箱: [__________]  [搜索] [重置]     │
├─────────────────────────────────────────────────────────────────────────────┤
│ # │ 用户名    │ 邮箱              │ 状态  │ 最后登录时间    │ 操作        │
│───┼───────────┼──────────────────┼───────┼─────────────────┼─────────────│
│ 1 │ admin     │ admin@example.com │ 正常  │ 2025-01-01 10:00│ 编辑 删除   │
│ 2 │ user001   │ user001@qq.com    │ 禁用  │ 2024-12-25 15:30│ 编辑 删除   │
│ 3 │ user002   │ user002@163.com   │ 正常  │ 2025-01-02 09:15│ 编辑 删除   │
├─────────────────────────────────────────────────────────────────────────────┤
│                              [1] [2] [3] ... [10]    共 98 条               │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 角色管理页面效果

```
┌─────────────────────────────────────────────────────────────────────────────┐
│ 角色管理                                              [+ 新建角色]          │
├─────────────────────────────────────────────────────────────────────────────┤
│ 角色名称: [__________]  角色编码: [__________]  [搜索] [重置]                │
├─────────────────────────────────────────────────────────────────────────────┤
│ # │ 角色名称    │ 角色编码   │ 描述              │ 操作                    │
│───┼───────────┼──────────┼──────────────────┼─────────────────────────│
│ 1 │ 超级管理员  │ SUPER_ADMIN│ 系统超级管理员    │ 编辑 分配权限 删除     │
│ 2 │ 普通管理员  │ ADMIN      │ 普通管理员        │ 编辑 分配权限 删除     │
│ 3 │ 普通用户    │ USER       | 普通用户          │ 编辑 分配权限 删除     │
├─────────────────────────────────────────────────────────────────────────────┤
│                              [1] [2] [3] ... [10]    共 5 条                │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│ 分配权限 - "超级管理员"                                           [×]       │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  权限类型: [全部 ▼]                                                         │
│                                                                             │
│  ☑ 用户管理    ☑ 角色管理    ☑ 权限管理    ☑ 菜单管理                      │
│  ☑ 用户查看    ☑ 用户创建    ☑ 用户编辑    ☑ 用户删除                      │
│  ☑ 角色查看    ☑ 角色创建    ☑ 角色编辑    ☑ 角色删除                      │
│  ☑ 权限查看    ☑ 权限创建    ☑ 权限编辑    ☑ 权限删除                      │
│                                                                             │
│                                               [取消]  [确认分配]            │
└─────────────────────────────────────────────────────────────────────────────┘
```

## 六、核心特性

### 1. 权限控制机制

- **基于角色的访问控制**：用户通过角色获得权限
- **细粒度权限**：支持菜单、操作按钮、API 三级权限控制
- **动态权限加载**：用户登录时动态加载权限列表
- **前端权限控制**：根据权限控制页面元素显示隐藏

### 2. 安全认证

- **JWT Token 认证**：基于 JSON Web Token 的无状态认证
- **密码加密存储**：使用 BCrypt 算法加密用户密码
- **登录状态管理**：支持 Token 过期和刷新机制

### 3. 审计日志

- **操作日志记录**：记录用户所有关键操作
- **权限变更追踪**：记录角色权限分配历史
- **登录日志**：记录用户登录时间和 IP 地址

## 七、菜单配置

| 菜单名称 | 路由地址           | 组件键名             | 权限标识        | 图标      |
| -------- | ------------------ | -------------------- | --------------- | --------- |
| 系统管理 | /system            | -                    | -               | Setting   |
| 用户管理 | /system/user       | UserManagement       | user:view       | User      |
| 角色管理 | /system/role       | RoleManagement       | role:view       | UserGroup |
| 权限管理 | /system/permission | PermissionManagement | permission:view | Key       |
| 菜单管理 | /system/menu       | MenuManagement       | menu:view       | Menu      |

## 八、开发计划

### 第一阶段：基础功能（预计 2 天）

- [ ] 用户 CRUD 功能完善
- [ ] 角色 CRUD 功能完善
- [ ] 权限 CRUD 功能完善
- [ ] 前端页面基础功能

### 第二阶段：权限分配（预计 1 天）

- [ ] 用户角色分配功能
- [ ] 角色权限分配功能
- [ ] 权限验证机制

### 第三阶段：菜单管理（预计 1 天）

- [ ] 菜单树管理功能
- [ ] 前端路由动态加载
- [ ] 菜单权限控制

## 九、待确认问题

1. **密码策略**：是否需要设置密码复杂度要求？ 答：不需要
2. **登录限制**：是否需要实现登录失败锁定机制？答：不需要
3. **租户隔离**：多租户场景下的权限隔离策略？ 答：使用租户 ID 进行权限隔离，当前阶段没有租户划分，都使用默认租户 ID 1
4. **权限缓存**：权限信息是否需要缓存机制提高性能？答：不需要
