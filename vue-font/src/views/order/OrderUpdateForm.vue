<template>
  <div class="order-update-form">
    <!-- 订单基本信息展示 -->
    <el-card class="order-info-card" style="margin-bottom: 20px">
      <el-row :gutter="20">
        <el-col :span="8">
          <div class="info-item">
            <span class="info-label">租户名称：</span>
            <span class="info-value">{{ tenantName }}</span>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="info-item">
            <span class="info-label">订单类型：</span>
            <el-tag
              :type="
                localFormData.orderType === 'normal'
                  ? 'primary'
                  : localFormData.orderType === 'gift'
                    ? 'success'
                    : 'warning'
              "
              size="small"
            >
              {{
                localFormData.orderType === 'normal'
                  ? '普通订单'
                  : localFormData.orderType === 'gift'
                    ? '赠送订单'
                    : '调整订单'
              }}
            </el-tag>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="info-item">
            <span class="info-label">支付状态：</span>
            <el-tag :type="localFormData.paymentStatus === 1 ? 'success' : 'danger'" size="small">
              {{ localFormData.paymentStatus === 1 ? '已支付' : '未支付' }}
            </el-tag>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="info-item">
            <span class="info-label">授权生效时间：</span>
            <span class="info-value">{{ formatDate(localFormData.validFrom) }}</span>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="info-item">
            <span class="info-label">授权截止时间：</span>
            <span class="info-value">{{ formatDate(localFormData.validTo) }}</span>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <!-- 可编辑表单 -->
    <el-form
      ref="formRef"
      :model="localFormData"
      :rules="formRules"
      label-width="100px"
      label-position="right"
    >
      <el-form-item label="实付金额(元)" prop="paidAmount">
        <el-input v-model.number="paidAmount" placeholder="请输入实付金额" />
      </el-form-item>
      <el-form-item label="订单描述" prop="description">
        <el-input
          v-model="localFormData.description"
          type="textarea"
          :rows="4"
          placeholder="请输入订单描述"
        />
      </el-form-item>
      <el-form-item label="内部备注" prop="remark">
        <el-input
          v-model="localFormData.remark"
          type="textarea"
          :rows="4"
          placeholder="请输入内部备注"
        />
      </el-form-item>
    </el-form>

    <!-- 功能信息展示 -->
    <div class="feature-info-section" v-if="localFormData.appliesToAllFeatures === 0">
      <h3>关联功能列表（不可编辑）</h3>
      <el-table :data="localFormData.orderFeatureList" style="width: 100%" border>
        <el-table-column type="index" width="50" />
        <el-table-column prop="featureName" label="功能名称" width="200" />
        <el-table-column prop="featureCode" label="功能编码" width="180" />
        <el-table-column prop="discountRate" label="折扣率(%)" width="150" />
        <el-table-column prop="featureDescription" label="功能描述" show-overflow-tooltip />
      </el-table>
    </div>
    <div v-else class="all-features-tip">
      <el-alert
        title="适用所有功能"
        type="success"
        description="订单包含系统所有可用功能"
        show-icon
      />
    </div>

    <div class="form-footer">
      <el-button @click="handleCancel">取消</el-button>
      <el-button type="primary" @click="handleSubmit" :loading="submitting"> 更新 </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, computed } from 'vue'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'
import { tenantApi } from '@/api/tenant'

const props = defineProps({
  formData: {
    type: Object,
    required: true,
    default: () => ({}),
  },
})

const emit = defineEmits(['submit', 'cancel'])

const formRef = ref()
const submitting = ref(false)
const localFormData = ref({
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

// 租户名称
const tenantName = ref('')

// 用于表单绑定的中间变量
const paidAmount = ref(0)

// 初始化本地数据
watch(
  () => props.formData,
  async (newValue) => {
    localFormData.value = { ...newValue }
    // 初始化中间变量
    paidAmount.value = newValue.paidAmount ? newValue.paidAmount / 100 : 0

    // 获取租户名称
    if (newValue.tenantId) {
      await loadTenantInfo(newValue.tenantId)
    }
  },
  { immediate: true, deep: true },
)

// 监听实付金额变化
watch(paidAmount, (newValue) => {
  localFormData.value.paidAmount = Math.round(newValue * 100)
})

// 表单验证规则
const formRules = {
  paidAmount: [
    { required: true, message: '请输入实付金额', trigger: 'blur' },
    { type: 'number', min: 0, message: '实付金额必须大于等于0', trigger: 'blur' },
  ],
}

// 加载租户信息
const loadTenantInfo = async (tenantId) => {
  try {
    const tenant = await tenantApi.getById(tenantId)
    if (tenant) {
      tenantName.value = tenant.name
    }
  } catch (error) {
    console.error('load tenant info failed', error)
  }
}

// 格式化日期
const formatDate = (date) => {
  if (!date) return '-'
  return dayjs(date).format('YYYY-MM-DD HH:mm:ss')
}

// 提交表单
const handleSubmit = async () => {
  try {
    submitting.value = true

    // 处理提交数据 - 只包含可编辑的三个字段
    const submitData = {
      id: localFormData.value.id,
      paidAmount: localFormData.value.paidAmount,
      description: localFormData.value.description,
      remark: localFormData.value.remark,
    }

    emit('submit', submitData)
  } catch (error) {
    console.log('error', error)
    ElMessage.error('请正确填写表单')
  } finally {
    submitting.value = false
  }
}

// 取消
const handleCancel = () => {
  emit('cancel')
}
</script>

<style scoped lang="scss">
.order-update-form {
  padding: 20px;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.order-info-card {
  .info-item {
    margin-bottom: 10px;
    display: flex;
    align-items: center;

    .info-label {
      color: #666;
      font-size: 14px;
      min-width: 100px;
    }

    .info-value {
      color: #333;
      font-size: 14px;
    }
  }
}

.el-form {
  flex: 1;

  :deep(.el-form-item) {
    margin-bottom: 22px;

    .el-form-item__label {
      font-weight: 500;
    }
  }
}

// 功能信息区域
.feature-info-section {
  margin-top: 20px;

  h3 {
    margin: 0 0 16px 0;
    font-size: 16px;
    font-weight: 500;
    color: #333;
  }
}

// 适用所有功能的提示样式
.all-features-tip {
  margin-top: 20px;
  padding: 10px;
  background-color: #f0f9eb;
  border-radius: 4px;
}

.form-footer {
  margin-top: auto;
  display: flex;
  justify-content: flex-end;
  gap: 20px;
  padding-top: 16px;
  border-top: 1px solid #eee;

  .el-button {
    min-width: 120px;
    padding: 0 24px;
  }
}
</style>
