package com.xiaodou.utils;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;

/**
 * 统一编码生成器（优化命名版）
 *
 * <p>提供多种编码生成策略，包括：</p>
 * <ul>
 *   <li>API密钥（UUID/安全随机数/时间戳+随机数）</li>
 *   <li>邀请码（时间戳编码+随机校验位）</li>
 *   <li>安全子域名（时间编码+随机码）</li>
 * </ul>
 */
public final class CodeGenerator {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    // 预定义字符集
    public static final String HEX_CHARS = "0123456789abcdef";
    public static final String BASE36_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    public static final String SAFE_BASE32_CHARS = "23456789abcdefghjkmnpqrstuvwxyz"; // 去除了0/o/1/l

    /**
     * 生成策略枚举（语义化命名）
     */
    public enum Strategy {
        /**
         * UUID格式API密钥（32位十六进制）
         * 示例：550e8400e29b41d4a716446655440000
         */
        API_KEY_UUID {
            @Override
            public String generate() {
                return UUID.randomUUID()
                    .toString()
                    .replace("-", "");
            }
        },

        /**
         * 安全随机数API密钥（Base64 URL安全格式）
         * 示例：Y3k5MzRfVHJhY2Uu
         */
        API_KEY_SECURE_RANDOM {
            @Override
            public String generate() {
                return generate(16); // 默认 16 字节，可根据业务调整
            }

            @Override
            public String generate(int length) {
                byte[] bytes = new byte[length];
                SECURE_RANDOM.nextBytes(bytes);
                return Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(bytes);
            }
        },

        /**
         * 时间戳+随机数API密钥（十六进制格式）
         * 示例：17d99a5b3d3c1a3a1a3a1a3a1a3a1a3a
         */
        API_KEY_TIMESTAMP_HEX {
            @Override
            public String generate() {
                return generateTimeBasedCode(HEX_CHARS, 8, "");
            }
        },

        /**
         * 邀请码（Base36编码时间戳+随机校验位）
         * 示例：A3K9R7B2
         */
        INVITE_CODE_BASE36 {
            @Override
            public String generate() {
                return generateTimeBasedCode(BASE36_CHARS, 4, "");
            }
        },

        /**
         * 安全子域名（Base32编码时间+随机码，前缀"t-"）
         * 示例：t-7x9fk
         */
        SUBDOMAIN_SAFE {
            @Override
            public String generate() {
                return generateTimeBasedCode(SAFE_BASE32_CHARS, 2, "t-");
            }
        };

        /**
         * 生成编码（默认参数）
         */
        public String generate() {
            throw new UnsupportedOperationException("该策略需要指定参数");
        }

        /**
         * 生成指定长度的编码（仅API_KEY_SECURE_RANDOM可用）
         *
         * @param length 随机字节长度
         */
        public String generate(int length) {
            throw new UnsupportedOperationException("该策略不支持指定长度");
        }

        /**
         * 生成唯一编码（自动去重）
         *
         * @param existingCodes 已存在的编码集合
         * @param maxAttempts 最大尝试次数
         * @throws IllegalStateException 超过最大尝试次数
         */
        public String generateUnique(Set<String> existingCodes, int maxAttempts) {
            if (existingCodes == null)
                existingCodes = Collections.emptySet();

            int attempts = 0;
            String candidate;
            do {
                if (attempts++ >= maxAttempts) {
                    throw new IllegalStateException("生成唯一码失败，已达最大尝试次数: " + maxAttempts);
                }
                candidate = generate();
            } while (existingCodes.contains(candidate));

            return candidate;
        }
    }

    /* ------------------------- 核心生成方法 ------------------------- */

    /**
     * 通用时间+随机码生成模板
     *
     * @param charset 使用的字符集
     * @param randomLength 随机部分长度
     * @param prefix 结果前缀
     */
    private static String generateTimeBasedCode(String charset, int randomLength, String prefix) {
        // 1. 获取当前时间数值（yyDDDHHmmss格式）
        long timeValue = getCurrentTimeValue();

        // 2. 编码时间部分
        String timePart = longToCustomBase(timeValue, charset);

        // 3. 生成随机部分
        String randomPart = generateRandomString(charset, randomLength);

        return prefix + timePart + randomPart;
    }

    /**
     * 获取当前时间数值（yyDDDHHmmss格式）
     */
    private static long getCurrentTimeValue() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyDDDHHmmss");
        String timeStr = LocalDateTime.now()
            .format(formatter);
        return Long.parseLong(timeStr);
    }

    /**
     * 长整型转自定义进制字符串
     *
     * @param num 输入数字
     * @param charset 字符集（定义进制）
     */
    private static String longToCustomBase(long num, String charset) {
        StringBuilder sb = new StringBuilder();
        int base = charset.length();
        while (num > 0) {
            sb.append(charset.charAt((int)(num % base)));
            num /= base;
        }
        return sb.reverse()
            .toString();
    }

    /**
     * 生成随机字符串
     *
     * @param charset 字符集
     * @param length 生成长度
     */
    private static String generateRandomString(String charset, int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(charset.charAt(SECURE_RANDOM.nextInt(charset.length())));
        }
        return sb.toString();
    }

    private CodeGenerator() {
        throw new IllegalStateException("工具类不允许实例化");
    }
}