# 结构化审计日志模块 (log) 设计文档

## 1. 模块概述

`log` 模块是一个功能强大、高度可扩展的**结构化审计日志系统**。它并非简单的文本日志记录工具，而是为满足**业务审计、操作追溯和安全监控**等高级需求而设计的。

### 核心特性

- **结构化日志**: 所有日志都以结构化的形式（如 JSON）存储，包含丰富的上下文信息（用户、IP、TraceID、业务ID等），便于后续的查询、分析和告警。
- **高性能 & 异步化**: 采用 Spring 的事件发布机制，将日志的持久化操作与主业务线程完全解耦。业务代码触发日志记录后立即返回，无 I/O 等待，对业务性能影响极小。
- **数据一致性**: 监听器绑定在数据库事务提交成功之后执行，确保只记录成功的业务操作，避免了业务回滚但日志却已保存的“脏数据”问题。
- **双模式记录**:
    1.  **声明式 (AOP)**: 通过 `@SystemLog` 注解，开发者可以零侵入式地为关键业务方法添加审计日志。
    2.  **编程式 (API)**: 提供流式（Fluent）API，允许在代码中灵活、精细地记录业务过程中的详细信息。
- **高扩展性**: 基于策略模式（Strategy Pattern），可以轻松扩展“用户信息获取”和“环境上下文获取”的逻辑，以适应不同的运行环境（如 HTTP 请求、消息队列、定时任务等）。

---

## 2. 两大核心使用方式

### 方式一：声明式日志 (AOP)

用于对**关键业务操作**进行高层次的审计。例如记录谁创建了什么、更新了什么。

**使用方法**: 在 Service 或 Controller 的方法上添加 `@SystemLog` 注解。

```java
import com.xiaodou.log.annotation.SystemLog;
import com.xiaodou.log.model.LogLevel;

@Service
public class UserService {

    @SystemLog(
        module = "用户管理",
        action = "创建用户",
        level = LogLevel.AUDIT,
        targetType = "User",
        targetId = "#result.id" // 使用SpEL表达式，从方法返回值中获取用户ID
    )
    public User createUser(CreateUserDTO userDTO) {
        // ... 创建用户的业务逻辑
        return savedUser;
    }
}
```

### 方式二：编程式日志

用于在复杂的业务逻辑**内部**，记录详细的过程、状态或调试信息。

**使用方法**: 通过 `@InjectLogger` 注解（或 `SystemLogFactory`）注入 `SystemLogger`，并使用其 `builder()` 方法。

```java
import com.xiaodou.log.annotation.InjectLogger;
import com.xiaodou.log.api.SystemLogger;

@Service
@InjectLogger("订单处理服务") // 注入一个模块名为"订单处理服务"的Logger
public class OrderService {

    private SystemLogger logger; // 会被自动注入

    public void processPayment(String orderId, PaymentInfo payment) {
        // 记录操作开始，并启动计时器
        TimeRecorder timer = logger.startTimer();

        try {
            logger.builder()
                .action("校验支付参数")
                .target("Order", orderId)
                .detail("paymentMethod", payment.getMethod())
                .info();

            // ... 执行支付逻辑 ...

            // 记录耗时和成功结果
            timer.record("支付成功", "订单 " + orderId + " 支付成功");

        } catch (PaymentException e) {
            // 记录耗时和异常信息
            timer.recordError("支付失败", "订单 " + orderId + " 支付失败", e);
            throw e;
        }
    }
}
```

---

## 3. 核心执行流程

无论是声明式还是编程式，日志的最终处理流程都是一致的：

1.  **触发记录**: `@SystemLog` 切面或业务代码调用 `SystemLogger` 的方法。
2.  **构建日志**: `DefaultSystemLogger` 作为核心实现，开始构建日志。
3.  **收集上下文**:
    - 调用 `UserInfoStrategy` 获取当前操作者信息（例如从 SecurityContext 获取已登录用户）。
    - 调用 `LogContextStrategy` 获取当前环境信息（例如从 HTTP 请求中获取 IP、TraceID、User-Agent 等）。
4.  **组装实体**: 将所有信息（模块、操作、用户、IP、业务详情、异常等）组装成一个 `SystemLog` 实体对象。
5.  **发布事件**: `DefaultSystemLogger` 将 `SystemLog` 实体封装成一个 `SystemLogEvent`，并将其发布到 Spring 的应用事件总线。**此时，业务线程的工作已完成并立即返回。**
6.  **异步监听**: `SystemLogEventListener` 在一个独立的线程池 (`@Async`) 中监听到该事件。
7.  **等待事务**: 监听器被配置为在**主业务的数据库事务成功提交后 (`AFTER_COMMIT`)** 才开始执行。
8.  **持久化**: 监听到事件后，调用 `SystemLogMapper` 将 `SystemLog` 实体异步地写入数据库。

---

## 4. 模块结构详解

本模块采用职责分离的包结构设计，清晰地划分了模块的各个功能部分。

