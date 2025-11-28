<template>
  <div class="page-container">
    <div class="page-header">
      <h2>埋点事件查询</h2>
    </div>
    <el-card class="table-card" shadow="never">
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column type="index" width="60" label="#" />
        <el-table-column prop="eventName" label="事件名" width="200" />
        <el-table-column prop="userId" label="用户ID" width="160" />
        <el-table-column prop="tenantId" label="租户ID" width="120" />
        <el-table-column prop="properties" label="属性" min-width="280" show-overflow-tooltip />
        <el-table-column prop="createdAt" label="时间" width="180">
          <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
        </el-table-column>
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
import { trackingEventApi } from '@/api/trackingEvent'

const loading = ref(false)
const tableData = ref([])
const pagination = reactive({ pageNum: 1, pageSize: 10, total: 0 })

const formatDate = (ts) => { if (!ts) return '-'; return dayjs(ts).format('YYYY-MM-DD HH:mm:ss') }

const loadData = async () => {
  loading.value = true
  try {
    const { records = [], total = 0 } = await trackingEventApi.getPage({ pageNum: pagination.pageNum, pageSize: pagination.pageSize })
    tableData.value = records
    pagination.total = total
  } catch { ElMessage.error('获取事件失败') } finally { loading.value = false }
}

const handleSizeChange = (s) => { pagination.pageSize = s; loadData() }
const handleCurrentChange = (p) => { pagination.pageNum = p; loadData() }

onMounted(loadData)
</script>
<style scoped lang="scss">
.page-header{display:flex;justify-content:space-between;align-items:center}
.table-card{margin-top:16px}
.pagination-container{margin-top:20px;display:flex;justify-content:flex-end}
</style>

