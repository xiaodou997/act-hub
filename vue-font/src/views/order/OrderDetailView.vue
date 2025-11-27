<template>
  <div class="order-detail-container">
    <div class="order-header">
      <div class="order-info">
        <h2>{{ orderData.orderNo }}</h2>
        <div class="order-meta">
          <el-tag
            :type="orderData.paymentStatus === 1 ? 'success' : 'danger'"
            size="large"
            style="margin-right: 10px"
          >
            {{ orderData.paymentStatus === 1 ? '已支付' : '未支付' }}
          </el-tag>
          <el-tag
            :type="
              orderData.orderType === 'normal'
                ? 'primary'
                : orderData.orderType === 'gift'
                  ? 'success'
                  : 'warning'
            "
            size="large"
          >
            {{
              orderData.orderType === 'normal'
                ? '普通订单'
                : orderData.orderType === 'gift'
                  ? '赠送订单'
                  : '调整订单'
            }}
          </el-tag>
        </div>
      </div>
    </div>

    <el-divider />

    <el-descriptions :column="2" border>
      <el-descriptions-item label="租户名称">{{
        orderData.tenantName || '-'
      }}</el-descriptions-item>
      <el-descriptions-item label="实付金额">{{
        formatPrice(orderData.paidAmount)
      }}</el-descriptions-item>
      <el-descriptions-item label="授权生效时间">{{
        formatDate(orderData.validFrom)
      }}</el-descriptions-item>
      <el-descriptions-item label="授权截止时间">{{
        formatDate(orderData.validTo)
      }}</el-descriptions-item>
      <el-descriptions-item label="适用所有功能">
        <el-switch v-model="orderData.appliesToAllFeatures" disabled />
      </el-descriptions-item>
      <el-descriptions-item label="默认折扣率"
        >{{ orderData.defaultDiscountRate }}%</el-descriptions-item
      >
      <el-descriptions-item label="订单描述" :span="2">
        {{ orderData.description || '暂无描述' }}
      </el-descriptions-item>
      <el-descriptions-item label="内部备注" :span="2">
        {{ orderData.remark || '暂无备注' }}
      </el-descriptions-item>
      <el-descriptions-item label="创建时间">{{
        formatDate(orderData.createdAt)
      }}</el-descriptions-item>
      <el-descriptions-item label="更新时间">{{
        formatDate(orderData.updatedAt)
      }}</el-descriptions-item>
    </el-descriptions>

    <!-- 功能列表 -->
    <div
      class="feature-section"
      v-if="orderData.orderFeatureList && orderData.orderFeatureList.length > 0"
    >
      <h3>关联功能列表</h3>
      <el-table :data="orderData.orderFeatureList" style="width: 100%" border>
        <el-table-column prop="featureName" label="功能名称" width="200" />
        <el-table-column prop="featureCode" label="功能编码" width="180" />
        <el-table-column prop="discountRate" label="折扣率(%)" width="120" />
        <el-table-column prop="featureDescription" label="功能描述" show-overflow-tooltip />
      </el-table>
    </div>

    <div class="drawer-footer">
      <el-button type="primary" @click="$emit('close')">关闭</el-button>
    </div>
  </div>
</template>

<script setup>
import dayjs from 'dayjs'

defineProps({
  orderData: {
    type: Object,
    required: true,
    default: () => ({}),
  },
})

defineEmits(['close'])

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
</script>

<style scoped lang="scss">
.order-detail-container {
  padding: 20px;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.order-header {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 16px;

  .order-info {
    flex: 1;

    h2 {
      margin: 0 0 8px 0;
      font-size: 24px;
      color: #1a1a1a;
    }

    .order-meta {
      display: flex;
      align-items: center;
      gap: 16px;
    }
  }
}

.el-divider {
  margin: 16px 0;
}

.el-descriptions {
  margin-bottom: 20px;

  :deep(.el-descriptions__body) {
    .el-descriptions-item__cell {
      padding: 12px 16px;
    }
  }
}

.feature-section {
  margin-top: 20px;

  h3 {
    margin: 0 0 16px 0;
    font-size: 16px;
    font-weight: 500;
    color: #333;
  }
}

.drawer-footer {
  margin-top: auto;
  display: flex;
  justify-content: flex-end;
  padding-top: 16px;
  border-top: 1px solid #eee;
}
</style>
