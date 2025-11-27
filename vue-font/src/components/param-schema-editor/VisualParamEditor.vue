<template>
  <div class="advanced-param-editor">
    <div class="editor-header">
      <div class="mode-switch">
        <el-radio-group v-model="activeMode" size="small" @change="handleModeChange">
          <el-radio-button label="visual">
            <el-icon class="mr-1"><Grid /></el-icon> 可视化设计
          </el-radio-button>
          <el-radio-button label="json">
            <el-icon class="mr-1"><Document /></el-icon> JSON 源码
          </el-radio-button>
        </el-radio-group>
      </div>

      <div class="actions">
        <el-button-group size="small">
          <el-button @click="handleImport">
            <el-icon><Upload /></el-icon> 导入
          </el-button>
          <el-button @click="handleCopyJSON">
            <el-icon><CopyDocument /></el-icon> 复制JSON
          </el-button>
          <el-button type="danger" plain @click="clearAll">
            <el-icon><Delete /></el-icon> 清空
          </el-button>
        </el-button-group>
      </div>
    </div>

    <div class="editor-body">
      <div v-show="activeMode === 'visual'" class="visual-mode-container">
        <div class="sidebar">
          <div class="panel-title">组件库</div>
          <draggable
            class="component-list"
            :list="componentLibrary"
            :group="{ name: 'canvas', pull: 'clone', put: false }"
            :sort="false"
            item-key="widget"
            :clone="cloneComponent"
          >
            <template #item="{ element }">
              <div class="component-item">
                <el-icon class="comp-icon"><component :is="element.icon" /></el-icon>
                <span>{{ element.label }}</span>
              </div>
            </template>
          </draggable>
        </div>

        <div class="canvas-area">
          <div class="canvas-bar">
            <span>参数配置预览</span>
            <span class="tip">拖拽调整顺序</span>
          </div>
          <el-scrollbar class="canvas-scroller">
            <el-form label-position="top" class="preview-form">
              <draggable
                v-model="canvasList"
                group="canvas"
                item-key="id"
                class="drag-area"
                ghost-class="ghost"
                :animation="200"
                @change="onCanvasChange"
              >
                <template #item="{ element, index }">
                  <div
                    class="form-item-wrapper"
                    :class="{ active: activeId === element.id }"
                    @click.stop="activeId = element.id"
                  >
                    <el-icon class="delete-btn" @click.stop="removeItem(index)">
                      <CloseBold />
                    </el-icon>

                    <el-form-item :label="element.label" :required="element.required">
                      <component
                        :is="getPreviewComponent(element.widget)"
                        v-bind="getPreviewProps(element)"
                        readonly
                      />
                      <div class="field-meta">
                        <el-tag size="small" type="info" effect="plain">{{ element.field }}</el-tag>
                        <span class="type-text">{{ element.type }}</span>
                      </div>
                    </el-form-item>
                  </div>
                </template>
                <template #header>
                  <div v-if="canvasList.length === 0" class="empty-placeholder">
                    <el-icon :size="48"><Box /></el-icon>
                    <p>从左侧拖入组件，或切换到 JSON 模式粘贴配置</p>
                  </div>
                </template>
              </draggable>
            </el-form>
          </el-scrollbar>
        </div>

        <div class="props-panel">
          <div class="panel-title">属性设置</div>
          <div v-if="activeItem" class="props-content">
            <el-form layout="vertical" size="small" label-position="top">
              <el-form-item label="显示名称 (Label)">
                <el-input v-model="activeItem.label" />
              </el-form-item>

              <el-form-item label="字段参数名 (Key)">
                <el-input v-model="activeItem.field" placeholder="API 参数名" />
              </el-form-item>

              <el-row :gutter="12">
                <el-col :span="12">
                  <el-form-item label="必填">
                    <el-switch v-model="activeItem.required" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="隐藏">
                    <el-switch v-model="activeItem.hidden" />
                  </el-form-item>
                </el-col>
              </el-row>

              <el-form-item label="默认值">
                <el-input v-if="activeItem.widget !== 'switch'" v-model="activeItem.defaultValue" />
                <el-switch v-else v-model="activeItem.defaultValue" />
              </el-form-item>

              <template v-if="activeItem.widget === 'select'">
                <el-divider content-position="left">选项设置</el-divider>
                <div v-for="(opt, idx) in activeItem.options" :key="idx" class="option-row">
                  <el-input v-model="opt.label" placeholder="名" style="width: 45%" />
                  <span class="sep">:</span>
                  <el-input v-model="opt.value" placeholder="值" style="width: 45%" />
                  <el-icon class="del-opt" @click="activeItem.options.splice(idx, 1)"
                    ><Remove
                  /></el-icon>
                </div>
                <el-button
                  type="primary"
                  link
                  size="small"
                  @click="activeItem.options.push({ label: '', value: '' })"
                >
                  + 添加选项
                </el-button>
              </template>
            </el-form>
          </div>
          <div v-else class="no-selection">请选择字段</div>
        </div>
      </div>

      <div v-show="activeMode === 'json'" class="json-mode-container">
        <div class="json-toolbar">
          <el-alert
            v-if="jsonError"
            :title="jsonError"
            type="error"
            show-icon
            :closable="false"
            class="mb-2"
          />
          <el-alert
            v-else
            title="在此处编辑标准 JSON Schema，切换回可视化模式会自动解析"
            type="info"
            show-icon
            :closable="false"
            class="mb-2"
          />
        </div>
        <el-input
          v-model="jsonContent"
          type="textarea"
          class="code-editor"
          :rows="20"
          resize="none"
          spellcheck="false"
          @input="handleJsonInput"
        />
        <div class="json-footer">
          <el-button size="small" @click="formatJSON">格式化代码</el-button>
        </div>
      </div>
    </div>

    <div class="dialog-footer">
      <el-button @click="$emit('cancel')">取消</el-button>
      <el-button type="primary" @click="handleConfirm">确定保存</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, nextTick } from 'vue'
