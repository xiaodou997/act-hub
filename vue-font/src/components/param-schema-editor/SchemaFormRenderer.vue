<template>
  <div class="schema-form-renderer">
    <template v-if="schema.type === 'object' && schema.properties">
      <!-- 渲染扁平化的字段 -->
      <div
        v-for="field in flattenedFields"
        :key="field.fullKey"
        class="form-field-wrapper"
        v-show="showHiddenFields || !(field.prop.hidden || field.prop['x-hidden'])"
      >
        <el-form-item
          :label="field.label"
          :prop="field.fullKey"
          :rules="getFieldRules(field.prop)"
          :required="field.required"
          :class="{ 'hidden-field': field.prop.hidden || field.prop['x-hidden'] }"
        >
          <!-- 隐藏字段提示 -->
          <div
            v-if="field.prop.hidden || field.prop['x-hidden']"
            class="custom-alert custom-alert-info"
          >
            <el-icon class="custom-alert-icon"><InfoFilled /></el-icon>
            <span class="custom-alert-text">此字段已隐藏，不会展示给最终用户</span>
          </div>

          <!-- 字符串类型 -->
          <el-input
            v-else-if="field.prop.type === 'string' && !field.prop.enum"
            :model-value="getNestedValue(field.path)"
            @update:model-value="updateNestedValue(field.path, $event)"
            :placeholder="field.prop.description || `请输入${field.prop.title || field.key}`"
            :clearable="!field.required"
            :type="getInputType(field.prop)"
            :rows="field.prop.format === 'textarea' ? 3 : undefined"
            :maxlength="field.prop.maxLength"
            :show-word-limit="field.prop.maxLength !== undefined"
          />

          <!-- 数字类型 -->
          <el-input-number
            v-else-if="field.prop.type === 'number' || field.prop.type === 'integer'"
            :model-value="getNestedValue(field.path)"
            @update:model-value="updateNestedValue(field.path, $event)"
            :min="field.prop.minimum"
            :max="field.prop.maximum"
            :step="field.prop.type === 'integer' ? 1 : 0.1"
            :precision="field.prop.type === 'integer' ? 0 : 2"
            controls="false"
            style="width: 100%"
          />

          <!-- 布尔类型 -->
          <el-switch
            v-else-if="field.prop.type === 'boolean'"
            :model-value="getNestedValue(field.path)"
            @update:model-value="updateNestedValue(field.path, $event)"
            :active-value="true"
            :inactive-value="false"
          />

          <!-- 枚举类型 -->
          <el-select
            v-else-if="field.prop.enum"
            :model-value="getNestedValue(field.path)"
            @update:model-value="updateNestedValue(field.path, $event)"
            :placeholder="field.prop.description || `请选择${field.prop.title || field.key}`"
            :clearable="!field.required"
            style="width: 100%"
          >
            <el-option
              v-for="(option, optionIndex) in field.prop.enum"
              :key="optionIndex"
              :label="option"
              :value="option"
            />
          </el-select>

          <!-- 对象类型 - 简化为文本输入框 -->
          <div v-else-if="field.prop.type === 'object'" class="object-field-simple">
            <el-input
              type="textarea"
              :rows="3"
              :model-value="formatObjectForInput(getNestedValue(field.path))"
              @update:model-value="updateObjectFromInput(field.path, $event)"
              @blur="validateObjectInput(field.path, $event)"
              placeholder='输入任意格式的数据，例如: {"key": "value"} 或 "字符串" 或 123'
              class="object-textarea"
              :class="{ 'input-error': objectInputErrors[field.fullKey] }"
            />
            <div v-if="objectInputErrors[field.fullKey]" class="object-input-error">
              {{ objectInputErrors[field.fullKey] }}
            </div>
            <div class="object-input-small-tip">
              智能类型转换：输入123 → {obj:123}，输入"123" → {obj:"123"}，输入true →
              {obj:true}，输入{"key":"value"} → {obj:{"key":"value"}}
            </div>
          </div>

          <!-- 数组类型 - 使用标签风格 -->
          <div v-else-if="field.prop.type === 'array'" class="array-field-compact">
            <div class="array-tags-container">
              <div
                v-for="(item, index) in getNestedValue(field.path) || []"
                :key="index"
                class="array-item-wrapper"
              >
                <!-- 对象类型的数组项 -->
                <div
                  v-if="field.prop.items && field.prop.items.type === 'object'"
                  class="array-object-item"
                >
                  <div class="array-object-header">
                    <span class="array-object-title">项 {{ index + 1 }}</span>
                    <el-button
                      type="danger"
                      text
                      size="small"
                      @click="removeArrayItem(field.path, index)"
                      class="array-object-remove"
                    >
                      <el-icon><Delete /></el-icon>
                    </el-button>
                  </div>
                  <div class="array-object-content">
                    {{ formatObjectSummary(item) }}
                  </div>
                </div>

                <!-- 基本类型的数组项 -->
                <div v-else class="custom-tag" @click="removeArrayItem(field.path, index)">
                  <span class="custom-tag-text">{{
                    formatArrayItemDisplay(item, field.prop.items)
                  }}</span>
                  <el-icon class="custom-tag-close"><Close /></el-icon>
                </div>
              </div>

              <div v-if="!(getNestedValue(field.path) || []).length" class="custom-empty">
                <el-icon class="custom-empty-icon"><Box /></el-icon>
                <span class="custom-empty-text">暂无项目</span>
              </div>
            </div>

            <!-- 添加新项的输入框 -->
            <div class="add-array-item-container">
              <el-input
                v-if="field.prop.items && field.prop.items.type === 'string'"
                v-model="newArrayItem[field.fullKey]"
                placeholder="输入新项目值"
                @keyup.enter="addArrayItemFromInput(field.path, field.prop)"
                class="add-array-item-input"
                size="default"
              >
                <template #append>
                  <button
                    @click="addArrayItemFromInput(field.path, field.prop)"
                    :disabled="
                      !newArrayItem[field.fullKey] || newArrayItem[field.fullKey].trim() === ''
                    "
                    class="custom-add-btn"
                    type="button"
                  >
                    <el-icon class="custom-add-icon"><Plus /></el-icon>
                  </button>
                </template>
              </el-input>

              <el-input-number
                v-else-if="
                  field.prop.items &&
                  (field.prop.items.type === 'number' || field.prop.items.type === 'integer')
                "
                v-model="newArrayItem[field.fullKey]"
                :min="field.prop.items.minimum"
                :max="field.prop.items.maximum"
                :step="field.prop.items.type === 'integer' ? 1 : 0.1"
                :precision="field.prop.items.type === 'integer' ? 0 : 2"
                placeholder="输入新数值"
                @keyup.enter="addArrayItemFromInput(field.path, field.prop)"
                class="add-array-item-input"
                size="default"
                controls="false"
                style="width: 100%"
              >
                <template #append>
                  <button
                    @click="addArrayItemFromInput(field.path, field.prop)"
                    :disabled="
                      newArrayItem[field.fullKey] === undefined ||
                      newArrayItem[field.fullKey] === null
                    "
                    class="custom-add-btn"
                    type="button"
                  >
                    <el-icon class="custom-add-icon"><Plus /></el-icon>
                  </button>
                </template>
              </el-input-number>

              <el-button
                v-else
                type="primary"
                size="default"
                @click="addArrayItem(field.path, field.prop)"
                class="add-object-item-btn"
              >
                <el-icon class="add-item-icon"><Plus /></el-icon>
                {{ field.prop.items && field.prop.items.type === 'object' ? '添加对象' : '添加项' }}
              </el-button>
            </div>
          </div>

          <!-- 不支持的类型 -->
          <div v-else class="custom-alert custom-alert-warning">
            <el-icon class="custom-alert-icon"><InfoFilled /></el-icon>
            <span class="custom-alert-text">不支持的类型: {{ field.prop.type }}</span>
          </div>

          <!-- 字段描述 -->
          <template
            v-if="field.prop.description && !(field.prop.hidden || field.prop['x-hidden'])"
            #label
          >
            <div class="field-label-with-description">
              <span>{{ field.label }}</span>
              <el-tooltip :content="field.prop.description" placement="top">
                <el-icon class="description-icon"><InfoFilled /></el-icon>
              </el-tooltip>
            </div>
          </template>
        </el-form-item>
      </div>
    </template>

    <!-- 空状态 -->
    <div v-else class="empty-schema">
      <el-empty description="暂无表单字段配置" />
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, InfoFilled, Delete, Box } from '@element-plus/icons-vue'

