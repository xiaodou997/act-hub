<template>
  <div class="permission-detail">
    <div class="header">
      <div class="header-info">
        <div class="avatar" :style="{ background: getAvatarBackground(modelData.name) }">
          {{ getAvatarText(modelData.name) }}
        </div>
        <div class="basic-info">
          <h3 class="title">{{ modelData.name || '--' }}</h3>
          <div class="status">
            <el-tag :type="getTypeTagType(modelData.type)" size="small">
              {{ getTypeName(modelData.type) }}
            </el-tag>
          </div>
        </div>
      </div>
    </div>

    <div class="detail-content">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="ID">
          {{ modelData.id || '--' }}
        </el-descriptions-item>
        <el-descriptions-item label="权限名称">
          {{ modelData.name || '--' }}
        </el-descriptions-item>
        <el-descriptions-item label="权限编码">
          {{ modelData.code || '--' }}
        </el-descriptions-item>
        <el-descriptions-item label="权限类型">
          <el-tag :type="getTypeTagType(modelData.type)" size="small">
            {{ getTypeName(modelData.type) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="描述">
          {{ modelData.description || '--' }}
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">
          {{ formatDate(modelData.createdAt) || '--' }}
        </el-descriptions-item>
      </el-descriptions>
    </div>
  </div>
</template>

<script setup>
const props = defineProps({
  modelData: {
    type: Object,
    required: true,
    default: () => ({}),
  },
})

// 定义事件
const emit = defineEmits(['close'])

// 获取权限类型名称
const getTypeName = (type) => {
  const typeMap = {
    1: '菜单',
    2: '操作按钮',
    3: 'API',
  }
  return typeMap[type] || '未知'
}

// 获取权限类型标签类型
const getTypeTagType = (type) => {
  const typeMap = {
    1: 'primary',
    2: 'success',
    3: 'warning',
  }
  return typeMap[type] || 'info'
}

// 生成头像背景色
const getAvatarBackground = (name) => {
  if (!name) return '#409EFF'
  const colors = [
    '#409EFF',
    '#67C23A',
    '#E6A23C',
    '#F56C6C',
    '#909399',
    '#C060A1',
    '#3CA272',
    '#FC8452',
    '#5470C6',
    '#91CC75',
  ]
  let total = 0
  for (let i = 0; i < name.length; i++) {
    total += name.charCodeAt(i)
  }
  return colors[total % colors.length]
}

// 生成头像文字
const getAvatarText = (name) => {
  if (!name) return '?'
  // 取首字母或第一个字符
  return name.charAt(0).toUpperCase()
}

// 格式化日期
const formatDate = (timestamp) => {
  if (!timestamp) return ''
  const date = new Date(timestamp)
  const year = date.getFullYear()
  const month = (date.getMonth() + 1).toString().padStart(2, '0')
  const day = date.getDate().toString().padStart(2, '0')
  const hours = date.getHours().toString().padStart(2, '0')
  const minutes = date.getMinutes().toString().padStart(2, '0')
  const seconds = date.getSeconds().toString().padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}
</script>

<style scoped lang="scss">
.permission-detail {
  padding: 20px;
}

.header {
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #eee;

  .header-info {
    display: flex;
    align-items: center;
    gap: 16px;
  }

  .avatar {
    width: 60px;
    height: 60px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    font-size: 24px;
    font-weight: 500;
  }

  .basic-info {
    display: flex;
    flex-direction: column;
    gap: 8px;
  }

  .title {
    margin: 0;
    font-size: 20px;
    font-weight: 500;
  }
}

.detail-content {
  :deep(.el-descriptions) {
    margin-bottom: 0;

    .el-descriptions__header {
      display: none;
    }

    .el-descriptions__item {
      padding: 16px 0;

      &:last-child {
        padding-bottom: 0;
      }
    }

    .el-descriptions__label {
      font-weight: 500;
      color: #333;
    }
  }
}
</style>
