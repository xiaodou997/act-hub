<template>
  <div class="field-editor-dialog">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" label-position="left">
      <!-- 基础信息 -->
      <el-divider content-position="left">基础信息</el-divider>

      <el-form-item label="字段标题" prop="title">
        <el-input v-model="form.title" placeholder="请输入字段标题" clearable />
      </el-form-item>

      <el-form-item label="字段标识" prop="key">
        <el-input v-model="form.key" placeholder="请输入字段标识（英文）" clearable />
        <div class="field-tip">用于JSON Schema中的属性名，建议使用英文</div>
      </el-form-item>

      <el-form-item label="字段描述">
        <el-input
          v-model="form.description"
          type="textarea"
          :rows="2"
          placeholder="请输入字段描述"
          resize="none"
        />
      </el-form-item>

      <!-- 类型配置 -->
      <el-divider content-position="left">类型配置</el-divider>

      <el-form-item label="字段类型" prop="type">
        <el-select
          v-model="form.type"
          placeholder="请选择字段类型"
          style="width: 100%"
          @change="handleTypeChange"
        >
          <el-option label="字符串" value="string" />
          <el-option label="整数" value="integer" />
          <el-option label="数字" value="number" />
          <el-option label="布尔值" value="boolean" />
          <el-option label="数组" value="array" />
          <el-option label="对象" value="object" />
          <el-option label="枚举" value="enum" />
        </el-select>
      </el-form-item>

      <!-- 数组类型配置 -->
      <el-form-item v-if="form.type === 'array'" label="数组项类型">
        <el-select v-model="form.itemsType" placeholder="请选择数组项类型" style="width: 100%">
          <el-option label="字符串" value="string" />
          <el-option label="整数" value="integer" />
          <el-option label="数字" value="number" />
          <el-option label="布尔值" value="boolean" />
          <el-option label="对象" value="object" />
        </el-select>
        <div class="field-tip">选择"对象"类型可以在数组中定义复杂的对象结构</div>
      </el-form-item>

      <!-- 枚举类型配置 -->
      <el-form-item v-if="form.type === 'enum'" label="枚举选项" prop="enumOptions">
        <div class="enum-options-editor">
          <!-- 枚举选项标签展示 -->
          <div class="enum-tags-container">
            <el-tag
              v-for="(option, index) in form.enumOptions.filter((opt) => opt && opt.trim() !== '')"
              :key="index"
              :closable="form.enumOptions.filter((opt) => opt && opt.trim() !== '').length > 1"
              @close="removeEnumOption(index)"
              class="enum-tag"
              size="default"
              type="primary"
            >
              {{ option }}
            </el-tag>
            <el-tag
              v-if="form.enumOptions.filter((opt) => opt && opt.trim() !== '').length === 0"
              class="enum-tag placeholder-tag"
              type="info"
            >
              暂无选项
            </el-tag>
          </div>

          <!-- 添加新选项的输入框 -->
          <div class="add-option-container">
            <el-input
              v-model="newEnumOption"
              placeholder="输入新选项值"
              @keyup.enter="addEnumOptionFromInput"
              @blur="addEnumOptionFromInput"
              class="add-option-input"
              size="small"
            >
              <template #append>
                <el-button
                  @click="addEnumOptionFromInput"
                  :disabled="!newEnumOption || newEnumOption.trim() === ''"
                  type="primary"
                  size="small"
                  class="add-enum-option-btn"
                >
                  <el-icon><Plus /></el-icon>
                </el-button>
              </template>
            </el-input>
          </div>
        </div>
      </el-form-item>

      <!-- 属性配置 -->
      <el-divider content-position="left">属性配置</el-divider>

      <el-form-item label="默认值">
        <el-input
          v-model="form.default"
          :placeholder="getDefaultValuePlaceholder()"
          :type="form.type === 'object' || form.type === 'array' ? 'textarea' : 'text'"
          :rows="form.type === 'object' ? 3 : 1"
          :disabled="false"
        />
        <div class="field-tip">
          {{ getDefaultValueTip() }}
        </div>
      </el-form-item>

      <el-form-item label="是否隐藏" class="switch-form-item">
        <div class="switch-container">
          <el-switch
            v-model="form.hidden"
            :active-value="true"
            :inactive-value="false"
            @change="handleHiddenChange"
          />
          <div class="field-tip">隐藏的字段不会在表单中显示</div>
        </div>
      </el-form-item>

      <el-form-item label="是否必填" class="switch-form-item">
        <div class="switch-container">
          <el-switch
            v-model="form.required"
            :active-value="true"
            :inactive-value="false"
            :disabled="form.hidden"
          />
          <div class="field-tip" v-if="form.hidden">隐藏字段不能设置为必填</div>
          <div class="field-tip" v-else>设置字段是否为必填项</div>
        </div>
      </el-form-item>

      <!-- 验证规则 -->
      <template v-if="showValidationRules">
        <el-divider content-position="left">验证规则</el-divider>

        <el-row :gutter="12">
          <el-col :span="12" v-if="form.type === 'string'">
            <el-form-item label="最小长度">
              <el-input-number
                v-model="form.minLength"
                :min="0"
                controls-position="right"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12" v-if="form.type === 'string'">
            <el-form-item label="最大长度">
              <el-input-number
                v-model="form.maxLength"
                :min="1"
                controls-position="right"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="12">
          <el-col :span="12" v-if="['integer', 'number'].includes(form.type)">
            <el-form-item label="最小值">
              <el-input-number
                v-model="form.minimum"
                controls-position="right"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12" v-if="['integer', 'number'].includes(form.type)">
            <el-form-item label="最大值">
              <el-input-number
                v-model="form.maximum"
                controls-position="right"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item v-if="form.type === 'string'" label="格式验证">
          <el-select v-model="form.format" placeholder="请选择格式" style="width: 100%">
            <el-option label="无" value="" />
            <el-option label="邮箱地址" value="email" />
            <el-option label="URL地址" value="uri" />
            <el-option label="日期时间" value="date-time" />
            <el-option label="IP地址" value="ipv4" />
            <el-option label="手机号码" value="phone" />
          </el-select>
        </el-form-item>
      </template>

      <!-- 字段信息提示 -->
      <div class="field-info-tip" v-if="parentField">
        <el-alert
          :title="`此字段将作为'${parentField.title || parentField.key}'的子字段`"
          type="info"
          :closable="false"
          show-icon
        />
      </div>
    </el-form>

    <!-- 操作按钮 -->
    <div class="dialog-actions">
      <el-button @click="handleCancel">取消</el-button>
      <el-button type="primary" @click="handleSave">保存</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Delete, Plus } from '@element-plus/icons-vue'

