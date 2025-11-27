<!-- /components/TenantSelect.vue -->
<template>
  <el-select
    v-model="selectedTenantId"
    filterable
    remote
    reserve-keyword
    :placeholder="currentTenantName ? currentTenantName : '请搜索并选择租户'"
    :remote-method="searchTenants"
    :loading="loading"
    @change="handleChange"
    @clear="handleClear"
    style="width: 100%"
    clearable
  >
    <el-option
      v-for="tenant in tenantList"
      :key="tenant.id"
      :label="tenant.name"
      :value="tenant.id"
    >
      <div class="tenant-option">
        <span>{{ tenant.name }}</span>
        <el-tag :type="getStatusType(tenant.status)" size="small" style="margin-left: 10px">
          {{ getStatusText(tenant.status) }}
        </el-tag>
        <span v-if="tenant.contactName" style="margin-left: 10px; color: #999; font-size: 12px">
          联系人: {{ tenant.contactName }}
        </span>
      </div>
    </el-option>
  </el-select>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { tenantApi } from '@/api/tenant'

const props = defineProps({
  modelValue: String,
})

const emit = defineEmits(['update:modelValue', 'change'])

const selectedTenantId = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val),
})

const loading = ref(false)
const tenantList = ref([])
const currentTenantName = ref('')

// 根据租户ID获取租户信息
const getTenantById = async (tenantId) => {
  if (!tenantId) {
    currentTenantName.value = ''
    return
  }

  loading.value = true
  try {
    // 先尝试从当前列表中查找
    const existingTenant = tenantList.value.find((tenant) => tenant.id === tenantId)
    if (existingTenant) {
      currentTenantName.value = existingTenant.name
      return
    }

    // 如果当前列表中没有，则通过API获取
    const { records = [] } = await tenantApi.getPage({ pageNum: 1, pageSize: 1, id: tenantId })

    if (records.length > 0) {
      const tenant = records[0]
      currentTenantName.value = tenant.name
      // 将获取到的租户添加到列表中，以便在下拉框中显示
      if (!tenantList.value.some((t) => t.id === tenant.id)) {
        tenantList.value = [tenant, ...tenantList.value]
      }
    } else {
      currentTenantName.value = ''
    }
  } catch (error) {
    console.error('get tenant by id failed', error)
    currentTenantName.value = ''
  } finally {
    loading.value = false
  }
}

// 监听租户ID变化
watch(
  () => props.modelValue,
  (newVal) => {
    getTenantById(newVal)
  },
  { immediate: true },
)

// 组件挂载时初始化
onMounted(() => {
  if (props.modelValue) {
    getTenantById(props.modelValue)
  }
})

// 搜索租户
const searchTenants = async (query) => {
  if (!query) {
    tenantList.value = []
    return
  }

  loading.value = true
  try {
    const { records = [] } = await tenantApi.getPage({ pageNum: 1, pageSize: 100, name: query })
    tenantList.value = records
  } catch (error) {
    console.error('search tenants failed', error)
  } finally {
    loading.value = false
  }
}

// 状态类型映射
const getStatusType = (status) => {
  const statusMap = {
    1: 'success',
    2: 'warning',
    3: 'danger',
  }
  return statusMap[status] || 'info'
}

// 状态文本映射
const getStatusText = (status) => {
  const statusMap = {
    1: '正常',
    2: '到期',
    3: '禁用',
  }
  return statusMap[status] || '未知'
}

// 选择变化
const handleChange = (val) => {
  const selectedTenant = tenantList.value.find((tenant) => tenant.id === val)
  if (selectedTenant) {
    currentTenantName.value = selectedTenant.name
  } else {
    currentTenantName.value = ''
  }
  emit('change', selectedTenant)
}

// 清空选择
const handleClear = () => {
  currentTenantName.value = ''
  emit('change', null)
}
</script>

<style scoped>
.tenant-option {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
}
</style>
