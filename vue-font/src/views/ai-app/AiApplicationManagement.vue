<template>
  <div class="ai-app-management-container">
    <div class="page-header">
      <h2>AI应用管理</h2>
      <el-button type="primary" :icon="Plus" @click="handleCreate">新建应用</el-button>
    </div>

    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="应用名称">
          <el-input v-model="searchForm.name" placeholder="请输入应用名称" clearable />
        </el-form-item>
        <el-form-item label="应用类型">
          <AiAppTypeSelect v-model="searchForm.typeId" style="min-width: 280px" />
        </el-form-item>
        <el-form-item label="是否启用">
          <el-select v-model="searchForm.enabled" placeholder="全部" clearable style="width: 140px">
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column type="index" width="60" label="#" />
        <el-table-column prop="name" label="应用名称" min-width="180" show-overflow-tooltip />
        <el-table-column prop="description" label="描述" min-width="240" show-overflow-tooltip />
        <el-table-column label="类型" min-width="160">
          <template #default="{ row }">
            <el-tag type="info">{{ getTypeName(row.typeId) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="启用" width="100">
          <template #default="{ row }">
            <el-tag :type="row.enabled === 1 ? 'success' : 'danger'">
              {{ row.enabled === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="updatedAt" label="更新时间" width="180">
          <template #default="{ row }">{{ formatDate(row.updatedAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" :icon="View" @click="handleView(row)"
              >查看</el-button
            >
            <el-button type="primary" link size="small" :icon="Edit" @click="handleEdit(row)"
              >编辑</el-button
            >
            <el-button type="danger" link size="small" :icon="Delete" @click="handleDelete(row)"
              >删除</el-button
            >
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
      v-model="drawerVisible"
      :title="drawerTitle"
      width="680px"
      align-center
      destroy-on-close
    >
      <AiApplicationForm
        :form-data="formData"
        :is-edit="isEdit"
        @submit="handleSubmit"
        @cancel="handleCancel"
      />
    </el-dialog>

    <el-dialog
      v-model="viewDrawerVisible"
      title="应用详情"
      width="680px"
      align-center
      destroy-on-close
    >
      <el-descriptions :column="1" border v-if="viewData.id">
        <el-descriptions-item label="应用名称">{{ viewData.name }}</el-descriptions-item>
        <el-descriptions-item label="应用描述">{{
          viewData.description || '-'
        }}</el-descriptions-item>
        <el-descriptions-item label="类型">{{ getTypeName(viewData.typeId) }}</el-descriptions-item>
        <el-descriptions-item label="启用">
          <el-tag :type="viewData.enabled === 1 ? 'success' : 'danger'">
            {{ viewData.enabled === 1 ? '启用' : '禁用' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="参数Schema">
          <div style="white-space: pre-wrap; word-break: break-word">
            {{ viewData.paramSchema || '-' }}
          </div>
        </el-descriptions-item>
        <el-descriptions-item label="更新时间">{{
          formatDate(viewData.updatedAt)
        }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{
          formatDate(viewData.createdAt)
        }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import dayjs from 'dayjs'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete, Search, Refresh, View } from '@element-plus/icons-vue'
import { aiApplicationApi } from '@/api/aiApplication'
import { aiAppTypeApi } from '@/api/aiAppType'
import AiApplicationForm from './AiApplicationForm.vue'
import AiAppTypeSelect from '@/components/AiAppTypeSelect.vue'

const loading = ref(false)
const tableData = ref([])
const drawerVisible = ref(false)
const viewDrawerVisible = ref(false)
const isEdit = ref(false)
const drawerTitle = ref('新建应用')
const formData = reactive({})
const viewData = reactive({})

const searchForm = reactive({
  name: '',
  typeId: '',
  enabled: undefined,
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0,
})

const typeMap = reactive(new Map())

const loadTypeNames = async (ids) => {
  const unique = Array.from(new Set(ids.filter(Boolean)))
  for (const id of unique) {
    if (typeMap.has(id)) continue
    try {
      const { records = [] } = await aiAppTypeApi.getPage({ pageNum: 1, pageSize: 1, id })
      const name = records[0]?.name || id
      typeMap.set(id, name)
    } catch {
      typeMap.set(id, id)
    }
  }
}

const getTypeName = (typeId) => typeMap.get(typeId) || typeId || '-'

const formatDate = (ts) => {
  if (!ts) return '-'
  return dayjs(ts).format('YYYY-MM-DD HH:mm:ss')
}

const getAppList = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      name: searchForm.name || undefined,
      typeId: searchForm.typeId || undefined,
      enabled: searchForm.enabled ?? undefined,
    }
    const { records = [], total = 0 } = await aiApplicationApi.getPage(params)
    tableData.value = records
    pagination.total = total
    await loadTypeNames(records.map((r) => r.typeId))
  } catch (error) {
    console.error('get application list failed', error)
    ElMessage.error('获取应用列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.pageNum = 1
  getAppList()
}

const handleReset = () => {
  Object.assign(searchForm, { name: '', typeId: '', enabled: undefined })
  pagination.pageNum = 1
  getAppList()
}

const handleSizeChange = (size) => {
  pagination.pageSize = size
  getAppList()
}

const handleCurrentChange = (page) => {
  pagination.pageNum = page
  getAppList()
}

const handleCreate = () => {
  isEdit.value = false
  drawerTitle.value = '新建应用'
  Object.assign(formData, {
    id: '',
    name: '',
    description: '',
    typeId: '',
    paramSchema: '',
    enabled: 1,
  })
  drawerVisible.value = true
}

const handleEdit = async (row) => {
  isEdit.value = true
  drawerTitle.value = '编辑应用'
  const res = await aiApplicationApi.getById(row.id)
  Object.assign(formData, res || {})
  drawerVisible.value = true
}

const handleView = (row) => {
  Object.assign(viewData, row)
  viewDrawerVisible.value = true
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定删除应用"${row.name}"吗？`, '确认删除', {
    confirmButtonText: '删除',
    cancelButtonText: '取消',
    type: 'warning',
    confirmButtonClass: 'el-button--danger',
  }).then(async () => {
    try {
      await aiApplicationApi.delete(row.id)
      ElMessage.success('删除成功')
      getAppList()
    } catch (error) {
      console.error('delete application failed', error)
      ElMessage.error('删除失败')
    }
  })
}

const handleSubmit = async (payload) => {
  try {
    if (isEdit.value) {
      await aiApplicationApi.update(payload.id, payload)
      ElMessage.success('更新成功')
    } else {
      await aiApplicationApi.create(payload)
      ElMessage.success('创建成功')
    }
    drawerVisible.value = false
    getAppList()
  } catch (error) {
    console.error(isEdit.value ? 'update application failed' : 'create application failed', error)
    ElMessage.error(isEdit.value ? '更新失败' : '创建失败')
  }
}

const handleCancel = () => {
  drawerVisible.value = false
}

onMounted(() => {
  getAppList()
})
</script>

<style scoped lang="scss">
@use '@/styles/common.scss';

.ai-app-management-container {
  @extend .page-container;
}

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
