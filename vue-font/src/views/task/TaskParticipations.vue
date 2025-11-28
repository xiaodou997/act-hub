<template>
  <div class="participation-list-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="header-left">
        <el-button :icon="ArrowLeft" @click="goBack">返回</el-button>
        <h2>参与记录</h2>
        <span class="task-name" v-if="taskInfo">{{ taskInfo.taskName }}</span>
      </div>
      <div class="header-right">
        <el-button :icon="Download" @click="handleExport">导出</el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-cards">
      <div class="stat-card">
        <div class="stat-value">{{ statistics.total || 0 }}</div>
        <div class="stat-label">总参与</div>
      </div>
      <div class="stat-card">
        <div class="stat-value">{{ statistics.claimedCount || 0 }}</div>
        <div class="stat-label">已领取</div>
      </div>
      <div class="stat-card">
        <div class="stat-value">{{ statistics.pendingCount || 0 }}</div>
        <div class="stat-label">待审核</div>
      </div>
      <div class="stat-card success">
        <div class="stat-value">{{ statistics.approvedCount || 0 }}</div>
        <div class="stat-label">已通过</div>
      </div>
      <div class="stat-card danger">
        <div class="stat-value">{{ statistics.rejectedCount || 0 }}</div>
        <div class="stat-label">已拒绝</div>
      </div>
    </div>

    <!-- 搜索筛选 -->
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="状态">
          <el-select
            v-model="searchForm.status"
            placeholder="全部状态"
            clearable
            style="width: 140px"
          >
            <el-option label="已领取" :value="1" />
            <el-option label="已提交" :value="2" />
            <el-option label="审核通过" :value="3" />
            <el-option label="审核拒绝" :value="4" />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 批量操作 -->
    <div class="action-bar" v-if="selectedIds.length > 0">
      <span class="selected-info">已选择 {{ selectedIds.length }} 条记录</span>
      <el-button type="success" size="small" @click="handleBatchApprove">批量通过</el-button>
      <el-button type="danger" size="small" @click="handleBatchReject">批量拒绝</el-button>
    </div>

    <!-- 参与记录列表 -->
    <el-table
      :data="tableData"
      v-loading="loading"
      border
      stripe
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="50" />

      <el-table-column label="用户" width="150">
        <template #default="{ row }">
          <div class="user-info">
            <span class="user-name">{{ row.userName || row.userId }}</span>
          </div>
        </template>
      </el-table-column>

      <el-table-column label="提交链接" min-width="250">
        <template #default="{ row }">
          <template v-if="row.submittedLink">
            <el-link :href="row.submittedLink" target="_blank" type="primary">
              {{ truncateLink(row.submittedLink) }}
            </el-link>
          </template>
          <span v-else class="empty-text">未提交</span>
        </template>
      </el-table-column>

      <el-table-column label="提交时间" width="170">
        <template #default="{ row }">
          {{ formatDate(row.submittedAt) }}
        </template>
      </el-table-column>

      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)">
            {{ row.statusDesc }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column label="审核信息" width="200">
        <template #default="{ row }">
          <template v-if="row.status === 3 || row.status === 4">
            <div class="audit-info">
              <div>{{ formatDate(row.auditedAt) }}</div>
              <div v-if="row.auditNotes" class="audit-notes">{{ row.auditNotes }}</div>
            </div>
          </template>
          <span v-else>-</span>
        </template>
      </el-table-column>

      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button
            v-if="row.status === 2"
            type="primary"
            link
            size="small"
            @click="handleAudit(row)"
          >
            审核
          </el-button>
          <el-button type="primary" link size="small" @click="handleView(row)"> 详情 </el-button>
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

    <!-- 审核弹窗 -->
    <el-dialog v-model="auditVisible" title="审核" width="600px" destroy-on-close>
      <div class="audit-dialog" v-if="currentRecord">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="用户">
            {{ currentRecord.userName || currentRecord.userId }}
          </el-descriptions-item>
          <el-descriptions-item label="提交链接">
            <el-link :href="currentRecord.submittedLink" target="_blank" type="primary">
              {{ currentRecord.submittedLink }}
            </el-link>
          </el-descriptions-item>
          <el-descriptions-item label="提交时间">
            {{ formatDate(currentRecord.submittedAt) }}
          </el-descriptions-item>
        </el-descriptions>

        <!-- 页面快照预览 -->
        <div class="snapshot-preview" v-if="currentRecord.snapshotUrl">
          <h4>页面快照</h4>
          <el-image
            :src="currentRecord.snapshotUrl"
            fit="contain"
            :preview-src-list="[currentRecord.snapshotUrl]"
          />
        </div>

        <!-- 审核操作 -->
        <div class="audit-form">
          <h4>审核操作</h4>
          <el-radio-group v-model="auditForm.auditStatus">
            <el-radio :value="3">通过</el-radio>
            <el-radio :value="4">不通过</el-radio>
          </el-radio-group>

          <el-input
            v-if="auditForm.auditStatus === 4"
            v-model="auditForm.auditNotes"
            type="textarea"
            :rows="3"
            placeholder="请输入不通过理由（必填）"
            style="margin-top: 16px"
          />
        </div>
      </div>

      <template #footer>
        <el-button @click="auditVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAudit" :loading="auditLoading">
          确认提交
        </el-button>
      </template>
    </el-dialog>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="参与记录详情" width="600px" destroy-on-close>
      <ParticipationDetail v-if="detailVisible" :record="currentRecord" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Download, Search, Refresh } from '@element-plus/icons-vue'
