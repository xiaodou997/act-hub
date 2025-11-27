<template>
  <div class="modern-query-filter">
    <el-form :inline="true" :model="modelValue" class="query-form">
      <div class="filters-section">
        <slot name="filters" />
      </div>
      <div class="actions-section">
        <el-form-item class="action-buttons">
          <el-button
            type="primary"
            @click="handleSearch"
            :loading="searchLoading"
            class="search-button"
          >
            <el-icon><Search /></el-icon>
            <span class="button-text">查询</span>
          </el-button>
          <el-button @click="handleReset" class="reset-button">
            <el-icon><Refresh /></el-icon>
            <span class="button-text">重置</span>
          </el-button>
        </el-form-item>
      </div>
    </el-form>
  </div>
</template>

<script setup>
import { Search, Refresh } from '@element-plus/icons-vue'

defineProps({
  modelValue: Object,
  searchLoading: {
    type: Boolean,
    default: false,
  },
})

const emit = defineEmits(['search', 'reset'])

const handleSearch = () => {
  emit('search')
}

const handleReset = () => {
  emit('reset')
}
</script>

<style scoped>
.modern-query-filter {
  margin-bottom: 24px;
  padding: 24px;
  background: #ffffff;
  border: 1px solid #f1f3f4;
  border-radius: 12px;
  box-shadow:
    0 2px 8px rgba(0, 0, 0, 0.04),
    0 1px 3px rgba(0, 0, 0, 0.08);
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
}

.modern-query-filter:hover {
  border-color: #e5e7eb;
  box-shadow:
    0 4px 12px rgba(0, 0, 0, 0.08),
    0 2px 4px rgba(0, 0, 0, 0.06);
  transform: translateY(-1px);
}

.query-form {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 24px;
  align-items: start;
  width: 100%;
}

.filters-section {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  align-items: center;
  min-width: 0;
}

.actions-section {
  flex-shrink: 0;
  display: flex;
  justify-content: flex-end;
  align-items: center;
}

/* Element Plus 表单项样式重写 */
:deep(.el-form-item) {
  margin-bottom: 0 !important;
  display: flex;
  align-items: center;
}

:deep(.el-form-item__label) {
  font-weight: 500;
  color: #374151;
  padding-right: 12px;
  font-size: 14px;
  white-space: nowrap;
  display: flex;
  align-items: center;
  line-height: normal;
}

:deep(.el-form-item__content) {
  display: flex;
  align-items: center;
}
/* 输入框和选择器样式 */
:deep(.el-input__wrapper),
:deep(.el-select__wrapper) {
  border-radius: 8px;
  border: 1px solid #e5e7eb;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.05);
  background-color: #ffffff;
}

:deep(.el-input__wrapper:hover),
:deep(.el-select__wrapper:hover) {
  border-color: #d1d5db;
}

:deep(.el-input__wrapper:focus-within),
:deep(.el-select__wrapper:focus-within) {
  border-color: var(--el-color-primary);
  box-shadow: 0 0 0 3px rgba(var(--app-primary-rgb), 0.08);
}

:deep(.el-input__inner) {
  font-size: 14px;
  color: #111827;
  font-weight: 400;
}

/* 按钮样式优化 - 大厂风格 */
.action-buttons {
  display: flex;
  gap: 12px;
  align-items: center;
}

.search-button {
  background: var(--app-primary-gradient);
  border: 1px solid var(--el-color-primary);
  border-radius: 8px;
  padding: 9px 16px;
  font-weight: 500;
  font-size: 14px;
  color: #ffffff;
  transition: all 0.15s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.05);
  min-width: 80px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-size: 200% 200%;
}

.search-button:hover {
  background: var(--app-primary-gradient);
  background-position: right center;
  border-color: var(--el-color-primary);
  box-shadow: 0 2px 4px 0 rgba(var(--app-primary-rgb), 0.15);
}

.search-button:active {
  transform: translateY(0.5px);
  box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.1);
}

.reset-button {
  background: #ffffff;
  border: 1px solid #e5e7eb;
  color: #6b7280;
  border-radius: 8px;
  padding: 9px 16px;
  font-weight: 500;
  font-size: 14px;
  transition: all 0.15s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.05);
  min-width: 80px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.reset-button:hover {
  background: #f9fafb;
  border-color: #d1d5db;
  color: #374151;
}

.reset-button:active {
  transform: translateY(0.5px);
  background: #f3f4f6;
}

/* 图标和文字间距 */
.search-button .el-icon,
.reset-button .el-icon {
  margin-right: 4px;
  font-size: 16px;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .query-form {
    grid-template-columns: 1fr;
    gap: 20px;
  }

  .actions-section {
    justify-content: flex-start;
    border-top: 1px solid #f3f4f6;
    padding-top: 20px;
  }
}

@media (max-width: 768px) {
  .modern-query-filter {
    padding: 20px;
    margin-bottom: 20px;
  }

  .filters-section {
    flex-direction: column;
    align-items: stretch;
    gap: 16px;
  }

  .filters-section :deep(.el-form-item) {
    width: 100%;
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .filters-section :deep(.el-form-item__content) {
    width: 100%;
  }

  .action-buttons {
    width: 100%;
    justify-content: center;
    gap: 12px;
  }

  .search-button,
  .reset-button {
    flex: 1;
    max-width: 120px;
  }
}

@media (max-width: 480px) {
  .modern-query-filter {
    padding: 16px;
  }

  .action-buttons {
    flex-direction: column;
    gap: 8px;
    width: 100%;
  }

  .search-button,
  .reset-button {
    width: 100%;
    max-width: none;
  }
}

/* 加载状态优化 */
:deep(.el-button.is-loading) {
  position: relative;
}

:deep(.el-button.is-loading .el-icon) {
  animation: rotate 1s linear infinite;
}

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}
</style>
