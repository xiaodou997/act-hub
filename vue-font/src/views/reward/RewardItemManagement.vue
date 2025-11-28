<template>
  <div class="page-container">
    <div class="page-header">
      <h2>奖品库存管理</h2>
      <el-button type="primary" @click="openImport">批量导入</el-button>
    </div>
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="奖品ID">
          <el-input v-model="searchForm.rewardId" placeholder="输入奖品ID" clearable />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="searchForm.phone" placeholder="输入手机号" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    <el-card class="table-card" shadow="never">
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column type="index" width="60" label="#" />
        <el-table-column prop="userId" label="用户ID" min-width="140" />
        <el-table-column prop="phone" label="手机号" min-width="140" />
        <el-table-column prop="code" label="券码" min-width="200" show-overflow-tooltip />
        <el-table-column prop="trackingNo" label="快递单号" min-width="180" show-overflow-tooltip />
        <el-table-column prop="rewardId" label="奖品ID" min-width="160" />
        <el-table-column prop="createdAt" label="创建时间" width="180">
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
    <el-dialog v-model="importVisible" title="批量导入" width="720px" align-center>
      <el-form :model="importForm" label-width="100px">
        <el-form-item label="导入类型">
          <el-select v-model="importForm.type" placeholder="选择类型">
            <el-option label="券码" value="code" />
            <el-option label="快递单号" value="tracking" />
          </el-select>
        </el-form-item>
        <el-form-item label="奖品ID">
          <el-input v-model="importForm.rewardId" />
        </el-form-item>
        <el-form-item label="数据">
          <el-input
            v-model="importForm.content"
            type="textarea"
            :rows="8"
            placeholder="userid,phone,value 每行一条"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="importVisible = false">取消</el-button>
        <el-button type="primary" @click="handleImport">导入</el-button>
      </template>
    </el-dialog>
  </div>
</template>
<script setup>
import { ref, reactive, onMounted } from 'vue'
import dayjs from 'dayjs'
import { ElMessage } from 'element-plus'
import { rewardItemApi } from '@/api/rewardItem'

const loading = ref(false)
const tableData = ref([])
const importVisible = ref(false)

const searchForm = reactive({ rewardId: '', phone: '' })
const pagination = reactive({ pageNum: 1, pageSize: 10, total: 0 })

const importForm = reactive({ type: 'code', rewardId: '', content: '' })

const formatDate = (ts) => {
  if (!ts) return '-'
  return dayjs(ts).format('YYYY-MM-DD HH:mm:ss')
}

const loadData = async () => {
  loading.value = true
  try {
    const { records = [], total = 0 } = await rewardItemApi.getPage({
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      rewardId: searchForm.rewardId || undefined,
      phone: searchForm.phone || undefined,
    })
    tableData.value = records
    pagination.total = total
  } catch {
    ElMessage.error('获取列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.pageNum = 1
  loadData()
}

const handleReset = () => {
  searchForm.rewardId = ''
  searchForm.phone = ''
  pagination.pageNum = 1
  loadData()
}

const handleSizeChange = (size) => {
  pagination.pageSize = size
  loadData()
}

const handleCurrentChange = (page) => {
  pagination.pageNum = page
  loadData()
}

const openImport = () => {
  importForm.type = 'code'
  importForm.rewardId = ''
  importForm.content = ''
  importVisible.value = true
}

const parseContent = () => {
  const lines = (importForm.content || '')
    .split(/\n+/)
    .map((l) => l.trim())
    .filter(Boolean)
  return lines.map((line) => {
    const [userId, phone, value] = line.split(/[,\s]+/)
    return { userId, phone, value }
  })
}

const handleImport = async () => {
  try {
    const items = parseContent()
    const payload = {
      rewardId: importForm.rewardId,
      type: importForm.type,
      items,
    }
    await rewardItemApi.importItems(payload)
    ElMessage.success('导入成功')
    importVisible.value = false
    loadData()
  } catch {
    ElMessage.error('导入失败')
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
.search-card {
  margin-top: 16px;
}
.search-form {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}
.table-card {
  margin-top: 16px;
}
.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
