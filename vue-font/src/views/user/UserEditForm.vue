<template>
  <div class="user-edit-form">
    <el-form
      ref="formRef"
      :model="localFormData"
      :rules="formRules"
      label-width="100px"
      label-position="right"
    >
      <el-form-item v-if="isEdit" label="用户名" prop="username">
        <el-input v-model="localFormData.username" placeholder="请输入用户名" :disabled="true" />
      </el-form-item>
      <el-form-item label="邮箱" prop="email">
        <el-input v-model="localFormData.email" placeholder="请输入邮箱" />
      </el-form-item>
      <!-- 租户相关已停用
      <el-form-item label="所属租户" prop="tenantId">
        <el-select
          v-model="localFormData.tenantId"
          placeholder="请选择租户"
          filterable
          clearable
          :remote="true"
          :remote-method="handleTenantSearch"
          :loading="tenantLoading"
          value-key="id"
          @change="handleTenantChange"
        >
          <el-option
            v-for="tenant in tenantList"
            :key="tenant.id"
            :label="tenant.name"
            :value="tenant.id"
          />
        </el-select>
      </el-form-item>
      -->
      <el-form-item label="状态" prop="status">
        <el-select v-model="localFormData.status" placeholder="请选择状态">
          <el-option label="正常" :value="1" />
          <el-option label="禁用" :value="0" />
          <el-option label="锁定" :value="2" />
        </el-select>
      </el-form-item>
      <el-form-item label="备注" prop="remark">
        <el-input
          v-model="localFormData.remark"
          type="textarea"
          :rows="4"
          placeholder="请输入备注信息"
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
import { ref, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
// import { tenantApi } from '@/api/tenant'

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

// 租户相关数据（已停用）
// const tenantList = ref([])
// const tenantLoading = ref(false)
// const lastTenantSearchQuery = ref('')

// 初始化本地数据
watch(
  () => props.formData,
  (newValue) => {
    localFormData.value = { ...newValue }
  },
  { immediate: true, deep: true },
)
/*
// 获取租户列表（租户功能已停用）
const getTenantList = async (query = '') => {
  tenantLoading.value = true
  try {
    const { records } = await tenantApi.getPage({
      pageNum: 1,
      pageSize: 100,
      name: query,
    })
    tenantList.value = records || []
    lastTenantSearchQuery.value = query
  } catch (error) {
    console.error('获取租户列表失败:', error)
    ElMessage.error('获取租户列表失败')
  } finally {
    tenantLoading.value = false
  }
}
*/

// 远程搜索租户（租户功能已停用）
// const handleTenantSearch = (query) => {
//   if (query !== lastTenantSearchQuery.value) {
//     getTenantList(query)
//   }
// }

// 处理租户选择变化（租户功能已停用）
// const handleTenantChange = (tenantId) => {
//   // 可以在这里添加额外的逻辑，如果需要的话
//   console.log('Selected tenant ID:', tenantId)
// }

// 组件挂载时获取租户列表（租户功能已停用）
// onMounted(() => {
//   getTenantList()
// })

// 表单验证规则
const formRules = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' },
  ],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }],
  remark: [{ max: 200, message: '备注信息不能超过 200 个字符', trigger: 'blur' }],
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
.user-edit-form {
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

  // 增加按钮宽度
  .el-button {
    min-width: 120px;
    padding: 0 24px;
  }
}
</style>
