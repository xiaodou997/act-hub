<template>
  <div class="ai-app-type-edit-form">
    <el-form
      ref="formRef"
      :model="localFormData"
      :rules="formRules"
      label-width="100px"
      label-position="right"
    >
      <el-form-item label="类型名称" prop="name">
        <el-input v-model="localFormData.name" placeholder="请输入类型名称" />
      </el-form-item>
      <el-form-item label="类型描述" prop="description">
        <el-input
          v-model="localFormData.description"
          type="textarea"
          :rows="4"
          placeholder="请输入类型描述"
        />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-switch
          v-model="localFormData.status"
          :active-value="1"
          :inactive-value="0"
          active-text="启用"
          inactive-text="禁用"
        />
      </el-form-item>
    </el-form>

    <div class="form-footer">
      <el-button @click="handleCancel">取消</el-button>
      <el-button type="primary" @click="handleSubmit" :loading="submitting">
        {{ isEdit ? '更新' : '创建' }}
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'

const props = defineProps({
  formData: {
    type: Object,
    required: true,
    default: () => ({}),
  },
  isEdit: {
    type: Boolean,
    default: false,
  },
})

// 定义事件
const emit = defineEmits(['submit', 'cancel'])

const formRef = ref(null)
const submitting = ref(false)
const localFormData = ref({})

// 监听表单数据变化
watch(
  () => props.formData,
  (newValue) => {
    localFormData.value = { ...newValue }
  },
  { immediate: true, deep: true },
)

// 表单验证规则
const formRules = {
  name: [
    { required: true, message: '请输入类型名称', trigger: 'blur' },
    { min: 2, max: 50, message: '类型名称长度在 2 到 50 个字符', trigger: 'blur' },
  ],
  description: [
    { max: 500, message: '类型描述长度不能超过 500 个字符', trigger: 'blur' },
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' },
    { type: 'number', message: '状态值必须为数字', trigger: 'change' },
  ],
}

// 提交表单
const handleSubmit = async () => {
  try {
    const valid = await formRef.value.validate()
    if (valid) {
      submitting.value = true
      emit('submit', { ...localFormData.value })
    }
  } catch (error) {
    console.log('error', error)
    ElMessage.error('请正确填写表单')
  } finally {
    submitting.value = false
  }
}

// 取消
const handleCancel = () => {
  emit('cancel')
}
</script>

<style scoped lang="scss">
.ai-app-type-edit-form {
  padding: 20px;
}

.el-form {
  :deep(.el-form-item) {
    margin-bottom: 22px;

    .el-form-item__label {
      font-weight: 500;
    }
  }
}

.form-footer {
  display: flex;
  justify-content: flex-end;
  gap: 20px;
  padding-top: 16px;
  border-top: 1px solid #eee;
  margin-top: 20px;

  .el-button {
    min-width: 120px;
    padding: 0 24px;
  }
}
</style>