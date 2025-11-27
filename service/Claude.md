# ActHub Service - 项目开发文档

## 项目简介

ActHub Service 是一个基于 Spring Boot 3 的后端服务项目，提供用户管理、权限管理、菜单管理、AI应用管理等核心功能的综合系统。

## 技术栈

- **框架**: Spring Boot 3.x
- **数据库**: MySQL + MyBatis-Plus
- **缓存**: Redis
- **安全**: Spring Security6 + JWT
- **API文档**: Swagger/OpenAPI 3.0
- **消息队列**: RabbitMQ
- **分布式追踪**: OpenTelemetry
- **JSON Schema 验证**: networknt/json-schema-validator
- **IP 地理位置**: ip2region
- **密码加密**: Argon2

## 主要功能模块

### 1. 用户管理
- 用户注册、登录、密码管理
- 用户信息查询、更新
- 用户权限验证

### 2. 权限管理
- 基于角色的访问控制（RBAC）
- 用户-角色-权限的关联管理
- 菜单权限控制

### 3. 菜单管理
- 菜单树结构管理
- 动态菜单加载
- 权限与菜单关联

### 4. AI应用管理
- AI应用创建、配置和管理
- AI应用执行记录管理
- AI工作流执行器
- 参数配置和验证（JSON Schema）

### 5. AI应用执行记录管理
- 记录AI应用的执行历史
- 执行状态跟踪（成功、失败、进行中）
- 执行时间统计

### 6. 系统管理
- 系统日志记录
- 操作审计
- 性能监控

## 核心开发规范

### 1. 时间字段处理规范 ⭐

**重要原则：后端接口与前端交互时，涉及到时间字段必须使用毫秒时间戳**

实现方式：所有包含时间字段的实体类（Entity）在返回给前端时，需要创建对应的 VO（View Object）类，使用 `DateTimeUtils` 工具类进行转换。

#### DateTimeUtils 工具类

```java
// LocalDateTime 转时间戳（毫秒）
DateTimeUtils.toTimestamp(LocalDateTime dateTime)

// 时间戳（毫秒）转 LocalDateTime
DateTimeUtils.toLocalDateTime(long timestamp)
```

示例：
```java
// VO类示例
@Data
public class UserVO {
    private String id;
    private String username;
    private Long createdAt;  // 使用Long类型存储毫秒时间戳
    private Long updatedAt;

    public static UserVO fromEntity(User user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setCreatedAt(DateTimeUtils.toTimestamp(user.getCreatedAt()));
        vo.setUpdatedAt(DateTimeUtils.toTimestamp(user.getUpdatedAt()));
        return vo;
    }
}
```

### 2. 权限控制规范

系统采用 RBAC 模型：用户 ↔ 角色 ↔ 权限 ↔ 菜单

使用 `@PreAuthorize` 注解控制访问权限：
```java
// 需要超级管理员权限
@PreAuthorize("hasRole('SUPER_ADMIN')")

// 需要特定权限
@PreAuthorize("hasAuthority('user:manage')")
```

### 3. DTO 规范

- **CreateDTO**: 创建操作的请求对象
- **UpdateDTO**: 更新操作的请求对象
- **QueryDTO**: 查询操作的请求对象
- **VO (View Object)**: 返回给前端的视图对象
- 使用 `@Valid` 进行参数校验

### 4. 统一响应格式

所有接口使用 `Result` 包装响应：

```java
// 成功响应
return Result.success(data);

// 失败响应
return Result.fail("错误信息");

// 业务异常
throw new AppException(ResultCodeEnum.DATA_NOT_FOUND, "数据不存在");
```

### 5. 日志记录

使用 `@SystemLog` 注解记录重要操作：
```java
@SystemLog(module = "用户管理", action = "创建用户", recordResponse = true)
public Result<UserVO> create(@Valid @RequestBody UserCreateDTO dto) {
    // ...
}
```

### 6. AI应用执行规范

项目支持多种AI应用执行器：
- **工作流执行器**: 支持同步和异步工作流执行
- **HTTP请求执行器**: 用于API调用
- **TikHub执行器**: 用于特定AI服务

## JSON Schema 参数验证

AI应用支持参数配置和验证，通过JSON Schema定义参数格式：

```java
// 参数验证示例
JsonSchemaUtils.validate(jsonSchema, inputParams);
```

## 项目结构

```
src/main/java/com/xiaodou/
├── aiapp/              # AI应用相关
│   ├── handler/       # AI应用执行器
│   └── JsonSchemaUtils.java  # JSON Schema工具
├── auth/               # 认证授权
├── config/             # 配置类
├── controller/         # 控制器层
├── exception/          # 异常处理
├── log/                # 系统日志
├── mapper/             # 数据访问层
├── model/              # 数据模型
│   ├── dto/           # 数据传输对象
│   └── vo/            # 视图对象
├── service/            # 业务逻辑层
├── utils/              # 工具类
└── ActhubServiceApplication.java  # 启动类
```

## 开发流程

1. **设计数据模型** - 创建 Entity 类
2. **创建 Mapper** - 继承 `BaseMapper<T>`
3. **定义 DTO** - CreateDTO、UpdateDTO、VO
4. **实现 Service** - 业务逻辑
5. **开发 Controller** - REST 接口
6. **添加权限控制** - `@PreAuthorize`
7. **添加日志记录** - `@SystemLog`
8. **时间字段转换** - 使用 VO + DateTimeUtils ⭐

## 注意事项

1. **时间字段**: 所有时间字段在返回给前端时必须转换为毫秒时间戳
2. **安全性**: 涉及敏感操作的接口必须添加权限控制
3. **事务管理**: 涉及多表操作的方法添加 `@Transactional`
4. **数据校验**: DTO 使用 JSR-303 注解进行参数校验
5. **日志记录**: 重要操作添加 `@SystemLog` 注解
6. **AI应用**: 配置参数需符合JSON Schema规范

## 常用命令

```bash
# 启动项目
mvn spring-boot:run

# 打包项目
mvn clean package -DskipTests

# 编译项目
mvn compile

# 运行测试
mvn test
```

---

**最后更新**: 2025-11-27
**维护者**: xiaodou
