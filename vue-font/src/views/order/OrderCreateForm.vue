<template>
  <div class="order-create-form">
    <!-- 步骤指示器 -->
    <div class="steps-container">
      <el-steps :active="currentStep" simple>
        <el-step title="填写基础信息" />
        <el-step title="选择关联功能" />
      </el-steps>
    </div>

    <!-- 步骤1:基础信息 -->
    <el-form
      v-if="currentStep === 0"
      ref="formRef"
      :model="formModel"
      :rules="formRules"
      label-width="140px"
      label-position="right"
      class="base-info-form"
    >
      <el-form-item label="租户名称" prop="tenantId">
        <TenantSelect v-model="localFormData.tenantId" />
      </el-form-item>

      <el-form-item label="实付金额" prop="paidAmount">
        <el-input-number
          v-model="paidAmount"
          :min="0"
          :precision="2"
          placeholder="请输入实付金额"
          style="width: 100%"
        >
          <template #append>元</template>
        </el-input-number>
      </el-form-item>

      <el-form-item label="授权生效时间" prop="validFromDate">
        <el-date-picker
          v-model="validFromDate"
          type="datetime"
          placeholder="选择授权生效时间"
          value-format="YYYY-MM-DD HH:mm:ss"
          style="width: 100%"
        />
      </el-form-item>

      <el-form-item label="授权截止时间" prop="validToDate">
        <el-date-picker
          v-model="validToDate"
          type="datetime"
          placeholder="选择授权截止时间"
          value-format="YYYY-MM-DD HH:mm:ss"
          style="width: 100%"
        />
      </el-form-item>

      <el-form-item label="订单类型" prop="orderType">
        <el-select
          v-model="localFormData.orderType"
          placeholder="请选择订单类型"
          style="width: 100%"
        >
          <el-option label="普通订单" value="normal" />
          <el-option label="赠送订单" value="gift" />
          <el-option label="调整订单" value="adjustment" />
        </el-select>
      </el-form-item>

      <el-form-item label="订单描述" prop="description">
        <el-input
          v-model="localFormData.description"
          type="textarea"
          :rows="4"
          placeholder="请输入订单描述"
          maxlength="500"
          show-word-limit
        />
      </el-form-item>

      <el-form-item label="内部备注" prop="remark">
        <el-input
          v-model="localFormData.remark"
          type="textarea"
          :rows="4"
          placeholder="请输入内部备注"
          maxlength="500"
          show-word-limit
        />
      </el-form-item>
    </el-form>

    <!-- 步骤2:选择功能 -->
    <div v-else-if="currentStep === 1" class="feature-selection-step">
      <el-form label-width="140px" label-position="right">
        <el-form-item label="适用所有功能">
          <div class="switch-with-tip">
            <el-switch v-model="appliesToAllFeatures" />
            <span class="switch-tip">开启后将自动包含系统所有功能，后续新增功能自动包含</span>
          </div>
        </el-form-item>

        <el-form-item label="默认折扣率" prop="defaultDiscountRate">
          <el-input-number
            v-model="localFormData.defaultDiscountRate"
            :min="0"
            :max="100"
            placeholder="请输入默认折扣率"
            style="width: 100%"
          >
            <template #append>%</template>
          </el-input-number>
          <div class="form-item-hint">例如:80 表示 8 折优惠</div>
        </el-form-item>

        <el-form-item label="赠送体验次数" prop="defaultQuantity">
          <el-input-number
            v-model="localFormData.defaultQuantity"
            :min="0"
            :max="9999"
            placeholder="请输入使用次数"
            style="width: 100%"
          >
            <template #append>次</template>
          </el-input-number>
          <div class="form-item-hint">设置为 0 表示不赠送使用次数</div>
        </el-form-item>
      </el-form>

      <!-- 功能选择列表 -->
      <div v-if="!appliesToAllFeatures" class="feature-list-section">
        <div class="section-header">
          <h3>已选功能列表</h3>
          <el-button type="primary" @click="showFeatureSelector">
            <el-icon><Plus /></el-icon>
            添加功能
          </el-button>
        </div>

        <el-table
          v-if="localFormData.orderFeatureList.length > 0"
          :data="localFormData.orderFeatureList"
          style="width: 100%"
          border
          stripe
        >
          <el-table-column type="index" label="序号" width="60" align="center" />

          <el-table-column prop="featureName" label="功能名称" min-width="150" />

          <el-table-column label="原价" width="120" align="right">
            <template #default="{ row }">
              <span class="price-text">¥{{ row.originalPrice.toFixed(2) }}</span>
            </template>
          </el-table-column>

          <el-table-column label="折扣率" width="150" align="center">
            <template #default="{ row }">
              <el-input-number
                v-model="row.discountRate"
                :min="0"
                :max="100"
                size="small"
                @change="updateActualPrice(row)"
                @focus="() => (row._discountRateManuallyModified = true)"
              >
                <template #append>%</template>
              </el-input-number>
            </template>
          </el-table-column>

          <el-table-column label="实际价格" width="150" align="center">
            <template #default="{ row }">
              <el-input-number
                v-model="row.actualPrice"
                :min="0"
                :precision="2"
                size="small"
                @change="updateDiscountRate(row)"
                @focus="() => (row._discountRateManuallyModified = true)"
              >
                <template #prepend>¥</template>
              </el-input-number>
            </template>
          </el-table-column>

          <el-table-column label="赠送次数" width="150" align="center">
            <template #default="{ row }">
              <el-input-number
                v-model="row.quantity"
                :min="0"
                :max="9999"
                size="small"
                @focus="() => (row._quantityManuallyModified = true)"
              >
                <template #append>次</template>
              </el-input-number>
            </template>
          </el-table-column>

          <el-table-column label="操作" width="80" align="center" fixed="right">
            <template #default="{ $index }">
              <el-button
                type="danger"
                size="small"
                :icon="Delete"
                @click="removeFeature($index)"
                circle
              />
            </template>
          </el-table-column>
        </el-table>

        <el-empty v-else description="暂无功能数据，请点击添加功能按钮" :image-size="120" />
      </div>

      <div v-else class="all-features-tip">
        <el-alert title="已选择适用所有功能" type="success" :closable="false">
          <template #default>
            <div>订单将包含系统所有可用功能以及后续新增功能，无需额外选择功能列表</div>
            <div v-if="backupFeatureList.length > 0" class="backup-hint">
              💡 您之前选择的
              {{ backupFeatureList.length }} 个功能已暂时隐藏，关闭此开关后会自动恢复
            </div>
          </template>
        </el-alert>
      </div>
    </div>

    <!-- 底部按钮 -->
    <div class="form-footer">
      <el-button v-if="currentStep > 0" @click="prevStep">
        <el-icon><ArrowLeft /></el-icon>
        上一步
      </el-button>
      <el-button v-else @click="handleCancel">取消</el-button>

      <el-button v-if="currentStep < 1" type="primary" @click="nextStep" :loading="submitting">
        下一步
        <el-icon><ArrowRight /></el-icon>
      </el-button>
      <el-button v-else type="primary" @click="handleSubmit" :loading="submitting">
        <el-icon><Check /></el-icon>
        创建订单
      </el-button>
    </div>

    <!-- 功能选择对话框 -->
    <FeatureSelector
      v-model:visible="featureSelectorVisible"
      :selected-feature-ids="selectedFeatureIds"
      @confirm="handleFeatureConfirm"
    />
  </div>