// 定义组件属性
const props = defineProps({
  field: {
    type: Object,
    default: null,
  },
  parentField: {
    type: Object,
    default: null,
  },
})

// 定义组件事件
const emit = defineEmits(['save', 'cancel'])

// 表单引用
const formRef = ref()

// 表单数据
const form = reactive({
  key: '',
  title: '',
  type: 'string',
  description: '',
  required: false,
  hidden: false,
  default: '',
  itemsType: 'string',
  enumOptions: [''],
  minLength: undefined,
  maxLength: undefined,
  minimum: undefined,
  maximum: undefined,
  format: '',
})

// 新枚举选项输入
const newEnumOption = ref('')

// 表单验证规则
const rules = {
  title: [{ required: true, message: '请输入字段标题', trigger: 'blur' }],
  key: [
    { required: true, message: '请输入字段标识', trigger: 'blur' },
    {
      pattern: /^[a-zA-Z_][a-zA-Z0-9_]*$/,
      message: '字段标识只能包含字母、数字和下划线，且不能以数字开头',
      trigger: 'blur',
    },
  ],
  type: [{ required: true, message: '请选择字段类型', trigger: 'change' }],
  enumOptions: [
    {
      validator: (rule, value, callback) => {
        if (form.type === 'enum') {
          const validOptions = value.filter((opt) => opt && opt.trim() !== '')
          if (validOptions.length === 0) {
            callback(new Error('至少需要填写一个枚举选项'))
          } else {
            callback()
          }
        } else {
          callback()
        }
      },
      trigger: 'blur',
    },
  ],
}

// 计算属性
const showValidationRules = computed(() => {
  return ['string', 'integer', 'number'].includes(form.type)
})

// 方法定义 - 在watch之前定义所有方法
const resetForm = () => {
  Object.keys(form).forEach((key) => {
    if (key === 'enumOptions') {
      form[key] = ['']
    } else if (typeof form[key] === 'boolean') {
      form[key] = false
    } else if (typeof form[key] === 'number') {
      form[key] = undefined
    } else {
      form[key] = ''
    }
  })
  form.type = 'string'
  form.itemsType = 'string'
}

