<template>
  <div class="field-tree-node" :style="{ marginLeft: level * 20 + 'px' }">
    <div class="node-content" :class="{ 'node-hidden': field.hidden }">
      <!-- 节点头部 -->
      <div class="node-header">
        <div class="node-drag-handle">
          <span class="node-index" v-if="showIndex">{{ index + 1 }}</span>
        </div>

        <!-- 字段信息 -->
        <div class="node-info">
          <!-- 第一行：标题 + 标识 + 标签 -->
          <div class="field-title">
            <span class="title-text">{{ field.title || field.key }}</span>
            <span v-if="field.key !== field.title" class="field-key-inline">{{ field.key }}</span>
            <el-tag size="small" :type="getTypeColor(field.type)">{{
              getTypeText(field.type)
            }}</el-tag>
            <el-tag v-if="field.required" size="small" type="danger" effect="plain">必填</el-tag>
            <el-tag v-if="field.hidden" size="small" type="info" effect="plain">隐藏</el-tag>
          </div>

          <!-- 第二行：默认值 + 描述 -->
          <div
            v-if="(field.default !== undefined && field.default !== '') || field.description"
            class="field-meta"
          >
            <span v-if="field.default !== undefined && field.default !== ''" class="field-default">
              默认值: <code>{{ field.default }}</code>
            </span>
            <span v-if="field.description" class="field-description">{{ field.description }}</span>
          </div>

          <!-- 第三行：枚举选项或校验规则 -->
          <div
            v-if="field.type === 'enum' && field.enumOptions && field.enumOptions.length > 0"
            class="field-enum"
          >
            <span class="enum-label">选项:</span>
            <div class="enum-preview">
              <el-tag
                v-for="(option, optIndex) in field.enumOptions.slice(0, 3)"
                :key="optIndex"
                size="small"
              >
                {{ option }}
              </el-tag>
              <el-tag v-if="field.enumOptions.length > 3" size="small" type="info">
                +{{ field.enumOptions.length - 3 }}
              </el-tag>
            </div>
          </div>
        </div>

        <!-- 操作按钮 -->
        <div class="node-actions">
          <!-- 移动按钮 -->
          <el-button-group v-if="!field.hidden">
            <el-button
              size="small"
              text
              :disabled="index === 0"
              @click="$emit('move-up', field, parentFields, index)"
              title="向上移动"
            >
              <el-icon><ArrowUp /></el-icon>
            </el-button>
            <el-button
              size="small"
              text
              :disabled="index >= (parentFields ? parentFields.length - 1 : 0)"
              @click="$emit('move-down', field, parentFields, index)"
              title="向下移动"
            >
              <el-icon><ArrowDown /></el-icon>
            </el-button>
          </el-button-group>

          <!-- 编辑按钮 -->
          <el-button size="small" text @click="$emit('update-field', field)">
            <el-icon><Edit /></el-icon>
          </el-button>

          <!-- 移除数组类型的添加子字段按钮 -->

          <!-- 删除按钮 -->
          <el-button
            size="small"
            text
            type="danger"
            @click="$emit('remove-field', field, parentFields, index)"
          >
            <el-icon><Delete /></el-icon>
          </el-button>

          <!-- 展开/折叠按钮（有子节点时显示） -->
          <el-button v-if="hasChildren" size="small" text @click="expanded = !expanded">
            <el-icon v-if="expanded"><Fold /></el-icon>
            <el-icon v-else><Expand /></el-icon>
          </el-button>
        </div>
      </div>
    </div>

    <!-- 移除数组类型的子节点渲染 -->
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ArrowUp, ArrowDown, Edit, Plus, Delete, Fold, Expand } from '@element-plus/icons-vue'

// 定义组件属性
const props = defineProps({
  field: {
    type: Object,
    required: true,
  },
  level: {
    type: Number,
    default: 0,
  },
  index: {
    type: Number,
    default: 0,
  },
  parentFields: {
    type: Array,
    default: null,
  },
})

// 定义组件事件
const emit = defineEmits(['update-field', 'add-child', 'remove-field', 'move-up', 'move-down'])

// 本地状态
const expanded = ref(true)

// 计算属性 - 使用安全的属性访问
const hasChildren = computed(() => {
  return (
    props.field.children && Array.isArray(props.field.children) && props.field.children.length > 0
  )
})

const showIndex = computed(() => {
  return props.level === 0
})

// 方法 - 使用安全的属性访问
const getTypeColor = (type) => {
  const colorMap = {
    string: 'success',
    integer: 'warning',
    number: 'warning',
    boolean: 'info',
    array: 'primary',
    object: 'danger',
    enum: 'success',
  }
  return colorMap[type] || 'info'
}