</template>

<script setup>
import { ref, watch, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete, Plus, ArrowLeft, ArrowRight, Check } from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import TenantSelect from '@/components/TenantSelect.vue'
import FeatureSelector from '@/components/FeatureSelector.vue'

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
const currentStep = ref(0)

const localFormData = ref({
  id: '',
  tenantId: '',
  orderNo: '',
  paidAmount: 0,
  validFrom: 0,
  validTo: 0,
  appliesToAllFeatures: 0,
  defaultDiscountRate: 0,
  defaultQuantity: 0,
  orderType: 'normal',
  paymentStatus: 0,
  description: '',
  remark: '',
  orderFeatureList: [],
})

// 备份功能列表（用于适用所有功能开关时的恢复）
const backupFeatureList = ref([])

// 用于表单绑定的中间变量
const paidAmount = ref(0)
const validFromDate = ref('')
const validToDate = ref('')

// 计算属性:适用所有功能(转换为布尔值方便使用)
const appliesToAllFeatures = computed({
  get: () => localFormData.value.appliesToAllFeatures === 1,
  set: (val) => {
    localFormData.value.appliesToAllFeatures = val ? 1 : 0
  },
})

// 已选功能ID列表
const selectedFeatureIds = computed(() => localFormData.value.orderFeatureList.map((f) => f.id))

