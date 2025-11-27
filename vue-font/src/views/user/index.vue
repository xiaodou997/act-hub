<template>
  <div class="user-management-container">
    <!-- 页面标题与操作区 -->
    <div class="page-header">
      <h2>用户管理</h2>
      <el-button type="primary" size="default" @click="handleCreate">
        <el-icon><Plus /></el-icon>&nbsp;新建用户
      </el-button>
    </div>

    <!-- 搜索区域 -->
    <div class="search-container">
      <SimpleQueryFilter v-model="searchForm" @search="handleSearch" @reset="handleReset">
        <!-- 筛选项 -->
        <template #filters>
          <el-form-item label="用户名">
            <el-input
              v-model="searchForm.username"
              placeholder="请输入用户名"
              clearable
              style="width: 200px"
            />
          </el-form-item>
          <!-- 租户相关筛选已停用
          <el-form-item label="租户名称">
            <el-input
              v-model="searchForm.tenantName"
              placeholder="请输入租户名称"
              clearable
              style="width: 200px"
            />
          </el-form-item>
          -->
          <el-form-item label="状态">
            <el-select
              v-model="searchForm.status"
              placeholder="请选择状态"
              clearable
              style="width: 150px"
            >
              <el-option label="全部" value="" />
              <el-option label="正常" value="1" />
              <el-option label="禁用" value="0" />
              <el-option label="锁定" value="2" />
            </el-select>
          </el-form-item>
          <el-form-item label="备注">
            <el-input
              v-model="searchForm.remark"
              placeholder="请输入备注关键词"
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
        <el-empty description="暂无用户数据"></el-empty>
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
        <el-table-column prop="username" label="用户名" min-width="150" />
        <el-table-column prop="email" label="邮箱" min-width="200" />
        <!-- <el-table-column prop="tenantName" label="租户名称" min-width="150" /> -->
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
        <el-table-column prop="remark" label="备注" min-width="200" show-overflow-tooltip />
        <el-table-column prop="lastLoginAt" label="最后登录" width="180" align="center">
          <template #default="{ row }">
            <span class="last-login-time">{{ formatDate(row.lastLoginAt) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" align="center">
          <template #default="{ row }">
            <span class="created-time">{{ formatDate(row.createdAt) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="350" align="center" fixed="right">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button type="primary" :icon="View" size="small" @click="handleView(row)" link>
                查看
              </el-button>
              <el-button type="warning" :icon="Edit" size="small" @click="handleEdit(row)" link>
                编辑
              </el-button>
              <el-button
                type="success"
                :icon="UserFilled"
                size="small"
                @click="handleAssignRoles(row)"
                link
                v-permission="['SUPER_ADMIN']"
              >
                分配角色
              </el-button>
              <el-button
                type="warning"
                :icon="Lock"
                size="small"
                @click="handleResetPassword(row)"
                link
              >
                重置密码
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
      title="用户详情"
      width="50%"
      :close-on-click-modal="true"
    >
      <UserDetailView :user-data="viewData" @close="viewDrawerVisible = false" />
    </el-dialog>

    <!-- 编辑/新建弹框 -->
    <el-dialog
      v-model="editDrawerVisible"
      :title="isEdit ? '编辑用户' : '新建用户'"
      width="50%"
      :show-close="false"
      :before-close="handleEditClose"
      :close-on-click-modal="false"
    >
      <UserEditForm
        :form-data="formData"
        :is-edit="isEdit"
        @submit="handleSubmit"
        @cancel="editDrawerVisible = false"
      />
    </el-dialog>

    <!-- 重置密码对话框 -->
    <el-dialog v-model="resetPasswordDialogVisible" title="重置密码" :close-on-click-modal="false">
      <div class="reset-password-dialog">
        <p>
          确定要将用户 <strong>{{ resetPasswordUserName }}</strong> 的密码重置为默认密码吗？
        </p>
        <p class="default-password">默认密码：123456</p>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="resetPasswordDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmResetPassword">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <el-dialog
      v-model="assignRolesDrawerVisible"
      title="分配角色"
      width="50%"
      :show-close="true"
      :before-close="handleCloseAssignRoles"
      :close-on-click-modal="false"
    >
      <MultiSelector
        :options="roleOptions"
        :selected-ids="selectedRoleIds"
        :loading="roleLoading"
        label-key="name"
        value-key="id"
        description-key="description"
        :show-description="true"
        @confirm="handleAssignRolesConfirm"
        @cancel="handleCloseAssignRoles"
      />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Edit, Delete, View, Plus, Lock, UserFilled } from '@element-plus/icons-vue'
import SimpleQueryFilter from '@/components/common/SimpleQueryFilter.vue'
import MultiSelector from '@/components/common/MultiSelector.vue'
import { userApi } from '@/api/user'
import { roleApi } from '@/api/role'
import dayjs from 'dayjs'
import UserDetailView from './UserDetailView.vue'
import UserEditForm from './UserEditForm.vue'

// 响应式数据
const loading = ref(false)
const tableData = ref([])
const selectedRows = ref([])

// 搜索表单
const searchForm = reactive({
  username: '',
  // tenantName: '',
  // tenantId: '',
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
  username: '',
  email: '',
  status: 1,
  tenantId: '',
  remark: '',
})

// 查看详情数据
const viewData = reactive({})

// 重置密码对话框
const resetPasswordDialogVisible = ref(false)
const resetPasswordUserId = ref('')
const resetPasswordUserName = ref('')

// 角色分配抽屉
const assignRolesDrawerVisible = ref(false)
const assignRolesUserId = ref('')
const assignRolesUserName = ref('')
const roleOptions = ref([])
const selectedRoleIds = ref([])
const roleLoading = ref(false)

// 获取用户列表
const getUserList = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      ...searchForm,
    }
    const { records = [], total = 0 } = await userApi.getUserPage(params)
    tableData.value = records
    pagination.total = total
  } catch (error) {
    console.error('get user info failed', error)
    ElMessage.error('获取用户列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.pageNum = 1
  getUserList()
}

// 重置
const handleReset = () => {
  Object.keys(searchForm).forEach((key) => {
    searchForm[key] = ''
  })
  pagination.pageNum = 1
  pagination.pageSize = 10
  getUserList()
}

// 分页处理
const handleSizeChange = (size) => {
  pagination.pageSize = size
  getUserList()
}

const handleCurrentChange = (current) => {
  pagination.pageNum = current
  getUserList()
}

// 选择行变化
const handleSelectionChange = (selection) => {
  selectedRows.value = selection
}

// 查看用户详情
const handleView = (row) => {
  viewData.id = row.id
  viewData.username = row.username
  viewData.email = row.email
  viewData.status = row.status
  // viewData.tenantId = row.tenantId
  // viewData.tenantName = row.tenantName
  viewData.remark = row.remark
  viewData.lastLoginAt = row.lastLoginAt
  viewData.createdAt = row.createdAt
  viewData.updatedAt = row.updatedAt
  viewDrawerVisible.value = true
}

// 编辑用户
const handleEdit = (row) => {
  formData.id = row.id
  formData.username = row.username
  formData.email = row.email
  formData.status = row.status
  // formData.tenantId = row.tenantId
  formData.remark = row.remark
  isEdit.value = true
  editDrawerVisible.value = true
}

// 创建用户
const handleCreate = () => {
  Object.keys(formData).forEach((key) => {
    formData[key] = ''
  })
  formData.status = 1
  isEdit.value = false
  editDrawerVisible.value = true
}

// 删除用户
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该用户吗？删除后将无法恢复。', '确认删除', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await userApi.delete(row.id)
    ElMessage.success('删除成功')
    getUserList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('delete user failed', error)
      ElMessage.error('删除失败')
    }
  }
}

