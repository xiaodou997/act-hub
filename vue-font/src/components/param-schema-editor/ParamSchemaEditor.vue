<template>
  <div class="param-schema-editor">
    <div class="editor-header">
      <h3>参数配置编辑器</h3>
      <div class="header-actions">
        <el-button-group>
          <el-button
            size="small"
            :type="editMode === 'visual' ? 'primary' : 'default'"
            @click="switchToVisualMode"
          >
            <el-icon><Grid /></el-icon>
            视图编辑
          </el-button>
          <el-button
            size="small"
            :type="editMode === 'json' ? 'primary' : 'default'"
            @click="switchToJsonMode"
          >
            <el-icon><Document /></el-icon>
            JSON编辑
          </el-button>
        </el-button-group>

        <el-button size="small" @click="importSchema">
          <el-icon><Upload /></el-icon>
          导入
        </el-button>
        <el-button size="small" @click="exportSchema">
          <el-icon><Download /></el-icon>
          导出
        </el-button>
        <el-button size="small" type="primary" @click="handleSave">
          <el-icon><Check /></el-icon>
          保存
        </el-button>
        <el-button size="small" @click="handleCancel">
          <el-icon><Close /></el-icon>
          取消
        </el-button>
      </div>
    </div>

    <div class="editor-container">
      <!-- 左侧：JSON编辑器或视图编辑器 -->
      <div v-if="editMode === 'json'" class="json-editor-section">
        <div class="section-header">
          <h4>JSON编辑器</h4>
          <div class="json-editor-actions">
            <el-button size="small" @click="restoreExample">恢复示例值</el-button>
            <el-button size="small" @click="resetSchema">重置</el-button>
            <el-button size="small" @click="formatJson">
              <el-icon><MagicStick /></el-icon>
              格式化JSON
            </el-button>
            <el-tooltip content="使用JSON Schema Draft-07格式定义参数结构" placement="top">
              <el-icon><InfoFilled /></el-icon>
            </el-tooltip>
          </div>
        </div>

        <div class="json-editor-wrapper">
          <textarea
            ref="jsonTextarea"
            v-model="jsonString"
            class="json-textarea"
            placeholder="请输入JSON Schema配置，支持Draft-07格式"
            @input="handleJsonChange"
          />
          <div v-if="jsonError" class="error-message">
            <el-icon><Warning /></el-icon>
            {{ jsonError }}
          </div>
        </div>
      </div>

      <!-- 视图编辑器 -->
      <div v-else class="visual-editor-section">
        <div class="section-header">
          <h4>视图编辑器</h4>
          <div class="visual-editor-actions">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索字段..."
              size="small"
              style="width: 140px; margin-right: 8px"
              clearable
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            <el-tag size="small" type="info" style="margin-right: 8px">
              总计: {{ fieldStatistics.total }}
            </el-tag>
            <el-tag
              size="small"
              type="warning"
              style="margin-right: 8px"
              v-if="fieldStatistics.hidden > 0"
            >
              隐藏: {{ fieldStatistics.hidden }}
            </el-tag>

            <el-button size="small" type="primary" @click="addRootField">
              <el-icon><Plus /></el-icon>
              添加字段
            </el-button>
            <el-dropdown size="small" style="margin-left: 8px">
              <el-button size="small">
                更多操作
                <el-icon class="el-icon--right"><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="clearAllFields">
                    <el-icon><Delete /></el-icon>
                    清空所有字段
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>

            <el-tooltip content="使用可视化方式编辑参数结构，适合非技术用户" placement="top">
              <el-icon><InfoFilled /></el-icon>
            </el-tooltip>
          </div>
        </div>

        <div class="visual-editor-wrapper">
          <div v-if="visualFields.length === 0" class="empty-visual-editor">
            <el-empty description="暂无字段配置，点击添加字段开始配置">
              <el-button type="primary" @click="addRootField">添加第一个字段</el-button>
            </el-empty>
          </div>

          <div v-else class="field-tree">
            <!-- 使用树形结构展示字段 -->
            <div class="field-tree-container">
              <field-tree-node
                v-for="(field, index) in filteredVisualFields"
                :key="field.id"
                :field="field"
                :level="0"
                :index="index"
                :parent-fields="visualFields"
                @update-field="updateField"
                @add-child="addChildField"
                @remove-field="removeField"
                @move-up="moveFieldUp"
                @move-down="moveFieldDown"
              />
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧：表单预览 -->
      <div class="form-preview-section">
        <div class="section-header">
          <h4>表单预览</h4>
          <div class="preview-actions">
            <el-tag type="info" size="small">实时预览</el-tag>
            <el-switch
              v-model="showHiddenFields"
              active-text="显示隐藏字段"
              inactive-text="隐藏字段"
              size="small"
            />
            <el-button size="small" text @click="resetFormData">重置表单</el-button>
          </div>
        </div>

        <div class="form-preview-wrapper">
          <div v-if="!isValidSchema" class="empty-preview">
            <el-empty description="请输入有效的JSON Schema配置" />
          </div>

          <div v-else class="form-container">
            <el-form v-if="formData" :model="formData" label-width="120px" label-position="left">
              <schema-form-renderer
                :schema="currentSchema"
                :form-data="formData"
                :show-hidden-fields="showHiddenFields"
                @update:form-data="updateFormData"
              />
            </el-form>
          </div>
        </div>
      </div>
    </div>

    <!-- 字段编辑弹窗 -->
    <el-dialog
      v-model="showFieldDialog"
      :title="editingField ? '编辑字段' : '添加字段'"
      width="700px"
      top="5vh"
      :close-on-click-modal="false"
      class="field-editor-dialog-wrapper"
    >
      <field-editor-dialog
        v-if="showFieldDialog"
        :field="editingField"
        :parent-field="editingParentField"
        @save="saveField"
        @cancel="cancelEditField"
      />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  InfoFilled,
  Warning,
  Grid,
  Document,
  MagicStick,
  Upload,
  Download,
  Check,
  Close,
  Plus,
  Search,
  ArrowDown,
  Delete,
} from '@element-plus/icons-vue'