// 创建表单模型
const formModel = computed(() => ({
  tenantId: localFormData.value.tenantId,
  paidAmount: paidAmount.value,
  validFromDate: validFromDate.value,
  validToDate: validToDate.value,
  orderType: localFormData.value.orderType,
  description: localFormData.value.description,
  remark: localFormData.value.remark,
  appliesToAllFeatures: localFormData.value.appliesToAllFeatures,
  defaultDiscountRate: localFormData.value.defaultDiscountRate,
  defaultQuantity: localFormData.value.defaultQuantity,
}))

// 功能选择对话框
const featureSelectorVisible = ref(false)

// 初始化本地数据
watch(
  () => props.formData,
  (newValue) => {
    currentStep.value = 0
    backupFeatureList.value = []
    localFormData.value = { ...newValue }

    if (
      localFormData.value.defaultQuantity === undefined ||
      localFormData.value.defaultQuantity === null
    ) {
      localFormData.value.defaultQuantity = 0
    }

    paidAmount.value = newValue.paidAmount ? newValue.paidAmount : 1000

    const now = dayjs()
    const oneYearLater = now.add(1, 'year')

    validFromDate.value = newValue.validFrom
      ? dayjs(newValue.validFrom).format('YYYY-MM-DD HH:mm:ss')
      : now.format('YYYY-MM-DD HH:mm:ss')

    validToDate.value = newValue.validTo
      ? dayjs(newValue.validTo).format('YYYY-MM-DD HH:mm:ss')
      : oneYearLater.format('YYYY-MM-DD HH:mm:ss')

    localFormData.value.paidAmount = Math.round(paidAmount.value * 100)
    localFormData.value.validFrom = dayjs(validFromDate.value).valueOf()
    localFormData.value.validTo = dayjs(validToDate.value).valueOf()
  },
  { immediate: true, deep: true },
)

// 监听中间变量变化
watch(paidAmount, (newValue) => {
  localFormData.value.paidAmount = Math.round(newValue * 100)
})

watch(validFromDate, (newValue) => {
  localFormData.value.validFrom = newValue ? dayjs(newValue).valueOf() : 0
})

watch(validToDate, (newValue) => {
  localFormData.value.validTo = newValue ? dayjs(newValue).valueOf() : 0
})

// 监听适用所有功能变化
watch(appliesToAllFeatures, async (newValue, oldValue) => {
  // 只在用户操作时触发，避免初始化时触发
  if (oldValue === undefined) return

  if (newValue) {
    // 开启时：如果有已选功能，提示用户
    if (localFormData.value.orderFeatureList.length > 0) {
      try {
        await ElMessageBox.confirm(
          `当前已选择 ${localFormData.value.orderFeatureList.length} 个功能，开启"适用所有功能"后这些功能将被暂时隐藏。关闭开关后会自动恢复，是否继续？`,
          '提示',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning',
          },
        )
        // 用户确认：备份当前功能列表，然后清空
        backupFeatureList.value = JSON.parse(JSON.stringify(localFormData.value.orderFeatureList))
        localFormData.value.orderFeatureList = []
        ElMessage.success('已开启适用所有功能，原有功能列表已备份')
      } catch {
        // 用户取消：恢复开关状态
        localFormData.value.appliesToAllFeatures = 0
      }
    }
  } else {
    // 关闭时：恢复之前备份的功能列表
    if (backupFeatureList.value.length > 0) {
      localFormData.value.orderFeatureList = JSON.parse(JSON.stringify(backupFeatureList.value))
      ElMessage.success(`已恢复 ${backupFeatureList.value.length} 个功能`)
      // 清空备份
      backupFeatureList.value = []
    }
  }
})