// 定义组件属性
const props = defineProps({
  schema: {
    type: Object,
    default: () => ({ type: 'object', properties: {} }),
  },
  formData: {
    type: Object,
    default: () => ({}),
  },
  showHiddenFields: {
    type: Boolean,
    default: false,
  },
})

// 定义组件事件
const emit = defineEmits(['update:formData'])

// 新数组项的临时存储
const newArrayItem = ref({})

// 对象输入错误状态
const objectInputErrors = ref({})

// 计算属性
const requiredFields = computed(() => {
  return props.schema.required || []
})

// 扁平化字段
const flattenedFields = computed(() => {
  const fields = []
  let globalOrder = 0

  const flattenObject = (
    properties,
    parentPath = [],
    parentLabel = '',
    parentRequired = [],
    parentOrder = {},
  ) => {
    const keys = Object.keys(properties)

    keys.forEach((key) => {
      const prop = properties[key]
      const currentPath = [...parentPath, key]
      const fullKey = currentPath.join('.')
      const label = parentLabel ? `${parentLabel}.${prop.title || key}` : prop.title || key
      const required = parentRequired.includes(key)

      const fieldOrder = parentOrder[key] !== undefined ? parentOrder[key] : globalOrder

      fields.push({
        key,
        fullKey,
        label,
        prop,
        path: currentPath,
        required,
        order: fieldOrder,
      })
      globalOrder++
    })
  }

  if (props.schema.properties) {
    const rootOrder = props.schema['x-field-order'] || {}
    flattenObject(props.schema.properties, [], '', requiredFields.value, rootOrder)
  }

  return fields.sort((a, b) => a.order - b.order)
})