// 导入组件
import FieldTreeNode from './FieldTreeNode.vue'
import FieldEditorDialog from './FieldEditorDialog.vue'
import SchemaFormRenderer from './SchemaFormRenderer.vue'

const props = defineProps({
  modelValue: {
    type: String,
    default: '{}',
  },
})

const emit = defineEmits(['update:modelValue', 'save', 'cancel'])

// 编辑模式：visual（视图编辑）或 json（JSON编辑）
const editMode = ref('visual')

// JSON字符串
const jsonString = ref(props.modelValue || '{}')

// JSON解析错误
const jsonError = ref('')

// 字段编辑弹窗
const showFieldDialog = ref(false)
const editingField = ref(null)
const editingParentField = ref(null)

// 表单数据
const formData = ref({})

// 是否显示隐藏字段
const showHiddenFields = ref(false)

// 视图编辑字段列表
const visualFields = ref([])

// 搜索关键词
const searchKeyword = ref('')

// 字段ID计数器
let fieldIdCounter = 0

// JSON Schema示例
const schemaExample = `{
  "type": "object",
  "properties": {
    "name": {
      "type": "string",
      "title": "姓名",
      "description": "请输入您的姓名",
      "minLength": 1,
      "maxLength": 50
    },
    "age": {
      "type": "integer",
      "title": "年龄",
      "description": "请输入您的年龄",
      "minimum": 0,
      "maximum": 150
    },
    "score": {
      "type": "number",
      "title": "分数",
      "description": "请输入分数",
      "minimum": 0,
      "maximum": 100
    },
    "isStudent": {
      "type": "boolean",
      "title": "是否学生",
      "description": "请选择是否为学生"
    },
    "hobbies": {
      "type": "array",
      "title": "兴趣爱好",
      "description": "请添加您的兴趣爱好",
      "items": {
        "type": "string"
      }
    },
    "address": {
      "type": "object",
      "title": "地址信息",
      "properties": {
        "province": {
          "type": "string",
          "title": "省份",
          "description": "请输入省份"
        },
        "city": {
          "type": "string",
          "title": "城市",
          "description": "请输入城市"
        },
        "detail": {
          "type": "string",
          "title": "详细地址",
          "description": "请输入详细地址"
        }
      }
    },
    "gender": {
      "type": "string",
      "title": "性别",
      "enum": ["男", "女", "其他"]
    }
  },
  "required": ["name", "age"]
}`