// 【修复】监听默认折扣率变化
watch(
  () => localFormData.value.defaultDiscountRate,
  (newValue) => {
    if (localFormData.value.orderFeatureList.length > 0) {
      localFormData.value.orderFeatureList.forEach((feature) => {
        // 只更新未被用户手动修改过的折扣率
        if (!feature._discountRateManuallyModified) {
          feature.discountRate = newValue
          // 自动更新实际价格，但不标记为手动修改
          const originalPrice = feature.originalPrice || 0
          const discountRate = feature.discountRate || 0
          feature.actualPrice = Number(((originalPrice * discountRate) / 100).toFixed(2))
        }
      })
    }
  },
)

// 监听默认使用次数变化
watch(
  () => localFormData.value.defaultQuantity,
  (newValue) => {
    if (localFormData.value.orderFeatureList.length > 0) {
      localFormData.value.orderFeatureList.forEach((feature) => {
        // 只更新未被用户手动修改过的次数
        if (!feature._quantityManuallyModified) {
          feature.quantity = newValue
        }
      })
    }
  },
)

// 表单验证规则
const formRules = {
  tenantId: [{ required: true, message: '请选择租户', trigger: 'change' }],
  paidAmount: [
    { required: true, message: '请输入实付金额', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value === null || value === undefined || value === '') {
          callback(new Error('请输入实付金额'))
        } else if (value < 0) {
          callback(new Error('实付金额必须大于等于0'))
        } else {
          callback()
        }
      },
      trigger: 'blur',
    },
  ],
  validFromDate: [{ required: true, message: '请选择授权生效时间', trigger: 'change' }],
  validToDate: [
    { required: true, message: '请选择授权截止时间', trigger: 'change' },
    {
      validator: (rule, value, callback) => {
        if (!value) {
          callback(new Error('请选择授权截止时间'))
        } else if (validFromDate.value && dayjs(value).isBefore(dayjs(validFromDate.value))) {
          callback(new Error('截止时间必须晚于生效时间'))
        } else {
          callback()
        }
      },
      trigger: 'change',
    },
  ],
  orderType: [{ required: true, message: '请选择订单类型', trigger: 'change' }],
  defaultDiscountRate: [
    { required: true, message: '请输入默认折扣率', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value === null || value === undefined || value === '') {
          callback(new Error('请输入默认折扣率'))
        } else if (value < 0 || value > 100) {
          callback(new Error('折扣率必须在0-100之间'))
        } else {
          callback()
        }
      },
      trigger: 'blur',
    },
  ],
  defaultQuantity: [
    { required: true, message: '请输入赠送授权使用次数', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value === null || value === undefined || value === '') {
          callback(new Error('请输入赠送授权使用次数'))
        } else if (value < 0) {
          callback(new Error('授权使用次数必须大于等于0'))
        } else if (value > 9999) {
          callback(new Error('授权使用次数不能超过9999'))
        } else {
          callback()
        }
      },
      trigger: 'blur',
    },
  ],
}

// 下一步
const nextStep = async () => {
  try {
    const valid = await formRef.value.validate()
    if (valid) {
      currentStep.value++
    }
  } catch (error) {
    ElMessage.error('请正确填写表单')
  }
}

// 上一步
const prevStep = () => {
  currentStep.value--
}

// 提交表单
const handleSubmit = async () => {
  // 验证功能列表
  if (!appliesToAllFeatures.value && localFormData.value.orderFeatureList.length === 0) {
    ElMessage.warning('请至少选择一个功能或开启"适用所有功能"')
    return
  }

  try {
    submitting.value = true

    const submitData = {
      ...localFormData.value,
      orderFeatureList: localFormData.value.orderFeatureList.map((feature) => ({
        ...feature,
        unitPrice: (feature.actualPrice || 0) * 100,
        featureId: feature.id,
      })),
    }

    emit('submit', submitData)
  } catch (error) {
    ElMessage.error('提交失败，请检查表单数据')
  } finally {
    submitting.value = false
  }
}

// 取消
const handleCancel = () => {
  emit('cancel')
}

// 显示功能选择器
const showFeatureSelector = () => {
  featureSelectorVisible.value = true
}

