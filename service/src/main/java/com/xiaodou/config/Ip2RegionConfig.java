package com.xiaodou.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;

/**
 * IP2Region 数据库配置类
 * <p>
 * 用于配置 ip2region 数据库文件的路径，通过 application.yml/properties 文件注入配置。
 * 配置示例：
 * <pre>
 * ip2region:
 *   path: classpath:ip2region.xdb
 * </pre>
 * </p>
 *
 * @author xiaodou V=>dddou117
 * @version V1.0
 * @since 2025/5/13
 */
@Setter
@Getter
@Configuration
@ConfigurationProperties(prefix = "ip2region")
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
public class Ip2RegionConfig {

    /**
     * ip2region 数据库文件路径
     * <p>
     * 支持以下形式：
     * 1. 类路径文件：classpath:ip2region.xdb
     * 2. 绝对路径文件：/data/ip2region.xdb
     * 3. 相对路径文件：config/ip2region.xdb
     * </p>
     * -- GETTER --
     * 获取数据库文件路径
     * -- SETTER --
     * 设置数据库文件路径
     */
    private String path;

}