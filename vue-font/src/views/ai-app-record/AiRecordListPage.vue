<template>
  <div class="record-list-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>执行记录</h2>
      <p class="subtitle">查看AI应用的历史执行记录</p>
    </div>

    <!-- 搜索筛选区域 -->
    <div class="search-bar">
      <el-form :model="searchForm" inline>
        <el-form-item label="应用名称">
          <el-select
            v-model="searchForm.appId"
            placeholder="全部应用"
            clearable
            filterable
            style="width: 200px"
          >
            <el-option
              v-for="app in applicationList"
              :key="app.id"
              :label="app.name"
              :value="app.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="执行状态">
          <el-select
            v-model="searchForm.status"
            placeholder="全部状态"
            clearable
            style="width: 140px"
          >
            <el-option label="执行中" value="RUNNING" />
            <el-option label="成功" value="SUCCESS" />
            <el-option label="失败" value="FAILED" />
          </el-select>
        </el-form-item>

        <el-form-item label="执行时间">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 260px"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 操作栏 -->
    <div class="action-bar">
      <el-button
        type="danger"
        :icon="Delete"
        :disabled="selectedIds.length === 0"
        @click="handleBatchDelete"
      >
        批量删除
      </el-button>
      <span class="selected-info" v-if="selectedIds.length > 0">
        已选择 {{ selectedIds.length }} 条记录
      </span>
    </div>

    <!-- 记录列表 -->
    <el-table
      ref="tableRef"
      v-loading="loading"
      :data="recordList"
      stripe
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="50" />

      <el-table-column label="应用名称" prop="appName" min-width="150">
        <template #default="{ row }">
          <div class="app-info">
            <span class="app-name">{{ row.appName || '未知应用' }}</span>
            <el-tag v-if="row.typeName" size="small" type="info">{{ row.typeName }}</el-tag>
          </div>
        </template>
      </el-table-column>

      <el-table-column label="执行状态" prop="status" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)">
            {{ getStatusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column label="输入参数" prop="inputParams" min-width="200">
        <template #default="{ row }">
          <el-tooltip
            :content="formatJson(row.inputParams)"
            placement="top"
            :disabled="!row.inputParams"
          >
            <span class="params-preview">{{ getParamsPreview(row.inputParams) }}</span>
          </el-tooltip>
        </template>
      </el-table-column>

      <el-table-column label="执行耗时" prop="duration" width="100" align="center">
        <template #default="{ row }">
          <span v-if="row.duration">{{ row.duration }}ms</span>
          <span v-else-if="row.status === 'RUNNING'" class="running-text">执行中...</span>
          <span v-else>-</span>
        </template>
      </el-table-column>

      <el-table-column label="执行时间" prop="createdAt" width="170" align="center">
        <template #default="{ row }">
          {{ formatDateTime(row.createdAt) }}
        </template>
      </el-table-column>

      <el-table-column label="操作" width="150" align="center" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link :icon="View" @click="handleView(row)"> 查看 </el-button>
          <el-button type="danger" link :icon="Delete" @click="handleDelete(row)"> 删除 </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination-container">
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
      />
    </div>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="执行记录详情" width="700px" destroy-on-close>
      <div class="record-detail" v-if="currentRecord">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="记录ID" :span="2">
            {{ currentRecord.id }}
          </el-descriptions-item>

          <el-descriptions-item label="应用名称">
            {{ currentRecord.appName || '未知应用' }}
          </el-descriptions-item>

          <el-descriptions-item label="应用分类">
            {{ currentRecord.typeName || '-' }}
          </el-descriptions-item>

          <el-descriptions-item label="执行状态">
            <el-tag :type="getStatusType(currentRecord.status)">
              {{ getStatusText(currentRecord.status) }}
            </el-tag>
          </el-descriptions-item>

          <el-descriptions-item label="执行耗时">
            {{ currentRecord.duration ? currentRecord.duration + 'ms' : '-' }}
          </el-descriptions-item>

          <el-descriptions-item label="执行时间" :span="2">
            {{ formatDateTime(currentRecord.createdAt) }}
          </el-descriptions-item>
        </el-descriptions>

        <!-- 输入参数 -->
        <div class="detail-section">
          <h4>输入参数</h4>
          <el-input
            type="textarea"
            :model-value="formatJson(currentRecord.inputParams)"
            :rows="6"
            readonly
          />
        </div>

        <!-- 输出结果 -->
        <div class="detail-section">
          <h4>
            输出结果
            <el-button
              v-if="currentRecord.outputResult"
              type="primary"
              link
              :icon="CopyDocument"
              @click="copyResult"
            >
              复制
            </el-button>
          </h4>
          <el-input
            type="textarea"
            :model-value="formatJson(currentRecord.outputResult)"
            :rows="10"
            readonly
          />
        </div>

        <!-- 错误信息 -->
        <div class="detail-section" v-if="currentRecord.errorMessage">
          <h4>错误信息</h4>
          <el-alert type="error" :title="currentRecord.errorMessage" show-icon :closable="false" />
        </div>
      </div>

      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button type="primary" v-if="currentRecord?.outputResult" @click="handleUseResult">
          使用此结果
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Delete, View, CopyDocument } from '@element-plus/icons-vue'
import { aiAppRecordApi } from '@/api/aiAppRecord'
import { aiWorkshopApi } from '@/api/aiWorkshop'
import dayjs from 'dayjs'

// 加载状态
const loading = ref(false)

// 搜索表单
const searchForm = reactive({
  appId: '',
  status: '',
  dateRange: [],
})

// 分页
const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0,
})

// 数据列表
const recordList = ref([])
const applicationList = ref([])
const selectedIds = ref([])

// 详情弹窗
const detailVisible = ref(false)
const currentRecord = ref(null)

