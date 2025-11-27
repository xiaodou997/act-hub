<template>
  <div class="simple-param-editor">
    <div class="toolbar">
      <el-button type="primary" @click="addRow">新增字段</el-button>
      <el-button @click="resetRows">清空</el-button>
    </div>
    <el-table :data="rows" border stripe style="width: 100%" class="param-table">
      <el-table-column type="index" width="50" label="#" align="center" />
      <el-table-column label="字段参数名" min-width="140">
        <template #default="{ row }">
          <el-input v-model="row.name" placeholder="例如: model" />
        </template>
      </el-table-column>
      <el-table-column label="类型" width="110">
        <template #default="{ row }">
          <el-select v-model="row.type" placeholder="类型">
            <el-option label="String" value="string" />
            <el-option label="Number" value="number" />
            <el-option label="Integer" value="integer" />
            <el-option label="Boolean" value="boolean" />
            <el-option label="Array" value="array" />
            <el-option label="Object" value="object" />
          </el-select>
        </template>
      </el-table-column>
      <el-table-column label="必填" width="70" align="center">
        <template #default="{ row }">
          <el-switch v-model="row.required" :disabled="row.hidden" />
        </template>
      </el-table-column>
      <el-table-column label="隐藏" width="70" align="center">
        <template #default="{ row }">
          <el-switch v-model="row.hidden" @change="(val) => val && (row.required = false)" />
        </template>
      </el-table-column>
      <el-table-column label="显示名称" min-width="160">
        <template #default="{ row }">
          <el-input v-model="row.description" placeholder="在表单显示的标签名称" />
        </template>
      </el-table-column>
      <el-table-column label="默认值" min-width="180">
        <template #default="{ row }">
          <el-input v-model="row.default" placeholder="默认值" />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="70" align="center" fixed="right">
        <template #default="{ $index }">
          <el-button type="danger" link :icon="Delete" @click="removeRow($index)" />
        </template>
      </el-table-column>
    </el-table>
    <div class="footer">
      <el-button @click="handleCancel">取消</el-button>
      <el-button type="primary" @click="handleConfirm">确定</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { Delete } from '@element-plus/icons-vue'

const props = defineProps({
  modelValue: { type: String, default: '' },
})
const emit = defineEmits(['update:modelValue', 'confirm', 'cancel'])

const rows = ref([])

const parseSchema = (json) => {
  try {
    const obj = json ? JSON.parse(json) : null
    if (!obj || obj.type !== 'object' || !obj.properties) return []
    const required = Array.isArray(obj.required) ? obj.required : []
    return Object.entries(obj.properties).map(([name, conf]) => ({
      name,
      type: conf.type || 'string',
      required: required.includes(name),
      description: conf.description || '',
      default: conf.default ?? '',
      hidden: !!conf.hidden,
    }))
  } catch {
    return []
  }
}

const buildSchema = () => {
  const properties = {}
  const required = []
  for (const r of rows.value) {
    if (!r.name) continue
    const conf = { type: r.type || 'string' }
    if (r.description) conf.description = r.description
    if (r.default !== '' && r.default !== undefined) conf.default = r.default
    if (r.hidden) conf.hidden = true
    properties[r.name] = conf
    if (r.required && !r.hidden) required.push(r.name)
  }
  const schema = { type: 'object', properties }
  if (required.length) schema.required = required
  return JSON.stringify(schema)
}

watch(
  () => props.modelValue,
  (val) => {
    rows.value = parseSchema(val)
  },
  { immediate: true },
)

const addRow = () => {
  rows.value.push({
    name: '',
    type: 'string',
    required: false,
    description: '',
    default: '',
    hidden: false,
  })
}
const removeRow = (i) => {
  rows.value.splice(i, 1)
}
const resetRows = () => {
  rows.value = []
}

const handleConfirm = () => {
  const json = buildSchema()
  emit('update:modelValue', json)
  emit('confirm', json)
}
const handleCancel = () => {
  emit('cancel')
}
</script>

<style scoped>
.simple-param-editor {
  padding: 4px 0;
}
.toolbar {
  margin-bottom: 12px;
  display: flex;
  gap: 12px;
}
.param-table {
  border-radius: 4px;
  overflow: hidden;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
}
.footer {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
:deep(.el-table th) {
  background-color: #f5f7fa !important;
  color: #606266;
  font-weight: 600;
}
:deep(.el-input__wrapper) {
  box-shadow: none !important;
  border: 1px solid transparent;
  padding: 0 8px;
}
:deep(.el-input__wrapper:hover),
:deep(.el-input__wrapper.is-focus) {
  border-color: #dcdfe6;
}
:deep(.el-table .cell) {
  padding: 0 8px;
}
</style>
