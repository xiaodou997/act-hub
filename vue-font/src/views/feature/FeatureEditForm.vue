<template>
  <div class="feature-edit-form">
    <el-form
      ref="formRef"
      :model="localFormData"
      :rules="formRules"
      label-width="100px"
      label-position="right"
    >
      <el-form-item label="功能名称" prop="name">
        <el-input v-model="localFormData.name" placeholder="请输入功能名称" />
      </el-form-item>
      <el-form-item label="功能编码" prop="code">
        <el-input v-model="localFormData.code" placeholder="请输入功能编码" />
      </el-form-item>
      <el-form-item label="功能描述" prop="description">
        <el-input
          v-model="localFormData.description"
          type="textarea"
          :rows="4"
          placeholder="请输入功能描述"
        />
      </el-form-item>
      <el-form-item label="关联平台" prop="platformId">
        <PlatformSelect v-model="localFormData.platformId" @change="handlePlatformChange" />
      </el-form-item>
      <el-form-item label="价格（元）" prop="price">
        <el-input-number
          v-model="priceInYuan"
          placeholder="请输入价格"
          :min="0"
          :step="0.01"
          style="width: 100%"
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
      <el-form-item label="排序" prop="sortOrder">
        <el-input-number
          v-model="localFormData.sortOrder"
          placeholder="请输入排序号"
          :min="0"
          style="width: 100%"
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
import { ref, watch, computed } from 'vue'
import { ElMessage } from 'element-plus'
import PlatformSelect from '@/components/PlatformSelect.vue'

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

// 修复这里：需要将defineEmits的返回值赋值给emit变量
const emit = defineEmits(['submit', 'cancel'])

const formRef = ref(null)
const submitting = ref(false)
const localFormData = ref({})

// 将价格从分转换为元进行显示
const priceInYuan = computed({
  get() {
    return localFormData.value.price ? localFormData.value.price / 100 : 0
  },
  set(value) {
    localFormData.value.price = value * 100
  },
})

// 监听表单数据变化
watch(
  () => props.formData,
  (newValue) => {
    localFormData.value = { ...newValue }
  },
  { immediate: true, deep: true },
)

// 平台选择变化处理
const handlePlatformChange = (platform) => {
  console.log('选择的平台:', platform)
}

// 表单验证规则
const formRules = {
  name: [
    { required: true, message: '请输入功能名称', trigger: 'blur' },
    { min: 2, max: 50, message: '功能名称长度在 2 到 50 个字符', trigger: 'blur' },
  ],
  code: [
    { required: true, message: '请输入功能编码', trigger: 'blur' },
    { min: 2, max: 50, message: '功能编码长度在 2 到 50 个字符', trigger: 'blur' },
    {
      pattern: /^[a-zA-Z0-9_-]+$/,
      message: '功能编码只能包含英文大小写字母、数字、连接符和下划线',
      trigger: 'blur',
    },
  ],
  description: [
    { required: true, message: '请输入功能描述', trigger: 'blur' },
    { max: 200, message: '功能描述长度不能超过 200 个字符', trigger: 'blur' },
  ],
  price: [
    { required: true, message: '请输入价格', trigger: 'blur' },
    { type: 'number', min: 0, message: '价格不能为负数', trigger: 'blur' },
  ],
  sortOrder: [{ type: 'number', min: 0, message: '排序号不能为负数', trigger: 'blur' }],
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
.feature-edit-form {
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