// 获取嵌套值
const getNestedValue = (path) => {
  let value = props.formData
  for (const key of path) {
    if (value && typeof value === 'object') {
      value = value[key]
    } else {
      return undefined
    }
  }
  return value
}

// 更新嵌套值
const updateNestedValue = (path, newValue) => {
  const newFormData = JSON.parse(JSON.stringify(props.formData))

  let current = newFormData
  for (let i = 0; i < path.length - 1; i++) {
    const key = path[i]
    if (!current[key] || typeof current[key] !== 'object') {
      current[key] = {}
    }
    current = current[key]
  }

  current[path[path.length - 1]] = newValue
  emit('update:formData', newFormData)
}

// 格式化数组项显示
const formatArrayItemDisplay = (item, itemSchema) => {
  if (itemSchema && itemSchema.type === 'object') {
    return formatObjectSummary(item)
  }
  return String(item)
}

// 格式化对象摘要
const formatObjectSummary = (obj) => {
  if (!obj || typeof obj !== 'object') {
    return '空对象'
  }

  const keys = Object.keys(obj)
  if (keys.length === 0) {
    return '空对象'
  }

  const validEntries = keys
    .filter((key) => obj[key] !== undefined && obj[key] !== null && obj[key] !== '')
    .slice(0, 2)
    .map(
      (key) =>
        `${key}: ${String(obj[key]).slice(0, 20)}${String(obj[key]).length > 20 ? '...' : ''}`,
    )

  if (validEntries.length === 0) {
    return `对象 (${keys.length}个字段)`
  }

  const summary = validEntries.join(', ')
  const remainingCount = keys.length - validEntries.length

  return remainingCount > 0 ? `${summary} +${remainingCount}项` : summary
}

// 格式化对象为输入框显示的字符串
const formatObjectForInput = (obj) => {
  if (obj === undefined || obj === null) {
    return ''
  }

  // 如果是对象或数组，格式化为JSON字符串
  if (typeof obj === 'object') {
    try {
      return JSON.stringify(obj, null, 2)
    } catch {
      return String(obj)
    }
  }

  // 其他类型直接转换为字符串显示
  return String(obj)
}

// 从输入框更新对象数据
const updateObjectFromInput = (path, inputString) => {
  // 实时输入时尝试智能处理并更新数据
  const fullKey = path.join('.')

  // 清除之前的错误状态
  objectInputErrors.value[fullKey] = ''

  // 如果输入为空，设置为空对象
  if (inputString.trim() === '') {
    updateNestedValue(path, {})
    return
  }

  try {
    // 实时输入时也尝试进行初步处理，提供即时反馈
    // 首先尝试直接解析JSON
    try {
      const parsed = JSON.parse(inputString)
      updateNestedValue(path, parsed)
    } catch {
      // JSON解析失败，尝试智能类型转换
      const smartParsed = smartTypeConversion(inputString)
      updateNestedValue(path, smartParsed)
    }
  } catch (error) {
    // 出错时保留原始字符串
    updateNestedValue(path, inputString)
  }
}

// 验证对象输入（在输入框失去焦点时调用）
const validateObjectInput = (path, event) => {
  const fullKey = path.join('.')
  const inputString = event.target.value

  // 清除之前的错误
  objectInputErrors.value[fullKey] = ''

  // 如果输入为空，设置为空对象
  if (inputString.trim() === '') {
    updateNestedValue(path, {})
    return
  }

  try {
    // 尝试解析为JSON
    const parsed = JSON.parse(inputString)
    // 支持各种数据类型，智能转换
    updateNestedValue(path, parsed)
  } catch (error) {
    // 如果JSON解析失败，尝试智能类型转换
    const smartParsed = smartTypeConversion(inputString)
    updateNestedValue(path, smartParsed)
  }
}