const getTypeText = (type) => {
  const textMap = {
    string: '字符串',
    integer: '整数',
    number: '数字',
    boolean: '布尔值',
    array: '数组',
    object: '对象',
    enum: '枚举',
  }
  return textMap[type] || type
}

const getTypeDetail = (field) => {
  if (field.type === 'array') {
    // 简化数组类型显示，不再显示对象属性数量
    if (field.itemsType === 'object') {
      return `数组[对象]`
    }
    return `数组[${getTypeText(field.itemsType)}]`
  } else if (field.type === 'object') {
    return `对象`
  } else if (field.type === 'enum') {
    const optionsCount =
      field.enumOptions && Array.isArray(field.enumOptions) ? field.enumOptions.length : 0
    return `枚举 (${optionsCount}个选项)`
  }
  return getTypeText(field.type)
}
</script>

<style scoped>
.field-tree-node {
  margin-bottom: 8px;
  animation: fadeInUp 0.3s ease-out;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(5px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.node-content {
  border: 1px solid #e8e8e8;
  border-radius: 6px;
  background: #ffffff;
  transition: all 0.2s ease;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  overflow: hidden;
}

.node-content:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  border-color: #d9d9d9;
}

.node-content.node-hidden {
  background: #fafafa;
  border-color: #d9d9d9;
  border-style: dashed;
}

.node-header {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  gap: 12px;
  background: #fafafa;
  border-bottom: 1px solid #f0f0f0;
  position: relative;
}

.node-content.node-hidden .node-header {
  background: #f5f5f5;
}

.node-drag-handle {
  display: flex;
  align-items: center;
  gap: 4px;
}

.drag-handle {
  cursor: move;
  color: #999999;
  font-size: 14px;
  transition: color 0.2s ease;
}

.drag-handle:hover {
  color: #666666;
}

.node-index {
  font-size: 10px;
  color: #ffffff;
  background: #409eff;
  border-radius: 10px;
  padding: 2px 6px;
  font-weight: 500;
  min-width: 18px;
  text-align: center;
}

.node-info {
  flex: 1;
  min-width: 0;
}

.field-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
  flex-wrap: wrap;
}

.title-text {
  font-weight: 600;
  color: #333333;
  font-size: 14px;
}

.field-title .el-tag {
  border-radius: 4px;
  font-size: 11px;
  font-weight: 500;
  padding: 1px 6px;
  border: 1px solid;
  height: 20px;
  line-height: 18px;
}

.field-key-inline {
  font-size: 11px;
  color: #666666;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  background: #f5f5f5;
  padding: 2px 6px;
  border-radius: 3px;
  border: 1px solid #e8e8e8;
  font-weight: 500;
  margin-left: 4px;
}

.field-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 4px;
  font-size: 12px;
  flex-wrap: wrap;
}

.field-default {
  color: #666666;
}

.field-default code {
  background: #f5f5f5;
  padding: 1px 4px;
  border-radius: 2px;
  font-size: 11px;
  color: #333333;
}

.field-description {
  color: #999999;
  line-height: 1.4;
  flex: 1;
}

.field-enum {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 4px;
  font-size: 12px;
}

.enum-label {
  color: #666666;
  font-weight: 500;
  flex-shrink: 0;
}

.node-actions {
  display: flex;
  align-items: center;
  gap: 4px;
  flex-shrink: 0;
}

.node-actions .el-button {
  border-radius: 4px;
  padding: 4px 8px;
  transition: all 0.2s ease;
  font-size: 12px;
  height: 28px;
}

.node-actions .el-button:hover {
  transform: translateY(-1px);
}

.node-actions .el-button-group .el-button {
  border-radius: 4px;
}

.enum-preview {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.enum-preview .el-tag {
  border-radius: 4px;
  font-size: 11px;
  font-weight: 400;
  border: 1px solid #e8e8e8;
  background: #f8f9fa;
  color: #666666;
  height: 20px;
  line-height: 18px;
  padding: 0 6px;
}

.node-children {
  margin-top: 8px;
  border-left: 2px solid #f0f0f0;
  padding-left: 12px;
  position: relative;
}

.node-children::before {
  content: '';
  position: absolute;
  left: -2px;
  top: 0;
  bottom: 0;
  width: 2px;
  background: linear-gradient(to bottom, #f0f0f0 0%, transparent 100%);
}

/* 响应式调整 */
@media (max-width: 768px) {
  .node-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
    padding: 10px 12px;
  }

  .node-actions {
    align-self: flex-end;
    width: 100%;
    justify-content: flex-end;
  }

  .field-title {
    flex-wrap: wrap;
  }

  .node-drag-handle {
    order: -1;
    flex-direction: row;
    align-self: flex-start;
  }

  .detail-row {
    flex-direction: column;
    gap: 2px;
  }

  .detail-label {
    min-width: auto;
  }
}
</style>
