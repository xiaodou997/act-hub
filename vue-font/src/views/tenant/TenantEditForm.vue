<template>
  <div class="tenant-edit-form">
    <el-form
      ref="formRef"
      :model="localFormData"
      :rules="formRules"
      label-width="100px"
      label-position="right"
    >
      <el-form-item label="租户名称" prop="name">
        <el-input v-model="localFormData.name" placeholder="请输入租户名称" />
      </el-form-item>
      <el-form-item label="联系人" prop="contactName">
        <el-input v-model="localFormData.contactName" placeholder="请输入联系人" />
      </el-form-item>
      <el-form-item label="联系邮箱" prop="contactEmail">
        <el-input v-model="localFormData.contactEmail" placeholder="请输入联系邮箱" />
      </el-form-item>
      <el-form-item label="联系电话" prop="contactPhone">
        <el-input v-model="localFormData.contactPhone" placeholder="请输入联系电话" />
      </el-form-item>
      <el-form-item label="租户描述" prop="remark">
        <el-input
          v-model="localFormData.remark"
          type="textarea"
          :rows="4"
          placeholder="请输入租户描述"
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
    { required: true, message: '请输入租户名称', trigger: 'blur' },
    { min: 2, max: 50, message: '租户名称长度在 2 到 50 个字符', trigger: 'blur' },
  ],
  contactName: [
    { required: true, message: '请输入联系人', trigger: 'blur' },
    { min: 2, max: 50, message: '联系人长度在 2 到 50 个字符', trigger: 'blur' },
  ],
  contactEmail: [
    { required: true, message: '请输入联系邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' },
  ],
  contactPhone: [{ pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }],
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
.tenant-edit-form {
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
  gap: 20px; // 增大间距从12px到20px
  padding-top: 16px;
  border-top: 1px solid #eee;

  // 增加按钮宽度
  .el-button {
    min-width: 120px; // 设置最小宽度
    padding: 0 24px; // 增加内边距
  }
}
</style>
