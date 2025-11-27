<template>
  <div class="expire-selector">
    <el-radio-group v-model="selection" @change="onSelectionChange" class="radio-group">
      <el-radio v-for="p in normalizedPresets" :key="p.label" :label="p.label" class="radio-item">
        {{ p.name }}
      </el-radio>
      <el-radio label="custom" class="radio-item">自定义</el-radio>
    </el-radio-group>

    <el-date-picker
      v-if="selection === 'custom'"
      v-model="customDate"
      type="datetime"
      placeholder="选择过期时间"
      style="margin-top: 16px; width: 100%"
      :disabled-date="disablePast ? (time) => time.getTime() < Date.now() : () => false"
      @change="emitCustomDate"
      class="date-picker"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, watch, computed, onMounted } from 'vue'

// =================== 单位映射 ===================
const UNIT_MAP = {
  ms: 1,
  s: 1000,
  m: 60 * 1000,
  h: 60 * 60 * 1000,
  d: 24 * 60 * 60 * 1000,
} as const

// =================== Props ===================
const props = withDefaults(
  defineProps<{
    modelValue: number | null
    presets?: Array<{ name: string; value: number | null }>
    unit?: keyof typeof UNIT_MAP
    matchTolerance?: number
    disablePast?: boolean
    emitOnCustomSelect?: boolean
    defaultPreset?: number | null
  }>(),
  {
    modelValue: null,
    presets: () => [
      { name: '30天', value: 30 },
      { name: '90天', value: 90 },
      { name: '1年', value: 365 },
      { name: '永不过期', value: null },
    ],
    unit: 'd',
    matchTolerance: 5000,
    disablePast: true,
    emitOnCustomSelect: false,
    defaultPreset: null,
  },
)

const emit = defineEmits<{
  'update:modelValue': [value: number | null]
}>()

// =================== 内部状态 ===================
const selection = ref<string>('never')
const customDate = ref<Date | null>(null)

// =================== 计算属性 ===================
const normalizedPresets = computed(() => {
  const { unit, presets } = props
  const factor = UNIT_MAP[unit]

  return presets.map((p) => {
    let msValue: number | null = null
    if (p.value !== null) {
      msValue = p.value * factor
    }
    return {
      ...p,
      label: p.value === null ? 'never' : `preset:${p.value}`,
      msValue,
    }
  })
})

// =================== Watcher: modelValue -> UI ===================
watch(
  () => props.modelValue,
  (val) => {
    if (val === null) {
      selection.value = 'never'
      customDate.value = null
      return
    }

    const nowMs = Date.now()
    const diffMs = val - nowMs

    const matched = normalizedPresets.value.find((p) => {
      if (p.label === 'never') return false
      return Math.abs(diffMs - p.msValue!) <= props.matchTolerance
    })

    if (matched) {
      selection.value = matched.label
      customDate.value = null
    } else {
      selection.value = 'custom'
      try {
        customDate.value = new Date(val)
      } catch {
        customDate.value = null
      }
    }
  },
  { immediate: true },
)

// =================== 事件处理 ===================
const onSelectionChange = () => {
  if (selection.value === 'custom') {
    if (props.emitOnCustomSelect) {
      emit('update:modelValue', customDate.value?.getTime() ?? null)
    }
    return
  }

  if (selection.value === 'never') {
    emit('update:modelValue', null)
    return
  }

  const preset = normalizedPresets.value.find((p) => p.label === selection.value)!
  const targetTime = Date.now() + preset.msValue!
  emit('update:modelValue', targetTime)
}

const emitCustomDate = (val: Date | null) => {
  if (!val) return
  emit('update:modelValue', val.getTime())
}

// =================== 初始化默认值 ===================
onMounted(() => {
  if (props.modelValue !== null) return

  if (props.defaultPreset === null) {
    selection.value = 'never'
    emit('update:modelValue', null)
  } else {
    const preset = normalizedPresets.value.find((p) => p.value === props.defaultPreset)
    if (preset) {
      selection.value = preset.label
      onSelectionChange()
    } else {
      selection.value = 'never'
      emit('update:modelValue', null)
    }
  }
})
</script>

<style scoped>
.expire-selector {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 16px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  background-color: #fafafa;
}

.radio-group {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
}

.radio-item {
  font-size: 14px;
  color: #333;
  cursor: pointer;
  transition: color 0.3s ease;
}

.radio-item:hover {
  color: #409eff;
}

.date-picker {
  margin-top: 16px;
  border-radius: 6px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  transition: box-shadow 0.3s ease;
}

.date-picker:focus-within {
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
}
</style>
