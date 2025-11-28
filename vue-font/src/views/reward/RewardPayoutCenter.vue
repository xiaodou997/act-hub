<template>
  <div class="page-container">
    <div class="page-header">
      <h2>奖励发放中心</h2>
    </div>
    <el-card shadow="never">
      <el-form :model="form" label-width="120px">
        <el-form-item label="任务ID">
          <el-input v-model="form.taskId" placeholder="输入任务ID" />
        </el-form-item>
        <el-form-item label="发放范围">
          <el-select v-model="form.scope" placeholder="选择范围">
            <el-option label="审核通过" value="approved" />
            <el-option label="全部参与" value="all" />
          </el-select>
        </el-form-item>
        <el-form-item label="奖品ID">
          <el-input v-model="form.rewardId" placeholder="输入奖品ID" />
        </el-form-item>
        <el-form-item label="平台">
          <el-select v-model="form.platform" placeholder="选择平台" clearable>
            <el-option label="小红书" value="xhs" />
            <el-option label="抖音" value="douyin" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleDistribute" :loading="submitting">确认发放</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    <el-card class="table-card" shadow="never" style="margin-top:16px">
      <div style="margin-bottom:12px">发放记录</div>
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column type="index" width="60" label="#" />
        <el-table-column prop="taskId" label="任务ID" width="160" />
        <el-table-column prop="userId" label="用户ID" width="160" />
        <el-table-column prop="rewardId" label="奖品ID" width="160" />
        <el-table-column prop="status" label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : row.status === 2 ? 'danger' : 'info'">
              {{ row.status === 1 ? '已发放' : row.status === 2 ? '失败' : '待发放' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="issuedAt" label="发放时间" width="180">
          <template #default="{ row }">{{ formatDate(row.issuedAt) }}</template>
        </el-table-column>
        <el-table-column prop="failReason" label="失败原因" min-width="200" show-overflow-tooltip />
      </el-table>
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
  </div>
</template>
<script setup>
import { ref, reactive, onMounted } from 'vue'
import dayjs from 'dayjs'
import { ElMessage } from 'element-plus'
import { rewardPayoutApi } from '@/api/rewardPayout'

const form = reactive({ taskId: '', scope: 'approved', rewardId: '', platform: '' })
const submitting = ref(false)

const loading = ref(false)
const tableData = ref([])
const pagination = reactive({ pageNum: 1, pageSize: 10, total: 0 })

const formatDate = (ts) => {
  if (!ts) return '-'
  return dayjs(ts).format('YYYY-MM-DD HH:mm:ss')
}

const loadRecords = async () => {
  loading.value = true
  try {
    const { records = [], total = 0 } = await rewardPayoutApi.getPage({
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      taskId: form.taskId || undefined,
      rewardId: form.rewardId || undefined,
    })
    tableData.value = records
    pagination.total = total
  } catch {
    ElMessage.error('获取记录失败')
  } finally {
    loading.value = false
  }
}

const handleDistribute = async () => {
  submitting.value = true
  try {
    const payload = { taskId: form.taskId, rewardId: form.rewardId, scope: form.scope, platform: form.platform }
    await rewardPayoutApi.distribute(payload)
    ElMessage.success('已提交发放')
    loadRecords()
  } catch {
    ElMessage.error('发放失败')
  } finally {
    submitting.value = false
  }
}

const handleSizeChange = (size) => {
  pagination.pageSize = size
  loadRecords()
}

const handleCurrentChange = (page) => {
  pagination.pageNum = page
  loadRecords()
}

onMounted(loadRecords)
</script>
<style scoped lang="scss">
.page-header{display:flex;justify-content:space-between;align-items:center}
.table-card{margin-top:16px}
.pagination-container{margin-top:20px;display:flex;justify-content:flex-end}
</style>

