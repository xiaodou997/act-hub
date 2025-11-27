package com.xiaodou.aiapp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.networknt.schema.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * JSON Schema 工具类（适配 json-schema-validator 2.0.0+）
 * <p>
 * 职责：
 * - 校验用户输入是否符合 JSON Schema
 * - 自动填充默认值（支持嵌套对象、数组）
 * - 生成中文友好错误提示
 * - 提取字段元信息（用于前端展示、文档生成）
 * <p>
 * 注意：本工具类不负责“从元数据生成 Schema”，因为系统已统一使用标准 JSON Schema。
 *
 * @author xiaodou V=>dddou117
 * @version V2.0 (适配 json-schema-validator 2.0.0+)
 * @since 2025/10/28
 */
public final class JsonSchemaUtilsV1 {

    // 1. ObjectMapper 实例 (线程安全, 可复用)
    private static final ObjectMapper MAPPER = new ObjectMapper();

    // 2. JsonSchemaFactory 实例 (指定 Schema 版本, e.g., Draft 7)
    private static final JsonSchemaFactory FACTORY = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);

    /**
     * 私有构造函数，防止实例化
     */
    private JsonSchemaUtilsV1() {
    }

    // --- 核心方法 1: 校验并填充默认值 ---

    /**
     * 1. 校验输入数据是否符合 JSON Schema，并自动填充默认值
     * 优化逻辑：先填充默认值，再校验完整数据
     *
     * @param inputData 用户输入的原始数据（Map 或 JSON 字符串）
     * @param schemaJson JSON Schema 字符串（来自数据库 param_schema）
     * @return 校验并填充默认值后的数据（Map）
     * @throws IllegalArgumentException 如果校验失败，或 JSON 解析失败
     */
    public static Map<String, Object> validateAndApplyDefaults(Object inputData, String schemaJson)
        throws IllegalArgumentException {

        // 1. 解析 Schema 字符串
        JsonNode schemaNode;
        try {
            schemaNode = MAPPER.readTree(schemaJson);
        } catch (Exception e) {
            throw new IllegalArgumentException("无法解析 JSON Schema: " + e.getMessage(), e);
        }

        // 2. 解析输入数据
        JsonNode inputNode;
        try {
            if (inputData instanceof String) {
                inputNode = MAPPER.readTree((String)inputData);
            } else if (inputData instanceof Map) {
                inputNode = MAPPER.convertValue(inputData, JsonNode.class);
            } else {
                throw new IllegalArgumentException("输入数据必须是 Map 或 JSON 字符串。");
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("无法解析输入数据: " + e.getMessage(), e);
        }

        // 3. 应用默认值填充缺失字段
        JsonNode filledNode = applyDefaults(inputNode, schemaNode);

        // 4. 校验填充后的完整数据
        validateData(filledNode, schemaNode);

        // 5. 将填充了默认值的 JsonNode 转回 Map
        @SuppressWarnings("unchecked") Map<String, Object> resultMap = MAPPER.convertValue(filledNode, Map.class);
        return resultMap;
    }

    /**
     * 应用默认值填充缺失字段（递归处理）
     */
    private static JsonNode applyDefaults(JsonNode inputNode, JsonNode schemaNode) {
        if (!schemaNode.has("properties")) {
            return inputNode;
        }

        JsonNode properties = schemaNode.get("properties");
        ObjectNode resultNode = inputNode.isObject() ? (ObjectNode)inputNode : MAPPER.createObjectNode();

        // 遍历 schema 中的所有属性定义
        for (Iterator<Map.Entry<String, JsonNode>> it = properties.fields(); it.hasNext(); ) {
            Map.Entry<String, JsonNode> entry = it.next();
            String propertyName = entry.getKey();
            JsonNode propertySchema = entry.getValue();

            JsonNode currentValue = resultNode.get(propertyName);

            if (currentValue == null || currentValue.isNull()) {
                // 字段缺失或为null，尝试应用默认值
                if (propertySchema.has("default")) {
                    resultNode.set(propertyName, propertySchema.get("default"));
                }
            } else {
                // 字段存在，递归处理嵌套对象和数组
                if (currentValue.isObject() && propertySchema.has("properties")) {
                    // 处理嵌套对象
                    JsonNode filledObject = applyDefaults(currentValue, propertySchema);
                    resultNode.set(propertyName, filledObject);
                } else if (currentValue.isArray() && propertySchema.has("items")) {
                    // 处理数组（为每个数组项应用默认值）
                    JsonNode itemSchema = propertySchema.get("items");
                    if (itemSchema.isObject()) {
                        ArrayNode filledArray = MAPPER.createArrayNode();
                        for (JsonNode arrayItem : currentValue) {
                            if (arrayItem.isObject() && itemSchema.has("properties")) {
                                filledArray.add(applyDefaults(arrayItem, itemSchema));
                            } else {
                                filledArray.add(arrayItem);
                            }
                        }
                        resultNode.set(propertyName, filledArray);
                    }
                }
            }
        }

        return resultNode;
    }

    /**
     * 校验完整数据（不应用默认值）
     */
    private static void validateData(JsonNode dataNode, JsonNode schemaNode) {
        // 创建不应用默认值的配置
        SchemaValidatorsConfig config = new SchemaValidatorsConfig();
        config.setApplyDefaultsStrategy(new ApplyDefaultsStrategy(false, false, false));

        JsonSchema schema = FACTORY.getSchema(schemaNode, config);
        Set<ValidationMessage> errors = schema.validate(dataNode);

        if (!errors.isEmpty()) {
            String friendlyErrors = buildFriendlyErrorMessages(errors, schemaNode);
            throw new IllegalArgumentException("参数校验失败: " + friendlyErrors);
        }
    }

    private static String buildFriendlyErrorMessages(Set<ValidationMessage> errors, JsonNode schemaNode) {
        return errors.stream()
            .map(error -> formatErrorMessage(error, schemaNode))
            .collect(Collectors.joining("；"));
    }

    private static String formatErrorMessage(ValidationMessage error, JsonNode schemaNode) {
        String path = error.getProperty(); // e.g., "$.text", "$.tags[0]"
        String fieldName = extractTopLevelField(path);
        String description = getFieldDescription(fieldName, schemaNode);
        String label = (description == null || description.isEmpty()) ? fieldName : description;

        String type = error.getType();
        if ("required".equals(type)) {
            return label + " 是必填项";
        } else if ("minLength".equals(type)) {
            return label + " 长度不能少于 " + getNumberFromMessage(error.getMessage(), "minLength");
        } else if ("maxLength".equals(type)) {
            return label + " 长度不能超过 " + getNumberFromMessage(error.getMessage(), "maxLength");
        } else if ("minimum".equals(type)) {
            return label + " 不能小于 " + getNumberFromMessage(error.getMessage(), "minimum");
        } else if ("maximum".equals(type)) {
            return label + " 不能大于 " + getNumberFromMessage(error.getMessage(), "maximum");
        } else if ("enum".equals(type)) {
            return label + " 取值不在允许范围内";
        } else if ("type".equals(type)) {
            return label + " 类型不正确";
        } else if ("minItems".equals(type)) {
            return label + " 项目数不能少于 " + getNumberFromMessage(error.getMessage(), "minItems");
        } else if ("maxItems".equals(type)) {
            return label + " 项目数不能超过 " + getNumberFromMessage(error.getMessage(), "maxItems");
        } else if ("pattern".equals(type)) {
            return label + " 格式不符合要求";
        } else {
            return label + " 校验失败: " + error.getMessage();
        }
    }

    private static String extractTopLevelField(String jsonPath) {
        if (jsonPath.startsWith("$.") || jsonPath.startsWith("$[")) {
            String clean = jsonPath.substring(2)
                .replaceAll("[\\[\\]'\"]", "");
            int dotIndex = clean.indexOf('.');
            return dotIndex > 0 ? clean.substring(0, dotIndex) : clean;
        }
        return "参数";
    }

    private static String getFieldDescription(String fieldName, JsonNode schemaNode) {
        JsonNode props = schemaNode.get("properties");
        if (props != null && props.isObject()) {
            JsonNode field = props.get(fieldName);
            if (field != null && field.has("description")) {
                return field.get("description")
                    .asText();
            }
        }
        return fieldName;
    }

    private static String getNumberFromMessage(String message, String keyword) {
        // 示例: "maxLength: must have length less than or equal to 100"
        try {
            int idx = message.indexOf(keyword);
            if (idx >= 0) {
                String substr = message.substring(idx + keyword.length());
                String numStr = substr.replaceAll("[^0-9.-]", "");
                if (!numStr.isEmpty())
                    return numStr;
            }
        } catch (Exception ignored) {
        }
        return "?";
    }

    // --- 核心方法 2: Schema → 元数据数组 ---

    /**
     * 2. Schema → 元数据数组 (PropertyMetadata)
     * <p>
     * 注意：此实现仅支持顶层 'properties'，不支持嵌套/allOf/anyOf/oneOf/definitions 等复杂结构。
     *
     * @param schemaJson JSON Schema 字符串
     * @return 属性元数据列表
     * @throws IllegalArgumentException 如果 JSON 解析失败
     */
    public static List<PropertyMetadata> schemaToMetadata(String schemaJson) throws IllegalArgumentException {
        List<PropertyMetadata> metadataList = new ArrayList<>();
        try {
            JsonNode schemaNode = MAPPER.readTree(schemaJson);
            JsonNode properties = schemaNode.get("properties");
            if (properties == null || !properties.isObject()) {
                return Collections.emptyList();
            }

            Set<String> requiredSet = parseRequiredSet(schemaNode);
            List<PropertyMetadata> result = new ArrayList<>();
            for (Iterator<Map.Entry<String, JsonNode>> it = properties.fields(); it.hasNext(); ) {
                Map.Entry<String, JsonNode> entry = it.next();
                PropertyMetadata meta =
                    parseProperty(entry.getKey(), entry.getValue(), requiredSet.contains(entry.getKey()));
                result.add(meta);
            }
            return result;
        } catch (Exception e) {
            throw new IllegalArgumentException("无法解析 Schema 以提取元数据: " + e.getMessage(), e);
        }
    }

    private static PropertyMetadata parseProperty(String name, JsonNode node, boolean required) {
        PropertyMetadata meta = new PropertyMetadata();
        meta.setName(name);
        meta.setRequired(required);

        if (node.has("type"))
            meta.setType(node.get("type")
                .asText());
        if (node.has("description"))
            meta.setDescription(node.get("description")
                .asText());
        if (node.has("default")) {
            meta.setDefaultValue(MAPPER.convertValue(node.get("default"), Object.class));
        }

        // 字符串约束
        if (node.has("minLength"))
            meta.setMinLength(node.get("minLength")
                .asInt());
        if (node.has("maxLength"))
            meta.setMaxLength(node.get("maxLength")
                .asInt());
        if (node.has("pattern"))
            meta.setPattern(node.get("pattern")
                .asText());
        if (node.has("format"))
            meta.setFormat(node.get("format")
                .asText());

        // 数值约束
        if (node.has("minimum"))
            meta.setMinimum(node.get("minimum")
                .asDouble());
        if (node.has("maximum"))
            meta.setMaximum(node.get("maximum")
                .asDouble());
        if (node.has("exclusiveMinimum"))
            meta.setExclusiveMinimum(node.get("exclusiveMinimum")
                .asDouble());
        if (node.has("exclusiveMaximum"))
            meta.setExclusiveMaximum(node.get("exclusiveMaximum")
                .asDouble());
        if (node.has("multipleOf"))
            meta.setMultipleOf(node.get("multipleOf")
                .asDouble());

        // 枚举
        if (node.has("enum") && node.get("enum")
            .isArray()) {
            List<String> enums = new ArrayList<>();
            for (JsonNode item : node.get("enum")) {
                enums.add(item.asText());
            }
            meta.setEnumValues(enums);
        }

        // 数组约束
        if (node.has("minItems"))
            meta.setMinItems(node.get("minItems")
                .asInt());
        if (node.has("maxItems"))
            meta.setMaxItems(node.get("maxItems")
                .asInt());
        if (node.has("uniqueItems"))
            meta.setUniqueItems(node.get("uniqueItems")
                .asBoolean());

        // 嵌套对象
        if ("object".equals(meta.getType()) && node.has("properties")) {
            Set<String> nestedRequired = parseRequiredSet(node);
            List<PropertyMetadata> children = new ArrayList<>();
            JsonNode props = node.get("properties");
            for (Iterator<Map.Entry<String, JsonNode>> it = props.fields(); it.hasNext(); ) {
                Map.Entry<String, JsonNode> entry = it.next();
                children.add(parseProperty(entry.getKey(), entry.getValue(), nestedRequired.contains(entry.getKey())));
            }
            meta.setChildren(children);
        }

        // 数组项（仅支持 items 为单一 schema）
        if ("array".equals(meta.getType()) && node.has("items") && node.get("items")
            .isObject()) {
            // 假设 items 是一个 schema 对象（非数组）
            PropertyMetadata itemMeta = parseProperty("item", node.get("items"), false);
            meta.setArrayItemSchema(itemMeta);
        }

        return meta;
    }

    private static Set<String> parseRequiredSet(JsonNode schemaNode) {
        Set<String> required = new HashSet<>();
        JsonNode reqNode = schemaNode.get("required");
        if (reqNode != null && reqNode.isArray()) {
            for (JsonNode item : reqNode) {
                if (item.isTextual())
                    required.add(item.asText());
            }
        }
        return required;
    }

    // --- 核心方法 3: 元数据数组 → Schema ---

    /**
     * 3. 元数据数组 → Schema
     * <p>
     * 将 PropertyMetadata 列表转换回一个简单的 JSON Schema 字符串。
     *
     * @param metadataList 属性元数据列表
     * @return JSON Schema 字符串
     * @throws IllegalArgumentException 如果 JSON 序列化失败
     */
    public static String metadataToSchema(List<PropertyMetadata> metadataList) throws IllegalArgumentException {
        try {
            // 1. 创建根 Schema 节点
            ObjectNode root = MAPPER.createObjectNode();
            root.put("$schema", "http://json-schema.org/draft-07/schema#");
            root.put("type", "object");

            ObjectNode properties = MAPPER.createObjectNode();
            ArrayNode required = MAPPER.createArrayNode();

            // 3. 遍历元数据
            for (PropertyMetadata meta : metadataList) {
                if (meta.getName() == null || meta.getName()
                    .isEmpty()) {
                    continue; // 忽略无效元数据
                }

                ObjectNode prop = buildPropertyNode(meta);
                properties.set(meta.getName(), prop);

                if (meta.isRequired()) {
                    required.add(meta.getName());
                }
            }

            root.set("properties", properties);
            if (!required.isEmpty()) {
                root.set("required", required);
            }

            return MAPPER.writerWithDefaultPrettyPrinter()
                .writeValueAsString(root);

        } catch (Exception e) {
            throw new IllegalArgumentException("无法序列化元数据到 Schema: " + e.getMessage(), e);
        }
    }

    private static ObjectNode buildPropertyNode(PropertyMetadata meta) {
        ObjectNode node = MAPPER.createObjectNode();

        if (meta.getType() != null)
            node.put("type", meta.getType());
        if (meta.getDescription() != null)
            node.put("description", meta.getDescription());
        if (meta.getDefaultValue() != null) {
            node.set("default", MAPPER.valueToTree(meta.getDefaultValue()));
        }

        // 字符串约束
        if (meta.getMinLength() != null)
            node.put("minLength", meta.getMinLength());
        if (meta.getMaxLength() != null)
            node.put("maxLength", meta.getMaxLength());
        if (meta.getPattern() != null)
            node.put("pattern", meta.getPattern());
        if (meta.getFormat() != null)
            node.put("format", meta.getFormat());

        // 数值约束
        if (meta.getMinimum() != null)
            node.put("minimum", meta.getMinimum());
        if (meta.getMaximum() != null)
            node.put("maximum", meta.getMaximum());
        if (meta.getExclusiveMinimum() != null)
            node.put("exclusiveMinimum", meta.getExclusiveMinimum());
        if (meta.getExclusiveMaximum() != null)
            node.put("exclusiveMaximum", meta.getExclusiveMaximum());
        if (meta.getMultipleOf() != null)
            node.put("multipleOf", meta.getMultipleOf());

        // 枚举
        if (meta.getEnumValues() != null && !meta.getEnumValues()
            .isEmpty()) {
            ArrayNode enumNode = MAPPER.createArrayNode();
            meta.getEnumValues()
                .forEach(enumNode::add);
            node.set("enum", enumNode);
        }

        // 数组约束
        if (meta.getMinItems() != null)
            node.put("minItems", meta.getMinItems());
        if (meta.getMaxItems() != null)
            node.put("maxItems", meta.getMaxItems());
        if (meta.getUniqueItems() != null)
            node.put("uniqueItems", meta.getUniqueItems());

        // 嵌套对象
        if ("object".equals(meta.getType()) && meta.getChildren() != null && !meta.getChildren()
            .isEmpty()) {
            ObjectNode childProps = MAPPER.createObjectNode();
            ArrayNode childRequired = MAPPER.createArrayNode();
            for (PropertyMetadata child : meta.getChildren()) {
                childProps.set(child.getName(), buildPropertyNode(child));
                if (child.isRequired())
                    childRequired.add(child.getName());
            }
            node.set("properties", childProps);
            if (!childRequired.isEmpty()) {
                node.set("required", childRequired);
            }
        }

        // 数组项
        if ("array".equals(meta.getType()) && meta.getArrayItemSchema() != null) {
            node.set("items", buildPropertyNode(meta.getArrayItemSchema()));
        }

        return node;
    }

}