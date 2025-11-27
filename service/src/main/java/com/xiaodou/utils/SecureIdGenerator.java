package com.xiaodou.utils;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Set;
import java.util.UUID;

/**
 * 安全唯一标识符生成器
 *
 * <p>提供多种基于加密安全随机源（SecureRandom）的唯一ID生成方案，支持：</p>
 * <ul>
 *   <li>UUID格式标识符</li>
 *   <li>Base64 URL安全编码</li>
 *   <li>时间戳+随机数组合编码</li>
 *   <li>自定义字符集随机字符串</li>
 * </ul>
 *
 * <h2>使用方式：</h2>
 * <pre>{@code
 * // 1. 简单场景 - 直接使用预定义方法
 * String apiKey = SecureIdGenerator.apiKey();
 * String inviteCode = SecureIdGenerator.inviteCode();
 *
 * // 2. 带参数场景 - 使用参数化方法
 * String customApiKey = SecureIdGenerator.apiKey(32);
 * String longInviteCode = SecureIdGenerator.inviteCode(10);
 *
 * // 3. 复杂场景 - 使用构建器模式
 * String customId = SecureIdGenerator.configure()
 *     .strategy(GenerationStrategy.TIME_BASED)
 *     .charset(SecureIdGenerator.SAFE_BASE32_CHARS)
 *     .length(16)
 *     .prefix("user_")
 *     .suffix("_2024")
 *     .timePattern("yyyyMMddHHmmss")
 *     .build();
 * }</pre>
 *
 * @author xiaodou V=>dddou117
 * @version V1.0
 * @since 2025/10/10
 */
public final class SecureIdGenerator {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    // ==================== 预定义字符集常量 ====================

    /**
     * 十六进制字符集（小写）
     * <p>字符：0123456789abcdef</p>
     */
    public static final String HEX_CHARS = "0123456789abcdef";

    /**
     * 十六进制字符集（大写）
     * <p>字符：0123456789ABCDEF</p>
     */
    public static final String HEX_CHARS_UPPER = "0123456789ABCDEF";

    /**
     * Base36字符集（大写字母+数字）
     * <p>字符：0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ</p>
     */
    public static final String BASE36_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    /**
     * 安全Base32字符集（去除了易混淆字符）
     * <p>移除了 0/o, 1/i/l, 8/B 等易混淆字符</p>
     * <p>字符：23456789abcdefghjkmnpqrstuvwxyz</p>
     */
    public static final String SAFE_BASE32_CHARS = "23456789abcdefghjkmnpqrstuvwxyz";

    /**
     * Base62字符集（数字+大小写字母）
     * <p>字符：0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz</p>
     */
    public static final String BASE62_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    /**
     * 数字字符集
     * <p>字符：0123456789</p>
     */
    public static final String NUMERIC_CHARS = "0123456789";

    /**
     * 字母字符集（大小写）
     * <p>字符：ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz</p>
     */
    public static final String ALPHABET_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    // ==================== 生成策略枚举 ====================

    /**
     * 标识符生成策略枚举
     */
    public enum GenerationStrategy {
        /**
         * UUID策略 - 生成标准的UUID字符串
         */
        UUID,

        /**
         * 随机字节策略 - 生成Base64 URL安全编码的随机字节
         */
        RANDOM_BYTES,

        /**
         * 时间戳策略 - 生成时间编码+随机后缀的组合标识符
         */
        TIME_BASED,

        /**
         * 纯随机策略 - 从指定字符集生成随机字符串
         */
        RANDOM_STRING
    }

    // ==================== 核心基础方法 ====================

    /**
     * 生成32位小写十六进制UUID（无横线）
     *
     * <p>示例：{@code 550e8400e29b41d4a716446655440000}</p>
     *
     * @return 32位小写十六进制UUID字符串
     */
    public static String uuidHex() {
        return UUID.randomUUID()
            .toString()
            .replace("-", "")
            .toLowerCase();
    }

