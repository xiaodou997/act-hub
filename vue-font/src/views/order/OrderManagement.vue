<template>
  <div class="order-management-container">
    <!-- 页面标题与操作区 -->
    <div class="page-header">
      <h2>订单管理</h2>
      <el-button type="primary" size="default" @click="handleCreate">
        <el-icon><Plus /></el-icon>&nbsp;新建订单
      </el-button>
    </div>

    <!-- 搜索区域 -->
    <div class="search-container">
      <SimpleQueryFilter v-model="searchForm" @search="handleSearch" @reset="handleReset">
        <!-- 筛选项 -->
        <template #filters>
          <el-form-item label="订单编号">
            <el-input
              v-model="searchForm.orderNo"
              placeholder="请输入订单编号"
              clearable
              style="width: 200px"
            />
          </el-form-item>
          <el-form-item label="租户名称">
            <el-input
              v-model="searchForm.tenantName"
              placeholder="请输入租户名称"
              clearable
              style="width: 200px"
            />
          </el-form-item>
          <el-form-item label="订单状态">
            <el-select
              v-model="searchForm.paymentStatus"
              placeholder="请选择订单状态"
              clearable
              style="width: 200px"
            >
              <el-option label="已失效" :value="0" />
              <el-option label="有效" :value="1" />
            </el-select>
          </el-form-item>
          <el-form-item label="订单类型">
            <el-select
              v-model="searchForm.orderType"
              placeholder="请选择订单类型"
              clearable
              style="width: 200px"
            >
              <el-option label="普通订单" value="normal" />
              <el-option label="赠送订单" value="gift" />
              <el-option label="调整订单" value="adjustment" />
            </el-select>
          </el-form-item>
        </template>
      </SimpleQueryFilter>
    </div>

    <!-- 数据表格区 -->
    <el-card class="table-card" shadow="hover">
      <div class="table-empty" v-if="!tableData.length && !loading">
        <el-empty description="暂无订单数据"></el-empty>
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
        <el-table-column prop="orderNo" label="订单编号" min-width="180" />
        <el-table-column prop="tenantName" label="租户名称" min-width="150" />
        <el-table-column prop="paidAmount" label="实付金额" min-width="100">
          <template #default="{ row }">
            {{ formatPrice(row.paidAmount) }}
          </template>
        </el-table-column>
        <el-table-column prop="orderType" label="订单类型" min-width="100">
          <template #default="{ row }">
            <el-tag
              :type="
                row.orderType === 'normal'
                  ? 'primary'
                  : row.orderType === 'gift'
                    ? 'success'
                    : 'warning'
              "
              size="small"
            >
              {{
                row.orderType === 'normal'
                  ? '普通订单'
                  : row.orderType === 'gift'
                    ? '赠送订单'
                    : '调整订单'
              }}
            </el-tag>
          </template>
        </el-table-column>
        <!-- <el-table-column prop="paymentStatus" label="支付状态" min-width="100">
          <template #default="{ row }">
            <el-tag :type="row.paymentStatus === 1 ? 'success' : 'danger'" size="small">
              {{ row.paymentStatus === 1 ? '已支付' : '未支付' }}
            </el-tag>
          </template>
        </el-table-column> -->
        <el-table-column prop="orderStatus" label="订单状态" min-width="100">
          <template #default="{ row }">
            <el-tag :type="row.orderStatus === 1 ? 'success' : 'danger'" size="small">
              {{ row.orderStatus === 1 ? '有效' : '已失效' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="validFrom" label="授权生效时间" width="180" align="center">
          <template #default="{ row }">
            <span class="date-time">{{ formatDate(row.validFrom) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="validTo" label="授权截止时间" width="180" align="center">
          <template #default="{ row }">
            <span class="date-time">{{ formatDate(row.validTo) }}</span>
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
    <el-drawer v-model="viewDrawerVisible" title="订单详情" size="50%">
      <OrderDetailView :order-data="viewData" @close="viewDrawerVisible = false" />
    </el-drawer>

    <!-- 编辑/新建侧边栏 -->
    <el-drawer
      v-model="editDrawerVisible"
      :title="isEdit ? '编辑订单' : '新建订单'"
      size="50%"
      :show-close="false"
      :before-close="handleEditClose"
    >
      <OrderFormFactory
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
import { orderApi } from '@/api/order'
import dayjs from 'dayjs'
import OrderDetailView from './OrderDetailView.vue'
import OrderFormFactory from './OrderFormFactory.vue'

// 响应式数据
const loading = ref(false)
const tableData = ref([])
const selectedRows = ref([])

// 搜索表单
const searchForm = reactive({
  orderNo: '',
  tenantName: '',
  paymentStatus: '',
  orderType: '',
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
  tenantId: '',
  orderNo: '',
  paidAmount: 0,
  validFrom: 0,
  validTo: 0,
  appliesToAllFeatures: 0,
  defaultDiscountRate: 100,
  orderType: 'normal',
  paymentStatus: 0,
  description: '',
  remark: '',
  orderFeatureList: [],
})

// 查看详情数据
const viewData = reactive({})

// 获取订单列表
const getOrderList = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      ...searchForm,
    }
    const { records = [], total = 0 } = await orderApi.getPage(params)
    tableData.value = records
    pagination.total = total
  } catch (error) {
    console.error('get order info failed', error)
    ElMessage.error('获取订单列表失败')
  } finally {
    loading.value = false
  }
}

// 格式化日期
const formatDate = (date) => {
  if (!date) return '-'
  return dayjs(date).format('YYYY-MM-DD HH:mm:ss')
}

// 格式化价格
const formatPrice = (price) => {
  if (!price) return '¥0.00'
  return `¥${(price / 100).toFixed(2)}`
}

// 搜索
const handleSearch = () => {
  pagination.pageNum = 1
  getOrderList()
}

// 重置搜索
const handleReset = () => {
  Object.assign(searchForm, {
    orderNo: '',
    tenantName: '',
    paymentStatus: '',
    orderType: '',
  })
  pagination.pageNum = 1
  getOrderList()
}

// 选择变化
const handleSelectionChange = (selection) => {
  selectedRows.value = selection
}

// 分页大小变化
const handleSizeChange = (size) => {
  pagination.pageSize = size
  getOrderList()
}

// 当前页变化
const handleCurrentChange = (page) => {
  pagination.pageNum = page
  getOrderList()
}

// 新建订单
const handleCreate = () => {
  isEdit.value = false
  resetForm()
  editDrawerVisible.value = true
}

// 编辑订单
const handleEdit = (row) => {
  isEdit.value = true
  Object.assign(formData, {
    id: row.id,
    tenantId: row.tenantId,
    orderNo: row.orderNo,
    paidAmount: row.paidAmount,
    validFrom: row.validFrom,
    validTo: row.validTo,
    appliesToAllFeatures: row.appliesToAllFeatures,
    defaultDiscountRate: row.defaultDiscountRate,
    orderType: row.orderType,
    paymentStatus: row.paymentStatus,
    description: row.description,
    remark: row.remark,
    orderFeatureList: row.orderFeatureList || [],
  })
  editDrawerVisible.value = true
}

// 查看详情
const handleView = (row) => {
  Object.assign(viewData, row)
  viewDrawerVisible.value = true
}

// 删除订单
const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除订单"${row.orderNo}"吗？此操作不可恢复。`, '确认删除', {
    confirmButtonText: '删除',
    cancelButtonText: '取消',
    type: 'warning',
    confirmButtonClass: 'el-button--danger',
  }).then(async () => {
    try {
      await orderApi.delete(row.id)
      ElMessage.success('删除成功')
      getOrderList()
    } catch (error) {
      console.error('delete order failed', error)
      ElMessage.error('删除失败')
    }
  })
}

// 提交表单
const handleSubmit = async (formData) => {
  try {
    if (isEdit.value) {
      await orderApi.update(formData)
      ElMessage.success('更新成功')
    } else {
      await orderApi.create(formData)
      ElMessage.success('创建成功')
    }
    editDrawerVisible.value = false
    getOrderList()
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
    tenantId: '',
    orderNo: '',
    paidAmount: 0,
    validFrom: 0,
    validTo: 0,
    appliesToAllFeatures: 0,
    defaultDiscountRate: 100,
    orderType: 'normal',
    paymentStatus: 0,
    description: '',
    remark: '',
    orderFeatureList: [],
  })
}

// 初始化
onMounted(() => {
  getOrderList()
})
</script>

<style scoped lang="scss">
@use '@/styles/common.scss';

// 保留页面特有样式
.order-management-container {
  @extend .page-container;
}

.date-time {
  display: flex;
  align-items: center;
  color: #666;
}
</style>
