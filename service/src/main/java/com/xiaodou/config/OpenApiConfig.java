package com.xiaodou.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI 配置类，用于生成 Swagger/OpenAPI 文档
 *
 * <p>本配置类定义 API 文档的基本信息，包括：
 * <ul>
 *   <li>API 标题</li>
 *   <li>API 版本</li>
 *   <li>API 描述信息</li>
 * </ul>
 *
 * @author xiaodou
 * @version 1.0.0
 * @since 2025/5/10
 */
@Configuration
public class OpenApiConfig {

    private static final String TOKEN_HEADER = "Authorization";
    private static final String CLIENT_TYPE_HEADER = "X-Client-Type";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI().info(new Info().title("AI 服务接口文档")
                .description(
                    "AI 微服务 RESTful API 文档\n\n提供以下主要功能：\n- 自然语言处理\n- 机器学习模型调用\n- 数据分析服务")
                .version("1.0.0"))
            .components(new Components()
                // 定义 Authorization 请求头
                .addSecuritySchemes(TOKEN_HEADER, new SecurityScheme().type(SecurityScheme.Type.APIKEY)
                    .in(SecurityScheme.In.HEADER)
                    .name(TOKEN_HEADER))
                // 定义 X-Client-Type 请求头
                .addSecuritySchemes(CLIENT_TYPE_HEADER, new SecurityScheme().type(SecurityScheme.Type.APIKEY)
                    .in(SecurityScheme.In.HEADER)
                    .name(CLIENT_TYPE_HEADER)))
            // 默认在请求里加上两个 header
            .addSecurityItem(new SecurityRequirement().addList(TOKEN_HEADER))
            .addSecurityItem(new SecurityRequirement().addList(CLIENT_TYPE_HEADER));
    }

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
            .group("全部接口") // 定义分组名称
            .pathsToMatch("/api/**") // 匹配以 /public/ 开头的路径
            .build();
    }

    // @Bean
    // public GroupedOpenApi workflowApi() {
    //     return GroupedOpenApi.builder()
    //         .group("工作流接口") // 定义分组名称
    //         .pathsToMatch("/api/workflow/**") // 匹配以 /public/ 开头的路径
    //         .build();
    // }
}