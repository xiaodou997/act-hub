<template>
  <div class="dynamic-param-form">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="120px"
      label-position="top"
    >
      <!-- 如果有自定义内容，显示为第一个字段 -->
      <el-form-item v-if="customContent" label="自定义主题">
        <el-input
          :model-value="customContent"
          type="textarea"
          :rows="3"
          disabled
          placeholder="您输入的自定义内容"
        />
      </el-form-item>

      <!-- 动态渲染表单字段 -->
      <template v-for="field in visibleFields" :key="field.name">
        <el-form-item
          :label="field.description || field.name"
          :prop="field.name"
          :required="field.required"
        >
          <!-- 字符串类型 -->
          <template v-if="field.type === 'string'">
            <!-- 带枚举的字符串 - 下拉选择 -->
            <el-select
              v-if="field.enum && field.enum.length > 0"
              v-model="formData[field.name]"
              :placeholder="getPlaceholder(field)"
              clearable
              style="width: 100%"
            >
              <el-option v-for="opt in field.enum" :key="opt" :label="opt" :value="opt" />
            </el-select>

            <!-- 长文本 - textarea -->
            <el-input
              v-else-if="field.maxLength > 200 || field.name.includes('content')"
              v-model="formData[field.name]"
              type="textarea"
              :rows="4"
              :placeholder="getPlaceholder(field)"
              :maxlength="field.maxLength"
              show-word-limit
            />

            <!-- 普通字符串 - input -->
            <el-input
              v-else
              v-model="formData[field.name]"
              :placeholder="getPlaceholder(field)"
              :maxlength="field.maxLength"
              clearable
            />
          </template>

          <!-- 数字类型 -->
          <template v-else-if="field.type === 'number' || field.type === 'integer'">
            <el-input-number
              v-model="formData[field.name]"
              :min="field.minimum"
              :max="field.maximum"
              :placeholder="getPlaceholder(field)"
              controls-position="right"
              style="width: 200px"
            />
            <span v-if="field.description" class="field-hint">
              {{ getNumberHint(field) }}
            </span>
          </template>

          <!-- 布尔类型 -->
          <template v-else-if="field.type === 'boolean'">
            <el-switch v-model="formData[field.name]" active-text="是" inactive-text="否" />
          </template>

          <!-- 数组类型 - 标签输入 -->
          <template v-else-if="field.type === 'array'">
            <el-select
              v-model="formData[field.name]"
              multiple
              filterable
              allow-create
              default-first-option
              :placeholder="getPlaceholder(field)"
              style="width: 100%"
            >
              <el-option v-for="opt in field.enum || []" :key="opt" :label="opt" :value="opt" />
            </el-select>
            <div class="field-tip">输入后按回车添加</div>
          </template>

          <!-- 其他类型 - 默认文本输入 -->
          <template v-else>
            <el-input
              v-model="formData[field.name]"
              :placeholder="getPlaceholder(field)"
              clearable
            />
          </template>
        </el-form-item>
      </template>

      <!-- 如果没有可见字段 -->
      <el-empty
        v-if="visibleFields.length === 0 && !customContent"
        description="该应用无需额外参数"
        :image-size="80"
      />
    </el-form>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'

const props = defineProps({
  // JSON Schema 字符串
  schema: {
    type: String,
    default: '',
  },
  // 自定义内容（如果用户选择了自定义模式）
  customContent: {
    type: String,
    default: '',
  },
  // v-model
  modelValue: {
    type: Object,
    default: () => ({}),
  },
})

const emit = defineEmits(['update:modelValue'])

const formRef = ref(null)
const formData = ref({})
const formRules = ref({})

// 解析后的字段列表
const parsedFields = ref([])

// 可见字段（排除隐藏字段）
const visibleFields = computed(() => {
  return parsedFields.value.filter((f) => !f.hidden)
})

// 解析 JSON Schema
const parseSchema = (schemaStr) => {
  if (!schemaStr) {
    parsedFields.value = []
    return
  }

  try {
    const schema = JSON.parse(schemaStr)

    if (schema.type !== 'object' || !schema.properties) {
      parsedFields.value = []
      return
    }

    const requiredFields = schema.required || []
    const fields = []

    for (const [name, config] of Object.entries(schema.properties)) {
      fields.push({
        name,
        type: config.type || 'string',
        description: config.description || name,
        required: requiredFields.includes(name),
        default: config.default,
        hidden: config.hidden || false,
        // 字符串约束
        minLength: config.minLength,
        maxLength: config.maxLength,
        pattern: config.pattern,
        // 数字约束
        minimum: config.minimum,
        maximum: config.maximum,
        // 枚举
        enum: config.enum,
        // 数组约束
        minItems: config.minItems,
        maxItems: config.maxItems,
      })
    }

    parsedFields.value = fields

    // 初始化表单数据和校验规则
    initFormData()
    initFormRules()
  } catch (error) {
    console.error('解析 Schema 失败:', error)
    parsedFields.value = []
  }
}