import draggable from 'vuedraggable'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Grid,
  Document,
  Upload,
  CopyDocument,
  Delete,
  EditPen,
  ChatLineSquare,
  ScaleToOriginal,
  SetUp,
  List,
  CloseBold,
  Box,
  Remove,
} from '@element-plus/icons-vue'

const props = defineProps({
  modelValue: { type: String, default: '{}' },
})
const emit = defineEmits(['update:modelValue', 'confirm', 'cancel'])

// --- 状态管理 ---
const activeMode = ref('visual') // visual | json
const jsonContent = ref('') // JSON 模式下的字符串
const jsonError = ref(null) // JSON 校验错误信息
const canvasList = ref([]) // 视图模式下的组件列表
const activeId = ref(null) // 当前选中的组件ID

// 计算当前选中的组件对象
const activeItem = computed(() => canvasList.value.find((item) => item.id === activeId.value))

// --- 1. 组件库定义 ---
const componentLibrary = [
  { label: '单行文本', widget: 'input', icon: EditPen, type: 'string' },
  { label: '多行描述', widget: 'textarea', icon: ChatLineSquare, type: 'string' },
  { label: '数字调节', widget: 'number', icon: ScaleToOriginal, type: 'number' },
  { label: '开关控制', widget: 'switch', icon: SetUp, type: 'boolean' },
  { label: '下拉选项', widget: 'select', icon: List, type: 'string' },
]

// --- 2. 核心：Visual <-> JSON 转换逻辑 ---

// A. 视图 -> JSON Schema
const buildSchemaFromVisual = () => {
  const properties = {}
  const required = []

  canvasList.value.forEach((item) => {
    if (!item.field) return

    const conf = {
      type: item.type,
      description: item.label, // 将 Label 存入 description
      'x-widget': item.widget, // 扩展字段：存组件类型
    }

    if (item.defaultValue !== undefined && item.defaultValue !== '') {
      conf.default = item.defaultValue
    }

    if (item.hidden) conf.hidden = true

    // 处理下拉选项
    if (item.widget === 'select') {
      conf.enum = item.options.map((o) => o.value)
      conf['x-options'] = item.options // 扩展字段：存 Label-Value 映射
    }

    properties[item.field] = conf
    if (item.required && !item.hidden) required.push(item.field)
  })

  const schema = { type: 'object', properties }
  if (required.length) schema.required = required
  return JSON.stringify(schema, null, 2)
}

