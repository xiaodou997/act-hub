<!-- /components/PlatformSelect.vue -->
<template>
  <el-select
    v-model="selectedPlatformId"
    filterable
    remote
    reserve-keyword
    :placeholder="currentPlatformName ? currentPlatformName : '请搜索并选择平台'"
    :remote-method="searchPlatforms"
    :loading="loading"
    @change="handleChange"
    @clear="handleClear"
    style="width: 100%"
    clearable
  >
    <el-option
      v-for="platform in platformList"
      :key="platform.id"
      :label="platform.name"
      :value="platform.id"
    >
      <div class="platform-option">
        <span>{{ platform.name }}</span>
        <el-tag :type="getStatusType(platform.status)" size="small" style="margin-left: 10px">
          {{ getStatusText(platform.status) }}
        </el-tag>
        <span v-if="platform.code" style="margin-left: 10px; color: #999; font-size: 12px">
          编码: {{ platform.code }}
        </span>
      </div>
    </el-option>
  </el-select>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { platformApi } from '@/api/platform'

const props = defineProps({
  modelValue: String,
})

const emit = defineEmits(['update:modelValue', 'change'])

const selectedPlatformId = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val),
})

const loading = ref(false)
const platformList = ref([])
const currentPlatformName = ref('')

// 根据平台ID获取平台信息
const getPlatformById = async (platformId) => {
  if (!platformId) {
    currentPlatformName.value = ''
    return
  }

  loading.value = true
  try {
    // 先尝试从当前列表中查找
    const existingPlatform = platformList.value.find((platform) => platform.id === platformId)
    if (existingPlatform) {
      currentPlatformName.value = existingPlatform.name
      return
    }

    // 如果当前列表中没有，则通过API获取
    const { records = [] } = await platformApi.getPage({ pageNum: 1, pageSize: 1, id: platformId })

    if (records.length > 0) {
      const platform = records[0]
      currentPlatformName.value = platform.name
      // 将获取到的平台添加到列表中，以便在下拉框中显示
      if (!platformList.value.some((p) => p.id === platform.id)) {
        platformList.value = [platform, ...platformList.value]
      }
    } else {
      currentPlatformName.value = ''
    }
  } catch (error) {
    console.error('get platform by id failed', error)
    currentPlatformName.value = ''
  } finally {
    loading.value = false
  }
}

// 监听平台ID变化
watch(
  () => props.modelValue,
  (newVal) => {
    getPlatformById(newVal)
  },
  { immediate: true },
)

// 组件挂载时初始化
onMounted(() => {
  if (props.modelValue) {
    getPlatformById(props.modelValue)
  }
})

// 搜索平台
const searchPlatforms = async (query) => {
  if (!query) {
    platformList.value = []
    return
  }

  loading.value = true
  try {
    const { records = [] } = await platformApi.getPage({ pageNum: 1, pageSize: 100, name: query })
    platformList.value = records
  } catch (error) {
    console.error('search platforms failed', error)
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
  const selectedPlatform = platformList.value.find((platform) => platform.id === val)
  if (selectedPlatform) {
    currentPlatformName.value = selectedPlatform.name
  } else {
    currentPlatformName.value = ''
  }
  emit('change', selectedPlatform)
}

// 清空选择
const handleClear = () => {
  currentPlatformName.value = ''
  emit('change', null)
}
</script>

<style scoped>
.platform-option {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
}
</style>