// 智能类型转换函数
const smartTypeConversion = (inputString) => {
  const trimmed = inputString.trim()

  // 如果是数字
  if (/^-?\d+(\.\d+)?$/.test(trimmed)) {
    return Number(trimmed)
  }

  // 如果是布尔值
  if (trimmed.toLowerCase() === 'true') {
    return true
  }
  if (trimmed.toLowerCase() === 'false') {
    return false
  }

  // 如果是带引号的字符串，去掉引号
  if (
    (trimmed.startsWith('"') && trimmed.endsWith('"')) ||
    (trimmed.startsWith("'") && trimmed.endsWith("'"))
  ) {
    return trimmed.slice(1, -1)
  }

  // 其他情况作为普通字符串处理
  return inputString
}

// 添加数组项
const addArrayItem = (path, prop) => {
  const currentArray = getNestedValue(path) || []
  let newItem = getDefaultValue(prop.items || { type: 'string' })

  const newArray = [...currentArray, newItem]
  updateNestedValue(path, newArray)
}

// 从输入框添加数组项
const addArrayItemFromInput = (path, prop) => {
  const fullKey = path.join('.')
  const inputValue = newArrayItem.value[fullKey]

  if (inputValue !== undefined && inputValue !== null && inputValue !== '') {
    const currentArray = getNestedValue(path) || []
    const newArray = [...currentArray, inputValue]
    updateNestedValue(path, newArray)

    newArrayItem.value[fullKey] =
      prop.items?.type === 'number' || prop.items?.type === 'integer' ? 0 : ''
  }
}

// 删除数组项
const removeArrayItem = (path, index) => {
  const currentArray = getNestedValue(path) || []
  const newArray = [...currentArray]
  newArray.splice(index, 1)
  updateNestedValue(path, newArray)
}

const getFieldRules = (prop) => {
  if (prop.hidden || prop['x-hidden']) {
    return undefined
  }

  const rules = []

  if (prop.required) {
    rules.push({ required: true, message: '此字段为必填项', trigger: 'blur' })
  }

  if (prop.type === 'string') {
    if (prop.minLength !== undefined) {
      rules.push({
        min: prop.minLength,
        message: `长度不能少于 ${prop.minLength} 个字符`,
        trigger: 'blur',
      })
    }
    if (prop.maxLength !== undefined) {
      rules.push({
        max: prop.maxLength,
        message: `长度不能超过 ${prop.maxLength} 个字符`,
        trigger: 'blur',
      })
    }

    if (prop.format) {
      const formatValidators = {
        email: {
          pattern: /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/,
          message: '请输入有效的邮箱地址',
        },
        uri: {
          pattern: /^https?:\/\/.+/,
          message: '请输入有效的URL地址',
        },
        phone: {
          pattern: /^1[3-9]\d{9}$/,
          message: '请输入有效的手机号码',
        },
      }

      const validator = formatValidators[prop.format]
      if (validator) {
        rules.push({
          pattern: validator.pattern,
          message: validator.message,
          trigger: 'blur',
        })
      }
    }
  }

  if (prop.type === 'number' || prop.type === 'integer') {
    if (prop.minimum !== undefined) {
      rules.push({
        validator: (rule, value, callback) => {
          if (value < prop.minimum) {
            callback(new Error(`值不能小于 ${prop.minimum}`))
          } else {
            callback()
          }
        },
        trigger: 'blur',
      })
    }
    if (prop.maximum !== undefined) {
      rules.push({
        validator: (rule, value, callback) => {
          if (value > prop.maximum) {
            callback(new Error(`值不能大于 ${prop.maximum}`))
          } else {
            callback()
          }
        },
        trigger: 'blur',
      })
    }
  }

  return rules.length > 0 ? rules : undefined
}

const getInputType = (prop) => {
  if (prop.format === 'textarea') {
    return 'textarea'
  }
  if (prop.format === 'email') {
    return 'email'
  }
  if (prop.format === 'password') {
    return 'password'
  }
  return 'text'
}

const getDefaultValue = (prop) => {
  if (prop.default !== undefined) {
    return prop.default
  }

  switch (prop.type) {
    case 'string':
      return prop.enum ? prop.enum[0] || '' : ''
    case 'integer':
    case 'number':
      return 0
    case 'boolean':
      return false
    case 'array':
      return []
    case 'object':
      const obj = {}
      if (prop.properties) {
        Object.keys(prop.properties).forEach((key) => {
          obj[key] = getDefaultValue(prop.properties[key])
        })
      }
      return obj
    default:
      return null
  }
}
</script>

<style scoped>
.schema-form-renderer {
  padding: 0;
  background: transparent;
  position: relative;
}

/* 使用左右布局的表单 */
.schema-form-renderer :deep(.el-form-item) {
  margin-bottom: 0;
  padding: 16px 20px;
  background: #ffffff;
  border-bottom: 1px solid #f0f0f0;
  transition: all 0.2s ease;
  display: flex;
  align-items: flex-start;
}

