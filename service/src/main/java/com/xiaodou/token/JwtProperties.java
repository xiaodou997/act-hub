package com.xiaodou.token;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT 配置属性类
 * <p>
 * 通过 @ConfigurationProperties 注解，将 application.yml 文件中以 "jwt" 为前缀的配置项，
 * 自动绑定到该类的属性上。
 * </p>
 */
@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /**
     * JWT 签发的密钥，必须严格保密。建议使用32位以上的随机字符串。
     */
    private String secret;

    /**
     * Access Token 的过期时间，单位：分钟。
     */
    private long accessTokenExpiration;

    /**
     * Refresh Token 的过期时间，单位：天。
     */
    private long refreshTokenExpiration;
}
