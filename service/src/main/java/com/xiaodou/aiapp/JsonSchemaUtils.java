package com.xiaodou.aiapp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.networknt.schema.*;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * JSON Schema 工具类（适配 json-schema-validator 2.0.0+）
 * <p>
 * 职责：
 * - 校验用户输入是否符合 JSON Schema
 * - 自动填充默认值（仅对 required 字段）
 * - 生成中文友好错误提示
 * - 提取字段元信息（用于前端展示、文档生成）
 * - 缓存已解析的 Schema（Caffeine）
 * <p>
 * 注意：本工具类不负责“从元数据生成 Schema”，因为系统已统一使用标准 JSON Schema。
 *
 * @author xiaodou V=>dddou117
 * @version V2.2 (支持 schema_id 作为缓存 key + expireAfterAccess 1小时 + 并发安全)
 * @since 2025/10/28
 */
public final class JsonSchemaUtils {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final JsonSchemaFactory FACTORY = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);

    // Caffeine 缓存：key = 缓存键（可以是 schema_id 或 schemaJson），value = JsonSchema
    private static final Cache<String, JsonSchema> SCHEMA_CACHE = Caffeine.newBuilder()
        .maximumSize(100) // 最多缓存 100 个 schema
        .expireAfterAccess(1, TimeUnit.HOURS) // 访问后 1 小时过期（支持刷新 TTL）
        .build();

    // 工具类，防止实例化
    private JsonSchemaUtils() {
    }

    // ========== 公共接口方法 ==========

    /**
     * 校验输入数据并应用默认值（兼容旧接口）
     *
     * @param inputData 输入数据，可以是 Map 或 JSON 字符串
     * @param schemaJson JSON Schema 字符串
     * @return 校验并填充默认值后的数据 Map
     * @throws IllegalArgumentException 当 Schema 解析失败或数据校验失败时抛出
     */
    public static Map<String, Object> validateAndApplyDefaults(Object inputData, String schemaJson)
        throws IllegalArgumentException {
        // 使用 schemaJson 本身作为缓存 key（旧逻辑）
        return validateAndApplyDefaultsInternal(inputData, schemaJson, schemaJson);
    }

    /**
     * 校验输入数据并应用默认值（新推荐接口）
     *
     * @param inputData 输入数据，可以是 Map 或 JSON 字符串
     * @param schemaId Schema ID，用作缓存键
     * @param schemaJson JSON Schema 字符串
     * @return 校验并填充默认值后的数据 Map
     * @throws IllegalArgumentException 当 Schema 解析失败或数据校验失败时抛出
     */
    public static Map<String, Object> validateAndApplyDefaults(Object inputData, String schemaId, String schemaJson)
        throws IllegalArgumentException {
        // 使用 schemaId 作为缓存 key，schemaJson 用于解析
        return validateAndApplyDefaultsInternal(inputData, schemaId, schemaJson);
    }

    /**
     * 从 JSON Schema 提取字段元数据信息
     *
     * @param schemaJson JSON Schema 字符串
     * @return 字段元数据列表
     * @throws IllegalArgumentException 当 Schema 解析失败时抛出
     */
    public static List<PropertyMetadata> schemaToMetadata(String schemaJson) throws IllegalArgumentException {
        try {
            JsonNode schemaNode = MAPPER.readTree(schemaJson);
            JsonNode properties = schemaNode.get("properties");

            if (properties == null || !properties.isObject()) {
                return Collections.emptyList();
            }

            Set<String> requiredSet = parseRequiredSet(schemaNode);

            // 修复：使用迭代器而不是 StreamSupport
            List<PropertyMetadata> result = new ArrayList<>();
            Iterator<Map.Entry<String, JsonNode>> fields = properties.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                PropertyMetadata meta =
                    parseProperty(entry.getKey(), entry.getValue(), requiredSet.contains(entry.getKey()));
                if (meta.getName() != null && !meta.getName()
                    .isEmpty()) {
                    result.add(meta);
                }
            }
            return result;

        } catch (Exception e) {
            throw new IllegalArgumentException("无法解析 Schema 以提取元数据: " + e.getMessage(), e);
        }
    }

    /**
     * 从字段元数据生成 JSON Schema
     *
     * @param metadataList 字段元数据列表
     * @return 格式化后的 JSON Schema 字符串
     * @throws IllegalArgumentException 当序列化失败时抛出
     */
    public static String metadataToSchema(List<PropertyMetadata> metadataList) throws IllegalArgumentException {
        try {
            ObjectNode root = MAPPER.createObjectNode();
            root.put("$schema", "http://json-schema.org/draft-07/schema#");
            root.put("type", "object");

            ObjectNode properties = MAPPER.createObjectNode();
            ArrayNode required = MAPPER.createArrayNode();

            for (PropertyMetadata meta : metadataList) {
                if (meta.getName() == null || meta.getName()
                    .isEmpty())
                    continue;

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

    /**
     * 清空所有缓存的 Schema
     */
    public static void clearSchemaCache() {
        SCHEMA_CACHE.invalidateAll();
    }

    /**
     * 清除指定键的缓存 Schema
     *
     * @param cacheKey 缓存键
     */
    public static void clearSchemaCache(String cacheKey) {
        SCHEMA_CACHE.invalidate(cacheKey);
    }
    // ========== 核心私有方法 ==========

    /**
     * 校验和应用默认值的核心实现
     *
     * @param inputData 输入数据
     * @param cacheKey 缓存键
     * @param schemaJson Schema JSON 字符串
     * @return 处理后的数据 Map
     */
    private static Map<String, Object> validateAndApplyDefaultsInternal(Object inputData, String cacheKey,
        String schemaJson) throws IllegalArgumentException {

        JsonNode schemaNode = parseSchemaNode(schemaJson);
        JsonNode inputNode = parseInputNode(inputData);
        Set<String> requiredFields = parseRequiredSet(schemaNode);

        JsonNode filledNode = applyDefaults(inputNode, schemaNode, requiredFields);
        JsonSchema schema = getOrBuildSchema(cacheKey, schemaNode);

        validateSchema(schema, filledNode, schemaNode);

        @SuppressWarnings("unchecked") Map<String, Object> resultMap = MAPPER.convertValue(filledNode, Map.class);
        return resultMap;
    }

    /**
     * 解析 JSON Schema 节点
     *
     * @param schemaJson Schema JSON 字符串
     * @return 解析后的 JsonNode
     * @throws IllegalArgumentException 当解析失败时抛出
     */
    private static JsonNode parseSchemaNode(String schemaJson) {
        try {
            return MAPPER.readTree(schemaJson);
        } catch (Exception e) {
            throw new IllegalArgumentException("无法解析 JSON Schema: " + e.getMessage(), e);
        }
    }

    /**
     * 解析输入数据节点
     *
     * @param inputData 输入数据
     * @return 解析后的 JsonNode
     * @throws IllegalArgumentException 当解析失败时抛出
     */
    private static JsonNode parseInputNode(Object inputData) {
        if (inputData instanceof String) {
            try {
                return MAPPER.readTree((String)inputData);
            } catch (Exception e) {
                throw new IllegalArgumentException("无法解析输入JSON字符串: " + e.getMessage(), e);
            }
        } else if (inputData instanceof Map) {
            return MAPPER.convertValue(inputData, JsonNode.class);
        } else {
            throw new IllegalArgumentException("输入数据必须是 Map 或 JSON 字符串。");
        }
    }

    /**
     * 校验 Schema
     *
     * @param schema JsonSchema 实例
     * @param filledNode 填充后的数据节点
     * @param schemaNode Schema 节点
     * @throws IllegalArgumentException 当校验失败时抛出
     */
    private static void validateSchema(JsonSchema schema, JsonNode filledNode, JsonNode schemaNode) {
        Set<ValidationMessage> errors = schema.validate(filledNode);
        if (errors.isEmpty()) {
            return;
        }
        String friendlyErrors = buildFriendlyErrorMessages(errors, schemaNode);
        throw new IllegalArgumentException("参数校验失败: " + friendlyErrors);
    }

    /**
     * 应用默认值到输入数据
     *
     * @param inputNode 输入数据节点
     * @param schemaNode Schema 节点
     * @param requiredFields 必填字段集合
     * @return 应用默认值后的数据节点
     */
    private static JsonNode applyDefaults(JsonNode inputNode, JsonNode schemaNode, Set<String> requiredFields) {
        if (!schemaNode.has("properties")) {
            return inputNode;
        }

        JsonNode properties = schemaNode.get("properties");
        ObjectNode resultNode = inputNode.isObject() ? (ObjectNode)inputNode : MAPPER.createObjectNode();

        properties.fieldNames()
            .forEachRemaining(propertyName -> {
                JsonNode propertySchema = properties.get(propertyName);
                processProperty(resultNode, propertyName, propertySchema, requiredFields);
            });

        return resultNode;
    }

    /**
     * 处理单个属性的默认值应用
     *
     * @param resultNode 结果节点
     * @param propertyName 属性名
     * @param propertySchema 属性 Schema
     * @param requiredFields 必填字段集合
     */
    private static void processProperty(ObjectNode resultNode, String propertyName, JsonNode propertySchema,
        Set<String> requiredFields) {
        JsonNode currentValue = resultNode.get(propertyName);
        boolean isRequired = requiredFields.contains(propertyName);
        boolean hasDefault = propertySchema.has("default");

        // 处理null值情况
        if (isNullValue(currentValue)) {
            if (isRequired && hasDefault) {
                resultNode.set(propertyName, propertySchema.get("default"));
            }
            return;
        }

        // 处理嵌套对象
        if (isObjectWithProperties(currentValue, propertySchema)) {
            Set<String> nestedRequired = parseRequiredSet(propertySchema);
            JsonNode filledObject = applyDefaults(currentValue, propertySchema, nestedRequired);
            resultNode.set(propertyName, filledObject);
            return;
        }

        // 处理数组
        if (isArrayWithItemSchema(currentValue, propertySchema)) {
            processArrayProperty(resultNode, propertyName, currentValue, propertySchema);
        }
    }

    /**
     * 处理数组属性的默认值应用
     *
     * @param resultNode 结果节点
     * @param propertyName 属性名
     * @param currentValue 当前值
     * @param propertySchema 属性 Schema
     */
    private static void processArrayProperty(ObjectNode resultNode, String propertyName, JsonNode currentValue,
        JsonNode propertySchema) {
        JsonNode itemSchema = propertySchema.get("items");
        if (!itemSchema.isObject() || !itemSchema.has("properties")) {
            return;
        }

        ArrayNode filledArray = MAPPER.createArrayNode();
        for (JsonNode arrayItem : currentValue) {
            if (arrayItem.isObject()) {
                Set<String> itemRequired = parseRequiredSet(itemSchema);
                JsonNode filledItem = applyDefaults(arrayItem, itemSchema, itemRequired);
                filledArray.add(filledItem);
            } else {
                filledArray.add(arrayItem);
            }
        }
        resultNode.set(propertyName, filledArray);
    }

    // ========== 谓词判断方法 ==========

    /**
     * 判断节点值是否为null
     *
     * @param value 节点值
     * @return 是否为null或null节点
     */
    private static boolean isNullValue(JsonNode value) {
        return value == null || value.isNull();
    }

    /**
     * 判断是否为具有properties的对象节点
     *
     * @param value 节点值
     * @param schema 对应的Schema
     * @return 是否符合条件
     */
    private static boolean isObjectWithProperties(JsonNode value, JsonNode schema) {
        return value != null && value.isObject() && schema.has("properties");
    }

    /**
     * 判断是否为具有items定义的数组节点
     *
     * @param value 节点值
     * @param schema 对应的Schema
     * @return 是否符合条件
     */
    private static boolean isArrayWithItemSchema(JsonNode value, JsonNode schema) {
        return value != null && value.isArray() && schema.has("items");
    }

    // ========== 缓存管理方法 ==========

    /**
     * 获取或构建 JsonSchema（带缓存）
     *
     * @param cacheKey 缓存键
     * @param schemaNode Schema 节点
     * @return JsonSchema 实例
     */
    private static JsonSchema getOrBuildSchema(String cacheKey, JsonNode schemaNode) {
        return SCHEMA_CACHE.get(cacheKey, key -> {
            SchemaValidatorsConfig config = new SchemaValidatorsConfig();
            config.setApplyDefaultsStrategy(new ApplyDefaultsStrategy(false, false, false));
            return FACTORY.getSchema(schemaNode, config);
        });
    }

    // ========== 错误处理相关方法 ==========

    /**
     * 构建友好的错误消息
     *
     * @param errors 验证错误集合
     * @param schemaNode Schema 节点
     * @return 格式化的错误消息字符串
     */
    private static String buildFriendlyErrorMessages(Set<ValidationMessage> errors, JsonNode schemaNode) {
        return errors.stream()
            .map(error -> formatErrorMessage(error, schemaNode))
            .collect(Collectors.joining("；"));
    }

    /**
     * 格式化单个错误消息
     *
     * @param error 验证错误
     * @param schemaNode Schema 节点
     * @return 格式化的错误消息
     */
    private static String formatErrorMessage(ValidationMessage error, JsonNode schemaNode) {
        String path = error.getProperty();
        String fieldName = extractTopLevelField(path);
        String description = getFieldDescription(fieldName, schemaNode);
        String label = (description == null || description.isEmpty()) ? fieldName : description;

        return ErrorMessageBuilder.build(error, label);
    }

    /**
     * 提取顶级字段名
     *
     * @param jsonPath JSON路径
     * @return 顶级字段名
     */
    private static String extractTopLevelField(String jsonPath) {
        if (jsonPath.startsWith("$.") || jsonPath.startsWith("$[")) {
            String clean = jsonPath.substring(2)
                .replaceAll("[\\[\\]'\"]", "");
            int dotIndex = clean.indexOf('.');
            return dotIndex > 0 ? clean.substring(0, dotIndex) : clean;
        }
        return "参数";
    }

    /**
     * 获取字段描述信息
     *
     * @param fieldName 字段名
     * @param schemaNode Schema 节点
     * @return 字段描述，如果没有则返回字段名
     */
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

    /**
     * 从错误消息中提取数字
     *
     * @param message 错误消息
     * @param keyword 关键词
     * @return 提取的数字字符串
     */
    private static String extractNumber(String message, String keyword) {
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

    // ========== 元数据解析相关方法 ==========

    /**
     * 解析必填字段集合
     *
     * @param schemaNode Schema 节点
     * @return 必填字段集合
     */
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

    /**
     * 解析单个属性元数据
     *
     * @param name 属性名
     * @param node 属性 Schema 节点
     * @param required 是否必填
     * @return 属性元数据
     */
    private static PropertyMetadata parseProperty(String name, JsonNode node, boolean required) {
        PropertyMetadata meta = new PropertyMetadata();
        meta.setName(name);
        meta.setRequired(required);

        // 提取基本属性
        extractBasicProperties(meta, node);

        // 提取数值约束
        extractNumericConstraints(meta, node);

        // 提取枚举值
        extractEnumValues(meta, node);

        // 提取数组约束
        extractArrayConstraints(meta, node);

        // 处理嵌套对象
        processNestedObject(meta, node);

        // 处理数组项schema
        processArrayItemSchema(meta, node);

        return meta;
    }

    /**
     * 提取基本属性
     *
     * @param meta 元数据对象
     * @param node Schema 节点
     */
    private static void extractBasicProperties(PropertyMetadata meta, JsonNode node) {
        if (node.has("type"))
            meta.setType(node.get("type")
                .asText());
        if (node.has("description"))
            meta.setDescription(node.get("description")
                .asText());
        if (node.has("default")) {
            meta.setDefaultValue(MAPPER.convertValue(node.get("default"), Object.class));
        }
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
    }

    /**
     * 提取数值约束
     *
     * @param meta 元数据对象
     * @param node Schema 节点
     */
    private static void extractNumericConstraints(PropertyMetadata meta, JsonNode node) {
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
    }

    /**
     * 提取枚举值
     *
     * @param meta 元数据对象
     * @param node Schema 节点
     */
    private static void extractEnumValues(PropertyMetadata meta, JsonNode node) {
        if (node.has("enum") && node.get("enum")
            .isArray()) {
            // 修复：使用迭代器而不是 StreamSupport
            List<String> enums = new ArrayList<>();
            Iterator<JsonNode> elements = node.get("enum")
                .elements();
            while (elements.hasNext()) {
                enums.add(elements.next()
                    .asText());
            }
            meta.setEnumValues(enums);
        }
    }

    /**
     * 提取数组约束
     *
     * @param meta 元数据对象
     * @param node Schema 节点
     */
    private static void extractArrayConstraints(PropertyMetadata meta, JsonNode node) {
        if (node.has("minItems"))
            meta.setMinItems(node.get("minItems")
                .asInt());
        if (node.has("maxItems"))
            meta.setMaxItems(node.get("maxItems")
                .asInt());
        if (node.has("uniqueItems"))
            meta.setUniqueItems(node.get("uniqueItems")
                .asBoolean());
    }

    /**
     * 处理嵌套对象
     *
     * @param meta 元数据对象
     * @param node Schema 节点
     */
    private static void processNestedObject(PropertyMetadata meta, JsonNode node) {
        if (!"object".equals(meta.getType()) || !node.has("properties")) {
            return;
        }

        Set<String> nestedRequired = parseRequiredSet(node);
        JsonNode props = node.get("properties");

        // 修复：使用迭代器而不是 StreamSupport
        List<PropertyMetadata> children = new ArrayList<>();
        Iterator<Map.Entry<String, JsonNode>> fields = props.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            PropertyMetadata childMeta =
                parseProperty(entry.getKey(), entry.getValue(), nestedRequired.contains(entry.getKey()));
            children.add(childMeta);
        }

        meta.setChildren(children);
    }

    /**
     * 处理数组项schema
     *
     * @param meta 元数据对象
     * @param node Schema 节点
     */
    private static void processArrayItemSchema(PropertyMetadata meta, JsonNode node) {
        if (!"array".equals(meta.getType()) || !node.has("items") || !node.get("items")
            .isObject()) {
            return;
        }

        PropertyMetadata itemMeta = parseProperty("item", node.get("items"), false);
        meta.setArrayItemSchema(itemMeta);
    }

    /**
     * 构建属性节点
     *
     * @param meta 属性元数据
     * @return 构建的属性节点
     */
    private static ObjectNode buildPropertyNode(PropertyMetadata meta) {
        ObjectNode node = MAPPER.createObjectNode();

        if (meta.getType() != null)
            node.put("type", meta.getType());
        if (meta.getDescription() != null)
            node.put("description", meta.getDescription());
        if (meta.getDefaultValue() != null) {
            node.set("default", MAPPER.valueToTree(meta.getDefaultValue()));
        }

        if (meta.getMinLength() != null)
            node.put("minLength", meta.getMinLength());
        if (meta.getMaxLength() != null)
            node.put("maxLength", meta.getMaxLength());
        if (meta.getPattern() != null)
            node.put("pattern", meta.getPattern());
        if (meta.getFormat() != null)
            node.put("format", meta.getFormat());

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

        if (meta.getEnumValues() != null && !meta.getEnumValues()
            .isEmpty()) {
            ArrayNode enumNode = MAPPER.createArrayNode();
            meta.getEnumValues()
                .forEach(enumNode::add);
            node.set("enum", enumNode);
        }

        if (meta.getMinItems() != null)
            node.put("minItems", meta.getMinItems());
        if (meta.getMaxItems() != null)
            node.put("maxItems", meta.getMaxItems());
        if (meta.getUniqueItems() != null)
            node.put("uniqueItems", meta.getUniqueItems());

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

        if ("array".equals(meta.getType()) && meta.getArrayItemSchema() != null) {
            node.set("items", buildPropertyNode(meta.getArrayItemSchema()));
        }

        return node;
    }

    // ========== 内部辅助类 ==========

    /**
     * 错误消息构建器（策略模式）
     */
    private static class ErrorMessageBuilder {
        private static final Map<String, ErrorFormatter> FORMATTERS =
            Map.of("required", (error, label) -> label + " 是必填项", "minLength",
                (error, label) -> label + " 长度不能少于 " + extractNumber(error.getMessage(), "minLength"),
                "maxLength",
                (error, label) -> label + " 长度不能超过 " + extractNumber(error.getMessage(), "maxLength"), "minimum",
                (error, label) -> label + " 不能小于 " + extractNumber(error.getMessage(), "minimum"), "maximum",
                (error, label) -> label + " 不能大于 " + extractNumber(error.getMessage(), "maximum"), "enum",
                (error, label) -> label + " 取值不在允许范围内", "type", (error, label) -> label + " 类型不正确",
                "minItems",
                (error, label) -> label + " 项目数不能少于 " + extractNumber(error.getMessage(), "minItems"),
                "maxItems",
                (error, label) -> label + " 项目数不能超过 " + extractNumber(error.getMessage(), "maxItems"), "pattern",
                (error, label) -> label + " 格式不符合要求");

        /**
         * 构建错误消息
         *
         * @param error 验证错误
         * @param label 字段标签
         * @return 格式化的错误消息
         */
        static String build(ValidationMessage error, String label) {
            String type = error.getType();
            ErrorFormatter formatter = FORMATTERS.get(type);
            return formatter != null ? formatter.format(error, label) : label + " 校验失败: " + error.getMessage();
        }
    }

    /**
     * 错误消息格式化器接口
     */
    @FunctionalInterface
    private interface ErrorFormatter {
        /**
         * 格式化错误消息
         *
         * @param error 验证错误
         * @param label 字段标签
         * @return 格式化的错误消息
         */
        String format(ValidationMessage error, String label);
    }
}