// 初始化表单数据
const initFormData = () => {
  const data = {}

  for (const field of parsedFields.value) {
    // 设置默认值
    if (field.default !== undefined) {
      data[field.name] = field.default
    } else if (field.type === 'number' || field.type === 'integer') {
      data[field.name] = field.minimum || undefined
    } else if (field.type === 'boolean') {
      data[field.name] = false
    } else if (field.type === 'array') {
      data[field.name] = []
    } else {
      data[field.name] = ''
    }
  }

  // 如果有自定义内容，添加到参数中
  if (props.customContent) {
    data['customTopic'] = props.customContent
  }

  formData.value = data
  emit('update:modelValue', data)
}

// 初始化表单校验规则
const initFormRules = () => {
  const rules = {}

  for (const field of parsedFields.value) {
    if (field.hidden) continue // 隐藏字段不校验

    const fieldRules = []

    // 必填校验
    if (field.required) {
      fieldRules.push({
        required: true,
        message: `请输入${field.description || field.name}`,
        trigger: field.type === 'string' ? 'blur' : 'change',
      })
    }

    // 字符串长度校验
    if (field.type === 'string') {
      if (field.minLength) {
        fieldRules.push({
          min: field.minLength,
          message: `最少输入 ${field.minLength} 个字符`,
          trigger: 'blur',
        })
      }
      if (field.maxLength) {
        fieldRules.push({
          max: field.maxLength,
          message: `最多输入 ${field.maxLength} 个字符`,
          trigger: 'blur',
        })
      }
    }

    // 数字范围校验
    if (field.type === 'number' || field.type === 'integer') {
      if (field.minimum !== undefined || field.maximum !== undefined) {
        fieldRules.push({
          validator: (rule, value, callback) => {
            if (value === undefined || value === null || value === '') {
              callback()
              return
            }
            if (field.minimum !== undefined && value < field.minimum) {
              callback(new Error(`不能小于 ${field.minimum}`))
              return
            }
            if (field.maximum !== undefined && value > field.maximum) {
              callback(new Error(`不能大于 ${field.maximum}`))
              return
            }
            callback()
          },
          trigger: 'change',
        })
      }
    }

    if (fieldRules.length > 0) {
      rules[field.name] = fieldRules
    }
  }

  formRules.value = rules
}

// 获取占位符文本
const getPlaceholder = (field) => {
  if (field.description && field.description !== field.name) {
    return `请输入${field.description}`
  }
  return `请输入${field.name}`
}

// 获取数字字段提示
const getNumberHint = (field) => {
  const hints = []
  if (field.minimum !== undefined) hints.push(`最小: ${field.minimum}`)
  if (field.maximum !== undefined) hints.push(`最大: ${field.maximum}`)
  return hints.length > 0 ? `(${hints.join(', ')})` : ''
}

// 表单校验
const validate = () => {
  if (!formRef.value) return true

  let isValid = true
  formRef.value.validate((valid) => {
    isValid = valid
  })
  return isValid
}

// 重置表单
const resetForm = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
  initFormData()
}

// 监听 schema 变化
watch(
  () => props.schema,
  (newSchema) => {
    parseSchema(newSchema)
  },
  { immediate: true },
)

// 监听表单数据变化，同步到父组件
watch(
  formData,
  (newData) => {
    emit('update:modelValue', { ...newData })
  },
  { deep: true },
)

// 监听自定义内容变化
watch(
  () => props.customContent,
  (newContent) => {
    if (newContent) {
      formData.value.customTopic = newContent
    }
  },
)

// 暴露方法给父组件
defineExpose({
  validate,
  resetForm,
})
</script>

<style scoped lang="scss">
.dynamic-param-form {
  :deep(.el-form-item) {
    margin-bottom: 20px;
  }

  :deep(.el-form-item__label) {
    font-weight: 500;
    color: #303133;
  }
}

.field-hint {
  margin-left: 12px;
  font-size: 12px;
  color: #909399;
}

.field-tip {
  margin-top: 4px;
  font-size: 12px;
  color: #c0c4cc;
}
</style>