const handleTypeChange = (newType) => {
  // 类型变化时重置相关字段
  if (newType !== 'enum') {
    form.enumOptions = ['']
  }

  if (newType !== 'array') {
    form.itemsType = 'string'
  }

  // 类型变化时可以保留默认值
  // 如果需要在类型变化时重置默认值，可以取消下面的注释
  // form.default = ''
}

const handleHiddenChange = (hidden) => {
  if (hidden) {
    // 隐藏时自动取消必填
    form.required = false
  }
}

const addEnumOption = () => {
  form.enumOptions.push('')
}

const addEnumOptionFromInput = () => {
  if (newEnumOption.value && newEnumOption.value.trim() !== '') {
    const trimmedValue = newEnumOption.value.trim()
    // 检查是否已存在相同选项
    const validOptions = form.enumOptions.filter((opt) => opt && opt.trim() !== '')
    if (!validOptions.includes(trimmedValue)) {
      // 移除空选项，添加新选项
      form.enumOptions = form.enumOptions.filter((opt) => opt && opt.trim() !== '')
      form.enumOptions.push(trimmedValue)
      newEnumOption.value = ''
    } else {
      ElMessage.warning('该选项已存在')
    }
  }
}

const removeEnumOption = (index) => {
  const validOptions = form.enumOptions.filter((opt) => opt && opt.trim() !== '')
  if (validOptions.length > 1) {
    const optionToRemove = validOptions[index]
    const originalIndex = form.enumOptions.findIndex((opt) => opt === optionToRemove)
    if (originalIndex !== -1) {
      form.enumOptions.splice(originalIndex, 1)
    }
  }
}

const getDefaultValuePlaceholder = () => {
  const placeholders = {
    string: '请输入字符串默认值',
    integer: '请输入整数默认值',
    number: '请输入数字默认值',
    boolean: 'true/false',
    enum: '请选择默认值',
    object: '请输入JSON格式的对象，如：{"key": "value"}',
    array: '请输入JSON格式的数组，如：["item1", "item2"]',
  }
  return placeholders[form.type] || '请输入默认值'
}

const getDefaultValueTip = () => {
  if (form.type === 'object') {
    return '对象类型默认值需输入有效的JSON格式，如：{"key": "value"}'
  } else if (form.type === 'array') {
    return '数组类型默认值需输入有效的JSON格式，如：["item1", "item2"]'
  }
  return '设置字段的默认值'
}

const handleSave = async () => {
  try {
    // 表单验证
    await formRef.value.validate()

    // 处理枚举选项
    if (form.type === 'enum') {
      form.enumOptions = form.enumOptions.filter((opt) => opt && opt.trim() !== '')
    }

    // 清理未使用的验证规则字段
    const cleanedForm = { ...form }
    if (!showValidationRules.value) {
      delete cleanedForm.minLength
      delete cleanedForm.maxLength
      delete cleanedForm.minimum
      delete cleanedForm.maximum
      delete cleanedForm.format
    }

    // 发出保存事件
    emit('save', cleanedForm)
    ElMessage.success(props.field ? '字段更新成功' : '字段添加成功')
  } catch (error) {
    // 验证失败
    console.log('表单验证失败:', error)
  }
}

const handleCancel = () => {
  emit('cancel')
}

// 监听属性变化 - 放在所有方法定义之后
watch(
  () => props.field,
  (newField) => {
    if (newField) {
      // 编辑现有字段
      Object.keys(form).forEach((key) => {
        if (newField[key] !== undefined) {
          form[key] = newField[key]
        }
      })

      // 确保枚举选项数组存在
      if (form.type === 'enum' && (!form.enumOptions || form.enumOptions.length === 0)) {
        form.enumOptions = ['']
      }
    } else {
      // 添加新字段
      resetForm()

      // 如果是子字段，设置默认key
      if (props.parentField) {
        form.key = `${props.parentField.key}_field`
        form.title = `${props.parentField.title}字段`
      }
    }
  },
  { immediate: true },
)

// 组件挂载时的初始化
onMounted(() => {
  // 确保表单初始状态正确
  if (!props.field) {
    resetForm()
  }
})
</script>

<style scoped>
.field-editor-dialog {
  max-width: 800px;
  margin: 0 auto;
}

.field-editor-dialog :deep(.el-dialog) {
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);
  border: 1px solid #f5f5f5;
}

.field-editor-dialog :deep(.el-dialog__header) {
  background: #ffffff;
  border-bottom: 1px solid #f8f8f8;
  padding: 24px 32px 20px;
}

