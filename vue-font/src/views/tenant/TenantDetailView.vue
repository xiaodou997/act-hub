<template>
  <div class="tenant-detail-container">
    <div class="tenant-header">
      <el-avatar :size="80" :src="getTenantAvatar(tenantData.name)" />
      <div class="tenant-info">
        <h2>{{ tenantData.name }}</h2>
        <div class="tenant-meta">
          <el-tag :type="getStatusType(tenantData.status)" size="large">
            {{ getStatusText(tenantData.status) }}
          </el-tag>
        </div>
      </div>
    </div>

    <el-divider />

    <el-descriptions :column="2" border>
      <el-descriptions-item label="租户名称">{{ tenantData.name }}</el-descriptions-item>
      <el-descriptions-item label="联系人">{{ tenantData.contactPerson }}</el-descriptions-item>
      <el-descriptions-item label="联系邮箱">{{ tenantData.contactEmail }}</el-descriptions-item>
      <el-descriptions-item label="联系电话">{{ tenantData.contactPhone }}</el-descriptions-item>
      <el-descriptions-item label="创建时间">{{
        formatDate(tenantData.createdAt)
      }}</el-descriptions-item>
      <el-descriptions-item label="更新时间">{{
        formatDate(tenantData.updatedAt)
      }}</el-descriptions-item>
      <el-descriptions-item label="租户描述" :span="2">
        {{ tenantData.remark || '暂无描述' }}
      </el-descriptions-item>
    </el-descriptions>

    <div class="drawer-footer">
      <el-button type="primary" @click="$emit('close')">关闭</el-button>
    </div>
  </div>
</template>

<script setup>
import dayjs from 'dayjs'

defineProps({
  tenantData: {
    type: Object,
    required: true,
    default: () => ({}),
  },
})

// 获取租户头像
const getTenantAvatar = (name) => {
  const firstChar = name ? name.charAt(0) : 'T'
  return `https://ui-avatars.com/api/?name=${firstChar}&background=409eff&color=fff&size=128`
}

// 状态类型映射
const getStatusType = (status) => {
  const statusMap = {
    1: 'success',
    2: 'warning',
    3: 'danger',
  }
  return statusMap[status] || 'info'
}

// 状态文本映射
const getStatusText = (status) => {
  const statusMap = {
    1: '正常',
    2: '到期',
    3: '禁用',
  }
  return statusMap[status] || '未知'
}

// 格式化日期
const formatDate = (date) => {
  if (!date) return '-'
  return dayjs(date).format('YYYY-MM-DD HH:mm:ss')
}
</script>

<style scoped lang="scss">
.tenant-detail-container {
  padding: 20px;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.tenant-header {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 16px;

  .tenant-info {
    flex: 1;

    h2 {
      margin: 0 0 8px 0;
      font-size: 24px;
      color: #1a1a1a;
    }

    .tenant-meta {
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

.drawer-footer {
  margin-top: auto;
  display: flex;
  justify-content: flex-end;
  padding-top: 16px;
  border-top: 1px solid #eee;
}
</style>
