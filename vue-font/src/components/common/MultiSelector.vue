<template>
  <div class="multi-selector-dialog">
    <!-- 搜索框 -->
    <div class="selector-search">
      <div class="search-input-wrapper">
        <svg class="search-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor">
          <circle cx="11" cy="11" r="8"></circle>
          <path d="m21 21-4.35-4.35"></path>
        </svg>
        <input
          v-model="searchKeyword"
          type="text"
          class="search-input"
          placeholder="搜索..."
          @input="handleSearch"
        />
        <svg
          v-if="searchKeyword"
          class="clear-icon"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          @click="clearSearch"
        >
          <circle cx="12" cy="12" r="10"></circle>
          <path d="m15 9-6 6m0-6 6 6"></path>
        </svg>
      </div>
    </div>

    <!-- 顶部统计信息 -->
    <div class="selector-header">
      <div class="header-info">
        <span class="info-label">总数</span>
        <span class="info-value">{{ options.length }}</span>
      </div>
      <div class="header-divider"></div>
      <div class="header-info">
        <span class="info-label">显示</span>
        <span class="info-value">{{ filteredOptions.length }}</span>
      </div>
      <div class="header-divider"></div>
      <div class="header-info">
        <span class="info-label">已选</span>
        <span class="info-value highlight">{{ selectedRows.length }}</span>
      </div>
      <div class="header-actions">
        <button class="select-all-btn" @click="toggleSelectAll">
          {{ isAllSelected ? '取消全选' : '全选当前显示' }}
        </button>
      </div>
    </div>

    <!-- 数据列表 -->
    <div class="selector-content">
      <div class="table-empty" v-if="!options.length && !loading">
        <div class="empty-icon">📦</div>
        <div class="empty-text">暂无数据</div>
      </div>

      <div v-else v-loading="loading" class="options-grid">
        <div
          v-for="option in filteredOptions"
          :key="option[valueKey]"
          class="option-item"
          :class="{ selected: isSelected(option) }"
          @click="toggleSelection(option)"
        >
          <div class="option-checkbox">
            <div class="checkbox-inner" :class="{ checked: isSelected(option) }">
              <svg v-if="isSelected(option)" class="check-icon" viewBox="0 0 24 24">
                <path
                  fill="currentColor"
                  d="M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41L9 16.17z"
                />
              </svg>
            </div>
          </div>

          <div class="option-content">
            <span class="option-label">{{ option[labelKey] }}</span>
            <el-tooltip
              v-if="showDescription && option[descriptionKey]"
              :content="option[descriptionKey]"
              placement="top"
              effect="light"
            >
              <span class="description-icon">?</span>
            </el-tooltip>
          </div>
        </div>
      </div>
    </div>

    <!-- 底部操作按钮 -->
    <div class="dialog-footer">
      <button class="footer-btn btn-cancel" @click="handleCancel">
        <span class="btn-text">取消</span>
      </button>
      <button class="footer-btn btn-confirm" @click="handleConfirm" :disabled="submitting">
        <span class="btn-text">{{ submitting ? '处理中...' : '确定' }}</span>
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, nextTick, computed } from 'vue'
import { ElMessage } from 'element-plus'

const props = defineProps({
  options: {
    type: Array,
    required: true,
    default: () => [],
  },
  selectedIds: {
    type: Array,
    default: () => [],
  },
  labelKey: {
    type: String,
    default: 'name',
  },
  valueKey: {
    type: String,
    default: 'id',
  },
  descriptionKey: {
    type: String,
    default: 'description',
  },
  showDescription: {
    type: Boolean,
    default: false,
  },
  loading: {
    type: Boolean,
    default: false,
  },
})

const emit = defineEmits(['confirm', 'cancel'])

const submitting = ref(false)
const selectedRows = ref([])
const searchKeyword = ref('')

// 过滤选项
const filteredOptions = computed(() => {
  if (!searchKeyword.value.trim()) {
    return props.options
  }
  const keyword = searchKeyword.value.toLowerCase().trim()
  return props.options.filter((option) => {
    const label = option[props.labelKey]
    return label && label.toLowerCase().includes(keyword)
  })
})

const isAllSelected = computed(() => {
  if (!filteredOptions.value.length) return false
  return filteredOptions.value.every((option) =>
    selectedRows.value.some((row) => row[props.valueKey] === option[props.valueKey]),
  )
})