// 状态映射
const getStatusType = (status) => {
  const map = {
    RUNNING: 'warning',
    SUCCESS: 'success',
    FAILED: 'danger',
  }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = {
    RUNNING: '执行中',
    SUCCESS: '成功',
    FAILED: '失败',
  }
  return map[status] || status
}

// 格式化JSON
const formatJson = (str) => {
  if (!str) return '-'
  try {
    const obj = typeof str === 'string' ? JSON.parse(str) : str
    return JSON.stringify(obj, null, 2)
  } catch {
    return str
  }
}

// 获取参数预览（截取前50个字符）
const getParamsPreview = (str) => {
  if (!str) return '-'
  try {
    const obj = typeof str === 'string' ? JSON.parse(str) : str
    const preview = JSON.stringify(obj)
    return preview.length > 50 ? preview.substring(0, 50) + '...' : preview
  } catch {
    return str.length > 50 ? str.substring(0, 50) + '...' : str
  }
}

// 格式化日期时间
const formatDateTime = (dateStr) => {
  if (!dateStr) return '-'
  return dayjs(dateStr).format('YYYY-MM-DD HH:mm:ss')
}

// 加载应用列表（用于筛选下拉）
const loadApplications = async () => {
  try {
    const data = await aiWorkshopApi.getCategories()
    const allApps = []
    for (const category of data || []) {
      const apps = await aiWorkshopApi.getApplicationsByType(category.id)
      allApps.push(...(apps || []))
    }
    applicationList.value = allApps
  } catch (error) {
    console.error('加载应用列表失败:', error)
  }
}

// 加载记录列表
const loadRecords = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      pageSize: pagination.pageSize,
    }

    if (searchForm.appId) {
      params.appId = searchForm.appId
    }
    if (searchForm.status) {
      params.status = searchForm.status
    }
    if (searchForm.dateRange && searchForm.dateRange.length === 2) {
      params.startTime = searchForm.dateRange[0]
      params.endTime = searchForm.dateRange[1]
    }

    const data = await aiAppRecordApi.page(params)
    recordList.value = data?.records || data?.list || []
    pagination.total = data?.total || 0
  } catch (error) {
    console.error('加载记录失败:', error)
    ElMessage.error('加载记录失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  loadRecords()
}

// 重置
const handleReset = () => {
  searchForm.appId = ''
  searchForm.status = ''
  searchForm.dateRange = []
  pagination.page = 1
  loadRecords()
}

// 分页变化
const handleSizeChange = (size) => {
  pagination.pageSize = size
  pagination.page = 1
  loadRecords()
}

const handlePageChange = (page) => {
  pagination.page = page
  loadRecords()
}

// 选择变化
const handleSelectionChange = (selection) => {
  selectedIds.value = selection.map((item) => item.id)
}

// 查看详情
const handleView = async (row) => {
  try {
    const data = await aiAppRecordApi.getById(row.id)
    currentRecord.value = data || row
    detailVisible.value = true
  } catch (error) {
    console.error('获取详情失败:', error)
    // 即使获取失败，也显示当前行数据
    currentRecord.value = row
    detailVisible.value = true
  }
}

// 删除单条
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除这条执行记录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })

    await aiAppRecordApi.delete(row.id)
    ElMessage.success('删除成功')
    loadRecords()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 批量删除
const handleBatchDelete = async () => {
  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedIds.value.length} 条记录吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })

    await aiAppRecordApi.batchDelete(selectedIds.value)
    ElMessage.success('删除成功')
    selectedIds.value = []
    loadRecords()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量删除失败:', error)
      ElMessage.error('批量删除失败')
    }
  }
}

// 复制结果
const copyResult = async () => {
  if (!currentRecord.value?.outputResult) return

  try {
    const text = formatJson(currentRecord.value.outputResult)
    await navigator.clipboard.writeText(text)
    ElMessage.success('复制成功')
  } catch {
    ElMessage.error('复制失败')
  }
}

// 使用此结果（跳转到AI工坊并带上结果）
const handleUseResult = () => {
  // 这里可以根据业务需求实现
  // 比如跳转到AI工坊页面，或者弹出选择任务的窗口
  ElMessage.info('该功能将在任务模块完成后实现')
  detailVisible.value = false
}

// 初始化
onMounted(() => {
  loadApplications()
  loadRecords()
})
</script>

<style scoped lang="scss">
.record-list-container {
  padding: 24px;
}

.page-header {
  margin-bottom: 24px;

  h2 {
    margin: 0 0 8px;
    font-size: 24px;
    font-weight: 600;
    color: #303133;
  }

  .subtitle {
    margin: 0;
    color: #909399;
    font-size: 14px;
  }
}

.search-bar {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 16px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);

  :deep(.el-form-item) {
    margin-bottom: 0;
    margin-right: 16px;
  }
}

.action-bar {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;

  .selected-info {
    color: #909399;
    font-size: 14px;
  }
}

.app-info {
  display: flex;
  align-items: center;
  gap: 8px;

  .app-name {
    font-weight: 500;
  }
}

.params-preview {
  color: #606266;
  font-family: 'Courier New', monospace;
  font-size: 12px;
  cursor: pointer;

  &:hover {
    color: #409eff;
  }
}

.running-text {
  color: #e6a23c;
  font-size: 12px;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
  padding: 16px 0;
}

// 详情弹窗样式
.record-detail {
  .detail-section {
    margin-top: 20px;

    h4 {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin: 0 0 12px;
      font-size: 14px;
      font-weight: 500;
      color: #303133;
    }

    :deep(.el-textarea__inner) {
      font-family: 'Courier New', monospace;
      font-size: 12px;
      background: #f5f7fa;
    }
  }
}
</style>
