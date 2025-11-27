<template>
  <div class="permission-edit-form">
    <el-form
      ref="formRef"
      :model="localFormData"
      :rules="formRules"
      label-width="100px"
      label-position="right"
    >
      <el-form-item label="权限名称" prop="name">
        <el-input v-model="localFormData.name" placeholder="请输入权限名称" />
      </el-form-item>
      <el-form-item label="权限编码" prop="code">
        <el-input v-model="localFormData.code" placeholder="请输入权限编码，如system:user:list" />
        <div class="form-tip">权限编码建议使用冒号分隔的格式，如system:user:list</div>
      </el-form-item>
      <el-form-item label="权限类型" prop="type">
        <el-select v-model="localFormData.type" placeholder="请选择权限类型">
          <el-option label="菜单" value="1" />
          <el-option label="操作按钮" value="2" />
          <el-option label="API" value="3" />
        </el-select>
      </el-form-item>
      <el-form-item label="描述" prop="description">
        <el-input
          v-model="localFormData.description"
          type="textarea"
          :rows="4"
          placeholder="请输入权限描述"
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
    { required: true, message: '请输入权限名称', trigger: 'blur' },
    { min: 2, max: 50, message: '权限名称长度在 2 到 50 个字符', trigger: 'blur' },
  ],
  code: [
    { required: true, message: '请输入权限编码', trigger: 'blur' },
    { min: 3, max: 100, message: '权限编码长度在 3 到 100 个字符', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9:_]+$/, message: '权限编码只能包含字母、数字、冒号和下划线', trigger: 'blur' },
  ],
  type: [
    { required: true, message: '请选择权限类型', trigger: 'change' },
    { type: 'number', message: '权限类型值必须为数字', trigger: 'change' },
  ],
  description: [
    { max: 200, message: '权限描述长度不能超过 200 个字符', trigger: 'blur' },
  ],
}

// 提交表单
const handleSubmit = async () => {
  try {
    const valid = await formRef.value.validate()
    if (valid) {
      submitting.value = true
      // 确保type是数字类型
      const submitData = {
        ...localFormData.value,
        type: parseInt(localFormData.value.type)
      }
      emit('submit', submitData)
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
.permission-edit-form {
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

.form-tip {
  font-size: 12px;
  color: #999;
  margin-top: 6px;
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