// 计算属性：当前Schema对象
const currentSchema = computed(() => {
  if (!isValidSchema.value) return { type: 'object', properties: {} }
  try {
    return JSON.parse(jsonString.value)
  } catch {
    return { type: 'object', properties: {} }
  }
})

// 计算属性：是否有效的Schema
const isValidSchema = computed(() => {
  return !jsonError.value && jsonString.value.trim() !== '' && jsonString.value.trim() !== '{}'
})

// 计算属性：字段统计信息
const fieldStatistics = computed(() => {
  const countFields = (fields) => {
    let total = 0
    let hidden = 0

    fields.forEach((field) => {
      total++
      if (field.hidden) {
        hidden++
      }

      if (field.children && field.children.length > 0) {
        const childStats = countFields(field.children)
        total += childStats.total
        hidden += childStats.hidden
      }
    })

    return { total, hidden }
  }

  return countFields(visualFields.value)
})

// 计算属性：过滤后的字段列表
const filteredVisualFields = computed(() => {
  if (!searchKeyword.value.trim()) {
    return visualFields.value
  }

  const keyword = searchKeyword.value.toLowerCase()

  const filterFields = (fields) => {
    return fields
      .filter((field) => {
        // 检查字段名、标题、描述是否匹配
        const matchesField =
          field.key?.toLowerCase().includes(keyword) ||
          field.title?.toLowerCase().includes(keyword) ||
          field.description?.toLowerCase().includes(keyword)

        // 检查子字段是否匹配
        const hasMatchingChildren =
          field.children && field.children.length > 0 && filterFields(field.children).length > 0

        return matchesField || hasMatchingChildren
      })
      .map((field) => {
        // 如果有子字段，也需要递归过滤
        if (field.children && field.children.length > 0) {
          return {
            ...field,
            children: filterFields(field.children),
          }
        }
        return field
      })
  }

  return filterFields(visualFields.value)
})

// 监听JSON变化
watch(jsonString, (newVal) => {
  validateJson(newVal)
  emit('update:modelValue', newVal)
})

// 监听编辑模式变化
watch(editMode, (newMode) => {
  if (newMode === 'visual') {
    // 从JSON转换为视图字段
    convertJsonToVisualFields()
  }
})

// 监听props变化
watch(
  () => props.modelValue,
  (newVal) => {
    if (newVal !== jsonString.value) {
      jsonString.value = newVal
      if (editMode.value === 'visual') {
        convertJsonToVisualFields()
      }
    }
  },
)

// JSON验证函数
const validateJson = (jsonStr) => {
  try {
    if (jsonStr.trim() === '') {
      jsonError.value = ''
      formData.value = {}
      return
    }

    const parsed = JSON.parse(jsonStr)
    jsonError.value = ''

    // 初始化表单数据
    initFormData(parsed)
  } catch (error) {
    jsonError.value = `JSON格式错误: ${error.message}`
    formData.value = {}
  }
}

// 初始化表单数据
const initFormData = (schema) => {
  if (schema.type === 'object' && schema.properties) {
    const data = {}
    Object.keys(schema.properties).forEach((key) => {
      const prop = schema.properties[key]
      data[key] = getDefaultValue(prop)
    })
    formData.value = data
  }
}

// 获取字段默认值
const getDefaultValue = (prop) => {
  if (prop.default !== undefined) {
    return prop.default
  }

  switch (prop.type) {
    case 'string':
      return ''
    case 'integer':
    case 'number':
      return 0
    case 'boolean':
      return false
    case 'array':
      return []
    case 'object':
      // 对于object类型，返回一个空对象或使用默认值（如果有）
      return prop.default !== undefined ? prop.default : {}
    default:
      return null
  }
}

