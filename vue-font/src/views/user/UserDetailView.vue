<template>
  <div class="user-detail-container">
    <div class="user-header">
      <el-avatar :size="80" :src="getUserAvatar(userData.username)" />
      <div class="user-info">
        <h2>{{ userData.username }}</h2>
        <div class="user-meta">
          <el-tag :type="getStatusType(userData.status)" size="large">
            {{ getStatusText(userData.status) }}
          </el-tag>
        </div>
      </div>
    </div>

    <el-divider />

    <el-descriptions :column="2" border>
      <el-descriptions-item label="用户ID">{{ userData.id || '-' }}</el-descriptions-item>
      <el-descriptions-item label="用户名">{{ userData.username }}</el-descriptions-item>
      <el-descriptions-item label="邮箱">{{ userData.email || '-' }}</el-descriptions-item>
      <!-- 租户相关展示已停用
      <el-descriptions-item label="所属租户">{{ userData.tenantName || '-' }}</el-descriptions-item>
      <el-descriptions-item label="租户ID">{{ userData.tenantId || '-' }}</el-descriptions-item>
      -->
      <el-descriptions-item label="最后登录时间">{{
        formatDate(userData.lastLoginAt)
      }}</el-descriptions-item>
      <el-descriptions-item label="创建时间">{{
        formatDate(userData.createdAt)
      }}</el-descriptions-item>
      <el-descriptions-item label="更新时间">{{
        formatDate(userData.updatedAt)
      }}</el-descriptions-item>
      <el-descriptions-item label="用户状态">{{
        getStatusText(userData.status)
      }}</el-descriptions-item>
      <el-descriptions-item label="备注" :span="2">
        {{ userData.remark || '暂无备注' }}
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
  userData: {
    type: Object,
    required: true,
    default: () => ({}),
  },
})

// 获取用户头像
const getUserAvatar = (username) => {
  const firstChar = username ? username.charAt(0).toUpperCase() : 'U'
  return `https://ui-avatars.com/api/?name=${firstChar}&background=409eff&color=fff&size=128`
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
</script>

<style scoped lang="scss">
.user-detail-container {
  padding: 20px;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.user-header {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 16px;

  .user-info {
    flex: 1;

    h2 {
      margin: 0 0 8px 0;
      font-size: 24px;
      color: #1a1a1a;
    }

    .user-meta {
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