// 获取所有角色
const getAllRoles = async () => {
  roleLoading.value = true
  try {
    const res = await roleApi.getPage({ pageNum: 1, pageSize: 1000 })
    roleOptions.value = res.records.map((role) => ({
      id: role.id,
      name: role.name,
      description: role.description,
    }))
  } catch (error) {
    console.error('get all role info failed', error)
    ElMessage.error('获取角色列表失败')
  } finally {
    roleLoading.value = false
  }
}

// 获取用户当前角色
const getUserRoles = async (userId) => {
  try {
    const res = await userApi.getUserRoles(userId)
    selectedRoleIds.value = res || []
  } catch (error) {
    console.error('get user role failed', error)
    ElMessage.error('获取用户角色失败')
    selectedRoleIds.value = []
  }
}

// 处理角色分配确认
const handleAssignRolesConfirm = async (selectedIds) => {
  try {
    await userApi.assignRoles(assignRolesUserId.value, selectedIds)
    ElMessage.success('角色分配成功')
    assignRolesDrawerVisible.value = false
  } catch (error) {
    console.error('assign roles failed', error)
    ElMessage.error('角色分配失败')
  }
}

// 关闭角色分配抽屉
const handleCloseAssignRoles = () => {
  assignRolesDrawerVisible.value = false
}

// 重置密码
const handleResetPassword = (row) => {
  resetPasswordUserId.value = row.id
  resetPasswordUserName.value = row.username
  resetPasswordDialogVisible.value = true
}

// 处理角色分配
const handleAssignRoles = async (row) => {
  assignRolesUserId.value = row.id
  assignRolesUserName.value = row.username
  assignRolesDrawerVisible.value = true

  // 获取所有角色
  await getAllRoles()

  // 获取用户当前角色
  await getUserRoles(row.id)
}

// 确认重置密码
const confirmResetPassword = async () => {
  try {
    await userApi.resetPassword(resetPasswordUserId.value)
    ElMessage.success('密码重置成功')
    resetPasswordDialogVisible.value = false
  } catch (error) {
    console.error('reset password failed', error)
    ElMessage.error('密码重置失败')
  }
}

// 提交表单
const handleSubmit = async (data) => {
  try {
    if (isEdit.value) {
      await userApi.update(data)
      ElMessage.success('更新成功')
    } else {
      await userApi.create(data)
      ElMessage.success('创建成功')
    }
    editDrawerVisible.value = false
    getUserList()
  } catch (error) {
    console.error(isEdit.value ? 'update user failed' : 'create user failed', error)
    ElMessage.error(isEdit.value ? '更新失败' : '创建失败')
  }
}

// 关闭编辑抽屉
const handleEditClose = (done) => {
  done()
}

// 状态类型映射
const getStatusType = (status) => {
  const statusMap = {
    1: 'success',
    2: 'warning',
    0: 'danger',
  }
  return statusMap[status] || 'info'
}

// 状态文本映射
const getStatusText = (status) => {
  const statusMap = {
    1: '正常',
    2: '锁定',
    0: '禁用',
  }
  return statusMap[status] || '未知'
}

// 格式化日期
const formatDate = (date) => {
  if (!date) return '-'
  return dayjs(date).format('YYYY-MM-DD HH:mm:ss')
}

// 组件挂载时获取数据
onMounted(() => {
  getUserList()
})
</script>

<style scoped lang="scss">
@use '@/styles/common.scss';

// 保留页面特有样式
.role-management-container {
  @extend .page-container;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.reset-password-dialog {
  padding: 20px 0;
}

.default-password {
  color: #f56c6c;
  font-weight: 500;
  margin-top: 10px;
}
</style>
