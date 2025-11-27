package com.xiaodou.log.extension.logback;

import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 敏感信息脱敏日志布局（Masking Pattern Layout）
 * <p>
 * 扩展Logback的PatternLayout，实现日志输出时的敏感信息自动脱敏处理。
 * 支持动态配置多个正则表达式规则，对匹配的敏感内容进行星号(*)替换。
 * </p>
 * <p><b>核心功能：</b></p>
 * <ul>
 * <li>动态管理脱敏规则：支持运行时添加/移除/清除正则表达式规则</li>
 * <li>高效脱敏处理：基于字符数组操作，避免频繁字符串创建</li>
 * <li>多分组支持：支持正则表达式中的多个捕获组，每个组匹配内容都会被脱敏</li>
 * <li>星号序列优化：将连续9个及以上星号替换为8个星号，优化日志可读性</li>
 * </ul>
 * <p><b>使用示例：</b></p>
 * <pre>
 * &lt;layout class="store.ainm.team.log.MaskingPatternLayout"&gt;
 *     &lt;pattern&gt;%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n&lt;/pattern&gt;
 *     &lt;maskPattern&gt;(\d{3}-\d{2}-\d{4})&lt;/maskPattern&gt;  &lt;!-- 社保号码格式 --&gt;
 *     &lt;maskPattern&gt;(\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Z|a-z]{2,}\b)&lt;/maskPattern&gt;  &lt;!-- 邮箱格式 --&gt;
 * &lt;/layout&gt;
 * </pre>
 *
 * @author xiaodou V=>dddou117
 * @version 1.0
 * @since 2025/5/14
 */
public class MaskingPatternLayout extends PatternLayout {

    /** 连续9个及以上星号的正则模式 */
    private static final Pattern LONG_ASTERISK_PATTERN = Pattern.compile("\\*{9,}");

    /** 脱敏规则正则表达式列表 */
    private final List<String> maskPatterns = new ArrayList<>();

    /** 多行匹配模式（volatile保证多线程可见性） */
    private volatile Pattern multilinePattern;

    /**
     * 添加脱敏规则
     * <p>
     * 将新的正则表达式规则添加到脱敏规则列表中，并重新编译组合模式。
     * 规则中的每个捕获组(capturing group)匹配的内容都会被脱敏处理。
     * </p>
     * <p><b>注意事项：</b></p>
     * <ul>
     * <li>正则表达式应包含捕获组()，只有捕获组内容会被替换</li>
     * <li>规则添加后立即生效，影响后续所有日志输出</li>
     * <li>频繁添加规则会影响性能，建议初始化时集中配置</li>
     * </ul>
     *
     * @param maskPattern 脱敏规则正则表达式（不能为null）
     * @throws IllegalArgumentException 如果maskPattern为null
     */
    public void addMaskPattern(String maskPattern) {
        if (maskPattern == null) {
            throw new IllegalArgumentException("脱敏规则不能为null");
        }
        maskPatterns.add(maskPattern);
        // 只有在确实需要时才重新编译
        multilinePattern = Pattern.compile(String.join("|", maskPatterns), Pattern.MULTILINE);
    }

    /**
     * 移除脱敏规则
     * <p>
     * 从脱敏规则列表中移除指定规则，并重新编译剩余规则。
     * 如果移除后规则列表为空，则清除组合模式。
     * </p>
     *
     * @param maskPattern 要移除的脱敏规则正则表达式
     * @return 如果规则存在并被成功移除返回true，否则返回false
     */
    public boolean removeMaskPattern(String maskPattern) {
        boolean removed = maskPatterns.remove(maskPattern);
        if (removed) {
            multilinePattern = maskPatterns.isEmpty() ? null :
                Pattern.compile(String.join("|", maskPatterns), Pattern.MULTILINE);
        }
        return removed;
    }

    /**
     * 清除所有脱敏规则
     * <p>
     * 清空脱敏规则列表并重置组合模式，后续日志输出将不再进行脱敏处理。
     * </p>
     */
    public void clearMaskPatterns() {
        maskPatterns.clear();
        multilinePattern = null;
    }

    /**
     * 执行日志布局处理
     * <p>
     * 继承自PatternLayout，在原始布局处理结果上应用脱敏处理。
     * </p>
     *
     * @param event 日志事件（不能为null）
     * @return 脱敏处理后的日志消息
     * @throws IllegalArgumentException 如果event为null
     */
    @Override
    public String doLayout(ILoggingEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("日志事件不能为null");
        }
        return maskMessage(super.doLayout(event));
    }

    /**
     * 执行消息脱敏处理
     * <p>
     * 核心脱敏逻辑实现：
     * 1. 检查组合模式和消息有效性
     * 2. 使用字符数组进行高效替换（避免频繁创建字符串）
     * 3. 处理所有匹配的捕获组
     * 4. 优化连续星号序列
     * </p>
     * <p><b>性能优化：</b></p>
     * <ul>
     * <li>仅当消息实际被修改时才创建新字符串</li>
     * <li>使用字符数组操作减少内存分配</li>
     * <li>最后才应用星号序列优化（减少正则匹配次数）</li>
     * </ul>
     *
     * @param message 原始日志消息
     * @return 脱敏处理后的日志消息（如果无需脱敏则返回原消息）
     */
    private String maskMessage(String message) {
        // 快速路径检查：无规则或空消息直接返回
        if (multilinePattern == null || message == null || message.isEmpty()) {
            return message;
        }

        char[] chars = message.toCharArray();
        boolean modified = false;

        Matcher matcher = multilinePattern.matcher(message);
        while (matcher.find()) {
            // 处理所有捕获组（group 0是整个匹配，从group 1开始是捕获组）
            for (int group = 1; group <= matcher.groupCount(); group++) {
                if (matcher.group(group) != null) {
                    Arrays.fill(chars, matcher.start(group), matcher.end(group), '*');
                    modified = true;
                }
            }
        }

        // 仅在消息被修改时才创建新字符串
        String result = modified ? new String(chars) : message;

        // 优化连续星号序列（仅在确实需要时应用）
        return LONG_ASTERISK_PATTERN.matcher(result).replaceAll("********");
    }
}
