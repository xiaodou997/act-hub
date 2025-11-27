<!-- /view/invite-code/InviteCodeManagement.vue -->
<template>
  <div class="invite-code-container">
    <!-- 页面标题与操作区 -->
    <div class="page-header">
      <h2>邀请码管理</h2>
      <el-button type="primary" size="default" @click="handleCreate">
        <el-icon><Plus /></el-icon> 生成邀请码
      </el-button>
    </div>

    <!-- 搜索区域 - 使用 SimpleQueryFilter 组件 -->
    <SimpleQueryFilter
      v-model="searchForm"
      :search-loading="loading"
      @search="handleSearch"
      @reset="handleReset"
    >
      <template #filters>
        <el-form-item label="邀请码">
          <el-input
            v-model="searchForm.code"
            placeholder="请输入邀请码"
            clearable
            style="width: 240px"
          />
        </el-form-item>
      </template>
    </SimpleQueryFilter>

    <!-- 数据表格区 -->
    <el-card class="table-card" shadow="hover">
      <el-table
        :data="tableData"
        v-loading="loading"
        border
        stripe
        highlight-current-row
        style="width: 100%"
        :row-class-name="tableRowClassName"
      >
        <el-table-column prop="code" label="邀请码" width="180" align="center">
          <template #default="{ row }">
            <div class="code-cell">
              <el-tag>{{ row.code }}</el-tag>
              <el-button link :icon="DocumentCopy" @click="handleCopy(row.code)" title="复制" />
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="groupName" label="所属分组" width="130" align="center">
          <template #default="{ row }">
            {{ row.groupName || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="usedCount" label="已使用/最大次数" width="180" align="center">
          <template #default="{ row }">
            <el-tag type="info" size="small">
              {{ row.usedCount }} / {{ row.maxUses || '∞' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="有效期" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getRemainingTimeInfo(row.expireAt).tagType">
              {{ getRemainingTimeInfo(row.expireAt).text }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" align="center">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column prop="expireAt" label="过期时间" width="180" align="center">
          <template #default="{ row }">
            {{ formatDate(row.expireAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" :icon="Edit" @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button type="danger" link size="small" :icon="Delete" @click="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
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

    <!-- 新增/编辑侧边栏 -->
    <el-drawer
      :title="drawerTitle"
      v-model="drawerVisible"
      size="40%"
      :show-close="false"
      :before-close="handleDrawerClose"
    >
      <InviteCodeForm
        ref="formRef"
        :form-data="formData"
        :is-edit="isEdit"
        @submit-form="handleSubmit"
        @cancel-form="handleCancel"
      />
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete, DocumentCopy } from '@element-plus/icons-vue'
import SimpleQueryFilter from '@/components/ common/SimpleQueryFilter.vue'
import { inviteCodeApi } from '@/api/inviteCode'
import { copyWithFeedback } from '@/utils/clipboard'
import dayjs from 'dayjs'
import InviteCodeForm from './InviteCodeForm.vue' // 导入新组件
import { getRemainingTimeInfo } from '@/utils/date'

// 响应式数据
const loading = ref(false)
const tableData = ref([])
const drawerVisible = ref(false)
const isEdit = ref(false)
const drawerTitle = ref('生成邀请码')
const formData = reactive({})
const formRef = ref(null)

// 搜索表单
const searchForm = reactive({
  code: '',
})

// 分页数据
const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0,
})

// 获取邀请码列表
const getInviteCodeList = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      ...searchForm,
    }
    const { records = [], total = 0 } = await inviteCodeApi.getPage(params)
    tableData.value = records
    pagination.total = total
  } catch (error) {
    console.error('获取邀请码列表失败', error)
    ElMessage.error('获取邀请码列表失败')
  } finally {
    loading.value = false
  }
}

// 格式化日期
const formatDate = (date) => {
  if (!date) return '-'
  return dayjs(date).format('YYYY-MM-DD HH:mm:ss')
}

// 复制邀请码
const handleCopy = (code) => {
  copyWithFeedback(code, '已复制邀请码', '复制失败，请手动复制')
}

// 搜索
const handleSearch = () => {
  pagination.pageNum = 1
  getInviteCodeList()
}

// 重置搜索
const handleReset = () => {
  Object.assign(searchForm, {
    code: '',
  })
  pagination.pageNum = 1
  getInviteCodeList()
}

// 分页大小变化
const handleSizeChange = (size) => {
  pagination.pageSize = size
  getInviteCodeList()
}

// 当前页变化
const handleCurrentChange = (page) => {
  pagination.pageNum = page
  getInviteCodeList()
}

// 新建邀请码
const handleCreate = () => {
  isEdit.value = false
  drawerTitle.value = '生成邀请码'
  Object.assign(formData, {
    id: '',
    maxUses: 100,
    expireAt: null,
    groupId: '',
    groupName: '',
  })
  drawerVisible.value = true
}

// 编辑邀请码
const handleEdit = (row) => {
  isEdit.value = true
  drawerTitle.value = '编辑邀请码'
  Object.assign(formData, {
    id: row.id,
    maxUses: row.maxUses,
    expireAt: dayjs(row.expireAt).format('YYYY-MM-DD HH:mm:ss'),
    groupId: row.groupId || '',
    groupName: row.groupName || '',
  })
  drawerVisible.value = true
}

// 删除邀请码
const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除邀请码"${row.code}"吗？此操作不可恢复。`, '确认删除', {
    confirmButtonText: '删除',
    cancelButtonText: '取消',
    type: 'warning',
    confirmButtonClass: 'el-button--danger',
  }).then(async () => {
    try {
      await inviteCodeApi.delete(row.id)
      ElMessage.success('删除成功')
      getInviteCodeList()
    } catch (error) {
      console.error('删除邀请码失败', error)
      ElMessage.error('删除失败')
    }
  })
}

// 提交表单处理
const handleSubmit = async (data) => {
  loading.value = true
  try {
    if (data.id) {
      await inviteCodeApi.update(data)
      ElMessage.success('更新成功')
    } else {
      await inviteCodeApi.create(data)
      ElMessage.success('创建成功')
    }
    drawerVisible.value = false
    getInviteCodeList()
  } catch (error) {
    console.error('提交表单失败', error)
    ElMessage.error(isEdit.value ? '更新失败' : '创建失败')
  } finally {
    loading.value = false
  }
}

// 取消表单
const handleCancel = () => {
  drawerVisible.value = false
}

// 关闭抽屉前检查
const handleDrawerClose = () => {
  if (formRef.value?.isFormModified()) {
    return ElMessageBox.confirm('表单内容已修改，确定要取消吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info',
    })
  }
  return true
}

// 表格行样式
const tableRowClassName = ({ rowIndex }) => {
  return rowIndex % 2 === 0 ? 'table-row-even' : 'table-row-odd'
}

// 初始化
onMounted(() => {
  getInviteCodeList()
})
</script>

// 替换样式部分
<style scoped lang="scss">
@use '@/styles/common.scss';

// 保留页面特有样式
.invite-code-container {
  @extend .page-container;
}

.code-cell {
  display: flex;
  align-items: center;
  gap: 8px;
  .el-tag {
    flex: 1;
  }
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 30px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
  button {
    margin-left: 12px;
    width: 100px;
  }
}

.el-form-item__tip {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}
</style>