.schema-form-renderer :deep(.el-form-item:hover) {
  background: #fafafa;
}

.schema-form-renderer :deep(.el-form-item:first-child) {
  border-top-left-radius: 8px;
  border-top-right-radius: 8px;
}

.schema-form-renderer :deep(.el-form-item:last-child) {
  border-bottom-left-radius: 8px;
  border-bottom-right-radius: 8px;
  border-bottom: none;
}

/* 标签样式 - 左侧固定宽度，右对齐 */
.schema-form-renderer :deep(.el-form-item__label) {
  width: 140px;
  min-width: 140px;
  padding-right: 16px;
  text-align: right;
  font-weight: 500;
  color: #262626;
  font-size: 14px;
  line-height: 32px;
  margin-bottom: 0;
  flex-shrink: 0;
}

/* 内容区域 */
.schema-form-renderer :deep(.el-form-item__content) {
  flex: 1;
  min-width: 0;
}

/* 确保所有输入组件宽度一致 */
.schema-form-renderer :deep(.el-input),
.schema-form-renderer :deep(.el-select),
.schema-form-renderer :deep(.el-input-number),
.schema-form-renderer .object-field-simple,
.schema-form-renderer .array-field-compact {
  width: 100%;
}

/* 输入框样式优化 */
.schema-form-renderer :deep(.el-input__wrapper) {
  border-radius: 6px;
  border: 1px solid #d9d9d9;
  transition: all 0.2s ease;
  box-shadow: none;
  padding: 4px 11px;
}

.schema-form-renderer :deep(.el-input__wrapper):hover {
  border-color: #4096ff;
}

.schema-form-renderer :deep(.el-input__wrapper.is-focus) {
  border-color: #4096ff;
  box-shadow: 0 0 0 2px rgba(64, 150, 255, 0.1);
}

.schema-form-renderer :deep(.el-input__inner) {
  font-size: 14px;
  color: #262626;
  line-height: 1.5715;
}

.schema-form-renderer :deep(.el-textarea__inner) {
  border-radius: 6px;
  border: 1px solid #d9d9d9;
  transition: all 0.2s ease;
  box-shadow: none;
  padding: 8px 11px;
  font-size: 14px;
  color: #262626;
  line-height: 1.5715;
}

.schema-form-renderer :deep(.el-textarea__inner):hover {
  border-color: #4096ff;
}

.schema-form-renderer :deep(.el-textarea__inner):focus {
  border-color: #4096ff;
  box-shadow: 0 0 0 2px rgba(64, 150, 255, 0.1);
}

.schema-form-renderer :deep(.el-select) {
  width: 100%;
}

.schema-form-renderer :deep(.el-select .el-input__wrapper) {
  border-radius: 6px;
}

.schema-form-renderer :deep(.el-input-number) {
  width: 100%;
}

.schema-form-renderer :deep(.el-input-number .el-input__wrapper) {
  border-radius: 6px;
  padding: 4px 11px;
}

.schema-form-renderer :deep(.el-switch) {
  --el-switch-on-color: #1890ff;
  --el-switch-border-color: #1890ff;
  height: 22px;
  line-height: 22px;
}

.schema-form-renderer :deep(.el-button) {
  border-radius: 6px;
  font-weight: normal;
  transition: all 0.2s cubic-bezier(0.645, 0.045, 0.355, 1);
  padding: 4px 15px;
  height: 32px;
  font-size: 14px;
  box-shadow: 0 2px 0 rgba(0, 0, 0, 0.016);
}

.schema-form-renderer :deep(.el-button--primary) {
  background: #1890ff;
  border: 1px solid #1890ff;
}

.schema-form-renderer :deep(.el-button--primary):hover {
  background: #40a9ff;
  border-color: #40a9ff;
}

.schema-form-renderer :deep(.el-button--primary):active {
  background: #096dd9;
  border-color: #096dd9;
}

.schema-form-renderer :deep(.el-button--danger) {
  background: #ff4d4f;
  border: 1px solid #ff4d4f;
}

.schema-form-renderer :deep(.el-button--danger):hover {
  background: #ff7875;
  border-color: #ff7875;
}

.schema-form-renderer :deep(.el-button--danger):active {
  background: #d9363e;
  border-color: #d9363e;
}

.form-field-wrapper {
  margin-bottom: 0;
}

