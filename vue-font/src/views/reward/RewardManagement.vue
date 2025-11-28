<template>
  <div class="page-container">
    <div class="page-header">
      <h2>奖品管理</h2>
      <el-button type="primary" @click="openCreate">新建奖品</el-button>
    </div>
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="奖品名称">
          <el-input v-model="searchForm.name" placeholder="输入名称" clearable />
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
        <el-table-column prop="name" label="奖品名称" min-width="180" show-overflow-tooltip />
        <el-table-column label="图片" width="120">
          <template #default="{ row }">
            <el-image
              v-if="row.imageUrl"
              :src="row.imageUrl"
              fit="cover"
              style="width: 64px; height: 64px"
            />
          </template>
        </el-table-column>
        <el-table-column prop="quantity" label="数量" width="100" />
        <el-table-column prop="description" label="说明" min-width="240" show-overflow-tooltip />
        <el-table-column prop="rules" label="使用规则" min-width="240" show-overflow-tooltip />
        <el-table-column prop="validFrom" label="开始" width="160">
          <template #default="{ row }">{{ formatDate(row.validFrom) }}</template>
        </el-table-column>
        <el-table-column prop="validTo" label="结束" width="160">
          <template #default="{ row }">{{ formatDate(row.validTo) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="openEdit(row)">编辑</el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
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
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="720px"
      align-center
      destroy-on-close
    >
      <el-form :model="formData" label-width="100px">
        <el-form-item label="奖品名称">
          <el-input v-model="formData.name" />
        </el-form-item>
        <el-form-item label="奖品图片">
          <ImageUploader v-model:url="formData.imageUrl" />
        </el-form-item>
        <el-form-item label="奖品数量">
          <el-input-number v-model="formData.quantity" :min="0" />
        </el-form-item>
        <el-form-item label="奖品说明">
          <el-input v-model="formData.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="使用规则">
          <el-input v-model="formData.rules" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="有效期">
          <el-date-picker
            v-model="dateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始"
            end-placeholder="结束"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>
<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import dayjs from 'dayjs'
import { ElMessage, ElMessageBox } from 'element-plus'
import { rewardApi } from '@/api/reward'
import ImageUploader from '@/components/ImageUploader.vue'

const loading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('新建奖品')
const formData = reactive({})
const dateRange = ref([])

const searchForm = reactive({ name: '' })

const pagination = reactive({ pageNum: 1, pageSize: 10, total: 0 })

const formatDate = (ts) => {
  if (!ts) return '-'
  return dayjs(ts).format('YYYY-MM-DD HH:mm')
}

const loadData = async () => {
  loading.value = true
  try {
    const { records = [], total = 0 } = await rewardApi.getPage({
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      name: searchForm.name || undefined,
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
  searchForm.name = ''
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

const openCreate = () => {
  dialogTitle.value = '新建奖品'
  Object.assign(formData, {
    id: '',
    name: '',
    imageUrl: '',
    quantity: 0,
    description: '',
    rules: '',
    validFrom: null,
    validTo: null,
  })
  dateRange.value = []
  dialogVisible.value = true
}

const openEdit = async (row) => {
  dialogTitle.value = '编辑奖品'
  const res = await rewardApi.getById(row.id)
  Object.assign(formData, res || {})
  dateRange.value = [
    res?.validFrom ? new Date(res.validFrom) : null,
    res?.validTo ? new Date(res.validTo) : null,
  ]
  dialogVisible.value = true
}

watch(dateRange, (val) => {
  if (val && val.length === 2) {
    formData.validFrom = val[0]?.getTime() || null
    formData.validTo = val[1]?.getTime() || null
  }
})

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定删除奖品"${row.name}"吗？`, '确认删除', {
    type: 'warning',
  }).then(async () => {
    try {
      await rewardApi.delete(row.id)
      ElMessage.success('删除成功')
      loadData()
    } catch {
      ElMessage.error('删除失败')
    }
  })
}

const handleSubmit = async () => {
  try {
    if (formData.id) {
      await rewardApi.update(formData)
      ElMessage.success('更新成功')
    } else {
      await rewardApi.create(formData)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadData()
  } catch {
    ElMessage.error('提交失败')
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
