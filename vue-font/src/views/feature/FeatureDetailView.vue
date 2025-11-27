<template>
  <div class="feature-detail-container">
    <div class="feature-header">
      <el-avatar :size="80" :src="getFeatureAvatar(featureData.name)" />
      <div class="feature-info">
        <h2>{{ featureData.name }}</h2>
        <div class="feature-meta">
          <el-tag type="success" size="large">
            {{ featureData.code }}
          </el-tag>
          <el-tag :type="featureData.status === 1 ? 'success' : 'danger'">
            {{ featureData.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </div>
      </div>
    </div>

    <el-divider />

    <el-descriptions :column="2" border>
      <el-descriptions-item label="功能名称">{{ featureData.name }}</el-descriptions-item>
      <el-descriptions-item label="功能编码">{{ featureData.code }}</el-descriptions-item>
      <el-descriptions-item label="关联平台">
        <span v-if="featureData.platformName">
          {{ featureData.platformName }}
          <el-tag v-if="featureData.platformCode" size="small" style="margin-left: 8px">
            {{ featureData.platformCode }}
          </el-tag>
        </span>
        <span v-else style="color: #999">未关联平台</span>
      </el-descriptions-item>
      <el-descriptions-item label="价格">
        ¥{{ (featureData.price || 0) / 100 }}
      </el-descriptions-item>
      <el-descriptions-item label="状态">
        {{ featureData.status === 1 ? '启用' : '禁用' }}
      </el-descriptions-item>
      <el-descriptions-item label="排序">{{ featureData.sortOrder || 0 }}</el-descriptions-item>
      <el-descriptions-item label="创建时间">{{
        formatDate(featureData.createdAt)
      }}</el-descriptions-item>
      <el-descriptions-item label="更新时间">{{
        formatDate(featureData.updatedAt)
      }}</el-descriptions-item>
      <el-descriptions-item label="功能描述" :span="2">
        {{ featureData.description || '暂无描述' }}
      </el-descriptions-item>
      <el-descriptions-item v-if="featureData.platformDescription" label="平台描述" :span="2">
        {{ featureData.platformDescription }}
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
  featureData: {
    type: Object,
    required: true,
    default: () => ({}),
  },
})

defineEmits(['close'])

// 获取功能头像
const getFeatureAvatar = (name) => {
  const firstChar = name ? name.charAt(0) : 'F'
  return `https://ui-avatars.com/api/?name=${firstChar}&background=409eff&color=fff&size=128`
}

// 格式化日期（支持毫秒时间戳和日期字符串）
const formatDate = (date) => {
  if (!date) return '-'

  // 如果是数字（毫秒时间戳），转换为日期对象
  if (typeof date === 'number') {
    return dayjs(date).format('YYYY-MM-DD HH:mm:ss')
  }

  // 如果是字符串，直接格式化
  return dayjs(date).format('YYYY-MM-DD HH:mm:ss')
}
</script>

<style scoped lang="scss">
.feature-detail-container {
  padding: 20px;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.feature-header {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 16px;

  .feature-info {
    flex: 1;

    h2 {
      margin: 0 0 8px 0;
      font-size: 24px;
      color: #1a1a1a;
    }

    .feature-meta {
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