.field-editor-dialog :deep(.el-dialog__title) {
  color: #1a1a1a;
  font-size: 20px;
  font-weight: 500;
  letter-spacing: -0.2px;
}

.field-editor-dialog :deep(.el-dialog__body) {
  background: #ffffff;
  padding: 32px;
}

.field-editor-dialog :deep(.el-dialog__footer) {
  background: #ffffff;
  border-top: 1px solid #f8f8f8;
  padding: 20px 32px 24px;
}

.field-editor-dialog :deep(.el-form-item__label) {
  color: #333333;
  font-weight: 500;
  font-size: 14px;
}

.field-editor-dialog :deep(.el-input__wrapper) {
  border-radius: 8px;
  border: 1px solid #e8e8e8;
  box-shadow: none;
  transition: all 0.2s ease;
  background: #ffffff;
}

.field-editor-dialog :deep(.el-input__wrapper):hover {
  border-color: #d0d0d0;
}

.field-editor-dialog :deep(.el-input__wrapper.is-focus) {
  border-color: #4096ff;
  box-shadow: 0 0 0 3px rgba(64, 150, 255, 0.08);
}

.field-editor-dialog :deep(.el-select) {
  width: 100%;
}

.field-editor-dialog :deep(.el-select .el-input__wrapper) {
  border-radius: 8px;
}

.field-editor-dialog :deep(.el-select-dropdown__item) {
  border-radius: 6px;
  margin: 2px 4px;
}

.field-editor-dialog :deep(.el-select-dropdown__item.selected) {
  background: #f0f7ff;
  color: #4096ff;
}

.field-editor-dialog :deep(.el-textarea__inner) {
  border-radius: 8px;
  border: 1px solid #e8e8e8;
  box-shadow: none;
  transition: all 0.2s ease;
  background: #ffffff;
}

.field-editor-dialog :deep(.el-textarea__inner):hover {
  border-color: #d0d0d0;
}

.field-editor-dialog :deep(.el-textarea__inner):focus {
  border-color: #4096ff;
  box-shadow: 0 0 0 3px rgba(64, 150, 255, 0.08);
}

/* 数字输入框优化 - 隐藏箭头按钮 */
.field-editor-dialog :deep(.el-input-number .el-input__wrapper) {
  border-radius: 8px;
  padding-right: 11px;
  background: #ffffff;
}

.field-editor-dialog :deep(.el-input-number .el-input__inner) {
  text-align: left;
  padding: 0 12px;
}

.field-editor-dialog :deep(.el-input-number__decrease),
.field-editor-dialog :deep(.el-input-number__increase) {
  display: none;
}

.field-editor-dialog :deep(.el-switch) {
  --el-switch-on-color: #4096ff;
}

.field-editor-dialog :deep(.el-button) {
  border-radius: 8px;
  font-weight: 500;
  transition: all 0.2s ease;
  padding: 10px 18px;
  border: 1px solid transparent;
}

.field-editor-dialog :deep(.el-button--primary) {
  background: #4096ff;
  border-color: #4096ff;
  color: #ffffff;
}

.field-editor-dialog :deep(.el-button--primary):hover {
  background: #1677ff;
  border-color: #1677ff;
  box-shadow: 0 4px 12px rgba(64, 150, 255, 0.25);
}

.field-editor-dialog :deep(.el-button--default) {
  background: #ffffff;
  border-color: #e8e8e8;
  color: #595959;
}

.field-editor-dialog :deep(.el-button--default):hover {
  border-color: #4096ff;
  color: #4096ff;
  background: #ffffff;
}

.field-editor-dialog :deep(.el-button--danger) {
  background: #ff4d4f;
  border-color: #ff4d4f;
  color: #ffffff;
}

.field-editor-dialog :deep(.el-button--danger):hover {
  background: #ff7875;
  border-color: #ff7875;
  box-shadow: 0 4px 12px rgba(255, 77, 79, 0.2);
}

.field-editor-dialog :deep(.el-checkbox__label) {
  color: #333333;
}

.field-editor-dialog :deep(.el-radio__label) {
  color: #333333;
}

.enum-options-editor {
  border-radius: 12px;
  padding: 24px;
  background: #fafafa;
  border: 1px solid #f5f5f5;
  transition: all 0.2s ease;
}

.enum-options-editor:hover {
  border-color: #e8e8e8;
  background: #ffffff;
}