// 从JSON转换为视图字段
const convertJsonToVisualFields = () => {
  try {
    if (!jsonString.value.trim() || jsonString.value.trim() === '{}') {
      visualFields.value = []
      return
    }

    const schema = JSON.parse(jsonString.value)
    if (schema.type === 'object' && schema.properties) {
      const requiredFields = schema.required || []
      const fields = []

      // 获取字段顺序信息（如果存在）
      const fieldOrder = schema['x-field-order'] || {}

      Object.keys(schema.properties).forEach((key, index) => {
        const prop = schema.properties[key]
        const field = createFieldFromProperty(key, prop, requiredFields.includes(key))

        // 设置字段顺序（用于后续排序）
        field._order = fieldOrder[key] !== undefined ? fieldOrder[key] : index

        fields.push(field)
      })

      // 按照原始顺序排序
      fields.sort((a, b) => a._order - b._order)

      visualFields.value = fields
    } else {
      visualFields.value = []
    }
  } catch (error) {
    console.error('JSON转换为视图字段失败:', error)
    visualFields.value = []
  }
}

// 从属性创建字段
const createFieldFromProperty = (key, prop, required = false, order = 0) => {
  // 优先判断是否为枚举类型
  let fieldType = prop.type || 'string'
  if (prop.enum && Array.isArray(prop.enum) && prop.enum.length > 0) {
    fieldType = 'enum'
  }

  const field = {
    id: fieldIdCounter++,
    key: key,
    title: prop.title || key,
    type: fieldType,
    description: prop.description || '',
    required: required,
    hidden: prop.hidden === true, // 确保正确读取hidden属性
    default: prop.default,
    children: [],
    _order: order, // 添加顺序字段
  }

  // 处理枚举类型
  if (prop.enum && Array.isArray(prop.enum) && prop.enum.length > 0) {
    field.enumOptions = [...prop.enum]
  }

  // 处理字符串类型的验证规则
  if (field.type === 'string') {
    if (prop.minLength !== undefined) {
      field.minLength = prop.minLength
    }
    if (prop.maxLength !== undefined) {
      field.maxLength = prop.maxLength
    }
    if (prop.format !== undefined) {
      field.format = prop.format
    }
  }

  // 处理数字类型的验证规则
  if (['integer', 'number'].includes(field.type)) {
    if (prop.minimum !== undefined) {
      field.minimum = prop.minimum
    }
    if (prop.maximum !== undefined) {
      field.maximum = prop.maximum
    }
  }

  // 处理数组类型 - 只记录itemsType，不再创建子字段
  if (prop.type === 'array' && prop.items) {
    if (prop.items.type === 'object' && prop.items.properties) {
      // 数组项是对象类型 - 只记录类型，不创建子字段
      field.itemsType = 'object'
    } else {
      // 数组项是基本类型
      field.itemsType = prop.items.type || 'string'
    }
  }

  // 处理对象类型 - 不再创建子字段，简化为文本输入方式
  if (prop.type === 'object') {
    // 保留空的children数组，确保兼容性
  }

  return field
}