// B. JSON Schema -> 视图
const parseVisualFromJson = (jsonStr) => {
  try {
    const obj = JSON.parse(jsonStr)
    if (!obj || obj.type !== 'object' || !obj.properties) return []

    const requiredSet = new Set(obj.required || [])

    return Object.entries(obj.properties).map(([key, conf]) => {
      // 智能推断 Widget 类型
      let widget = conf['x-widget'] || 'input'
      if (!conf['x-widget']) {
        if (conf.type === 'boolean') widget = 'switch'
        else if (conf.type === 'number' || conf.type === 'integer') widget = 'number'
        else if (conf.enum) widget = 'select'
        else if (
          conf.description &&
          (conf.description.includes('多行') || conf.description.length > 20)
        )
          widget = 'textarea'
      }

      const item = {
        id: `f_${key}_${Date.now()}`, // 生成临时唯一ID
        field: key,
        label: conf.description || key,
        type: conf.type || 'string',
        widget,
        required: requiredSet.has(key),
        hidden: !!conf.hidden,
        defaultValue: conf.default,
        options: [],
      }

      // 还原下拉选项
      if (widget === 'select') {
        if (conf['x-options']) {
          item.options = conf['x-options']
        } else if (conf.enum) {
          item.options = conf.enum.map((v) => ({ label: v, value: v }))
        }
      }

      return item
    })
  } catch (e) {
    console.error('Parse Error', e)
    throw new Error('JSON 格式错误，无法解析为可视化视图')
  }
}

// --- 3. 事件处理 ---

// 初始化
watch(
  () => props.modelValue,
  (val) => {
    jsonContent.value = val || '{}'
    try {
      canvasList.value = parseVisualFromJson(jsonContent.value)
    } catch (e) {
      // 如果初始 JSON 有错，默认空视图
      canvasList.value = []
    }
  },
  { immediate: true },
)

// 切换模式处理
const handleModeChange = (val) => {
  if (val === 'json') {
    // 视图 -> JSON
    jsonContent.value = buildSchemaFromVisual()
    jsonError.value = null
  } else {
    // JSON -> 视图
    try {
      canvasList.value = parseVisualFromJson(jsonContent.value)
      jsonError.value = null
    } catch (e) {
      ElMessage.warning('JSON 格式有误，请修复后再切换')
      activeMode.value = 'json' // 强制切回
      jsonError.value = e.message
    }
  }
}

// JSON 编辑实时校验
const handleJsonInput = () => {
  try {
    JSON.parse(jsonContent.value)
    jsonError.value = null
  } catch (e) {
    jsonError.value = e.message
  }
}

// 拖拽克隆
const cloneComponent = (origin) => {
  const id = Date.now()
  return {
    ...origin,
    id: `field_${id}`,
    field: `param_${id}`, // 自动生成 Key
    label: origin.label,
    defaultValue: origin.widget === 'switch' ? false : '',
    required: false,
    hidden: false,
    options: origin.widget === 'select' ? [{ label: '选项1', value: 'opt1' }] : [],
  }
}

// 预览组件帮助函数
const getPreviewComponent = (widget) => {
  const map = {
    input: 'el-input',
    textarea: 'el-input',
    number: 'el-input-number',
    switch: 'el-switch',
    select: 'el-select',
  }
  return map[widget] || 'el-input'
}

const getPreviewProps = (element) => {
  const p = { placeholder: '默认值预览' }
  if (element.widget === 'textarea') p.type = 'textarea'
  return p
}

// 工具栏操作
const clearAll = () => {
  ElMessageBox.confirm('确定清空所有配置吗？', '警告', { type: 'warning' }).then(() => {
    canvasList.value = []
    jsonContent.value = '{}'
    activeId.value = null
  })
}

const formatJSON = () => {
  try {
    const obj = JSON.parse(jsonContent.value)
    jsonContent.value = JSON.stringify(obj, null, 2)
  } catch (e) {}
}

const handleCopyJSON = () => {
  // 确保拿到最新
  if (activeMode.value === 'visual') {
    jsonContent.value = buildSchemaFromVisual()
  }
  navigator.clipboard.writeText(jsonContent.value)
  ElMessage.success('JSON 已复制')
}

// 导入功能 (简化版，直接弹窗输入)
const handleImport = () => {
  ElMessageBox.prompt('请粘贴 JSON Schema', '导入', { inputType: 'textarea' }).then(({ value }) => {
    try {
      const list = parseVisualFromJson(value)
      canvasList.value = list
      jsonContent.value = value
      activeMode.value = 'visual'
      ElMessage.success('导入成功')
    } catch (e) {
      ElMessage.error('导入失败：无效的 JSON Schema')
    }
  })
}

// 提交保存
const handleConfirm = () => {
  // 无论在哪种模式，最终都以 JSON 为准
  let finalJson = jsonContent.value
  if (activeMode.value === 'visual') {
    finalJson = buildSchemaFromVisual()
  }

  // 最终校验
  try {
    JSON.parse(finalJson)
    emit('update:modelValue', finalJson)
    emit('confirm', finalJson)
  } catch (e) {
    ElMessage.error('无法保存：JSON 格式错误')
  }
}