    /**
     * 生成标准格式UUID（带横线）
     *
     * <p>示例：{@code 550e8400-e29b-41d4-a716-446655440000}</p>
     *
     * @return 标准格式UUID字符串
     */
    public static String uuidStandard() {
        return UUID.randomUUID()
            .toString();
    }

    /**
     * 生成指定字节数的Base64 URL安全编码（无填充）
     *
     * <p>示例（16字节）：{@code Y3k5MzRfVHJhY2Uu}</p>
     *
     * @param byteLength 随机字节长度，建议16-32字节
     * @return Base64 URL安全编码字符串
     * @throws IllegalArgumentException 如果字节长度小于等于0
     */
    public static String randomBase64(int byteLength) {
        if (byteLength <= 0) {
            throw new IllegalArgumentException("字节长度必须大于0");
        }
        byte[] bytes = new byte[byteLength];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(bytes);
    }

    /**
     * 生成指定长度的随机字符串（从给定字符集）
     *
     * <p>示例：{@code randomString("ABC123", 6) -> "A3B1C2"}</p>
     *
     * @param charset 字符集，不能为null或空
     * @param length 随机字符串长度，必须大于0
     * @return 随机字符串
     * @throws IllegalArgumentException 如果字符集为空或长度不合法
     */
    public static String randomString(String charset, int length) {
        if (charset == null || charset.isEmpty()) {
            throw new IllegalArgumentException("字符集不能为空");
        }
        if (length <= 0) {
            throw new IllegalArgumentException("长度必须大于0");
        }

        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(charset.charAt(SECURE_RANDOM.nextInt(charset.length())));
        }
        return sb.toString();
    }

    /**
     * 生成"时间编码 + 随机后缀"格式的字符串
     *
     * <p>时间部分使用指定字符集进行进制编码，随机部分从随机字符集生成</p>
     * <p>示例：{@code timeBasedCode("yyMMdd", BASE36, "ABC123", 4, "ID_") -> "ID_A3B12X9C"}</p>
     *
     * @param timePattern 时间格式模式（如："yyDDDHHmmss"）
     * @param timeCharset 时间部分编码字符集
     * @param randomCharset 随机部分字符集
     * @param randomLength 随机部分长度
     * @param prefix 前缀字符串
     * @return 时间基础编码字符串
     */
    public static String timeBasedCode(String timePattern, String timeCharset, String randomCharset, int randomLength,
        String prefix) {

        long timeValue = getTimeValue(timePattern);
        String timePart = longToCustomBase(timeValue, timeCharset);
        String randomPart = randomString(randomCharset, randomLength);
        return (prefix != null ? prefix : "") + timePart + randomPart;
    }

    /**
     * 便捷方法：时间与随机部分使用相同字符集
     *
     * @param charset 时间部分和随机部分的字符集
     * @param randomLength 随机部分长度
     * @param prefix 前缀字符串
     * @return 时间基础编码字符串
     */
    public static String timeBasedCode(String charset, int randomLength, String prefix) {
        return timeBasedCode("yyDDDHHmmss", charset, charset, randomLength, prefix);
    }

    // ==================== 场景化便捷方法 ====================

    /**
     * 生成API密钥（默认24字节Base64编码）
     *
     * <p>示例：{@code Y3k5MzRfVHJhY2UuVHJhY2UuVHJhY2Uu}</p>
     *
     * @return API密钥字符串
     */
    public static String apiKey() {
        return randomBase64(24);
    }

    /**
     * 生成指定长度的API密钥
     *
     * @param byteLength 字节长度
     * @return API密钥字符串
     */
    public static String apiKey(int byteLength) {
        return randomBase64(byteLength);
    }

    /**
     * 生成邀请码（Base36编码时间戳+6位随机字符）
     *
     * <p>示例：{@code A3K9R7B2}</p>
     *
     * @return 邀请码字符串（大写）
     */
    public static String inviteCode() {
        return timeBasedCode(BASE36_CHARS, 6, "").toUpperCase();
    }

    /**
     * 生成指定随机长度的邀请码
     *
     * @param randomLength 随机部分长度
     * @return 邀请码字符串（大写）
     */
    public static String inviteCode(int randomLength) {
        return timeBasedCode(BASE36_CHARS, randomLength, "").toUpperCase();
    }

    /**
     * 生成安全子域名（安全Base32编码+4位随机字符，前缀"t-"）
     *
     * <p>示例：{@code t-7x9fkp}</p>
     *
     * @return 安全子域名字符串
     */
    public static String secureSubdomain() {
        return timeBasedCode(SAFE_BASE32_CHARS, 4, "t-");
    }

    /**
     * 生成文件安全名称（时间戳+8位安全随机字符，前缀"file-"）
     *
     * <p>示例：{@code file-8xk9f3m2}</p>
     *
     * @return 文件安全名称
     */
    public static String fileSafeName() {
        return timeBasedCode(SAFE_BASE32_CHARS, 8, "file-");
    }

    /**
     * 生成短链接ID（8位Base62随机字符）
     *
     * <p>示例：{@code A3b9Kj2L}</p>
     *
     * @return 短链接ID
     */
    public static String shortLinkId() {
        return randomString(BASE62_CHARS, 8);
    }

    /**
     * 生成交易ID（时间戳+6位随机数字，前缀"TX"）
     *
     * <p>示例：{@code TX20241225143045283947}</p>
     *
     * @return 交易ID
     */
    public static String transactionId() {
        String timePart = LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String randomPart = randomString(NUMERIC_CHARS, 6);
        return "TX" + timePart + randomPart;
    }

    /**
     * 生成用户ID（时间戳+8位安全随机字符，前缀"user_"）
     *
     * <p>示例：{@code user_8xk9f3m2}</p>
     *
     * @return 用户ID
     */
    public static String userId() {
        return timeBasedCode(SAFE_BASE32_CHARS, 8, "user_");
    }

    // ==================== 构建器模式配置类 ====================

    /**
     * 配置化标识符生成器
     *
     * <p>用于复杂场景下的标识符生成，支持链式配置：</p>
     * <pre>{@code
     * String id = SecureIdGenerator.configure()
     *     .strategy(GenerationStrategy.TIME_BASED)
     *     .charset(SecureIdGenerator.SAFE_BASE32_CHARS)
     *     .length(16)
     *     .prefix("order_")
     *     .timePattern("yyyyMMddHHmmss")
     *     .build();
     * }</pre>
     */
    public static class ConfigurableBuilder {
        private GenerationStrategy strategy = GenerationStrategy.RANDOM_BYTES;
        private String charset = BASE62_CHARS;
        private int length = 16;
        private String prefix = "";
        private String suffix = "";
        private String timePattern = "yyDDDHHmmss";
        private int timeRandomLength = 4;
        private int byteLength = 16;

        /**
         * 设置生成策略
         *
         * @param strategy 生成策略
         * @return 构建器实例
         */
        public ConfigurableBuilder strategy(GenerationStrategy strategy) {
            this.strategy = strategy;
            return this;
        }

        /**
         * 设置字符集（用于RANDOM_STRING和TIME_BASED策略）
         *
         * @param charset 字符集
         * @return 构建器实例
         */
        public ConfigurableBuilder charset(String charset) {
            this.charset = charset;
            return this;
        }

        /**
         * 设置生成长度（用于RANDOM_STRING策略）
         *
         * @param length 字符串长度
         * @return 构建器实例
         */
        public ConfigurableBuilder length(int length) {
            this.length = length;
            return this;
        }

        /**
         * 设置字节长度（用于RANDOM_BYTES策略）
         *
         * @param byteLength 字节长度
         * @return 构建器实例
         */
        public ConfigurableBuilder byteLength(int byteLength) {
            this.byteLength = byteLength;
            return this;
        }

        /**
         * 设置前缀
         *
         * @param prefix 前缀字符串
         * @return 构建器实例
         */
        public ConfigurableBuilder prefix(String prefix) {
            this.prefix = prefix != null ? prefix : "";
            return this;
        }

        /**
         * 设置后缀
         *
         * @param suffix 后缀字符串
         * @return 构建器实例
         */
        public ConfigurableBuilder suffix(String suffix) {
            this.suffix = suffix != null ? suffix : "";
            return this;
        }

        /**
         * 设置时间格式模式（用于TIME_BASED策略）
         *
         * @param timePattern 时间格式模式
         * @return 构建器实例
         */
        public ConfigurableBuilder timePattern(String timePattern) {
            this.timePattern = timePattern;
            return this;
        }

        /**
         * 设置时间编码的随机部分长度（用于TIME_BASED策略）
         *
         * @param timeRandomLength 随机部分长度
         * @return 构建器实例
         */
        public ConfigurableBuilder timeRandomLength(int timeRandomLength) {
            this.timeRandomLength = timeRandomLength;
            return this;
        }

        /**
         * 构建并生成标识符
         *
         * @return 生成的标识符字符串
         * @throws IllegalStateException 如果配置不合法
         */
        public String build() {
            switch (strategy) {
                case UUID:
                    return prefix + uuidHex() + suffix;

                case RANDOM_BYTES:
                    return prefix + randomBase64(byteLength) + suffix;

                case TIME_BASED:
                    return prefix + timeBasedCode(timePattern, charset, charset, timeRandomLength, "") + suffix;

                case RANDOM_STRING:
                    return prefix + randomString(charset, length) + suffix;

                default:
                    throw new IllegalStateException("不支持的生成策略: " + strategy);
            }
        }
    }

    /**
     * 创建配置化构建器实例
     *
     * @return 配置化构建器实例
     */
    public static ConfigurableBuilder configure() {
        return new ConfigurableBuilder();
    }
    // ==================== 工具方法 ====================

    /**
     * 生成不与现有编码冲突的唯一编码（通过重试机制）
     *
     * <p>示例：{@code generateUnique(() -> inviteCode(), existingCodes, 5)}</p>
     *
     * @param generator 编码生成函数
     * @param existingCodes 已存在编码集合
     * @param maxAttempts 最大重试次数
     * @return 唯一编码
     * @throws IllegalStateException 超过最大尝试次数仍未生成唯一编码
     */
    public static String generateUnique(java.util.function.Supplier<String> generator, Set<String> existingCodes,
        int maxAttempts) {
        if (existingCodes == null) {
            existingCodes = Set.of();
        }

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            String code = generator.get();
            if (!existingCodes.contains(code)) {
                return code;
            }
        }

        throw new IllegalStateException("生成唯一码失败，已达最大尝试次数: " + maxAttempts);
    }

    /**
     * 验证字符串是否符合指定字符集和长度要求
     *
     * @param code 待验证字符串
     * @param allowedChars 允许的字符集
     * @param minLength 最小长度
     * @param maxLength 最大长度
     * @return 如果符合要求返回true，否则返回false
     */
    public static boolean isValidFormat(String code, String allowedChars, int minLength, int maxLength) {
        if (code == null || code.length() < minLength || code.length() > maxLength) {
            return false;
        }

        return code.chars()
            .allMatch(ch -> allowedChars.indexOf(ch) >= 0);
    }
    // ==================== 私有工具方法 ====================

    /**
     * 根据时间格式模式获取当前时间数值
     *
     * @param pattern 时间格式模式
     * @return 时间数值
     */
    private static long getTimeValue(String pattern) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        String timeStr = LocalDateTime.now()
            .format(formatter);
        return Long.parseLong(timeStr);
    }

    /**
     * 将长整型转为自定义进制字符串
     *
     * @param num 输入数字
     * @param charset 字符集（定义进制）
     * @return 自定义进制字符串
     */
    private static String longToCustomBase(long num, String charset) {
        if (num == 0) {
            return String.valueOf(charset.charAt(0));
        }

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
     * 私有构造方法，防止实例化
     */
    private SecureIdGenerator() {
        throw new IllegalStateException("工具类不允许实例化");
    }
}