- **`log/`**
    - **`api/`**: **公共API层**。包含了开发者直接使用的所有接口和工厂，是模块的统一入口。
        - `SystemLogger.java`: 定义了日志记录器的核心接口，提供了便捷方法和构建器模式。
        - `LogBuilder.java`: 定义了流式（Fluent）构建日志内容的接口。
        - `TimeRecorder.java`: 定义了代码块耗时记录器的接口。
        - `SystemLogFactory.java`: 获取 `SystemLogger` 实例的统一工厂，是与业务代码交互的主要入口。

    - **`annotation/`**: **注解定义层**。存放所有用于 AOP 和依赖注入的注解。
        - `SystemLog.java`: 声明式审计日志的核心注解，用于标记需要被自动记录日志的方法。
        - `InjectLogger.java`: 辅助依赖注入的注解，用于在类中自动注入 `SystemLogger` 实例。

    - **`aspect/`**: **AOP实现层**。包含注解的处理器，如切面和 Bean 后置处理器。
        - `SystemLogAspect.java`: `@SystemLog` 注解的切面实现，负责拦截方法、收集信息并触发日志记录。
        - `InjectLoggerBeanPostProcessor.java`: `@InjectLogger` 注解的处理器，通过 Spring 的 `BeanPostProcessor` 机制实现 `SystemLogger` 的自动注入。

    - **`core/`**: **核心实现层**。模块的心脏，包含核心实现类和事件驱动相关类。
        - `DefaultSystemLogger.java`: `SystemLogger` 接口的默认实现，是日志构建和事件发布的中心。
        - `SystemLogEvent.java`: 日志事件对象，用于在业务线程和日志持久化线程之间传递日志数据。
        - `SystemLogEventListener.java`: 日志事件监听器，负责异步、安全地将日志事件持久化到数据库。

    - **`model/`**: **数据模型层**。定义了日志系统内部流转的、不可变的 `record` 数据模型。
        - `LogUser.java`, `LogContext.java`, `LogLevel.java` 等。

    - **`strategy/`**: **策略实现层**。将易变的信息获取逻辑（如获取用户、获取IP）抽象为策略，易于扩展。
        - **`context/`**: **环境上下文策略**
            - `LogContextStrategy.java`: 定义了获取环境信息（如IP、TraceID）的策略接口。
            - `HttpRequestContextStrategy.java`: 从 HTTP 请求中获取环境信息的具体实现。
            - `EmptyContextStrategy.java`: 提供一个空的上下文，用于非 HTTP 环境（如后台任务）。
        - **`userinfo/`**: **用户信息策略**
            - `UserInfoStrategy.java`: 定义了获取当前操作者信息的策略接口。
            - `SecurityContextUserStrategy.java`: 从 Spring Security 上下文中获取用户信息的具体实现。
            - `ManualUserStrategy.java`: 使用手动指定的用户信息。
            - `AnonymousUserStrategy.java`: 提供匿名用户信息。

    - **`extension/`**: **框架扩展层**。存放与特定第三方框架集成的代码。
        - **`logback/`**:
            - `MaskingPatternLayout.java`: 对 Logback 框架的扩展，用于在日志输出到文件或控制台时，对敏感信息进行脱敏。

---

## 5. 如何扩展

本模块的扩展性极佳。例如，当你的应用需要从**消息队列（MQ）的消费者**中记录日志时，默认的 `HttpRequestContextStrategy` 无法获取 IP 等信息。此时你可以轻松扩展：

1.  **创建一个新的上下文策略**:

    ```java
    @Component
    public class MqContextStrategy implements LogContextStrategy {
        @Override
        public LogContext resolveContext() {
            // 逻辑：从 MQ 消息的 header 中获取 traceId, spanId等
            String traceId = MqContextHolder.getHeader("trace-id");
            // ...
            return new LogContext(traceId, ...);
        }
    }
    ```

2.  **在消费逻辑中使用**:
    在 MQ 消费者中，没有 Spring Security 上下文，也没有 HTTP 请求。你可以组合使用不同的策略来获取一个定制的 Logger。

    ```java
    @Component
    public class OrderEventConsumer {
        @Autowired
        private SystemLogFactory logFactory;
        @Autowired
        private MqContextStrategy mqContextStrategy;

        public void handle(OrderEvent event) {
            // 手动指定操作者，并使用自定义的MQ上下文策略
            UserInfoStrategy userStrategy = new ManualUserStrategy(event.getOperatorId(), event.getTenantId());
            SystemLogger logger = logFactory.getLogger("订单MQ消费", userStrategy, mqContextStrategy);

            logger.info("开始处理订单创建事件");
            // ...
        }
    }
    ```

---

## 6. 日志脱敏功能

`MaskingPatternLayout` 是一个独立的、作用于 **Logback 输出层面** 的功能。它可以在日志写入**控制台或文件**时，对敏感信息进行脱敏，防止密码、手机号等信息在日志文件中泄露。

它与上面描述的结构化审计日志是两个互补的系统。

**如何配置**: 在 `logback-spring.xml` 文件中，将 `layout` 的 class 指向它，并配置你自己的脱敏规则。

```xml
<appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
    <encoder>
        <!-- 使用自定义的脱敏 Layout -->
        <layout class="com.xiaodou.log.extension.logback.MaskingPatternLayout">
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
            <!-- 定义手机号脱敏规则：捕获第4位到第7位的数字 -->
            <maskPattern>(\d{3})\d{4}(\d{4})</maskPattern>
             <!-- 定义邮箱脱敏规则 -->
            <maskPattern>(\b[A-Za-z0-9._%+-]+)@[A-Za-z0-9.-]+(\.[A-Z|a-z]{2,}\b)</maskPattern>
        </layout>
    </encoder>
</appender>
```
以上配置会将日志中的 `13812345678` 显示为 `138****5678`。