const toggleSelectAll = () => {
  if (isAllSelected.value) {
    const filtIds = new Set(filteredOptions.value.map((o) => o[props.valueKey]))
    selectedRows.value = selectedRows.value.filter((row) => !filtIds.has(row[props.valueKey]))
  } else {
    const map = new Map(selectedRows.value.map((row) => [row[props.valueKey], row]))
    filteredOptions.value.forEach((option) => {
      const id = option[props.valueKey]
      if (!map.has(id)) {
        map.set(id, option)
      }
    })
    selectedRows.value = Array.from(map.values())
  }
}

// 初始化选中项 - 修复初始化问题
const initSelectedRows = () => {
  if (!props.options.length) {
    selectedRows.value = []
    return
  }
  const ids = Array.isArray(props.selectedIds) ? props.selectedIds : []
  if (!ids.length) {
    selectedRows.value = []
    return
  }
  const selected = props.options.filter((option) => ids.includes(option[props.valueKey]))
  selectedRows.value = selected
}

// 监听 options 和 selectedIds 的变化
watch(
  [() => props.options, () => props.selectedIds],
  () => {
    nextTick(() => {
      initSelectedRows()
    })
  },
  { immediate: true, deep: true },
)

// 判断是否选中
const isSelected = (option) => {
  return selectedRows.value.some((row) => row[props.valueKey] === option[props.valueKey])
}

// 切换选中状态
const toggleSelection = (option) => {
  const index = selectedRows.value.findIndex(
    (row) => row[props.valueKey] === option[props.valueKey],
  )
  if (index > -1) {
    selectedRows.value.splice(index, 1)
  } else {
    selectedRows.value.push(option)
  }
}

// 搜索处理
const handleSearch = () => {
  // 搜索时自动触发
}

// 清空搜索
const clearSearch = () => {
  searchKeyword.value = ''
}

// 确认选择
const handleConfirm = async () => {
  try {
    submitting.value = true
    const selectedIds = selectedRows.value.map((row) => row[props.valueKey])
    console.log('确认选择的ID:', selectedIds)
    emit('confirm', selectedIds)
  } catch (error) {
    console.error('确认选择失败', error)
    ElMessage.error('操作失败')
  } finally {
    submitting.value = false
  }
}

// 取消选择
const handleCancel = () => {
  emit('cancel')
}
</script>

<style scoped lang="scss">
.multi-selector-dialog {
  padding: 20px;
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #ffffff;
}

// 搜索框
.selector-search {
  margin-bottom: 16px;

  .search-input-wrapper {
    position: relative;
    display: flex;
    align-items: center;

    .search-icon {
      position: absolute;
      left: 14px;
      width: 18px;
      height: 18px;
      color: var(--el-color-primary);
      pointer-events: none;
      stroke-width: 2;
    }

    .search-input {
      width: 100%;
      padding: 10px 40px 10px 42px;
      border: 1.5px solid #e9d5ff;
      border-radius: 8px;
      font-size: 14px;
      color: #4b5563;
      background: #ffffff;
      transition: all 0.25s;
      outline: none;

      &::placeholder {
        color: var(--el-color-primary);
      }

      &:focus {
        border-color: var(--el-color-primary);
        background: var(--el-color-primary-light-9);
        box-shadow: 0 0 0 3px rgba(var(--app-primary-rgb), 0.1);
      }
    }

    .clear-icon {
      position: absolute;
      right: 14px;
      width: 18px;
      height: 18px;
      color: var(--el-color-primary);
      cursor: pointer;
      transition: all 0.2s;
      stroke-width: 2;

      &:hover {
        color: var(--el-color-primary);
        transform: scale(1.1);
      }
    }
  }
}

// 顶部统计
.selector-header {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 12px 20px;
  background: var(--el-color-primary-light-9);
  border: 1px solid var(--el-color-primary-light-9);
  border-radius: 10px;
  margin-bottom: 16px;

  .header-info {
    display: flex;
    align-items: center;
    gap: 10px;

    .info-label {
      font-size: 13px;
      color: var(--el-color-primary);
      font-weight: 500;
    }

    .info-value {
      font-size: 20px;
      font-weight: 700;
      color: var(--el-color-primary);
      font-family: 'Arial', sans-serif;

      &.highlight {
        color: var(--el-color-primary);
      }
    }
  }

  .header-divider {
    width: 1px;
    height: 24px;
    background: var(--el-color-primary-light-9);
  }

  .header-actions {
    margin-left: auto;
  }

  .select-all-btn {
    padding: 6px 12px;
    border: 1px solid #e5e7eb;
    border-radius: 6px;
    background: #ffffff;
    color: #4b5563;
  }
  .select-all-btn:hover {
    background: #f9fafb;
    border-color: #d1d5db;
  }
}

