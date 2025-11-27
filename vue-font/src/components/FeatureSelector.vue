<template>
  <el-dialog
    v-model="dialogVisible"
    title="选择功能"
    width="70%"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <div class="search-bar">
      <el-input
        v-model="searchText"
        placeholder="搜索功能名称或编码"
        clearable
        @keyup.enter="handleSearch"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-button type="primary" @click="handleSearch" :loading="loading">
        <el-icon><Search /></el-icon>
        搜索
      </el-button>
      <el-button @click="handleReset">
        <el-icon><RefreshLeft /></el-icon>
        重置
      </el-button>
    </div>

    <div v-if="selectedFeatures.length > 0" class="selected-tip">
      <el-tag type="success" size="large"> 已选择 {{ selectedFeatures.length }} 个功能 </el-tag>
      <el-button type="text" @click="clearSelection">清空选择</el-button>
    </div>

    <el-table
      ref="tableRef"
      v-loading="loading"
      :data="featureList"
      style="width: 100%"
      border
      stripe
      @selection-change="handleSelectionChange"
      :row-key="(row) => row.id"
    >
      <el-table-column type="selection" width="55" :reserve-selection="true" />

      <el-table-column prop="name" label="功能名称" min-width="180" show-overflow-tooltip />

      <el-table-column prop="code" label="功能编码" width="160" show-overflow-tooltip />

      <el-table-column label="价格" width="120" align="right">
        <template #default="{ row }">
          <span class="price-text"> ¥{{ formatPrice(row.price) }} </span>
        </template>
      </el-table-column>

      <el-table-column prop="description" label="功能描述" show-overflow-tooltip />
    </el-table>

    <div class="pagination-container">
      <el-pagination
        v-model:current-page="pagination.currentPage"
        v-model:page-size="pagination.pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handlePageSizeChange"
        @current-change="handlePageChange"
      />
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" @click="handleConfirm" :disabled="selectedFeatures.length === 0">
          确定选择 ({{ selectedFeatures.length }})
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, RefreshLeft } from '@element-plus/icons-vue'
import { featureApi } from '@/api/feature'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false,
  },
  // 已选择的功能ID列表(用于回显选中状态)
  selectedFeatureIds: {
    type: Array,
    default: () => [],
  },
})

const emit = defineEmits(['update:visible', 'confirm'])

// 对话框显示状态
const dialogVisible = ref(false)

// 表格引用
const tableRef = ref()

// 搜索文本
const searchText = ref('')

// 加载状态
const loading = ref(false)

// 功能列表
const featureList = ref([])

// 已选择的功能
const selectedFeatures = ref([])

// 分页信息
const pagination = ref({
  currentPage: 1,
  pageSize: 20,
  total: 0,
})

// 监听visible变化
watch(
  () => props.visible,
  (newVal) => {
    dialogVisible.value = newVal
    if (newVal) {
      // ✨ 核心修复：在加载数据前，先清空表格上一次的选中状态
      // ✨ 使用 nextTick 确保 tableRef 已经挂载
      nextTick(() => {
        tableRef.value?.clearSelection()
      })
      resetSearch()
      loadFeatures()
    }
  },
  { immediate: true },
)

// 监听对话框关闭
watch(dialogVisible, (newVal) => {
  if (!newVal) {
    emit('update:visible', false)
  }
})

// 加载功能列表
const loadFeatures = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.value.currentPage,
      pageSize: pagination.value.pageSize,
    }

    // 添加搜索条件
    if (searchText.value) {
      params.name = searchText.value
    }

    const { records = [], total = 0 } = await featureApi.getPage(params)
    featureList.value = records
    pagination.value.total = total

    // 回显已选择的功能
    await nextTick()
    restoreSelection()
  } catch (error) {
    ElMessage.error('获取功能列表失败')
    console.error('Load features error:', error)
  } finally {
    loading.value = false
  }
}

// 恢复选择状态
const restoreSelection = () => {
  if (!tableRef.value || props.selectedFeatureIds.length === 0) return

  featureList.value.forEach((row) => {
    if (props.selectedFeatureIds.includes(row.id)) {
      tableRef.value.toggleRowSelection(row, true)
    }
  })
}

// 搜索
const handleSearch = () => {
  pagination.value.currentPage = 1
  loadFeatures()
}

// 重置搜索
const handleReset = () => {
  searchText.value = ''
  pagination.value.currentPage = 1
  loadFeatures()
}

// 重置搜索(内部使用)
const resetSearch = () => {
  searchText.value = ''
  pagination.value.currentPage = 1
  // 注意：这里不要清空 selectedFeatures.value，因为它由 el-table 的 selection-change 管理
}

// 分页大小变化
const handlePageSizeChange = (newSize) => {
  pagination.value.pageSize = newSize
  pagination.value.currentPage = 1
  loadFeatures()
}

// 页码变化
const handlePageChange = (newPage) => {
  pagination.value.currentPage = newPage
  loadFeatures()
}

// 选择变化
const handleSelectionChange = (selection) => {
  selectedFeatures.value = selection
}

// 清空选择
const clearSelection = () => {
  if (tableRef.value) {
    tableRef.value.clearSelection()
  }
  selectedFeatures.value = []
}

// 关闭对话框
const handleClose = () => {
  dialogVisible.value = false
}

// 确认选择
const handleConfirm = () => {
  if (selectedFeatures.value.length === 0) {
    ElMessage.warning('请至少选择一个功能')
    return
  }

  emit('confirm', selectedFeatures.value)
  dialogVisible.value = false
}

// 格式化价格
const formatPrice = (price) => {
  if (!price) return '0.00'
  return (price / 100).toFixed(2)
}
</script>

<style scoped lang="scss">
/* 样式部分未作任何修改 */
.search-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;

  .el-input {
    flex: 1;
    max-width: 400px;
  }
}

.selected-tip {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  margin-bottom: 16px;
  background: #f0f9ff;
  border-radius: 4px;
  border: 1px solid #d1e9ff;

  .el-tag {
    font-size: 14px;
  }

  .el-button {
    padding: 0;
  }
}

.price-text {
  color: #f56c6c;
  font-weight: 500;
  font-size: 14px;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid #ebeef5;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;

  .el-button {
    min-width: 100px;
  }
}

:deep(.el-table) {
  .el-table__header-wrapper {
    th {
      background: #f5f7fa;
      color: #303133;
      font-weight: 500;
    }
  }

  .el-table__body-wrapper {
    .el-table__row {
      &:hover {
        background: #f5f7fa;
      }
    }
  }
}
</style>