// 确认选择功能
const handleFeatureConfirm = (selectedFeatures) => {
  const existingFeatureIds = localFormData.value.orderFeatureList.map((f) => f.id)

  const newFeatures = selectedFeatures
    .filter((f) => !existingFeatureIds.includes(f.id))
    .map((f) => {
      const originalPrice = (f.price || 0) / 100
      const discountRate = localFormData.value.defaultDiscountRate
      const actualPrice = Number(((originalPrice * discountRate) / 100).toFixed(2))

      return {
        id: f.id,
        featureId: f.id,
        featureName: f.name,
        featureDescription: f.description,
        originalPrice: originalPrice,
        discountRate: discountRate,
        actualPrice: actualPrice,
        quantity: localFormData.value.defaultQuantity,
        // 标记：是否由用户手动修改过折扣率和次数
        _discountRateManuallyModified: false,
        _quantityManuallyModified: false,
      }
    })

  localFormData.value.orderFeatureList = [...localFormData.value.orderFeatureList, ...newFeatures]
}

// 移除功能
const removeFeature = (index) => {
  localFormData.value.orderFeatureList.splice(index, 1)
}

// 根据折扣率更新实际价格（用户手动修改时调用）
const updateActualPrice = (row) => {
  const originalPrice = row.originalPrice || 0
  const discountRate = row.discountRate || 0
  row.actualPrice = Number(((originalPrice * discountRate) / 100).toFixed(2))
  // 标记为手动修改（因为是用户触发的change事件）
  row._discountRateManuallyModified = true
}

// 根据实际价格更新折扣率（用户手动修改时调用）
const updateDiscountRate = (row) => {
  const originalPrice = row.originalPrice || 0
  const actualPrice = row.actualPrice || 0
  if (originalPrice > 0) {
    row.discountRate = Math.round((actualPrice / originalPrice) * 100)
  }
  // 标记为手动修改（因为是用户触发的change事件）
  row._discountRateManuallyModified = true
}

// 更新授权使用次数（移除了这个方法，因为逻辑已经在focus事件中处理）
</script>

<style scoped lang="scss">
.order-create-form {
  padding: 24px;
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #fff;
}

.steps-container {
  margin-bottom: 32px;

  :deep(.el-step__title) {
    font-size: 15px;
  }
}

.base-info-form {
  flex: 1;
  overflow-y: auto;
  padding-right: 8px;

  :deep(.el-form-item) {
    margin-bottom: 24px;

    .el-form-item__label {
      font-weight: 500;
      color: #303133;
    }
  }
}

.feature-selection-step {
  flex: 1;
  overflow-y: auto;

  .switch-with-tip {
    display: flex;
    align-items: center;
    gap: 12px;

    .switch-tip {
      color: #909399;
      font-size: 13px;
    }
  }

  .form-item-hint {
    color: #909399;
    font-size: 12px;
    margin-top: 6px;
    line-height: 1.5;
  }
}

.feature-list-section {
  margin-top: 24px;

  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;

    h3 {
      margin: 0;
      font-size: 16px;
      font-weight: 500;
      color: #303133;
    }
  }

  .price-text {
    color: #f56c6c;
    font-weight: 500;
  }

  :deep(.el-table) {
    .el-input-number {
      width: 100%;
    }
  }
}

.all-features-tip {
  margin-top: 24px;

  :deep(.el-alert__content) {
    line-height: 1.8;
  }

  .backup-hint {
    margin-top: 8px;
    padding-top: 8px;
    border-top: 1px dashed #b3d8ff;
    color: #409eff;
    font-size: 13px;
  }
}

.form-footer {
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
  display: flex;
  justify-content: flex-end;
  gap: 12px;

  .el-button {
    min-width: 110px;
    padding: 10px 24px;

    .el-icon {
      margin-right: 4px;
    }
  }
}

// 滚动条样式优化
::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

::-webkit-scrollbar-thumb {
  background: #dcdfe6;
  border-radius: 3px;

  &:hover {
    background: #c0c4cc;
  }
}

::-webkit-scrollbar-track {
  background: #f5f7fa;
}
</style>
