<template>
  <div class="permission-management-container">
    <!-- 页面标题与操作区 -->
    <div class="page-header">
      <h2>权限管理</h2>
      <el-button type="primary" size="default" @click="handleCreate">
        <el-icon><Plus /></el-icon>&nbsp;新建权限
      </el-button>
    </div>

    <!-- 搜索区域 -->
    <div class="search-container">
      <SimpleQueryFilter v-model="searchForm" @search="handleSearch" @reset="handleReset">
        <!-- 筛选项 -->
        <template #filters>
          <el-form-item label="权限名称">
            <el-input
              v-model="searchForm.name"
              placeholder="请输入权限名称"
              clearable
              style="width: 200px"
            />
          </el-form-item>
          <el-form-item label="权限编码">
            <el-input
              v-model="searchForm.code"
              placeholder="请输入权限编码"
              clearable
              style="width: 200px"
            />
          </el-form-item>
          <el-form-item label="权限类型">
            <el-select
              v-model="searchForm.type"
              placeholder="请选择权限类型"
              clearable
              style="width: 200px"
            >
              <el-option label="菜单" value="1" />
              <el-option label="操作按钮" value="2" />
              <el-option label="API" value="3" />
            </el-select>
          </el-form-item>
        </template>
      </SimpleQueryFilter>
    </div>

    <!-- 数据表格区 -->
    <el-card class="table-card" shadow="hover">
      <div class="table-empty" v-if="!tableData.length && !loading">
        <el-empty description="暂无权限数据"></el-empty>
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
        <el-table-column prop="name" label="权限名称" min-width="150" />
        <el-table-column prop="code" label="权限编码" min-width="200" show-overflow-tooltip />
        <el-table-column prop="type" label="权限类型" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getTypeTagType(row.type)">
              {{ getTypeName(row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="createdAt" label="创建时间" width="180" align="center">
          <template #default="{ row }">
            <span class="created-time">{{ formatDate(row.createdAt) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button type="primary" :icon="View" size="small" @click="handleView(row)" link>
                查看
              </el-button>
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

    <!-- 查看详情弹框 -->
    <el-dialog v-model="viewDrawerVisible" title="权限详情" width="50%" destroy-on-close>
      <PermissionDetailView :model-data="viewData" @close="handleViewClose" />
    </el-dialog>

    <!-- 编辑/创建弹框 -->
    <el-dialog
      v-model="editDrawerVisible"
      :title="isEdit ? '编辑权限' : '创建权限'"
      width="50%"
      destroy-on-close
    >
      <PermissionEditForm
        :form-data="formData"
        :is-edit="isEdit"
        @submit="handleSubmit"
        @cancel="handleEditClose"
      />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Edit, Delete, View, Plus } from '@element-plus/icons-vue'
import SimpleQueryFilter from '@/components/common/SimpleQueryFilter.vue'
import { permissionApi } from '@/api/permission'
import dayjs from 'dayjs'
import PermissionDetailView from './PermissionDetailView.vue'
import PermissionEditForm from './PermissionEditForm.vue'

// 响应式数据
const loading = ref(false)
const tableData = ref([])
const selectedRows = ref([])

// 搜索表单
const searchForm = reactive({
  name: '',
  code: '',
  type: '',
})

// 分页数据
const pagination = reactive({
  pageNum: 1,
  pageSize: 20,
  total: 0,
})

// 侧边栏控制
const viewDrawerVisible = ref(false)
const editDrawerVisible = ref(false)
const isEdit = ref(false)

// 表单数据
const formData = reactive({
  id: '',
  name: '',
  code: '',
  type: 1,
  description: '',
})

// 查看详情数据
const viewData = reactive({})

// 获取权限列表
const getPermissionList = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      ...searchForm,
    }
    const { records = [], total = 0 } = await permissionApi.getPage(params)
    tableData.value = records
    pagination.total = total
  } catch (error) {
    console.error('get permission list failed', error)
    ElMessage.error('获取权限列表失败')
  } finally {
    loading.value = false
  }
}

// 获取权限类型名称
const getTypeName = (type) => {
  const typeMap = {
    1: '菜单',
    2: '操作按钮',
    3: 'API',
  }
  return typeMap[type] || '未知'
}

// 获取权限类型标签类型
const getTypeTagType = (type) => {
  const typeMap = {
    1: 'primary',
    2: 'success',
    3: 'warning',
  }
  return typeMap[type] || 'info'
}

// 格式化日期
const formatDate = (date) => {
  if (!date) return '-'
  // 支持毫秒时间戳和日期字符串
  if (typeof date === 'number') {
    return dayjs(date).format('YYYY-MM-DD HH:mm:ss')
  }
  return dayjs(date).format('YYYY-MM-DD HH:mm:ss')
}

// 搜索
const handleSearch = () => {
  pagination.pageNum = 1
  getPermissionList()
}

// 重置搜索
const handleReset = () => {
  Object.assign(searchForm, {
    name: '',
    code: '',
    type: '',
  })
  pagination.pageNum = 1
  getPermissionList()
}

// 选择变化
const handleSelectionChange = (selection) => {
  selectedRows.value = selection
}

// 分页大小变化
const handleSizeChange = (size) => {
  pagination.pageSize = size
  getPermissionList()
}

// 当前页变化
const handleCurrentChange = (page) => {
  pagination.pageNum = page
  getPermissionList()
}

// 新建权限
const handleCreate = () => {
  isEdit.value = false
  resetForm()
  editDrawerVisible.value = true
}

// 编辑权限
const handleEdit = async (row) => {
  isEdit.value = true
  try {
    const res = await permissionApi.getById(row.id)
    Object.assign(formData, res)
    editDrawerVisible.value = true
  } catch (error) {
    console.error('get permission detail failed', error)
    ElMessage.error('获取权限详情失败')
  }
}

// 查看详情
const handleView = (row) => {
  // 直接使用表格行数据，减少服务器调用
  Object.assign(viewData, row)
  viewDrawerVisible.value = true
}

// 删除权限
const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除权限"${row.name}"吗？此操作不可恢复。`, '确认删除', {
    confirmButtonText: '删除',
    cancelButtonText: '取消',
    type: 'warning',
    confirmButtonClass: 'el-button--danger',
  }).then(async () => {
    try {
      await permissionApi.delete(row.id)
      ElMessage.success('删除成功')
      getPermissionList()
    } catch (error) {
      console.error('delete permission failed', error)
      ElMessage.error('删除失败')
    }
  })
}

// 提交表单
const handleSubmit = async (formData) => {
  try {
    if (isEdit.value) {
      await permissionApi.update(formData)
      ElMessage.success('更新成功')
    } else {
      await permissionApi.create(formData)
      ElMessage.success('创建成功')
    }
    editDrawerVisible.value = false
    getPermissionList()
  } catch (error) {
    console.error(isEdit.value ? 'update permission failed' : 'create permission failed', error)
    ElMessage.error(isEdit.value ? '更新失败' : '创建失败')
  }
}

// 关闭编辑侧边栏
const handleEditClose = () => {
  editDrawerVisible.value = false
}

// 关闭查看侧边栏
const handleViewClose = () => {
  viewDrawerVisible.value = false
}

// 重置表单
const resetForm = () => {
  Object.assign(formData, {
    id: '',
    name: '',
    code: '',
    type: 1,
    description: '',
  })
}

// 初始化
onMounted(() => {
  getPermissionList()
})
</script>

<style scoped lang="scss">
@use '@/styles/common.scss';

// 保留页面特有样式
.permission-management-container {
  @extend .page-container;
}

.created-time {
  display: flex;
  align-items: center;
  color: #666;
}

.action-buttons {
  display: flex;
  justify-content: center;
  gap: 12px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.table-card {
  margin-top: 20px;
}

.table-empty {
  padding: 60px 0;
  display: flex;
  justify-content: center;
}

.search-container {
  margin-top: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 0;
  border-bottom: 1px solid #eee;
}

.page-header h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 500;
}
</style>
