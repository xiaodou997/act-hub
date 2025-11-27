<template>
  <div class="tenant-management-container">
    <!-- 页面标题与操作区 -->
    <div class="page-header">
      <h2>租户管理</h2>
      <el-button type="primary" size="default" @click="handleCreate">
        <el-icon><Plus /></el-icon>&nbsp;新建租户
      </el-button>
    </div>

    <!-- 搜索区域 -->
    <div class="search-container">
      <SimpleQueryFilter v-model="searchForm" @search="handleSearch" @reset="handleReset">
        <!-- 筛选项 -->
        <template #filters>
          <el-form-item label="租户名称">
            <el-input
              v-model="searchForm.name"
              placeholder="请输入租户名称"
              clearable
              style="width: 200px"
            />
          </el-form-item>
          <el-form-item label="状态">
            <el-select
              v-model="searchForm.status"
              placeholder="请选择状态"
              clearable
              style="width: 150px"
            >
              <el-option label="全部" value="" />
              <el-option label="正常" value="1" />
              <el-option label="禁用" value="3" />
            </el-select>
          </el-form-item>
          <el-form-item label="描述">
            <el-input
              v-model="searchForm.remark"
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
        <el-empty description="暂无租户数据"></el-empty>
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
        <el-table-column prop="name" label="租户名称" min-width="200" />
        <el-table-column prop="contactName" label="联系人" min-width="120" />
        <el-table-column prop="contactEmail" label="联系邮箱" min-width="200" />
        <el-table-column prop="contactPhone" label="联系电话" min-width="120" />
        <el-table-column prop="remark" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag
              :type="getStatusType(row.status)"
              size="small"
              :effect="row.status === 1 ? 'dark' : 'plain'"
            >
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
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

    <!-- 查看详情侧边栏 -->
    <el-drawer v-model="viewDrawerVisible" title="租户详情" size="50%">
      <TenantDetailView :tenant-data="viewData" @close="viewDrawerVisible = false" />
    </el-drawer>

    <!-- 编辑/新建侧边栏 -->
    <el-drawer
      v-model="editDrawerVisible"
      :title="isEdit ? '编辑租户' : '新建租户'"
      size="50%"
      :show-close="false"
      :before-close="handleEditClose"
    >
      <TenantEditForm
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
import { tenantApi } from '@/api/tenant'
import dayjs from 'dayjs'
import TenantDetailView from './TenantDetailView.vue'
import TenantEditForm from './TenantEditForm.vue'

// 响应式数据
const loading = ref(false)
const tableData = ref([])
const selectedRows = ref([])

// 搜索表单
const searchForm = reactive({
  name: '',
  status: '',
  remark: '',
})

// 分页数据
const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
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
  contactName: '',
  contactEmail: '',
  contactPhone: '',
  status: 1,
  remark: '',
})

// 查看详情数据
const viewData = reactive({})

// 获取租户列表
const getTenantList = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      ...searchForm,
    }
    const { records = [], total = 0 } = await tenantApi.getPage(params)
    tableData.value = records
    pagination.total = total
  } catch (error) {
    console.error('get tenant info failed', error)
    ElMessage.error('获取租户列表失败')
  } finally {
    loading.value = false
  }
}

// 状态类型映射
const getStatusType = (status) => {
  const statusMap = {
    1: 'success',
    3: 'danger',
  }
  return statusMap[status] || 'info'
}

// 状态文本映射
const getStatusText = (status) => {
  const statusMap = {
    1: '正常',
    3: '禁用',
  }
  return statusMap[status] || '未知'
}

// 格式化日期
const formatDate = (date) => {
  if (!date) return '-'
  return dayjs(date).format('YYYY-MM-DD HH:mm:ss')
}

// 搜索
const handleSearch = () => {
  pagination.pageNum = 1
  getTenantList()
}

// 重置搜索
const handleReset = () => {
  Object.assign(searchForm, {
    name: '',
    status: '',
    remark: '',
  })
  pagination.pageNum = 1
  getTenantList()
}

// 选择变化
const handleSelectionChange = (selection) => {
  selectedRows.value = selection
}

// 分页大小变化
const handleSizeChange = (size) => {
  pagination.pageSize = size
  getTenantList()
}

// 当前页变化
const handleCurrentChange = (page) => {
  pagination.pageNum = page
  getTenantList()
}

// 新建租户
const handleCreate = () => {
  isEdit.value = false
  resetForm()
  editDrawerVisible.value = true
}

// 编辑租户
const handleEdit = (row) => {
  isEdit.value = true
  Object.assign(formData, {
    id: row.id,
    name: row.name,
    contactName: row.contactName,
    contactEmail: row.contactEmail,
    contactPhone: row.contactPhone,
    status: row.status,
    remark: row.remark,
  })
  editDrawerVisible.value = true
}

// 查看详情
const handleView = (row) => {
  Object.assign(viewData, row)
  viewDrawerVisible.value = true
}

// 删除租户
const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除租户"${row.name}"吗？此操作不可恢复。`, '确认删除', {
    confirmButtonText: '删除',
    cancelButtonText: '取消',
    type: 'warning',
    confirmButtonClass: 'el-button--danger',
  }).then(async () => {
    try {
      await tenantApi.delete(row.id)
      ElMessage.success('删除成功')
      getTenantList()
    } catch (error) {
      console.error('delete tenant failed', error)
      ElMessage.error('删除失败')
    }
  })
}

// 提交表单
const handleSubmit = async (formData) => {
  try {
    if (isEdit.value) {
      await tenantApi.update(formData)
      ElMessage.success('更新成功')
    } else {
      await tenantApi.create(formData)
      ElMessage.success('创建成功')
    }
    editDrawerVisible.value = false
    getTenantList()
  } catch (error) {
    console.error('submit form failed', error)
    ElMessage.error(isEdit.value ? '更新失败' : '创建失败')
  }
}

// 关闭编辑侧边栏
const handleEditClose = () => {
  editDrawerVisible.value = false
}

// 重置表单
const resetForm = () => {
  Object.assign(formData, {
    id: '',
    name: '',
    contactName: '',
    contactEmail: '',
    contactPhone: '',
    status: 1,
    remark: '',
  })
}

// 初始化
onMounted(() => {
  getTenantList()
})
</script>

<style scoped lang="scss">
@use '@/styles/common.scss';

// 保留页面特有样式
.tenant-management-container {
  @extend .page-container;
}

.expire-time {
  display: flex;
  align-items: center;
  color: #666;
}
</style>