import { taskApi, taskParticipationApi } from '@/api/task'
import dayjs from 'dayjs'
import ParticipationDetail from './ParticipationDetail.vue'

const route = useRoute()
const router = useRouter()

const taskId = computed(() => route.params.id)

// 状态
const loading = ref(false)
const auditVisible = ref(false)
const auditLoading = ref(false)
const detailVisible = ref(false)

// 任务信息
const taskInfo = ref(null)

// 统计数据
const statistics = reactive({
  total: 0,
  claimedCount: 0,
  pendingCount: 0,
  approvedCount: 0,
  rejectedCount: 0,
})

// 搜索
const searchForm = reactive({
  status: null,
})

// 分页
const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0,
})

// 数据
const tableData = ref([])
const selectedIds = ref([])
const currentRecord = ref(null)

// 审核表单
const auditForm = reactive({
  auditStatus: 3,
  auditNotes: '',
})

// 状态类型
const getStatusType = (status) => {
  const map = {
    1: 'info', // 已领取
    2: 'warning', // 已提交
    3: 'success', // 审核通过
    4: 'danger', // 审核拒绝
  }
  return map[status] || 'info'
}

// 格式化日期
const formatDate = (timestamp) => {
  if (!timestamp) return '-'
  return dayjs(timestamp).format('YYYY-MM-DD HH:mm:ss')
}

// 截断链接
const truncateLink = (link) => {
  if (!link) return ''
  return link.length > 50 ? link.substring(0, 50) + '...' : link
}

// 返回
const goBack = () => {
  router.push('/task')
}

// 加载任务信息
const loadTaskInfo = async () => {
  try {
    const data = await taskApi.getById(taskId.value)
    taskInfo.value = data
  } catch (error) {
    console.error('加载任务信息失败:', error)
  }
}

// 加载参与记录
const loadParticipations = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      taskId: taskId.value,
      status: searchForm.status ?? undefined,
    }

    const data = await taskParticipationApi.page(params)
    tableData.value = data?.records || []
    pagination.total = data?.total || 0
  } catch (error) {
    console.error('加载参与记录失败:', error)
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.pageNum = 1
  loadParticipations()
}

// 重置
const handleReset = () => {
  searchForm.status = null
  pagination.pageNum = 1
  loadParticipations()
}

// 分页
const handleSizeChange = (size) => {
  pagination.pageSize = size
  pagination.pageNum = 1
  loadParticipations()
}

const handleCurrentChange = (page) => {
  pagination.pageNum = page
  loadParticipations()
}

// 选择变化
const handleSelectionChange = (selection) => {
  selectedIds.value = selection
    .filter((item) => item.status === 2) // 只选择待审核的
    .map((item) => item.id)
}

