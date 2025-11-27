<template>
  <div class="platform-management-container">
    <!-- 页面标题与操作区 -->
    <div class="page-header">
      <h2>平台管理</h2>
      <el-button type="primary" size="default" @click="handleCreate">
        <el-icon><Plus /></el-icon>&nbsp;新建平台
      </el-button>
    </div>

    <!-- 搜索区域 -->
    <div class="search-container">
      <SimpleQueryFilter v-model="searchForm" @search="handleSearch" @reset="handleReset">
        <!-- 筛选项 -->
        <template #filters>
          <el-form-item label="平台名称">
            <el-input
              v-model="searchForm.name"
              placeholder="请输入平台名称"
              clearable
              style="width: 200px"
            />
          </el-form-item>
          <el-form-item label="平台编码">
            <el-input
              v-model="searchForm.code"
              placeholder="请输入平台编码"
              clearable
              style="width: 200px"
            />
          </el-form-item>
          <el-form-item label="描述">
            <el-input
              v-model="searchForm.description"
              placeholder="请输入描述关键词"
              clearable
              style="width: 200px"
            />
          </el-form-item>
        </template>
      </SimpleQueryFilter>
    </div>

    <!-- 数据表格区 -->
    <el-card class="table-card" shadow="hover">
      <div class="table-empty" v-if="!tableData.length && !loading">
        <el-empty description="暂无平台数据"></el-empty>
      </div>

      <el-table
        v-else
        :data="tableData"
        v-loading="loading"
        style="width: 100%"
        stripe
        highlight-current-row
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="index" width="50" align="center" />
        <el-table-column prop="name" label="平台名称" min-width="150" />
        <el-table-column prop="code" label="平台编码" min-width="150" />
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="createdAt" label="创建时间" width="180" align="center">
          <template #default="{ row }">
            <span class="created-time">{{ formatDate(row.createdAt) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button type="warning" :icon="Edit" size="small" @click="handleEdit(row)" link>
                编辑
              </el-button>
              <el-button type="danger" :icon="Delete" size="small" @click="handleDelete(row)" link>
                删除
              </el-button>
            </div>
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
    </el-card>

    <!-- 编辑/新建侧边栏 -->
    <el-drawer
      v-model="editDrawerVisible"
      :title="isEdit ? '编辑平台' : '新建平台'"
      size="50%"
      :show-close="false"
      :before-close="handleEditClose"
    >
      <PlatformEditForm
        :form-data="formData"
        :is-edit="isEdit"
        @submit="handleSubmit"
        @cancel="editDrawerVisible = false"
      />
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Edit, Delete, View, Plus } from '@element-plus/icons-vue'
import SimpleQueryFilter from '@/components/common/SimpleQueryFilter.vue'
import { platformApi } from '@/api/platform'
import dayjs from 'dayjs'
import PlatformEditForm from './PlatformEditForm.vue'

// 响应式数据
const loading = ref(false)
const tableData = ref([])
const selectedRows = ref([])

// 搜索表单
const searchForm = reactive({
  name: '',
  code: '',
  description: '',
})

// 分页数据
const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0,
})

// 侧边栏控制
const editDrawerVisible = ref(false)
const isEdit = ref(false)

// 表单数据
const formData = reactive({
  id: '',
  name: '',
  code: '',
  description: '',
  status: 1, // 默认启用状态
})

// 获取平台列表
const getPlatformList = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      ...searchForm,
    }
    const { records = [], total = 0 } = await platformApi.getPage(params)
    tableData.value = records
    pagination.total = total
  } catch (error) {
    console.error('get platform info failed', error)
    ElMessage.error('获取平台列表失败')
  } finally {
    loading.value = false
  }
}

// 格式化日期
const formatDate = (date) => {
  if (!date) return '-'
  return dayjs(date).format('YYYY-MM-DD HH:mm:ss')
}

// 搜索
const handleSearch = () => {
  pagination.pageNum = 1
  getPlatformList()
}

// 重置搜索
const handleReset = () => {
  searchForm.name = ''
  searchForm.code = ''
  searchForm.description = ''
  pagination.pageNum = 1
  getPlatformList()
}

// 页面大小变化
const handleSizeChange = (size) => {
  pagination.pageSize = size
  getPlatformList()
}

// 当前页面变化
const handleCurrentChange = (current) => {
  pagination.pageNum = current
  getPlatformList()
}

// 选择行变化
const handleSelectionChange = (rows) => {
  selectedRows.value = rows
}

// 编辑
const handleEdit = (row) => {
  formData.id = row.id
  formData.name = row.name
  formData.code = row.code
  formData.description = row.description
  formData.status = row.status || 1
  isEdit.value = true
  editDrawerVisible.value = true
}

// 新建
const handleCreate = () => {
  // 清空表单数据
  formData.id = ''
  formData.name = ''
  formData.code = ''
  formData.description = ''
  formData.status = 1
  isEdit.value = false
  editDrawerVisible.value = true
}

// 提交表单
const handleSubmit = async (data) => {
  try {
    if (isEdit.value) {
      await platformApi.update(data)
      ElMessage.success('平台更新成功')
    } else {
      await platformApi.create(data)
      ElMessage.success('平台创建成功')
    }
    editDrawerVisible.value = false
    getPlatformList()
  } catch (error) {
    console.error('save platform failed', error)
    ElMessage.error(isEdit.value ? '平台更新失败' : '平台创建失败')
  }
}

// 删除平台
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除平台 "${row.name}" 吗？`, '确认删除', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await platformApi.delete(row.id)
    ElMessage.success('平台删除成功')
    getPlatformList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('delete platform failed', error)
      ElMessage.error('平台删除失败')
    }
  }
}

// 编辑侧边栏关闭前检查
const handleEditClose = (done) => {
  ElMessageBox.confirm('确定要关闭吗？未保存的内容将会丢失。', '确认关闭', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  })
    .then(() => {
      done()
    })
    .catch(() => {
      // 不执行任何操作，保持侧边栏打开
    })
}

// 组件挂载时获取列表数据
onMounted(() => {
  getPlatformList()
})
</script>

<style scoped lang="scss">
@use '@/styles/common.scss';
.platform-management-container {
  @extend .page-container;
}
</style>
