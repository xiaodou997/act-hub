<template>
  <div class="page-container">
    <div class="page-header">
      <h2>教程文章管理</h2>
      <el-button type="primary" @click="openCreate">新建文章</el-button>
    </div>
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="标题">
          <el-input v-model="searchForm.title" placeholder="输入标题" clearable />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="searchForm.categoryId" clearable placeholder="选择分类">
            <el-option v-for="c in categoryOptions" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
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
        <el-table-column prop="title" label="标题" min-width="220" show-overflow-tooltip />
        <el-table-column prop="categoryId" label="分类" width="160">
          <template #default="{ row }">{{ getCategoryName(row.categoryId) }}</template>
        </el-table-column>
        <el-table-column prop="updatedAt" label="更新时间" width="180">
          <template #default="{ row }">{{ formatDate(row.updatedAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
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
      width="820px"
      align-center
      destroy-on-close
    >
      <el-form :model="formData" label-width="100px">
        <el-form-item label="标题">
          <el-input v-model="formData.title" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="formData.categoryId" placeholder="选择分类">
            <el-option v-for="c in categoryOptions" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="formData.content" type="textarea" :rows="10" />
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
import { ref, reactive, onMounted } from 'vue'
import dayjs from 'dayjs'
import { ElMessage, ElMessageBox } from 'element-plus'
import { articleApi } from '@/api/article'
import { articleCategoryApi } from '@/api/articleCategory'

const loading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('新建文章')
const formData = reactive({})

const searchForm = reactive({ title: '', categoryId: '' })
const pagination = reactive({ pageNum: 1, pageSize: 10, total: 0 })
const categoryOptions = ref([])

const formatDate = (ts) => {
  if (!ts) return '-'
  return dayjs(ts).format('YYYY-MM-DD HH:mm:ss')
}

const getCategoryName = (id) => {
  const item = categoryOptions.value.find((c) => c.id === id)
  return item ? item.name : '-'
}

const loadCategories = async () => {
  try {
    const tree = await articleCategoryApi.getTree()
    const flatten = []
    const walk = (nodes) =>
      nodes.forEach((n) => {
        flatten.push({ id: n.id, name: n.name })
        if (n.children) walk(n.children)
      })
    walk(tree || [])
    categoryOptions.value = flatten
  } catch {}
}

const loadData = async () => {
  loading.value = true
  try {
    const { records = [], total = 0 } = await articleApi.getPage({
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      title: searchForm.title || undefined,
      categoryId: searchForm.categoryId || undefined,
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
  searchForm.title = ''
  searchForm.categoryId = ''
  pagination.pageNum = 1
  loadData()
}
const handleSizeChange = (s) => {
  pagination.pageSize = s
  loadData()
}
const handleCurrentChange = (p) => {
  pagination.pageNum = p
  loadData()
}

const openCreate = () => {
  dialogTitle.value = '新建文章'
  Object.assign(formData, { id: '', title: '', categoryId: '', content: '' })
  dialogVisible.value = true
}

const openEdit = async (row) => {
  dialogTitle.value = '编辑文章'
  const res = await articleApi.getById(row.id)
  Object.assign(formData, res || {})
  dialogVisible.value = true
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定删除文章"${row.title}"吗？`, '确认删除', { type: 'warning' }).then(
    async () => {
      try {
        await articleApi.delete(row.id)
        ElMessage.success('删除成功')
        loadData()
      } catch {
        ElMessage.error('删除失败')
      }
    }
  )
}

const handleSubmit = async () => {
  try {
    if (formData.id) {
      await articleApi.update(formData)
      ElMessage.success('更新成功')
    } else {
      await articleApi.create(formData)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadData()
  } catch {
    ElMessage.error('提交失败')
  }
}

onMounted(async () => {
  await loadCategories()
  await loadData()
})
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
