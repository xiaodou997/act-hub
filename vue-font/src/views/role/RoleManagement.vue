<template>
  <div class="role-management-container">
    <!-- 页面标题与操作区 -->
    <div class="page-header">
      <h2>角色管理</h2>
      <el-button type="primary" size="default" @click="handleCreate">
        <el-icon><Plus /></el-icon>&nbsp;新建角色
      </el-button>
    </div>

    <!-- 搜索区域 -->
    <div class="search-container">
      <SimpleQueryFilter v-model="searchForm" @search="handleSearch" @reset="handleReset">
        <!-- 筛选项 -->
        <template #filters>
          <el-form-item label="角色名称">
            <el-input
              v-model="searchForm.name"
              placeholder="请输入角色名称"
              clearable
              style="width: 200px"
            />
          </el-form-item>
          <el-form-item label="角色编码">
            <el-input
              v-model="searchForm.code"
              placeholder="请输入角色编码"
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
        <el-empty description="暂无角色数据"></el-empty>
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
        <el-table-column prop="name" label="角色名称" min-width="150" />
        <el-table-column prop="code" label="角色编码" min-width="150" />
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <!-- <el-table-column prop="createdAt" label="创建时间" width="180" align="center">
          <template #default="{ row }">
            <span class="created-time">{{ formatDate(row.createdAt) }}</span>
          </template>
        </el-table-column> -->
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="{ row }">
            <div class="action-buttons">
              <!-- <el-button type="primary" :icon="View" size="small" @click="handleView(row)" link>
                查看
              </el-button> -->
              <el-button
                type="primary"
                :icon="Edit"
                size="small"
                @click="handleAssignPermissions(row)"
                link
              >
                分配权限
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
    <el-dialog
      v-model="viewDrawerVisible"
      title="角色详情"
      width="50%"
      :close-on-click-modal="true"
    >
      <RoleDetailView :role-data="viewData" @close="viewDrawerVisible = false" />
    </el-dialog>

    <!-- 编辑/新建弹框 -->
    <el-dialog
      v-model="editDrawerVisible"
      :title="isEdit ? '编辑角色' : '新建角色'"
      width="50%"
      :show-close="false"
      :before-close="handleEditClose"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
    >
      <RoleEditForm
        :form-data="formData"
        :is-edit="isEdit"
        @submit="handleSubmit"
        @cancel="editDrawerVisible = false"
      />
    </el-dialog>

    <el-dialog v-model="permissionDialogVisible" title="分配权限" width="60%" destroy-on-close>
      <MultiSelector
        :key="currentRoleId"
        :options="permissionOptions"
        :selected-ids="selectedPermissionIds"
        :loading="permissionLoading"
        :show-description="true"
        @confirm="handlePermissionConfirm"
        @cancel="handlePermissionCancel"
      />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Edit, Delete, View, Plus } from '@element-plus/icons-vue'
import SimpleQueryFilter from '@/components/common/SimpleQueryFilter.vue'
import { roleApi } from '@/api/role'
import { permissionApi } from '@/api/permission'
import dayjs from 'dayjs'
import RoleDetailView from './RoleDetailView.vue'
import RoleEditForm from './RoleEditForm.vue'
import MultiSelector from '@/components/common/MultiSelector.vue'

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
const viewDrawerVisible = ref(false)
const editDrawerVisible = ref(false)
const isEdit = ref(false)

// 表单数据
const formData = reactive({
  id: '',
  name: '',
  code: '',
  description: '',
})

// 查看详情数据
const viewData = reactive({})

// 获取角色列表
const getRoleList = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      ...searchForm,
    }
    const { records = [], total = 0 } = await roleApi.getPage(params)
    tableData.value = records
    pagination.total = total
  } catch (error) {
    console.error('get role info failed', error)
    ElMessage.error('获取角色列表失败')
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
  getRoleList()
}

// 重置搜索
const handleReset = () => {
  Object.assign(searchForm, {
    name: '',
    code: '',
    description: '',
  })
  pagination.pageNum = 1
  getRoleList()
}

// 选择变化
const handleSelectionChange = (selection) => {
  selectedRows.value = selection
}

// 分页大小变化
const handleSizeChange = (size) => {
  pagination.pageSize = size
  getRoleList()
}

// 当前页变化
const handleCurrentChange = (page) => {
  pagination.pageNum = page
  getRoleList()
}

// 新建角色
const handleCreate = () => {
  isEdit.value = false
  resetForm()
  editDrawerVisible.value = true
}

// 编辑角色
const handleEdit = (row) => {
  isEdit.value = true
  Object.assign(formData, {
    id: row.id,
    name: row.name,
    code: row.code,
    description: row.description,
  })
  editDrawerVisible.value = true
}

// 查看详情
const handleView = (row) => {
  Object.assign(viewData, row)
  viewDrawerVisible.value = true
}

// 删除角色
const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除角色"${row.name}"吗？此操作不可恢复。`, '确认删除', {
    confirmButtonText: '删除',
    cancelButtonText: '取消',
    type: 'warning',
    confirmButtonClass: 'el-button--danger',
  }).then(async () => {
    try {
      await roleApi.delete(row.id)
      ElMessage.success('删除成功')
      getRoleList()
    } catch (error) {
      console.error('delete role failed', error)
      ElMessage.error('删除失败')
    }
  })
}

// 提交表单
const handleSubmit = async (formData) => {
  try {
    if (isEdit.value) {
      await roleApi.update(formData)
      ElMessage.success('更新成功')
    } else {
      await roleApi.create(formData)
      ElMessage.success('创建成功')
    }
    editDrawerVisible.value = false
    getRoleList()
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
    code: '',
    description: '',
  })
}

// 权限分配
const permissionDialogVisible = ref(false)
const permissionOptions = ref([])
const selectedPermissionIds = ref([])
const permissionLoading = ref(false)
const currentRoleId = ref('')

const handleAssignPermissions = async (row) => {
  currentRoleId.value = row.id
  selectedPermissionIds.value = []
  permissionOptions.value = []
  permissionDialogVisible.value = true
  permissionLoading.value = true
  try {
    const [options, assignedIds] = await Promise.all([
      permissionApi.list(),
      roleApi.getPermissionIds(row.id),
    ])
    permissionOptions.value = options || []
    selectedPermissionIds.value = assignedIds || []
  } catch (error) {
    console.error('load permissions failed', error)
    ElMessage.error('加载权限失败')
  } finally {
    permissionLoading.value = false
  }
}

const handlePermissionConfirm = async (ids) => {
  try {
    await roleApi.assignPermissions(currentRoleId.value, ids)
    ElMessage.success('权限分配成功')
    permissionDialogVisible.value = false
  } catch (error) {
    console.error('assign permissions failed', error)
    ElMessage.error('权限分配失败')
  }
}

const handlePermissionCancel = () => {
  permissionDialogVisible.value = false
}

// 初始化
onMounted(() => {
  getRoleList()
})
</script>

<style scoped lang="scss">
@use '@/styles/common.scss';

// 保留页面特有样式
.role-management-container {
  @extend .page-container;
}

.created-time {
  display: flex;
  align-items: center;
  color: #666;
}
</style>
