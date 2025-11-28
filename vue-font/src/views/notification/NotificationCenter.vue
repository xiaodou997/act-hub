<template>
  <div class="page-container">
    <div class="page-header">
      <h2>消息通知</h2>
    </div>
    <el-card class="table-card" shadow="never">
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column type="index" width="60" label="#" />
        <el-table-column prop="title" label="标题" min-width="220" show-overflow-tooltip />
        <el-table-column prop="content" label="内容" min-width="280" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="row.read ? 'success' : 'info'">{{ row.read ? '已读' : '未读' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="时间" width="180">
          <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="markRead(row)" :disabled="row.read"
              >标记已读</el-button
            >
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import dayjs from 'dayjs'
import { ElMessage } from 'element-plus'
import { notificationApi } from '@/api/notification'

const loading = ref(false)
const tableData = ref([])

const formatDate = (ts) => {
  if (!ts) return '-'
  return dayjs(ts).format('YYYY-MM-DD HH:mm:ss')
}

const loadData = async () => {
  loading.value = true
  try {
    const { records = [] } = await notificationApi.getPage({ pageNum: 1, pageSize: 100 })
    tableData.value = records
  } catch {
    ElMessage.error('获取通知失败')
  } finally {
    loading.value = false
  }
}

const markRead = async (row) => {
  try {
    await notificationApi.markRead(row.id)
    ElMessage.success('已标记')
    loadData()
  } catch {
    ElMessage.error('操作失败')
  }
}

onMounted(loadData)
</script>
<style scoped lang="scss">
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.table-card {
  margin-top: 16px;
}
</style>