/* 字段标签和描述 */
.field-label-with-description {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.field-label-with-description span {
  font-weight: 500;
  color: #262626;
  font-size: 14px;
}

.description-icon {
  color: #8c8c8c;
  cursor: help;
  font-size: 16px;
  transition: color 0.2s ease;
  flex-shrink: 0;
}

.description-icon:hover {
  color: #1890ff;
}

/* 紧凑的数组字段样式 */
.array-field-compact {
  border: 1px solid #d9d9d9;
  border-radius: 6px;
  padding: 8px;
  background: #fafafa;
  transition: all 0.2s ease;
  width: 100%;
}

.array-field-compact:hover {
  border-color: #4096ff;
  background: #f5f5f5;
}

.array-tags-container {
  margin-bottom: 8px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  min-height: 32px;
  align-items: flex-start;
}

.array-item-wrapper {
  display: flex;
  align-items: center;
}

.array-tag {
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  border: 1px solid #d9d9d9;
  background: #ffffff;
  color: #262626;
  font-weight: normal;
  transition: all 0.2s ease;
  padding: 0 8px;
  height: 24px;
  line-height: 22px;
  font-size: 12px;
  border-radius: 4px;
}

.array-tag:hover {
  background: #fafafa;
  border-color: #4096ff;
}

/* 对象数组项样式 */
.array-object-item {
  border: 1px solid #d9d9d9;
  border-radius: 6px;
  background: #ffffff;
  padding: 12px;
  min-width: 200px;
  max-width: 300px;
  transition: all 0.2s ease;
}

.array-object-item:hover {
  border-color: #4096ff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.array-object-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  padding-bottom: 8px;
  border-bottom: 1px solid #f0f0f0;
}

.array-object-title {
  font-size: 13px;
  font-weight: 500;
  color: #1890ff;
}

.array-object-remove {
  padding: 0;
  width: 24px;
  height: 24px;
  border-radius: 4px;
}

.array-object-remove:hover {
  background: #fff1f0;
  color: #ff4d4f;
}

.array-object-content {
  font-size: 12px;
  color: #595959;
  line-height: 1.5;
  word-break: break-all;
}

/* 空状态样式 */
.array-empty-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 24px;
  color: #bfbfbf;
  font-size: 13px;
  width: 100%;
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  background: #ffffff;
}

.empty-icon {
  font-size: 32px;
  margin-bottom: 8px;
  opacity: 0.4;
  color: #bfbfbf;
}

.empty-text {
  font-size: 12px;
  color: #8c8c8c;
}

.add-array-item-container {
  display: flex;
  gap: 0;
  align-items: stretch;
  width: 100%;
}

.add-array-item-input {
  flex: 1;
  width: 100%;
  min-width: 0;
  max-width: 100%;
}

.add-array-item-input :deep(.el-input__wrapper) {
  border-radius: 6px 0 0 6px;
}

.add-array-item-input :deep(.el-input-number__wrapper) {
  border-radius: 6px 0 0 6px;
}

.add-array-item-input :deep(.el-input-group__append) {
  padding: 0;
  background: transparent;
  border: none;
  border-left: 1px solid #d9d9d9;
}

.add-array-item-input :deep(.el-input-group__append .el-button) {
  margin: 0;
  border-radius: 0 6px 6px 0;
}

.custom-add-btn {
  border-radius: 0 6px 6px 0;
  font-weight: normal;
  transition: all 0.2s ease;
  padding: 0 12px;
  height: 100%;
  border: none;
  background: #1890ff;
  color: #ffffff;
  margin: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  min-width: 40px;
  box-sizing: border-box;
}

.custom-add-btn:hover:not(:disabled) {
  background: #40a9ff;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(24, 144, 255, 0.3);
}

.custom-add-btn:active:not(:disabled) {
  background: #096dd9;
  transform: translateY(0);
}

.custom-add-btn:disabled {
  background: #f5f5f5;
  color: #d9d9d9;
  cursor: not-allowed;
  opacity: 0.6;
}

.custom-add-icon {
  font-size: 14px;
}

.add-object-item-btn {
  width: 100%;
  border: 1px dashed #d9d9d9;
  background: #fafafa;
  color: #595959;
  transition: all 0.2s ease;
}

.add-object-item-btn:hover {
  border-color: #1890ff;
  background: #f0f8ff;
  color: #1890ff;
}

/* 对象字段样式 */
.object-field-simple {
  border: 1px solid #d9d9d9;
  border-radius: 6px;
  padding: 8px;
  background: #fafafa;
  transition: all 0.2s ease;
  width: 100%;
}

.object-field-simple:hover {
  border-color: #4096ff;
  background: #f5f5f5;
}

.object-textarea {
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 13px;
  line-height: 1.5;
  resize: vertical;
}

.object-textarea :deep(.el-textarea__inner) {
  font-family: inherit;
  background: #ffffff;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  transition: all 0.2s ease;
}

.object-textarea :deep(.el-textarea__inner):focus {
  border-color: #1890ff;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.1);
}

