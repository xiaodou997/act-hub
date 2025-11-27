<template>
  <div class="platform-edit-form">
    <el-form
      ref="formRef"
      :model="localFormData"
      :rules="formRules"
      label-width="100px"
      label-position="right"
    >
      <el-form-item label="平台名称" prop="name">
        <el-input v-model="localFormData.name" placeholder="请输入平台名称" />
      </el-form-item>
      <el-form-item label="平台编码" prop="code">
        <el-input v-model="localFormData.code" placeholder="请输入平台编码" />
      </el-form-item>
      <el-form-item label="平台描述" prop="description">
        <el-input
          v-model="localFormData.description"
          type="textarea"
          :rows="4"
          placeholder="请输入平台描述"
        />
      </el-form-item>
      <el-form-item label="状态" prop="status" v-if="false">
        <!-- 根据需求，状态字段暂时不显示和使用，默认设置为1 -->
        <el-switch v-model="localFormData.status" />
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
    default: () => ({ status: 1 }),
  },
  isEdit: {
    type: Boolean,
    default: false,
  },
})

const emit = defineEmits(['submit', 'cancel'])

const formRef = ref()
const submitting = ref(false)
const localFormData = ref({})

// 初始化本地数据
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
    { required: true, message: '请输入平台名称', trigger: 'blur' },
    { min: 2, max: 50, message: '平台名称长度在 2 到 50 个字符', trigger: 'blur' },
  ],
  code: [
    { required: true, message: '请输入平台编码', trigger: 'blur' },
    { min: 2, max: 50, message: '平台编码长度在 2 到 50 个字符', trigger: 'blur' },
    {
      pattern: /^[a-zA-Z0-9_-]+$/,
      message: '平台编码只能包含英文大小写字母、数字、连接符和下划线',
      trigger: 'blur',
    },
  ],
  description: [{ max: 200, message: '平台描述长度不能超过 200 个字符', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }],
}

// 提交表单
const handleSubmit = async () => {
  try {
    const valid = await formRef.value.validate()
    if (valid) {
      submitting.value = true
      // 确保状态字段有值，默认设置为1（启用状态）
      if (localFormData.value.status === undefined || localFormData.value.status === null) {
        localFormData.value.status = 1
      }
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
.platform-edit-form {
  padding: 20px;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.el-form {
  flex: 1;

  :deep(.el-form-item) {
    margin-bottom: 22px;

    .el-form-item__label {
      font-weight: 500;
    }
  }
}

.form-footer {
  margin-top: auto;
  display: flex;
  justify-content: flex-end;
  gap: 20px;
  padding-top: 16px;
  border-top: 1px solid #eee;

  .el-button {
    min-width: 120px;
    padding: 0 24px;
  }
}
</style>
