<template>
  <div class="system-log-container">
    <!-- 页面标题与操作区 -->
    <div class="page-header">
      <h2>系统日志管理</h2>
    </div>

    <!-- 搜索区域 -->
    <div class="search-container">
      <SimpleQueryFilter v-model="searchForm" @search="handleSearch" @reset="handleReset">
        <!-- 筛选项 -->
        <template #filters>
          <el-form-item label="操作人">
            <el-input
              v-model="searchForm.operatorUserName"
              placeholder="请输入操作人姓名"
              clearable
              style="width: 200px"
            />
          </el-form-item>
          <el-form-item label="租户名称">
            <el-input
              v-model="searchForm.operatorTenantName"
              placeholder="请输入租户名称"
              clearable
              style="width: 200px"
            />
          </el-form-item>
          <el-form-item label="功能模块">
            <el-input
              v-model="searchForm.module"
              placeholder="请输入功能模块名称"
              clearable
              style="width: 200px"
            />
          </el-form-item>
          <el-form-item label="操作动作">
            <el-input
              v-model="searchForm.action"
              placeholder="请输入操作动作"
              clearable
              style="width: 200px"
            />
          </el-form-item>
          <el-form-item label="日志级别">
            <el-select
              v-model="searchForm.logLevel"
              placeholder="请选择日志级别"
              clearable
              style="width: 200px"
            >
              <el-option label="DEBUG" value="DEBUG" />
              <el-option label="INFO" value="INFO" />
              <el-option label="WARN" value="WARN" />
              <el-option label="ERROR" value="ERROR" />
              <el-option label="AUDIT" value="AUDIT" />
            </el-select>
          </el-form-item>
          <el-form-item label="操作状态">
            <el-select
              v-model="searchForm.success"
              placeholder="请选择操作状态"
              clearable
              style="width: 200px"
            >
              <el-option label="成功" :value="1" />
              <el-option label="失败" :value="0" />
            </el-select>
          </el-form-item>
          <el-form-item label="操作IP">
            <el-input
              v-model="searchForm.ipAddress"
              placeholder="请输入IP地址"
              clearable
              style="width: 200px"
            />
          </el-form-item>
          <el-form-item label="操作时间">
            <el-date-picker
              v-model="dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
              style="width: 320px"
            />
          </el-form-item>
        </template>
      </SimpleQueryFilter>
    </div>

    <!-- 数据表格区 -->
    <el-card class="table-card" shadow="hover">
      <div class="table-empty" v-if="!tableData.length && !loading">
        <el-empty description="暂无日志数据"></el-empty>
      </div>

      <el-table
        v-else
        :data="tableData"
        v-loading="loading"
        style="width: 100%"
        stripe
        highlight-current-row
      >
        <el-table-column type="index" width="50" align="center" />
        <el-table-column prop="operatorUserName" label="操作人" min-width="120" />
        <el-table-column prop="operatorTenantName" label="租户名称" min-width="150" />
        <el-table-column prop="logLevel" label="日志级别" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getLogLevelTagType(row.logLevel)" effect="light">
              {{ row.logLevel }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="module" label="功能模块" min-width="120" />
        <el-table-column prop="action" label="操作动作" min-width="120" />
        <el-table-column
          prop="description"
          label="操作描述"
          min-width="200"
          show-overflow-tooltip
        />
        <el-table-column prop="success" label="操作状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.success === 1 ? 'success' : 'danger'">
              {{ row.success === 1 ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="ipAddress" label="IP地址" min-width="150" />
        <el-table-column prop="costTimeMs" label="耗时(ms)" width="100" align="center">
          <template #default="{ row }">
            <span v-if="row.costTimeMs">{{ row.costTimeMs }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="操作时间" width="180" align="center">
          <template #default="{ row }">
            <span>{{ formatDate(row.createdAt) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" :icon="View" size="small" @click="handleView(row)" link>
              查看
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.pageNum"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 查看详情侧边栏 -->
    <el-drawer v-model="detailDrawerVisible" title="系统日志详情" size="50%">
      <SystemLogDetailView :log-data="detailData" @close="detailDrawerVisible = false" />
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { View } from '@element-plus/icons-vue'
import SimpleQueryFilter from '@/components/common/SimpleQueryFilter.vue'
import SystemLogDetailView from './SystemLogDetailView.vue'
import { systemLogApi } from '@/api/systemLog'
import dayjs from 'dayjs'

// 响应式数据
const loading = ref(false)
const tableData = ref([])
const detailDrawerVisible = ref(false)
const detailData = ref({})
const dateRange = ref([])

// 搜索表单
const searchForm = reactive({
  operatorUserName: '',
  operatorTenantName: '',
  module: '',
  action: '',
  logLevel: '',
  success: undefined,
  ipAddress: '',
  createdAtStart: undefined,
  createdAtEnd: undefined,
})

// 分页数据
const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0,
})

// 格式化日期
const formatDate = (timestamp) => {
  if (!timestamp) return '-'
  return dayjs(timestamp).format('YYYY-MM-DD HH:mm:ss')
}

// 获取日志级别对应的标签类型
const getLogLevelTagType = (logLevel) => {
  const levelMap = {
    DEBUG: 'info',
    INFO: 'primary',
    WARN: 'warning',
    ERROR: 'danger',
    AUDIT: 'success',
  }
  return levelMap[logLevel] || 'info'
}

// 加载日志数据
const loadLogData = async () => {
  loading.value = true
  try {
    // 处理日期范围
    if (dateRange.value && dateRange.value.length === 2) {
      searchForm.createdAtStart = dayjs(dateRange.value[0]).startOf('day').valueOf()
      searchForm.createdAtEnd = dayjs(dateRange.value[1]).endOf('day').valueOf()
    } else {
      searchForm.createdAtStart = undefined
      searchForm.createdAtEnd = undefined
    }

    const params = {
      ...searchForm,
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
    }

    const result = await systemLogApi.getPage(params)
    tableData.value = result.records || []
    pagination.total = result.total || 0
  } catch (error) {
    ElMessage.error('获取日志数据失败')
    console.error('获取日志数据失败:', error)
  } finally {
    loading.value = false
  }
}

// 处理搜索
const handleSearch = () => {
  pagination.pageNum = 1
  loadLogData()
}

// 处理重置
const handleReset = () => {
  Object.keys(searchForm).forEach((key) => {
    searchForm[key] = undefined
  })
  dateRange.value = []
  pagination.pageNum = 1
  pagination.pageSize = 10
}

// 处理分页大小变化
const handleSizeChange = (size) => {
  pagination.pageSize = size
  loadLogData()
}

// 处理当前页变化
const handleCurrentChange = (current) => {
  pagination.pageNum = current
  loadLogData()
}

// 查看详情
const handleView = (rowData) => {
  // 直接使用表格行数据，避免再次调用接口
  detailData.value = { ...rowData }
  detailDrawerVisible.value = true
}

// 组件挂载时加载数据
onMounted(() => {
  loadLogData()
})
</script>

<style scoped lang="scss">
.system-log-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.table-card {
  margin-top: 20px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

/* 优化表格样式 */
:deep(.el-table) {
  .el-table__header-wrapper {
    th {
      background-color: #f5f7fa;
      font-weight: 500;
    }
  }

  .el-table__body-wrapper {
    .el-table__row:hover {
      background-color: #f5f7fa;
    }
  }
}
</style>
