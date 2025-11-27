<template>
  <el-form
    ref="formRef"
    :model="localFormData"
    :rules="formRules"
    label-width="120px"
    status-icon
    class="invite-code-form"
  >
    <el-form-item label="所属分组" prop="groupId">
      <el-select
        v-model="localFormData.groupId"
        placeholder="请选择分组"
        filterable
        remote
        :remote-method="searchUserGroups"
        :loading="groupLoading"
        style="width: 100%"
        @change="
          (value) => {
            const selected = groupOptions.value.find((item) => item.value === value)
            localFormData.groupName = selected ? selected.label : ''
          }
        "
      >
        <el-option
          v-for="option in groupOptions"
          :key="option.value"
          :label="option.label"
          :value="option.value"
        />
      </el-select>
    </el-form-item>
    <el-form-item label="最大使用次数" prop="maxUses">
      <el-input-number
        v-model="localFormData.maxUses"
        :min="0"
        :max="9999"
        placeholder="请输入最大使用次数"
        style="width: 100%"
      />
      <div class="el-form-item__tip">设置为0表示不限制使用次数</div>
    </el-form-item>
    <el-form-item label="过期时间" prop="expireAt">
      <ExpireSelector
        v-model="localFormData.expireAt"
        unit="d"
        :defaultPreset="30"
        :presets="[
          { name: '7天', value: 7 },
          { name: '30天', value: 30 },
          { name: '90天', value: 90 },
          { name: '180天', value: 180 },
          { name: '1年', value: 365 },
        ]"
      />
    </el-form-item>
    <el-form-item class="form-actions">
      <el-button @click="handleCancel" size="default">取消</el-button>
      <el-button type="primary" @click="handleSubmit" size="default" :loading="submitLoading">
        {{ isEdit ? '更新' : '创建' }}
      </el-button>
    </el-form-item>
  </el-form>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { userGroupApi } from '@/api/userGroup'
import ExpireSelector from '@/components/ common/ExpireSelector.vue'
import dayjs from 'dayjs'

const props = defineProps({
  formData: {
    type: Object,
    required: true,
    default: () => ({}),
  },
  isEdit: {
    type: Boolean,
    required: true,
    default: false,
  },
})

const emit = defineEmits(['submit-form', 'cancel-form'])

// 创建prop的本地副本，避免直接修改prop
const localFormData = ref({ ...props.formData })
const initialFormData = ref({ ...props.formData })
const formRef = ref(null)
const submitLoading = ref(false)
const groupOptions = ref([]) // 分组选项
const groupLoading = ref(false) // 分组加载状态

// 表单验证规则
const formRules = {
  maxUses: [
    { required: true, message: '请输入最大使用次数', trigger: 'blur' },
    {
      type: 'number',
      min: 0,
      max: 9999,
      message: '最大使用次数在 0 到 9999 之间',
      trigger: 'blur',
    },
  ],
  expireAt: [{ required: true, message: '请选择过期时间', trigger: 'change' }],
}

// 搜索用户组
const searchUserGroups = async (query) => {
  groupLoading.value = true
  try {
    const params = {
      pageNum: 1,
      pageSize: 10,
      name: query,
    }
    const { records } = await userGroupApi.getUserGroupPage(params)
    groupOptions.value = records.map((item) => ({
      value: item.id,
      label: item.name,
    }))
  } catch (error) {
    console.error('搜索用户组失败', error)
    ElMessage.error('搜索用户组失败')
  } finally {
    groupLoading.value = false
  }
}

// 监听prop变化并同步到本地副本
watch(
  () => props.formData,
  (newVal) => {
    // 如果是从字符串日期格式转换到时间戳格式
    const formDataCopy = { ...newVal }
    if (formDataCopy.expireAt && typeof formDataCopy.expireAt === 'string') {
      formDataCopy.expireAt = dayjs(formDataCopy.expireAt).valueOf()
    }
    localFormData.value = formDataCopy
    initialFormData.value = { ...formDataCopy }
  },
  { deep: true },
)

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  submitLoading.value = true
  try {
    await formRef.value.validate()
    emit('submit-form', { ...localFormData.value })
  } catch (error) {
    if (error !== false) {
      ElMessage.error(props.isEdit ? '更新失败' : '创建失败')
    }
  } finally {
    submitLoading.value = false
  }
}

// 取消表单
const handleCancel = () => {
  emit('cancel-form')
}

// 检查表单是否修改
const isFormModified = () => {
  return JSON.stringify(localFormData.value) !== JSON.stringify(initialFormData.value)
}

// 初始化加载用户组
onMounted(() => {
  searchUserGroups('')
})

// 暴露方法给父组件
defineExpose({
  validate: (callback) => formRef.value?.validate(callback),
  resetFields: () => formRef.value?.resetFields(),
  isFormModified,
})
</script>

<style scoped lang="scss">
.invite-code-form {
  padding: 24px;
  background-color: #fff;
  border-radius: 8px;

  .el-form-item {
    margin-bottom: 20px;
  }
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 30px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;

  button {
    margin-left: 12px;
    width: 100px;
  }
}

.el-form-item__tip {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}
</style>