// 内容区域
.selector-content {
  flex: 1;
  overflow: hidden;

  .table-empty {
    height: 100%;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    color: #a78bfa;

    .empty-icon {
      font-size: 48px;
      margin-bottom: 12px;
      opacity: 0.6;
    }

    .empty-text {
      font-size: 14px;
      color: #9333ea;
    }
  }

  .options-grid {
    height: 100%;
    overflow-y: auto;
    padding-right: 6px;
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
    gap: 10px;
    align-content: start;

    // 无搜索结果提示
    &:empty::after {
      content: '没有匹配的结果';
      display: block;
      text-align: center;
      padding: 40px;
      color: #a78bfa;
      font-size: 14px;
    }

    &::-webkit-scrollbar {
      width: 6px;
    }

    &::-webkit-scrollbar-track {
      background: #f3e8ff;
      border-radius: 3px;
    }

    &::-webkit-scrollbar-thumb {
      background: #c084fc;
      border-radius: 3px;

      &:hover {
        background: #a855f7;
      }
    }
  }
}

// 选项卡片
.option-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 14px;
  background: #ffffff;
  border: 1.5px solid #e9d5ff;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.25s ease;
  position: relative;

  &:hover {
    border-color: var(--el-color-primary);
    background: var(--el-color-primary-light-9);
    transform: translateY(-1px);
    box-shadow: 0 2px 8px rgba(var(--app-primary-rgb), 0.12);
  }

  &.selected {
    background: #f6faff;
    border-color: var(--el-color-primary-light-7);
    box-shadow: 0 2px 8px rgba(var(--app-primary-rgb), 0.08);
  }
}

// 复选框
.option-checkbox {
  flex-shrink: 0;

  .checkbox-inner {
    width: 18px;
    height: 18px;
    border: 2px solid var(--el-color-primary);
    border-radius: 4px;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 0.25s;
    background: #ffffff;

    &.checked {
      background: var(--app-primary-gradient);
      border-color: var(--el-color-primary);
      animation: checkBounce 0.3s cubic-bezier(0.68, -0.55, 0.265, 1.55);
    }

    .check-icon {
      width: 12px;
      height: 12px;
      color: white;
    }
  }
}

@keyframes checkBounce {
  0% {
    transform: scale(0);
  }
  50% {
    transform: scale(1.15);
  }
  100% {
    transform: scale(1);
  }
}

// 选项内容
.option-content {
  flex: 1;
  min-width: 0;
  display: flex;
  align-items: center;
  gap: 6px;

  .option-label {
    font-size: 14px;
    color: #4b5563;
    transition: all 0.25s;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  .description-icon {
    flex-shrink: 0;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 14px;
    height: 14px;
    font-size: 10px;
    font-weight: 600;
    background: var(--el-color-primary-light-9);
    color: var(--el-color-primary);
    border-radius: 50%;
    border: 1px solid var(--el-color-primary-light-9);
    cursor: help;
    transition: all 0.2s;

    &:hover {
      background: var(--el-color-primary-light-9);
      transform: scale(1.1);
      border-color: var(--el-color-primary);
    }
  }
}

// 底部按钮
.dialog-footer {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding-top: 16px;
  border-top: 1px solid var(--el-color-primary-light-9);

  .footer-btn {
    padding: 10px 28px;
    border: none;
    border-radius: 8px;
    font-size: 14px;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.25s;

    &:disabled {
      opacity: 0.5;
      cursor: not-allowed;
    }

    .btn-text {
      position: relative;
    }
  }

  .btn-cancel {
    background: #ffffff;
    color: #6b7280;
    border: 1.5px solid #e5e7eb;

    &:hover {
      background: #f9fafb;
      color: #4b5563;
      border-color: #d1d5db;
    }
  }

  .btn-confirm {
    background: var(--app-primary-gradient);
    color: white;
    border: 1.5px solid var(--el-color-primary);
    box-shadow: 0 2px 8px rgba(var(--app-primary-rgb), 0.25);

    &:hover:not(:disabled) {
      transform: translateY(-1px);
      box-shadow: 0 4px 14px rgba(var(--app-primary-rgb), 0.35);
      background: var(--app-primary-gradient);
    }

    &:active:not(:disabled) {
      transform: translateY(0);
    }
  }
}

// 响应式调整
@media (max-width: 768px) {
  .options-grid {
    grid-template-columns: 1fr;
  }
}

@media (min-width: 769px) and (max-width: 1200px) {
  .options-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (min-width: 1201px) {
  .options-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}
</style>
