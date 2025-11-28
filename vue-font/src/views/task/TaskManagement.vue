<template>
  <div class="task-management-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>任务管理</h2>
      <el-button type="primary" :icon="Plus" @click="handleCreate">创建任务</el-button>
    </div>

    <!-- 搜索筛选区域 -->
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="任务名称">
          <el-input
            v-model="searchForm.taskName"
            placeholder="请输入任务名称"
            clearable
            style="width: 200px"
          />
        </el-form-item>

        <el-form-item label="任务状态">
          <el-select
            v-model="searchForm.status"
            placeholder="全部状态"
            clearable
            style="width: 140px"
          >
            <el-option label="草稿" :value="0" />
            <el-option label="上线" :value="1" />
            <el-option label="下线" :value="2" />
            <el-option label="已结束" :value="3" />
          </el-select>
        </el-form-item>

        <el-form-item label="发布平台">
          <el-select
            v-model="searchForm.platform"
            placeholder="全部平台"
            clearable
            style="width: 140px"
          >
            <el-option label="小红书" value="XIAOHONGSHU" />
            <el-option label="抖音" value="DOUYIN" />
            <el-option label="快手" value="KUAISHOU" />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 任务列表 -->
    <el-card class="table-card" shadow="never">
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column type="index" width="60" label="#" />

        <el-table-column prop="taskName" label="任务名称" min-width="200" show-overflow-tooltip />

        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ row.statusDesc }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="发布平台" width="150">
          <template #default="{ row }">
            <template v-if="row.platforms && row.platforms.length > 0">
              <el-tag
                v-for="platform in row.platforms"
                :key="platform"
                size="small"
                style="margin-right: 4px"
              >
                {{ getPlatformName(platform) }}
              </el-tag>
            </template>
            <span v-else>-</span>
          </template>
        </el-table-column>

        <el-table-column label="完成进度" width="120" align="center">
          <template #default="{ row }">
            <span class="progress-text">
              {{ row.completedCount || 0 }} / {{ row.totalQuota || 0 }}
            </span>
            <el-progress
              :percentage="getProgressPercentage(row)"
              :stroke-width="4"
              :show-text="false"
              style="margin-top: 4px"
            />
          </template>
        </el-table-column>

        <el-table-column label="任务奖励" width="100" align="center">
          <template #default="{ row }">
            <span class="reward-text">{{ row.rewardAmount || 0 }}元/条</span>
          </template>
        </el-table-column>

        <el-table-column label="任务时间" width="180">
          <template #default="{ row }">
            <div class="time-range">
              <div>{{ formatDate(row.startTime) }}</div>
              <div>至 {{ formatDate(row.endTime) }}</div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleView(row)"> 详情 </el-button>
            <el-button type="primary" link size="small" @click="handleParticipations(row)">
              参与记录
            </el-button>
            <el-button
              v-if="row.status === 0 || row.status === 2"
              type="primary"
              link
              size="small"
              @click="handleEdit(row)"
            >
              编辑
            </el-button>
            <el-dropdown @command="(cmd) => handleStatusChange(row, cmd)">
              <el-button type="primary" link size="small">
                更多<el-icon class="el-icon--right"><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item v-if="row.status === 0" command="online">
                    上线
                  </el-dropdown-item>
                  <el-dropdown-item v-if="row.status === 1" command="offline">
                    下线
                  </el-dropdown-item>
                  <el-dropdown-item v-if="row.status === 2" command="online">
                    重新上线
                  </el-dropdown-item>
                  <el-dropdown-item v-if="row.status === 1" command="end">
                    结束任务
                  </el-dropdown-item>
                  <el-dropdown-item v-if="row.status === 0" command="delete" divided>
                    <span style="color: #f56c6c">删除</span>
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container" v-if="pagination.total > 0">
        <el-pagination
          v-model:current-page="pagination.pageNum"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="pagination.total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 任务表单弹窗 -->
    <el-dialog
      v-model="formVisible"
      :title="isEdit ? '编辑任务' : '创建任务'"
      width="800px"
      destroy-on-close
    >
      <TaskForm
        v-if="formVisible"
        :task-data="currentTask"
        :is-edit="isEdit"
        @submit="handleFormSubmit"
        @cancel="formVisible = false"
      />
    </el-dialog>

    <!-- 任务详情弹窗 -->
    <el-dialog v-model="detailVisible" title="任务详情" width="700px" destroy-on-close>
      <TaskDetail v-if="detailVisible" :task-id="currentTaskId" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh, ArrowDown } from '@element-plus/icons-vue'
import { taskApi } from '@/api/task'
import dayjs from 'dayjs'
import TaskForm from './components/TaskForm.vue'
import TaskDetail from './components/TaskDetail.vue'

const router = useRouter()