// 从视图字段转换为JSON
const convertVisualFieldsToJson = () => {
  try {
    const properties = {}
    const requiredFields = []

    const processField = (field) => {
      if (!field.key || field.key.trim() === '') return null

      const prop = {
        title: field.title || field.key,
      }

      // 处理枚举类型 - 根据JSON Schema标准，有enum时不需要type
      if (field.type === 'enum' && field.enumOptions && field.enumOptions.length > 0) {
        prop.enum = field.enumOptions.filter((opt) => opt && opt.trim() !== '')
      } else {
        // 非枚举类型才设置type
        prop.type = field.type
      }

      // 添加描述
      if (field.description) {
        prop.description = field.description
      }

      // 设置默认值
      if (field.default !== undefined && field.default !== '') {
        prop.default = field.default
      }

      // 保存hidden属性到JSON - 确保隐藏字段信息不丢失
      if (field.hidden === true) {
        prop.hidden = true
        prop['x-hidden'] = true // 添加扩展属性以确保兼容性
      }

      // 处理字符串类型的验证规则
      if (field.type === 'string') {
        if (field.minLength !== undefined && field.minLength !== null && field.minLength !== '') {
          prop.minLength = Number(field.minLength)
        }
        if (field.maxLength !== undefined && field.maxLength !== null && field.maxLength !== '') {
          prop.maxLength = Number(field.maxLength)
        }
        if (field.format && field.format.trim() !== '') {
          prop.format = field.format
        }
      }

      // 处理数字类型的验证规则
      if (['integer', 'number'].includes(field.type)) {
        if (field.minimum !== undefined && field.minimum !== null && field.minimum !== '') {
          prop.minimum = Number(field.minimum)
        }
        if (field.maximum !== undefined && field.maximum !== null && field.maximum !== '') {
          prop.maximum = Number(field.maximum)
        }
      }

      // 处理数组类型 - 简化处理，不再处理子字段
      if (field.type === 'array') {
        // 直接设置items类型，不再处理子字段
        prop.items = {
          type: field.itemsType || 'string',
        }

        // 如果是对象类型且JSON中定义了properties，保留原始定义
        if (
          field.itemsType === 'object' &&
          currentSchema.value.properties &&
          currentSchema.value.properties[field.key] &&
          currentSchema.value.properties[field.key].items &&
          currentSchema.value.properties[field.key].items.properties
        ) {
          prop.items.properties = currentSchema.value.properties[field.key].items.properties
          prop.items.required = currentSchema.value.properties[field.key].items.required || []
        }
      }

      // 处理对象类型 - 不再处理子字段，简化为基本的object类型定义
      if (field.type === 'object') {
        // 直接设置为object类型，不再处理子字段
        // 默认值会作为文本输入，由用户输入JSON字符串
      }

      return prop
    }

    visualFields.value.forEach((field) => {
      if (field.key && field.key.trim() !== '') {
        const prop = processField(field)
        if (prop) {
          properties[field.key] = prop
          // 只有非隐藏且必填的字段才添加到required
          if (field.required && !field.hidden) {
            requiredFields.push(field.key)
          }
        }
      }
    })

    const schema = {
      type: 'object',
      properties: properties,
    }

    if (requiredFields.length > 0) {
      schema.required = requiredFields
    }

    const newJsonString = JSON.stringify(schema, null, 2)
    if (newJsonString !== jsonString.value) {
      jsonString.value = newJsonString
    }
  } catch (error) {
    console.error('转换视图字段到JSON失败:', error)
    ElMessage.error('字段转换失败，请检查字段配置')
  }
}

// 切换到视图模式
const switchToVisualMode = () => {
  editMode.value = 'visual'
  convertJsonToVisualFields()
}

// 切换到JSON模式
const switchToJsonMode = () => {
  if (editMode.value === 'visual') {
    convertVisualFieldsToJson()
  }
  editMode.value = 'json'
}

// 添加根字段
const addRootField = () => {
  editingField.value = null
  editingParentField.value = null
  showFieldDialog.value = true
}

// 添加子字段
const addChildField = (parentField) => {
  editingField.value = null
  editingParentField.value = parentField
  showFieldDialog.value = true
}

// 编辑字段
const updateField = (field) => {
  editingField.value = { ...field }
  editingParentField.value = null
  showFieldDialog.value = true
}

// 保存字段
const saveField = (fieldData) => {
  if (editingField.value) {
    // 更新现有字段 - 使用响应式的方式更新
    const fieldIndex = findFieldIndex(editingField.value)
    if (fieldIndex !== null) {
      // 创建新的字段对象以触发响应式更新
      const updatedField = { ...editingField.value, ...fieldData }
      replaceField(editingField.value, updatedField)
    }
  } else {
    // 添加新字段
    const newField = {
      id: fieldIdCounter++,
      ...fieldData,
      children: [],
    }

    if (editingParentField.value) {
      // 添加到父字段
      if (!editingParentField.value.children) {
        editingParentField.value.children = []
      }
      editingParentField.value.children.push(newField)
    } else {
      // 添加到根字段
      visualFields.value.push(newField)
    }
  }

  showFieldDialog.value = false
  editingField.value = null
  editingParentField.value = null

  // 自动更新JSON
  convertVisualFieldsToJson()

  // 强制触发视图更新
  nextTick(() => {
    // 触发响应式更新
    visualFields.value = [...visualFields.value]
  })

  ElMessage.success('字段保存成功')
}

