<template>
  <div class="page-container">
    <div class="page-header">
      <h2>黑名单管理</h2>
      <el-button type="primary" @click="openAdd">添加黑名单</el-button>
    </div>
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="用户ID">
          <el-input v-model="searchForm.userId" clearable />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="searchForm.phone" clearable />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="searchForm.nickname" clearable />
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
        <el-table-column prop="userId" label="用户ID" width="160" />
        <el-table-column prop="phone" label="手机号" width="160" />
        <el-table-column prop="nickname" label="昵称" min-width="160" />
        <el-table-column prop="createdAt" label="加入时间" width="180">
          <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button type="danger" link size="small" @click="handleRemove(row)">移除</el-button>
          </template>
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
    <el-dialog v-model="dialogVisible" title="添加黑名单" width="520px" align-center>
      <el-form :model="formData" label-width="90px">
        <el-form-item label="用户ID">
          <el-input v-model="formData.userId" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="formData.phone" />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="formData.nickname" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAdd">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>
<script setup>
import { ref, reactive, onMounted } from 'vue'
import dayjs from 'dayjs'
import { ElMessage } from 'element-plus'
import { blacklistApi } from '@/api/blacklist'

const loading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const formData = reactive({ userId: '', phone: '', nickname: '' })

const searchForm = reactive({ userId: '', phone: '', nickname: '' })
const pagination = reactive({ pageNum: 1, pageSize: 10, total: 0 })

const formatDate = (ts) => { if (!ts) return '-'; return dayjs(ts).format('YYYY-MM-DD HH:mm:ss') }

const loadData = async () => {
  loading.value = true
  try {
    const { records = [], total = 0 } = await blacklistApi.getPage({
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      userId: searchForm.userId || undefined,
      phone: searchForm.phone || undefined,
      nickname: searchForm.nickname || undefined,
    })
    tableData.value = records
    pagination.total = total
  } catch { ElMessage.error('获取列表失败') } finally { loading.value = false }
}

const handleSearch = () => { pagination.pageNum = 1; loadData() }
const handleReset = () => { Object.assign(searchForm, { userId: '', phone: '', nickname: '' }); pagination.pageNum = 1; loadData() }
const handleSizeChange = (s) => { pagination.pageSize = s; loadData() }
const handleCurrentChange = (p) => { pagination.pageNum = p; loadData() }

const openAdd = () => { Object.assign(formData, { userId: '', phone: '', nickname: '' }); dialogVisible.value = true }
const handleAdd = async () => { try { await blacklistApi.add(formData); ElMessage.success('添加成功'); dialogVisible.value = false; loadData() } catch { ElMessage.error('添加失败') } }
const handleRemove = async (row) => { try { await blacklistApi.remove(row.id); ElMessage.success('移除成功'); loadData() } catch { ElMessage.error('移除失败') } }

onMounted(loadData)
</script>
<style scoped lang="scss">
.page-header{display:flex;justify-content:space-between;align-items:center}
.search-card{margin-top:16px}
.search-form{display:flex;align-items:center;gap:16px;flex-wrap:wrap}
.table-card{margin-top:16px}
.pagination-container{margin-top:20px;display:flex;justify-content:flex-end}
</style>

