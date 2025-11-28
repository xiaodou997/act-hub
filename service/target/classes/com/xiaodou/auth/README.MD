# 认证授权模块 (auth) 设计文档

本文档详细介绍了 `auth` 模块的设计理念、核心流程、组件职责和未来扩展方向，旨在为开发者提供清晰的指引。

## 模块概述

本模块是基于 **Spring Security** 和 **JWT (JSON Web Token)** 构建的现代化、无状态认证授权中心。其核心特性包括：

- **无状态认证**: 完全基于 Token，服务器不存储 Session，易于水平扩展。
- **策略模式**: 采用策略模式（Strategy Pattern）将不同认证方式（如 JWT、API Key 等）解耦，具备优秀的扩展性。
- **职责分离**: 各组件职责清晰，配置、过滤器、处理器、策略等各司其职。
- **开发友好**: 提供统一的异常处理、便捷的用户信息获取工具，并与 SpringDoc (Swagger) 友好集成。

---

## 核心认证流程

所有受保护的 API 请求都会经过以下认证流程：

1.  **请求到达**: 客户端携带 `Authorization: Bearer <token>` 和 `X-Client-Type` 请求头访问 API。
2.  **过滤器拦截**: `JwtAuthenticationFilter` 拦截请求。
3.  **白名单检查**: 过滤器首先判断请求是否属于公开的白名单路径（由 `SecurityWhitelistConfig` 定义）。如果是，则直接放行，不进行认证。
4.  **上下文构建**: 对于非白名单请求，过滤器从请求头中提取凭证和客户端信息，构建一个 `AuthenticationContext` 对象。如果请求头缺失或格式错误，流程在此中断，并抛出异常。
5.  **策略选择**: `AuthenticationStrategyFactory` 根据 `AuthenticationContext` 的内容，从已注册的策略中选择一个合适的认证策略（当前默认为 `JwtTokenAuthenticationStrategy`）。
6.  **执行认证**: 选定的策略执行具体的认证逻辑：
    - **JWT 验证**: 校验 Token 的签名、有效期。
    - **信息解析**: 从 Token 中解析出 `LoginUser`（包含用户 ID、角色、权限等）。
    - **客户端验证**: 校验请求的客户端类型是否与 Token 中记录的一致。
    - **服务端状态验证**: 查询 Redis（通过 `TokenManager`）确认该 Token 未被主动注销（例如用户登出、被踢下线等）。
7.  **处理认证结果**:
    - **认证失败**: 任何一个验证步骤失败，都会抛出 `AuthenticationException`。该异常最终被 `JwtAuthenticationEntryPoint` 捕获，向客户端返回 `401 Unauthorized` 响应。
    - **认证成功**: 创建一个 `Authentication` 对象，并将其存入 `SecurityContextHolder`，标记当前请求为已认证状态。
8.  **权限校验**: 请求继续流转到控制器。如果控制器方法上使用了 `@PreAuthorize` 等注解，Spring Security 会在方法执行前检查当前用户是否具备所需权限。
    - **权限不足**: 如果权限不足，抛出 `AccessDeniedException`，最终被 `JwtAccessDeniedHandler` 捕获，向客户端返回 `403 Forbidden` 响应。
9.  **业务执行**: 权限校验通过后，执行业务逻辑。业务代码中可随时通过 `UserContextHolder` 获取当前登录用户的信息。

---

## 组件职责详解

为了提升代码的可读性和维护性，推荐将 `auth` 包下的文件按以下结构组织。这里按照推荐的结构对各组件进行说明。

### `config/` - 配置中心

存放所有与安全相关的 `@Configuration` 类。

- **`SecurityConfig.java`**: **安全总指挥**。组装所有安全组件，配置过滤器链、CORS、CSRF、会话管理、异常处理和路径权限规则。
- **`SecurityWhitelistConfig.java`**: **白名单管家**。集中定义所有无需认证即可访问的公开路径，与 `SecurityConfig` 解耦。
- **`PasswordConfig.java`**: **密码编码器**。提供 `DelegatingPasswordEncoder`，支持多种加密算法，是密码处理的最佳实践。
- **`WebCorsConfig.java`**: **跨域策略**。配置应用的跨域资源共享（CORS）规则。

### `filter/` - 认证过滤器

- **`JwtAuthenticationFilter.java`**: **认证流程的入口**。作为自定义的核心过滤器，它负责启动认证流程，并委托给具体的认证策略执行。

### `handler/` - 异常处理器

- **`JwtAuthenticationEntryPoint.java`**: **未认证处理器**。处理 `AuthenticationException`，当用户未登录或 Token 无效时，返回 401 响应。
- **`JwtAccessDeniedHandler.java`**: **权限不足处理器**。处理 `AccessDeniedException`，当用户已登录但角色/权限不足时，返回 403 响应。

### `strategy/` - 认证策略模式

这是框架最具扩展性的部分。

- **`AuthenticationStrategy.java`**: **策略接口**。定义了所有认证策略必须遵守的契约（`supports`, `authenticate`, `setSecurityContext`）。
- **`AuthenticationStrategyFactory.java`**: **策略工厂**。根据请求上下文，动态选择一个合适的认证策略。
- **`JwtTokenAuthenticationStrategy.java`**: **JWT 认证策略**。实现了基于 JWT Token 的完整认证逻辑。
- **`AuthenticationContext.java`**: **认证上下文**。一个不可变的 `record` 类，封装了单次认证所需的所有信息，在策略间传递。

### `util/` - 工具类

- **`UserContextHolder.java`**: **用户上下文工具**。提供静态方法，让业务代码能方便、安全地从 `SecurityContextHolder` 中获取当前用户信息，实现业务与安全框架的解耦。

### `exception/` - 自定义异常

- **`AuthenticationException.java`**: **自定义认证异常**。携带更丰富的请求上下文（如 URI、Method），使错误日志更具信息量，便于排查问题。

---

## 如何扩展

得益于策略模式，扩展新的认证方式非常简单。例如，要增加一种基于 `API-Key` 的认证：

1.  **创建新的认证策略**:
    ```java
    @Component
    public class ApiKeyAuthenticationStrategy implements AuthenticationStrategy {
        
        @Override
        public boolean supports(AuthenticationContext context) {
            // 判断是否为 API Key 认证，例如检查特定请求头
            return context.request().getHeader("X-Api-Key") != null;
        }

        @Override
        public LoginUser authenticate(AuthenticationContext context) {
            // 实现 API Key 的认证逻辑...
            // 1. 从 context 获取 API Key
            // 2. 查询数据库或缓存，验证 Key 的有效性
            // 3. 构建并返回 LoginUser 对象
        }

        @Override
        public void setSecurityContext(HttpServletRequest request, LoginUser loginUser) {
            // 设置安全上下文的逻辑（通常与 JWT 策略类似）
        }

        @Override
        public String getStrategyType() {
            return "API_KEY";
        }
    }
    ```
2.  **注册为 Bean**: 只需将新的策略类加上 `@Component` 注解，Spring 容器会自动扫描并注入到 `AuthenticationStrategyFactory` 的策略列表中。

无需修改任何现有代码，新的认证方式即可生效。工厂会自动根据 `supports` 方法的逻辑选择合适的策略。

---

## 关键请求头

| Header            | 作用                               | 示例                               | 是否必须 |
| ----------------- | ---------------------------------- | ---------------------------------- | -------- |
| `Authorization`   | 存放用户的身份凭证（JWT Token）    | `Bearer eyJhbGciOiJIUzI1Ni...`     | 是       |
| `X-Client-Type`   | 标识发起请求的客户端类型           | `web`, `app`, `plugin`             | 是       |