.object-input-small-tip {
  font-size: 12px;
  color: #8c8c8c;
  margin-top: 8px;
  line-height: 1.4;
}

/* 对象输入错误样式 */
.object-input-error {
  font-size: 12px;
  color: #ff4d4f;
  margin-top: 4px;
  line-height: 1.4;
}

.input-error :deep(.el-textarea__inner) {
  border-color: #ff4d4f !important;
  box-shadow: 0 0 0 2px rgba(255, 77, 79, 0.1) !important;
}

/* 自定义提示样式 */
.custom-alert {
  padding: 12px 16px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  font-weight: normal;
}

.custom-alert-info {
  background: #f6ffed;
  border: 1px solid #b7eb8f;
  color: #389e0d;
}

.custom-alert-warning {
  background: #fff2e8;
  border: 1px solid #ffbb96;
  color: #d46b08;
}

.custom-alert-icon {
  font-size: 14px;
  flex-shrink: 0;
}

.custom-alert-info .custom-alert-icon {
  color: #52c41a;
}

.custom-alert-warning .custom-alert-icon {
  color: #ff7a45;
}

.custom-alert-text {
  line-height: 1.4;
}

/* 自定义标签样式 */
.custom-tag {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 8px;
  background: #f5f5f5;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  font-size: 12px;
  color: #595959;
  cursor: pointer;
  transition: all 0.2s ease;
  margin-right: 8px;
  margin-bottom: 8px;
}

.custom-tag:hover {
  background: #f0f0f0;
  border-color: #bfbfbf;
}

.custom-tag-text {
  line-height: 1;
}

.custom-tag-close {
  font-size: 12px;
  color: #8c8c8c;
  transition: color 0.2s ease;
}

.custom-tag:hover .custom-tag-close {
  color: #595959;
}

/* 自定义空状态样式 */
.custom-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 24px;
  color: #bfbfbf;
  font-size: 13px;
  width: 100%;
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  background: #ffffff;
  gap: 8px;
}

.custom-empty-icon {
  font-size: 32px;
  margin-bottom: 8px;
  opacity: 0.4;
  color: #bfbfbf;
}

.custom-empty-text {
  font-size: 12px;
  color: #8c8c8c;
}

/* 空状态样式 */
.empty-schema {
  padding: 48px 24px;
  text-align: center;
  background: #ffffff;
  border-radius: 8px;
  border: 1px solid #f0f0f0;
}

.empty-schema :deep(.el-empty) {
  padding: 0;
}

.empty-schema :deep(.el-empty__image) {
  width: 80px;
  height: 80px;
}

.empty-schema :deep(.el-empty__description) {
  font-size: 14px;
  color: #8c8c8c;
  margin-top: 16px;
}

/* 必填字段星号样式 */
.schema-form-renderer :deep(.el-form-item.is-required .el-form-item__label::before) {
  content: '*';
  color: #ff4d4f;
  margin-right: 4px;
  font-size: 14px;
  font-weight: 600;
}

/* 响应式设计 */
/* 较小桌面屏幕优化 */
@media (max-width: 1400px) {
  .schema-form-renderer :deep(.el-form-item__label) {
    width: 130px;
    min-width: 130px;
    padding-right: 14px;
  }
}

/* 中等屏幕优化 - 进一步减少标签宽度 */
@media (max-width: 1200px) {
  .schema-form-renderer :deep(.el-form-item__label) {
    width: 120px;
    min-width: 120px;
    padding-right: 12px;
  }
}

/* 小桌面/大平板优化 */
@media (max-width: 992px) {
  .schema-form-renderer :deep(.el-form-item__label) {
    width: 110px;
    min-width: 110px;
    padding-right: 10px;
    font-size: 13px;
  }

  .schema-form-renderer :deep(.el-form-item) {
    padding: 18px 20px;
  }
}

/* 平板设备优化 */
@media (max-width: 768px) {
  .schema-form-renderer :deep(.el-form-item) {
    flex-direction: column;
    align-items: stretch;
    padding: 16px;
  }

  .schema-form-renderer :deep(.el-form-item__label) {
    width: 100%;
    min-width: auto;
    text-align: left;
    padding-right: 0;
    margin-bottom: 8px;
    line-height: 1.5;
  }

  .schema-form-renderer :deep(.el-form-item__content) {
    width: 100%;
  }

  .array-field-compact {
    padding: 8px;
  }

  .array-tags-container {
    flex-direction: column;
    gap: 6px;
  }

  .array-object-item {
    max-width: 100%;
    min-width: auto;
  }

  .add-array-item-container {
    flex-direction: column;
    gap: 8px;
  }

  .add-array-item-input {
    width: 100%;
  }

  .add-array-item-input :deep(.el-input__wrapper),
  .add-array-item-input :deep(.el-input-number__wrapper) {
    border-radius: 6px;
  }

  .add-item-btn {
    border-radius: 6px;
  }

  .object-field-simple {
    padding: 8px;
  }

  .hidden-field-tip,
  .unsupported-field {
    padding: 8px 12px;
  }
}

