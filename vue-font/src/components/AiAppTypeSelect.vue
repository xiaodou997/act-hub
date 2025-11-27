<template>
  <el-select
    v-model="selectedTypeId"
    filterable
    remote
    reserve-keyword
    :placeholder="currentTypeName ? currentTypeName : '请搜索并选择应用类型'"
    :remote-method="searchTypes"
    :loading="loading"
    style="width: 100%"
    clearable
    @change="handleChange"
    @clear="handleClear"
  >
    <el-option v-for="item in typeList" :key="item.id" :label="item.name" :value="item.id">
      <div class="type-option">
        <span>{{ item.name }}</span>
      </div>
    </el-option>
  </el-select>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { aiAppTypeApi } from '@/api/aiAppType'

const props = defineProps({
  modelValue: String,
})
const emit = defineEmits(['update:modelValue', 'change'])

const selectedTypeId = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val),
})

const loading = ref(false)
const typeList = ref([])
const currentTypeName = ref('')

const getTypeById = async (typeId) => {
  if (!typeId) {
    currentTypeName.value = ''
    return
  }
  loading.value = true
  try {
    const existing = typeList.value.find((x) => x.id === typeId)
    if (existing) {
      currentTypeName.value = existing.name
      return
    }
    const { records = [] } = await aiAppTypeApi.getPage({ pageNum: 1, pageSize: 1, id: typeId })
    if (records.length > 0) {
      const item = records[0]
      currentTypeName.value = item.name
      if (!typeList.value.some((x) => x.id === item.id)) {
        typeList.value = [item, ...typeList.value]
      }
    } else {
      currentTypeName.value = ''
    }
  } finally {
    loading.value = false
  }
}

watch(
  () => props.modelValue,
  (val) => getTypeById(val),
  { immediate: true },
)

const searchTypes = async (query) => {
  if (!query) {
    typeList.value = []
    return
  }
  loading.value = true
  try {
    const { records = [] } = await aiAppTypeApi.getPage({ pageNum: 1, pageSize: 100, name: query })
    typeList.value = records
  } finally {
    loading.value = false
  }
}

const handleChange = (val) => {
  const selected = typeList.value.find((x) => x.id === val)
  currentTypeName.value = selected ? selected.name : ''
  emit('change', selected || null)
}

const handleClear = () => {
  currentTypeName.value = ''
  emit('change', null)
}
</script>

<style scoped>
.type-option {
  display: flex;
  align-items: center;
}
</style>