// 查找字段在数组中的索引
const findFieldIndex = (targetField) => {
  const findInArray = (fields, target) => {
    for (let i = 0; i < fields.length; i++) {
      if (fields[i].id === target.id) {
        return { array: fields, index: i }
      }
      if (fields[i].children && fields[i].children.length > 0) {
        const result = findInArray(fields[i].children, target)
        if (result) return result
      }
    }
    return null
  }

  return findInArray(visualFields.value, targetField)
}

// 替换字段
const replaceField = (oldField, newField) => {
  const result = findFieldIndex(oldField)
  if (result) {
    // 保持children引用
    newField.children = oldField.children || []
    // 使用Vue的响应式替换
    result.array.splice(result.index, 1, newField)
  }
}

// 取消编辑字段
const cancelEditField = () => {
  showFieldDialog.value = false
  editingField.value = null
  editingParentField.value = null
}

// 删除字段
const removeField = (field, parentFields, index) => {
  ElMessageBox.confirm(`确定要删除字段"${field.title || field.key}"吗？`, '确认删除', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  })
    .then(() => {
      if (parentFields && index !== undefined) {
        parentFields.splice(index, 1)
      } else {
        // 根字段
        const fieldIndex = visualFields.value.findIndex((f) => f.id === field.id)
        if (fieldIndex > -1) {
          visualFields.value.splice(fieldIndex, 1)
        }
      }
      ElMessage.success('字段删除成功')
      convertVisualFieldsToJson()
    })
    .catch(() => {
      // 用户取消删除
    })
}

// 移动字段位置 - 向上移动
const moveFieldUp = (field, parentFields, index) => {
  const targetFields = parentFields || visualFields.value
  if (index > 0 && targetFields && targetFields.length > 1) {
    // 交换位置
    const temp = targetFields[index - 1]
    targetFields[index - 1] = targetFields[index]
    targetFields[index] = temp

    convertVisualFieldsToJson()
    ElMessage.success('字段顺序已调整')
  }
}

// 移动字段位置 - 向下移动
const moveFieldDown = (field, parentFields, index) => {
  const targetFields = parentFields || visualFields.value
  if (index < targetFields.length - 1 && targetFields && targetFields.length > 1) {
    // 交换位置
    const temp = targetFields[index + 1]
    targetFields[index + 1] = targetFields[index]
    targetFields[index] = temp

    convertVisualFieldsToJson()
    ElMessage.success('字段顺序已调整')
  }
}

// 清空所有字段
const clearAllFields = () => {
  ElMessageBox.confirm('确定要清空所有字段吗？此操作不可撤销。', '确认清空', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(() => {
    visualFields.value = []
    convertVisualFieldsToJson()
    ElMessage.success('所有字段已清空')
  })
}

// 导出Schema
const exportSchema = () => {
  try {
    const schema = JSON.parse(jsonString.value)
    const dataStr = JSON.stringify(schema, null, 2)
    const dataBlob = new Blob([dataStr], { type: 'application/json' })

    const link = document.createElement('a')
    link.href = URL.createObjectURL(dataBlob)
    link.download = `schema_${new Date().toISOString().slice(0, 10)}.json`
    link.click()

    ElMessage.success('Schema导出成功')
  } catch (error) {
    ElMessage.error('导出失败：Schema格式错误')
  }
}

// 导入Schema
const importSchema = () => {
  const input = document.createElement('input')
  input.type = 'file'
  input.accept = '.json'

  input.onchange = (event) => {
    const file = event.target.files[0]
    if (!file) return

    const reader = new FileReader()
    reader.onload = (e) => {
      try {
        const content = e.target.result
        const schema = JSON.parse(content)

        // 验证是否为有效的JSON Schema
        if (typeof schema === 'object' && schema !== null) {
          jsonString.value = JSON.stringify(schema, null, 2)
          convertJsonToVisualFields()
          ElMessage.success('Schema导入成功')
        } else {
          ElMessage.error('无效的Schema格式')
        }
      } catch (error) {
        ElMessage.error('文件解析失败：请确保文件为有效的JSON格式')
      }
    }
    reader.readAsText(file)
  }

  input.click()
}

// JSON变化处理
const handleJsonChange = () => {
  // 实时验证已经在watch中处理
}

// 格式化JSON
const formatJson = () => {
  try {
    const parsed = JSON.parse(jsonString.value)
    jsonString.value = JSON.stringify(parsed, null, 2)
    ElMessage.success('JSON格式化成功')
  } catch (error) {
    ElMessage.error('JSON格式错误，无法格式化')
  }
}

// 恢复示例值
const restoreExample = () => {
  ElMessageBox.confirm('确定要恢复示例值吗？当前内容将被替换。', '确认恢复示例值', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  })
    .then(() => {
      jsonString.value = schemaExample
      if (editMode.value === 'visual') {
        convertJsonToVisualFields()
      }
      ElMessage.success('示例值已恢复')
    })
    .catch(() => {
      // 用户取消操作
    })
}

