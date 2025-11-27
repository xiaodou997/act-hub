package com.xiaodou.aiapp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

/**
 *
 * @author xiaodou V=>dddou117
 * @version V1.0
 * @since 2025/10/28
 */
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class PropertyMetadata {
    private String name;
    private String type;
    private String description;
    private Object defaultValue;
    private boolean required;

    // === 字符串约束 ===
    private Integer minLength;
    private Integer maxLength;
    private String pattern; // 正则表达式
    private String format;  // e.g., "email", "uri", "date-time"

    // === 数值约束 ===
    private Double minimum;
    private Double maximum;
    private Double exclusiveMinimum; // Draft 7: number
    private Double exclusiveMaximum; // Draft 7: number
    private Double multipleOf;

    // === 枚举 ===
    private List<String> enumValues;

    // === 数组约束 ===
    private Integer minItems;
    private Integer maxItems;
    private Boolean uniqueItems;

    // === 嵌套对象支持 ===
    private List<PropertyMetadata> children; // 当 type == "object" 时使用

    // === 数组项类型（简单支持）===
    private PropertyMetadata arrayItemSchema; // 当 type == "array" 时使用（仅支持 items 为单一 schema）

    // @Override
    // public String toString() {
    //     return "PropertyMetadata{" + "name='" + name + '\'' + ", type='" + type + '\'' + ", description='" + description + '\'' + ", defaultValue=" + defaultValue + ", required=" + required + '}';
    // }
}