/* 小屏幕优化 */
@media (max-width: 480px) {
  .schema-form-renderer :deep(.el-form-item) {
    padding: 12px;
  }

  .array-field-compact {
    padding: 6px;
  }

  .object-field-simple {
    padding: 6px;
  }

  .array-tag {
    max-width: 150px;
    font-size: 11px;
    padding: 0 6px;
    height: 20px;
    line-height: 18px;
  }

  .add-item-btn {
    padding: 6px 10px;
    height: 28px;
  }
}

/* 动画效果优化 */
.schema-form-renderer :deep(.el-form-item) {
  transition: all 0.3s cubic-bezier(0.645, 0.045, 0.355, 1);
}

.schema-form-renderer :deep(.el-input__wrapper),
.schema-form-renderer :deep(.el-textarea__inner) {
  transition: all 0.2s cubic-bezier(0.645, 0.045, 0.355, 1);
}

.schema-form-renderer :deep(.el-button) {
  transition: all 0.2s cubic-bezier(0.645, 0.045, 0.355, 1);
}

/* 滚动条样式优化 */
.schema-form-renderer :deep(.el-textarea__inner)::-webkit-scrollbar {
  width: 6px;
}

.schema-form-renderer :deep(.el-textarea__inner)::-webkit-scrollbar-track {
  background: #f5f5f5;
  border-radius: 3px;
}

.schema-form-renderer :deep(.el-textarea__inner)::-webkit-scrollbar-thumb {
  background: #d9d9d9;
  border-radius: 3px;
}

.schema-form-renderer :deep(.el-textarea__inner)::-webkit-scrollbar-thumb:hover {
  background: #bfbfbf;
}

/* 焦点状态优化 */
.schema-form-renderer :deep(.el-input__wrapper.is-focus),
.schema-form-renderer :deep(.el-textarea__inner:focus) {
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.1);
}

/* 禁用状态样式 */
.schema-form-renderer :deep(.el-input.is-disabled .el-input__wrapper) {
  background-color: #f5f5f5;
  border-color: #d9d9d9;
  color: #bfbfbf;
}

.schema-form-renderer :deep(.el-input-number.is-disabled .el-input__wrapper) {
  background-color: #f5f5f5;
  border-color: #d9d9d9;
}

.schema-form-renderer :deep(.el-switch.is-disabled) {
  opacity: 0.6;
}

/* 错误状态样式 */
.schema-form-renderer :deep(.el-form-item.is-error .el-input__wrapper) {
  border-color: #ff4d4f;
}

.schema-form-renderer :deep(.el-form-item.is-error .el-textarea__inner) {
  border-color: #ff4d4f;
}

.schema-form-renderer :deep(.el-form-item.is-error .el-input__wrapper.is-focus),
.schema-form-renderer :deep(.el-form-item.is-error .el-textarea__inner:focus) {
  box-shadow: 0 0 0 2px rgba(255, 77, 79, 0.1);
}

/* 成功状态样式 */
.schema-form-renderer :deep(.el-form-item.is-success .el-input__wrapper) {
  border-color: #52c41a;
}

.schema-form-renderer :deep(.el-form-item.is-success .el-textarea__inner) {
  border-color: #52c41a;
}

/* 加载状态样式 */
.schema-form-renderer :deep(.el-input.is-loading .el-input__wrapper) {
  border-color: #1890ff;
}

/* 悬停状态优化 */
.schema-form-renderer :deep(.el-form-item:hover) {
  background: #fafafa;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

/* 最后一个字段的圆角优化 */
.schema-form-renderer :deep(.el-form-item:last-child) {
  border-bottom-left-radius: 8px;
  border-bottom-right-radius: 8px;
  border-bottom: none;
}

/* 第一个字段的圆角优化 */
.schema-form-renderer :deep(.el-form-item:first-child) {
  border-top-left-radius: 8px;
  border-top-right-radius: 8px;
}

/* 字段标签和描述样式 */
.field-label-with-description {
  display: flex;
  align-items: center;
  gap: 6px;
  justify-content: flex-end;
}

.description-icon {
  font-size: 14px;
  color: #8c8c8c;
  cursor: help;
  transition: color 0.2s ease;
}

.description-icon:hover {
  color: #1890ff;
}

/* 表单字段间距优化 */
.form-field-wrapper:not(:last-child) {
  margin-bottom: 0;
}
</style>