const onCanvasChange = () => {
  // 每次拖拽变动，如果需要可以自动保存草稿等
}
const removeItem = (idx) => {
  if (canvasList.value[idx].id === activeId.value) activeId.value = null
  canvasList.value.splice(idx, 1)
}
</script>

<style scoped lang="scss">
.advanced-param-editor {
  display: flex;
  flex-direction: column;
  height: 650px;
  background: #fff;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  overflow: hidden;
}

.editor-header {
  height: 48px;
  background: #f5f7fa;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  flex-shrink: 0;

  .mr-1 {
    margin-right: 4px;
  }
}

.editor-body {
  flex: 1;
  overflow: hidden;
  position: relative;
}

/* 视图模式布局 */
.visual-mode-container {
  display: flex;
  height: 100%;

  .sidebar {
    width: 200px;
    border-right: 1px solid #e4e7ed;
    background: #fdfdfd;
    display: flex;
    flex-direction: column;

    .component-list {
      padding: 10px;
      overflow-y: auto;
      flex: 1;
      .component-item {
        display: flex;
        align-items: center;
        padding: 8px 12px;
        margin-bottom: 8px;
        background: #fff;
        border: 1px solid #e4e7ed;
        border-radius: 4px;
        cursor: grab;
        font-size: 13px;
        transition: all 0.2s;
        box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
        &:hover {
          border-color: #409eff;
          color: #409eff;
        }
        .comp-icon {
          margin-right: 8px;
          font-size: 16px;
        }
      }
    }
  }

  .canvas-area {
    flex: 1;
    background: #fafafa;
    display: flex;
    flex-direction: column;
    border-right: 1px solid #e4e7ed;

    .canvas-bar {
      height: 36px;
      background: #fff;
      border-bottom: 1px solid #f0f0f0;
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 0 16px;
      font-size: 12px;
      color: #909399;
    }

    .canvas-scroller {
      padding: 16px;
      :deep(.el-scrollbar__view) {
        height: 100%;
      }
    }

    .drag-area {
      min-height: 100%;
      padding-bottom: 40px;
    }

    .form-item-wrapper {
      position: relative;
      padding: 12px;
      margin-bottom: 8px;
      background: #fff;
      border: 1px dashed transparent;
      border-radius: 4px;
      cursor: pointer;

      &:hover {
        background: #f0f9eb;
      }
      &.active {
        border: 1px solid #409eff;
        background: #ecf5ff;
        box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
      }

      .delete-btn {
        position: absolute;
        right: 8px;
        top: 8px;
        color: #f56c6c;
        display: none;
        z-index: 10;
        &:hover {
          transform: scale(1.1);
        }
      }
      &:hover .delete-btn {
        display: block;
      }

      .field-meta {
        margin-top: 4px;
        display: flex;
        align-items: center;
        gap: 8px;
        .type-text {
          font-size: 12px;
          color: #c0c4cc;
        }
      }
    }

    .empty-placeholder {
      height: 300px;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      color: #909399;
      font-size: 14px;
      opacity: 0.6;
    }
  }

  .props-panel {
    width: 280px;
    background: #fff;
    display: flex;
    flex-direction: column;

    .props-content {
      padding: 16px;
      overflow-y: auto;
      flex: 1;
    }
    .no-selection {
      padding: 40px;
      text-align: center;
      color: #909399;
      font-size: 13px;
    }
    .option-row {
      display: flex;
      align-items: center;
      margin-bottom: 6px;
      .sep {
        margin: 0 4px;
        color: #dcdfe6;
      }
      .del-opt {
        margin-left: 4px;
        color: #f56c6c;
        cursor: pointer;
      }
    }
  }

  .panel-title {
    padding: 12px 16px;
    font-weight: 600;
    color: #303133;
    border-bottom: 1px solid #f0f0f0;
    background: #fff;
  }
}

/* JSON 模式布局 */
.json-mode-container {
  padding: 20px;
  height: 100%;
  display: flex;
  flex-direction: column;

  .code-editor {
    flex: 1;
    font-family: 'Menlo', 'Monaco', 'Courier New', monospace;
    :deep(.el-textarea__inner) {
      height: 100%;
      background: #282c34;
      color: #abb2bf;
      font-size: 13px;
      line-height: 1.5;
    }
  }
  .json-footer {
    margin-top: 10px;
    text-align: right;
  }
}

.dialog-footer {
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding: 0 16px;
  border-top: 1px solid #e4e7ed;
  background: #fff;
}
</style>
