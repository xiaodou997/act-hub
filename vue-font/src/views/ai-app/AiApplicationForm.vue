<template>
  <div class="ai-app-form-container">
    <el-form
      ref="formRef"
      :model="localForm"
      :rules="formRules"
      label-width="100px"
      label-position="top"
    >
      <el-row :gutter="24">
        <el-col :span="12">
          <el-form-item label="应用名称" prop="name">
            <el-input v-model="localForm.name" placeholder="请输入应用名称" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="应用类型" prop="typeId">
            <AiAppTypeSelect v-model="localForm.typeId" />
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="应用描述" prop="description">
            <el-input
              v-model="localForm.description"
              type="textarea"
              :rows="3"
              placeholder="请输入应用描述"
            />
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="参数定义" prop="paramSchema">
            <div style="width: 100%">
              <div style="margin-bottom: 8px">
                <el-button type="primary" @click="paramEditorVisible = true">
                  <el-icon class="el-icon--left"><Edit /></el-icon>编辑参数配置
                </el-button>
              </div>
              <el-table
                v-if="paramList.length > 0"
                :data="paramList"
                border
                stripe
                size="small"
                style="width: 100%"
              >
                <el-table-column prop="name" label="参数名" min-width="140" show-overflow-tooltip />
                <el-table-column prop="type" label="类型" width="90" />
                <el-table-column label="必填" width="60" align="center">
                  <template #default="{ row }">
                    <el-tag :type="row.required ? 'danger' : 'info'" size="small">{{
                      row.required ? '是' : '否'
                    }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="隐藏" width="60" align="center">
                  <template #default="{ row }">
                    <el-tag :type="row.hidden ? 'warning' : 'info'" size="small">{{
                      row.hidden ? '是' : '否'
                    }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column
                  prop="description"
                  label="显示名称"
                  min-width="150"
                  show-overflow-tooltip
                />
                <el-table-column
                  prop="default"
                  label="默认值"
                  min-width="180"
                  show-overflow-tooltip
                />
              </el-table>
              <div v-else class="empty-params">
                <span class="text-gray-400">暂无参数定义</span>
              </div>
            </div>
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="启用状态" prop="enabled">
            <el-switch
              v-model="enabledSwitch"
              inline-prompt
              active-text="启用"
              inactive-text="禁用"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <div class="form-footer">
        <el-button @click="$emit('cancel')">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">{{
          isEdit ? '保存更新' : '立即创建'
        }}</el-button>
      </div>
    </el-form>
    <el-dialog
      v-model="paramEditorVisible"
      title="编辑参数"
      width="720px"
      align-center
      destroy-on-close
    >
      <SimpleParamEditor
        v-model="localForm.paramSchema"
        @confirm="() => (paramEditorVisible = false)"
        @cancel="() => (paramEditorVisible = false)"
      />
    </el-dialog>

    <!-- <el-dialog
      v-model="paramEditorVisible"
      title="应用参数设计"
      width="1100px"
      align-center
      destroy-on-close
      :close-on-click-modal="false"
    >
      <VisualParamEditor
        v-model="localForm.paramSchema"
        @confirm="() => (paramEditorVisible = false)"
        @cancel="() => (paramEditorVisible = false)"
      />
    </el-dialog> -->
  </div>
</template>

<script setup>
import { ref, watch, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Edit } from '@element-plus/icons-vue'
import AiAppTypeSelect from '@/components/AiAppTypeSelect.vue'
import SimpleParamEditor from '@/components/param-schema-editor/SimpleParamEditor.vue'
import VisualParamEditor from '@/components/param-schema-editor/VisualParamEditor.vue'

const props = defineProps({
  formData: { type: Object, required: true, default: () => ({}) },
  isEdit: { type: Boolean, default: false },
})
const emit = defineEmits(['submit', 'cancel'])

const formRef = ref()
const submitting = ref(false)
const paramEditorVisible = ref(false)
const localForm = ref({
  id: '',
  name: '',
  description: '',
  typeId: '',
  paramSchema: '',
  enabled: 1,
})

watch(
  () => props.formData,
  (val) => {
    localForm.value = { ...localForm.value, ...(val || {}) }
  },
  { immediate: true, deep: true },
)

const enabledSwitch = computed({
  get: () => localForm.value.enabled === 1,
  set: (v) => (localForm.value.enabled = v ? 1 : 0),
})

const paramList = computed(() => {
  try {
    const json = localForm.value.paramSchema
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
})

const formRules = {
  name: [{ required: true, message: '请输入应用名称', trigger: 'blur' }],
  typeId: [{ required: true, message: '请选择应用类型', trigger: 'change' }],
  paramSchema: [
    {
      validator: (_, val, cb) => {
        if (!val) return cb()
        try {
          JSON.parse(val)
          cb()
        } catch {
          cb(new Error('JSON Schema 格式不正确'))
        }
      },
      trigger: 'blur',
    },
  ],
}

const handleSubmit = async () => {
  try {
    const valid = await formRef.value.validate()
    if (!valid) return
    submitting.value = true
    const submitData = { ...localForm.value }
    delete submitData.createdAt
    delete submitData.updatedAt
    delete submitData.resultSchema
    delete submitData.handlerBean
    delete submitData.tenantId
    delete submitData.price
    delete submitData.timeoutMs
    delete submitData.config
    emit('submit', submitData)
  } catch (e) {
    ElMessage.error('请完善表单信息')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped lang="scss">
.ai-app-form-container {
  padding: 0 10px;
}

.empty-params {
  padding: 16px;
  text-align: center;
  border: 1px dashed #dcdfe6;
  border-radius: 4px;
  background-color: #fafafa;
}
.text-gray-400 {
  color: #909399;
  font-size: 13px;
}

.form-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 24px;
  padding-top: 16px;
  border-top: 1px solid #f0f2f5;
}
</style>