.enum-tags-container {
  min-height: 48px;
  padding: 16px;
  background: #ffffff;
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  margin-bottom: 16px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}

/* 优化添加选项输入区域 */
.add-option-container {
  margin-top: 12px;
  padding: 0;
  background: transparent;
  margin-right: 8px; /* 添加右边距，与枚举组件右边框保持空隙 */
}

.add-option-input {
  max-width: 300px;
}

/* 优化输入框和按钮的整体外观 */
.add-option-input :deep(.el-input__wrapper) {
  border-radius: 6px 0 0 6px;
  border-right: none;
  box-shadow: none;
  transition: all 0.2s ease;
  background: #ffffff;
}

.add-option-input :deep(.el-input__wrapper):hover {
  border-color: #4096ff;
}

.add-option-input :deep(.el-input__wrapper.is-focus) {
  border-color: #4096ff;
  box-shadow: 0 0 0 2px rgba(64, 150, 255, 0.1);
}

/* 优化按钮在输入框中的显示 */
.add-option-input :deep(.el-input-group__append) {
  padding: 0;
  border: none;
  background: transparent;
}

.add-option-input :deep(.el-input-group__append .add-enum-option-btn) {
  border-radius: 0 6px 6px 0;
  height: 100%;
  border-left: none;
  transition: all 0.2s ease;
}

.add-option-input :deep(.el-input-group__append .add-enum-option-btn):hover {
  border-color: #1677ff;
  background: #1677ff;
}

.enum-tag {
  margin: 0;
  font-size: 13px;
  border-radius: 4px;
  transition: all 0.2s ease;
}

.enum-tag:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.placeholder-tag {
  color: #999999;
  background: #f5f5f5;
  border-color: #e8e8e8;
}

.add-option-container {
  margin-top: 8px;
}

.add-option-input {
  max-width: 300px;
}

/* 枚举选项添加按钮样式优化 */
.add-enum-option-btn {
  background: #4096ff !important;
  border-color: #4096ff !important;
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
  padding: 8px 12px !important;
  transition: all 0.2s ease !important;
  box-shadow: 0 2px 4px rgba(64, 150, 255, 0.2) !important;
}

.add-enum-option-btn:hover {
  background: #1677ff !important;
  border-color: #1677ff !important;
  box-shadow: 0 4px 8px rgba(64, 150, 255, 0.3) !important;
  transform: translateY(-1px) !important;
}

.add-enum-option-btn:active {
  transform: translateY(0) !important;
  box-shadow: 0 2px 4px rgba(64, 150, 255, 0.2) !important;
}

.add-enum-option-btn .el-icon {
  color: #ffffff !important;
  font-size: 14px !important;
  font-weight: 600 !important;
  vertical-align: middle !important;
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
}

.add-enum-option-btn:disabled {
  background: #f5f5f5 !important;
  border-color: #d9d9d9 !important;
  cursor: not-allowed !important;
  box-shadow: none !important;
  transform: none !important;
}

.add-enum-option-btn:disabled .el-icon {
  color: #bfbfbf !important;
}

.switch-form-item .el-form-item__content {
  align-items: flex-start;
}

.switch-container {
  display: flex;
  align-items: center;
  gap: 16px;
  width: 100%;
}

.switch-container .field-tip {
  margin-top: 0;
  flex: 1;
}

.field-tip {
  font-size: 12px;
  color: #666666;
  margin-top: 4px;
  line-height: 1.4;
}

.field-info-tip {
  margin: 16px 0;
}

.dialog-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}

/* 分隔符样式优化 */
.field-editor-dialog :deep(.el-divider) {
  margin: 32px 0 24px;
  background-color: #f0f0f0;
}

.field-editor-dialog :deep(.el-divider__text) {
  background: #ffffff;
  color: #8c8c8c;
  font-size: 14px;
  font-weight: 500;
  padding: 0 16px;
}

/* 表单标签样式优化 */
.field-editor-dialog :deep(.el-form-item__label) {
  color: #262626;
  font-weight: 500;
  font-size: 14px;
  padding-bottom: 8px;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .field-editor-dialog {
    max-width: 95vw;
    margin: 5vh auto;
  }

  .field-editor-dialog :deep(.el-dialog__body) {
    padding: 24px;
  }

  .field-editor-dialog :deep(.el-dialog__footer) {
    padding: 20px 24px;
  }
}

.enum-option-item {
  flex-direction: column;
  align-items: stretch;
  gap: 8px;
}

.enum-options-editor {
  padding: 12px;
}
</style>
