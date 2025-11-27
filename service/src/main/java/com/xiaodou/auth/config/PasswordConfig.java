package com.xiaodou.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 密码编码器配置类
 * <p>
 * 配置Spring Security的密码编码器，使用DelegatingPasswordEncoder提供以下支持：
 * 1. 支持多种密码编码格式（BCrypt、PBKDF2、SCrypt等）
 * 2. 自动识别存储的密码编码方式
 * 3. 默认使用BCrypt强哈希算法
 * 4. 提供密码升级机制（当有更安全的算法时自动升级）
 * </p>
 *
 * @ClassName: PasswordConfig
 * @Description: 密码编码器配置
 * @Author: xiaodou V=>dddou117
 * @Date: 2025/5/15
 * @Version: V1.0
 * @JDK: JDK21
 */
@Configuration
public class PasswordConfig {

    /**
     * 配置密码编码器
     * <p>
     * 创建DelegatingPasswordEncoder实例，该编码器：
     * 1. 默认使用BCrypt算法（前缀为{bcrypt}）
     * 2. 支持以下格式密码：
     * - {bcrypt} - BCrypt算法
     * - {pbkdf2} - PBKDF2算法
     * - {scrypt} - SCrypt算法
     * - {sha256} - SHA-256算法
     * 3. 能够自动检测密码的前缀标识并选择合适的验证器
     * </p>
     *
     * @return PasswordEncoder 密码编码器实例
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}