// 重置Schema
const resetSchema = () => {
  ElMessageBox.confirm('确定要重置Schema吗？所有更改将丢失。', '确认重置', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(() => {
    jsonString.value = '{}'
    if (editMode.value === 'visual') {
      visualFields.value = []
    }
    ElMessage.success('重置成功')
  })
}

// 重置表单数据
const resetFormData = () => {
  initFormData(currentSchema.value)
  ElMessage.success('表单数据已重置')
}

// 更新表单数据
const updateFormData = (newFormData) => {
  formData.value = newFormData
}

// 保存
const handleSave = async () => {
  if (editMode.value === 'visual') {
    convertVisualFieldsToJson()
  }

  if (jsonError.value) {
    ElMessage.error('请先修正JSON格式错误')
    return
  }

  try {
    // 验证JSON格式
    const schema = JSON.parse(jsonString.value)

    // 生成字段顺序信息（仅在视图模式下）
    if (editMode.value === 'visual') {
      const fieldOrder = {}
      let orderCounter = 0

      const collectFieldOrder = (fields, prefix = '') => {
        fields.forEach((field) => {
          const fullKey = prefix ? `${prefix}.${field.key}` : field.key
          fieldOrder[fullKey] = orderCounter++

          // 处理子字段
          if (field.children && field.children.length > 0) {
            collectFieldOrder(field.children, fullKey)
          }
        })
      }

      collectFieldOrder(visualFields.value)

      // 添加字段顺序到schema的元数据中
      schema['x-field-order'] = fieldOrder
    }

    emit('save', JSON.stringify(schema, null, 2))
    ElMessage.success('参数配置保存成功')
  } catch (error) {
    ElMessage.error('JSON格式错误，请检查后重试')
  }
}

// 取消
const handleCancel = () => {
  emit('cancel')
}

// 组件挂载时初始化
onMounted(() => {
  validateJson(jsonString.value)
  if (editMode.value === 'visual') {
    convertJsonToVisualFields()
  }
})
</script>

<style scoped>
.param-schema-editor {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: #ffffff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.editor-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: #fafafa;
  border-bottom: 1px solid #e8e8e8;
}

.editor-header h3 {
  margin: 0;
  color: #333333;
  font-size: 16px;
  font-weight: 600;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-actions .el-button-group {
  margin-right: 4px;
}

.editor-container {
  display: flex;
  flex: 1;
  min-height: 0;
}

/* JSON编辑器样式 */
.json-editor-section {
  flex: 1.2;
  min-width: 600px;
  display: flex;
  flex-direction: column;
  border-right: 1px solid #e8e8e8;
  background: #ffffff;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: #f8f9fa;
  border-bottom: 1px solid #e8e8e8;
}

.section-header h4 {
  margin: 0;
  color: #333333;
  font-size: 14px;
  font-weight: 500;
}

.json-editor-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 16px;
}

.json-textarea {
  flex: 1;
  width: 100%;
  min-height: 400px;
  padding: 12px;
  border: 1px solid #d9d9d9;
  border-radius: 6px;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 13px;
  line-height: 1.5;
  resize: none;
  outline: none;
  background: #ffffff;
  color: #333333;
  transition: border-color 0.2s ease;
}

.json-textarea:focus {
  border-color: #409eff;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.1);
}

.error-message {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 8px;
  padding: 8px 12px;
  background: #fef0f0;
  border: 1px solid #fbc4c4;
  border-radius: 4px;
  color: #f56c6c;
  font-size: 12px;
}

.json-editor-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 视觉编辑器样式 */
.visual-editor-section {
  flex: 1.2;
  min-width: 600px;
  display: flex;
  flex-direction: column;
  border-right: 1px solid #e8e8e8;
  background: #ffffff;
}

.visual-editor-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.visual-editor-wrapper {
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.empty-visual-editor {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  background: #fafafa;
}

.field-tree {
  flex: 1;
  overflow: auto;
  padding: 16px;
}

.field-tree-container {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

/* 表单预览样式 */
.form-preview-section {
  flex: 1;
  min-width: 450px;
  display: flex;
  flex-direction: column;
  background: #ffffff;
  border-left: 1px solid #e8e8e8;
}

.preview-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.form-preview-wrapper {
  flex: 1;
  overflow: auto;
  padding: 20px;
  background: #ffffff;
}

.empty-preview {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 200px;
  background: #fafafa;
  border-radius: 4px;
  border: 1px dashed #d9d9d9;
}

.form-container {
  background: #ffffff;
  padding: 0;
}

/* 示例代码样式 */
.example-code {
  background: #f8f9fa;
  border: 1px solid #e8e8e8;
  border-radius: 6px;
  padding: 16px;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 13px;
  line-height: 1.5;
  color: #333333;
  white-space: pre-wrap;
  word-wrap: break-word;
  max-height: 400px;
  overflow: auto;
}

/* 滚动条样式 */
.json-textarea::-webkit-scrollbar,
.field-tree::-webkit-scrollbar,
.form-preview-wrapper::-webkit-scrollbar,
.example-code::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

.json-textarea::-webkit-scrollbar-track,
.field-tree::-webkit-scrollbar-track,
.form-preview-wrapper::-webkit-scrollbar-track,
.example-code::-webkit-scrollbar-track {
  background: #f5f5f5;
  border-radius: 3px;
}

.json-textarea::-webkit-scrollbar-thumb,
.field-tree::-webkit-scrollbar-thumb,
.form-preview-wrapper::-webkit-scrollbar-thumb,
.example-code::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.json-textarea::-webkit-scrollbar-thumb:hover,
.field-tree::-webkit-scrollbar-thumb:hover,
.form-preview-wrapper::-webkit-scrollbar-thumb:hover,
.example-code::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

/* 动画效果 */
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.json-editor-section,
.visual-editor-section,
.form-preview-section {
  animation: fadeInUp 0.3s ease-out;
}

/* 响应式设计 */

@media (max-width: 768px) {
  .editor-container {
    flex-direction: column;
  }

  .form-preview-section {
    width: 100%;
    border-left: none;
    border-top: 1px solid #e8e8e8;
  }

  .header-actions {
    flex-wrap: wrap;
    gap: 4px;
  }
}

/* 字段编辑弹框样式 */
:deep(.field-editor-dialog-wrapper) {
  .el-dialog {
    margin-top: 5vh !important;
    margin-bottom: 5vh !important;
    max-height: 90vh;
    display: flex;
    flex-direction: column;
  }

  .el-dialog__body {
    flex: 1;
    overflow-y: auto;
    max-height: calc(90vh - 120px);
    padding: 20px;
  }

  .el-dialog__header {
    flex-shrink: 0;
    padding: 20px 20px 10px 20px;
  }

  .el-dialog__footer {
    flex-shrink: 0;
    padding: 10px 20px 20px 20px;
  }
}
</style>