// 审核
const handleAudit = (row) => {
  currentRecord.value = row
  auditForm.auditStatus = 3
  auditForm.auditNotes = ''
  auditVisible.value = true
}

// 提交审核
const submitAudit = async () => {
  if (auditForm.auditStatus === 4 && !auditForm.auditNotes?.trim()) {
    ElMessage.warning('请填写不通过理由')
    return
  }

  auditLoading.value = true
  try {
    await taskParticipationApi.audit(currentRecord.value.id, {
      auditStatus: auditForm.auditStatus,
      auditNotes: auditForm.auditNotes,
    })
    ElMessage.success('审核成功')
    auditVisible.value = false
    loadParticipations()
  } catch (error) {
    console.error('审核失败:', error)
    ElMessage.error('审核失败')
  } finally {
    auditLoading.value = false
  }
}

// 批量通过
const handleBatchApprove = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要批量通过选中的 ${selectedIds.value.length} 条记录吗？`,
      '提示',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' },
    )

    await taskParticipationApi.batchAudit(selectedIds.value, 3, '')
    ElMessage.success('批量通过成功')
    loadParticipations()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

// 批量拒绝
const handleBatchReject = async () => {
  try {
    const { value } = await ElMessageBox.prompt('请输入拒绝理由', '批量拒绝', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputValidator: (val) => !!val?.trim() || '请输入拒绝理由',
    })

    await taskParticipationApi.batchAudit(selectedIds.value, 4, value)
    ElMessage.success('批量拒绝成功')
    loadParticipations()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

// 查看详情
const handleView = (row) => {
  currentRecord.value = row
  detailVisible.value = true
}

// 导出
const handleExport = async () => {
  try {
    const response = await taskParticipationApi.export({
      taskId: taskId.value,
      status: searchForm.status,
    })

    const blob = new Blob([response], { type: 'text/csv;charset=utf-8' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `参与记录_${dayjs().format('YYYYMMDDHHmmss')}.csv`
    a.click()
    URL.revokeObjectURL(url)

    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败')
  }
}

// 初始化
onMounted(() => {
  loadTaskInfo()
  loadParticipations()
})
</script>

<style scoped lang="scss">
.participation-list-container {
  padding: 24px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;

  .header-left {
    display: flex;
    align-items: center;
    gap: 16px;

    h2 {
      margin: 0;
      font-size: 24px;
      font-weight: 600;
    }

    .task-name {
      color: #909399;
      font-size: 14px;
    }
  }
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 16px;
  margin-bottom: 24px;

  .stat-card {
    background: #fff;
    padding: 20px;
    border-radius: 8px;
    text-align: center;
    box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);

    .stat-value {
      font-size: 28px;
      font-weight: 600;
      color: #303133;
    }

    .stat-label {
      font-size: 14px;
      color: #909399;
      margin-top: 8px;
    }

    &.success .stat-value {
      color: #67c23a;
    }

    &.danger .stat-value {
      color: #f56c6c;
    }
  }
}

.search-card {
  margin-bottom: 16px;
}

.action-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  padding: 12px 16px;
  background: #fdf6ec;
  border-radius: 8px;

  .selected-info {
    color: #e6a23c;
    font-weight: 500;
  }
}

.user-info {
  .user-name {
    font-weight: 500;
  }
}

.empty-text {
  color: #c0c4cc;
}

.audit-info {
  font-size: 12px;
  color: #909399;

  .audit-notes {
    margin-top: 4px;
    color: #606266;
  }
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

// 审核弹窗
.audit-dialog {
  .snapshot-preview {
    margin-top: 20px;

    h4 {
      margin: 0 0 12px;
      font-size: 14px;
      font-weight: 500;
    }

    .el-image {
      max-height: 300px;
      border: 1px solid #ebeef5;
      border-radius: 8px;
    }
  }

  .audit-form {
    margin-top: 20px;

    h4 {
      margin: 0 0 12px;
      font-size: 14px;
      font-weight: 500;
    }
  }
}
</style>
