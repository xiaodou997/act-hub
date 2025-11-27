<template>
  <div class="role-detail-container">
    <div class="role-header">
      <el-avatar :size="80" :src="getRoleAvatar(roleData.name)" />
      <div class="role-info">
        <h2>{{ roleData.name }}</h2>
        <div class="role-meta">
          <el-tag type="success" size="large">
            {{ roleData.code }}
          </el-tag>
        </div>
      </div>
    </div>

    <el-divider />

    <el-descriptions :column="2" border>
      <el-descriptions-item label="角色名称">{{ roleData.name }}</el-descriptions-item>
      <el-descriptions-item label="角色编码">{{ roleData.code }}</el-descriptions-item>
      <el-descriptions-item label="创建时间">{{
        formatDate(roleData.createdAt)
      }}</el-descriptions-item>
      <el-descriptions-item label="更新时间">{{
        formatDate(roleData.updatedAt)
      }}</el-descriptions-item>
      <el-descriptions-item label="角色描述" :span="2">
        {{ roleData.description || '暂无描述' }}
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
  roleData: {
    type: Object,
    required: true,
    default: () => ({}),
  },
})

defineEmits(['close'])

// 获取角色头像
const getRoleAvatar = (name) => {
  const firstChar = name ? name.charAt(0) : 'R'
  return `https://ui-avatars.com/api/?name=${firstChar}&background=409eff&color=fff&size=128`
}

// 格式化日期
const formatDate = (date) => {
  if (!date) return '-'
  return dayjs(date).format('YYYY-MM-DD HH:mm:ss')
}
</script>

<style scoped lang="scss">
.role-detail-container {
  padding: 20px;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.role-header {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 16px;

  .role-info {
    flex: 1;

    h2 {
      margin: 0 0 8px 0;
      font-size: 24px;
      color: #1a1a1a;
    }

    .role-meta {
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