// 加载状态
const loading = ref(false)

// 搜索表单
const searchForm = reactive({
  taskName: '',
  status: null,
  platform: '',
})

// 分页
const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0,
})

// 数据
const tableData = ref([])

// 表单弹窗
const formVisible = ref(false)
const isEdit = ref(false)
const currentTask = ref({})

// 详情弹窗
const detailVisible = ref(false)
const currentTaskId = ref('')

// 状态类型映射
const getStatusType = (status) => {
  const map = {
    0: 'info', // 草稿
    1: 'success', // 上线
    2: 'warning', // 下线
    3: 'danger', // 已结束
  }
  return map[status] || 'info'
}

// 平台名称映射
const getPlatformName = (platform) => {
  const map = {
    XIAOHONGSHU: '小红书',
    DOUYIN: '抖音',
    KUAISHOU: '快手',
  }
  return map[platform] || platform
}

// 计算进度百分比
const getProgressPercentage = (row) => {
  if (!row.totalQuota || row.totalQuota === 0) return 0
  return Math.round(((row.completedCount || 0) / row.totalQuota) * 100)
}

// 格式化日期
const formatDate = (timestamp) => {
  if (!timestamp) return '-'
  return dayjs(timestamp).format('YYYY-MM-DD HH:mm')
}

// 加载任务列表
const loadTasks = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      taskName: searchForm.taskName || undefined,
      status: searchForm.status ?? undefined,
      platform: searchForm.platform || undefined,
    }

    const data = await taskApi.page(params)
    tableData.value = data?.records || []
    pagination.total = data?.total || 0
  } catch (error) {
    console.error('加载任务列表失败:', error)
    ElMessage.error('加载任务列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.pageNum = 1
  loadTasks()
}

// 重置
const handleReset = () => {
  searchForm.taskName = ''
  searchForm.status = null
  searchForm.platform = ''
  pagination.pageNum = 1
  loadTasks()
}

// 分页变化
const handleSizeChange = (size) => {
  pagination.pageSize = size
  pagination.pageNum = 1
  loadTasks()
}

const handleCurrentChange = (page) => {
  pagination.pageNum = page
  loadTasks()
}

// 创建任务
const handleCreate = () => {
  isEdit.value = false
  currentTask.value = {}
  formVisible.value = true
}

// 编辑任务
const handleEdit = async (row) => {
  isEdit.value = true
  try {
    const data = await taskApi.getById(row.id)
    currentTask.value = data || row
    formVisible.value = true
  } catch (error) {
    console.error('获取任务详情失败:', error)
    currentTask.value = row
    formVisible.value = true
  }
}

// 查看详情
const handleView = (row) => {
  currentTaskId.value = row.id
  detailVisible.value = true
}

// 查看参与记录
const handleParticipations = (row) => {
  router.push(`/task/${row.id}/participations`)
}

// 状态变更
const handleStatusChange = async (row, command) => {
  if (command === 'delete') {
    handleDelete(row)
    return
  }

  const statusMap = {
    online: 1,
    offline: 2,
    end: 3,
  }

  const actionMap = {
    online: '上线',
    offline: '下线',
    end: '结束',
  }

  try {
    await ElMessageBox.confirm(`确定要${actionMap[command]}任务"${row.taskName}"吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })

    await taskApi.changeStatus(row.id, statusMap[command])
    ElMessage.success(`${actionMap[command]}成功`)
    loadTasks()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('状态变更失败:', error)
      ElMessage.error('操作失败')
    }
  }
}

// 删除任务
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除任务"${row.taskName}"吗？此操作不可恢复。`, '警告', {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning',
      confirmButtonClass: 'el-button--danger',
    })

    await taskApi.delete(row.id)
    ElMessage.success('删除成功')
    loadTasks()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 表单提交
const handleFormSubmit = async (formData) => {
  try {
    if (isEdit.value) {
      await taskApi.update(currentTask.value.id, formData)
      ElMessage.success('更新成功')
    } else {
      await taskApi.create(formData)
      ElMessage.success('创建成功')
    }
    formVisible.value = false
    loadTasks()
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error('保存失败')
  }
}

// 初始化
onMounted(() => {
  loadTasks()
})
</script>

<style scoped lang="scss">
.task-management-container {
  padding: 24px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;

  h2 {
    margin: 0;
    font-size: 24px;
    font-weight: 600;
    color: #303133;
  }
}

.search-card {
  margin-bottom: 16px;
}

.search-form {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
}

.table-card {
  // 表格卡片样式
}

.progress-text {
  font-size: 12px;
  color: #606266;
}

.reward-text {
  color: #e6a23c;
  font-weight: 500;
}

.time-range {
  font-size: 12px;
  color: #909399;
  line-height: 1.6;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
