<template>
  <div class="system-log-detail-container">
    <el-descriptions :column="2" border>
      <el-descriptions-item label="日志ID">{{ logData.id }}</el-descriptions-item>
      <el-descriptions-item label="链路追踪ID">{{ logData.traceId || '-' }}</el-descriptions-item>
      <el-descriptions-item label="操作人">{{
        logData.operatorUserName || '-'
      }}</el-descriptions-item>
      <el-descriptions-item label="操作人类型">
        <el-tag :type="getUserTypeTagType(logData.operatorUserType)" effect="light">
          {{ getUserTypeText(logData.operatorUserType) }}
        </el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="租户名称">{{
        logData.operatorTenantName || '-'
      }}</el-descriptions-item>
      <el-descriptions-item label="日志级别">
        <el-tag :type="getLogLevelTagType(logData.logLevel)" effect="light">
          {{ logData.logLevel }}
        </el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="功能模块">{{ logData.module }}</el-descriptions-item>
      <el-descriptions-item label="操作动作">{{ logData.action }}</el-descriptions-item>
      <el-descriptions-item label="目标对象类型">{{
        logData.targetType || '-'
      }}</el-descriptions-item>
      <el-descriptions-item label="目标对象ID">{{ logData.targetId || '-' }}</el-descriptions-item>
      <el-descriptions-item label="操作状态">
        <el-tag :type="logData.success === 1 ? 'success' : 'danger'">
          {{ logData.success === 1 ? '成功' : '失败' }}
        </el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="IP地址">{{ logData.ipAddress }}</el-descriptions-item>
      <el-descriptions-item label="耗时(ms)">{{ logData.costTimeMs || '-' }}</el-descriptions-item>
      <el-descriptions-item label="操作时间">{{
        formatDate(logData.createdAt)
      }}</el-descriptions-item>
      <el-descriptions-item label="User-Agent" :span="2">
        <div class="user-agent">{{ logData.userAgent || '-' }}</div>
      </el-descriptions-item>
      <el-descriptions-item label="操作描述" :span="2">{{
        logData.description
      }}</el-descriptions-item>
      <el-descriptions-item v-if="logData.errorCode" label="错误码" :span="2">
        <div class="error-code">{{ logData.errorCode }}</div>
      </el-descriptions-item>
      <el-descriptions-item v-if="logData.errorMessage" label="错误信息" :span="2">
        <div class="error-message">{{ logData.errorMessage }}</div>
      </el-descriptions-item>
      <el-descriptions-item v-if="logData.stackTrace" label="堆栈跟踪" :span="2">
        <div class="stack-trace">{{ logData.stackTrace }}</div>
      </el-descriptions-item>
    </el-descriptions>

    <div class="detail-section" v-if="logData.detail">
      <h4>详细变更内容</h4>
      <div class="json-container">
        <vue-json-pretty
          :data="parsedDetail"
          :deep="3"
          :show-length="true"
          :show-double-quotes="false"
          :highlight-selected-node="true"
          class="json-viewer"
        />
      </div>
    </div>

    <div class="detail-section" v-if="logData.tags">
      <h4>标签信息</h4>
      <div class="json-container">
        <vue-json-pretty
          :data="parsedTags"
          :deep="3"
          :show-length="true"
          :show-double-quotes="false"
          :highlight-selected-node="true"
          class="json-viewer"
        />
      </div>
    </div>

    <div class="drawer-footer">
      <el-button type="primary" @click="handleClose">关闭</el-button>
    </div>
  </div>
</template>

<script setup>
import dayjs from 'dayjs'
import VueJsonPretty from 'vue-json-pretty'
import 'vue-json-pretty/lib/styles.css'
import { computed } from 'vue'

const props = defineProps({
  logData: {
    type: Object,
    required: true,
    default: () => ({}),
  },
})

const emit = defineEmits(['close'])

// 解析JSON详情数据
const parsedDetail = computed(() => {
  if (!props.logData.detail) return null

  try {
    // 如果detail是字符串，尝试解析为JSON对象
    if (typeof props.logData.detail === 'string') {
      return JSON.parse(props.logData.detail)
    }
    // 如果已经是对象，直接返回
    return props.logData.detail
  } catch (error) {
    console.error('JSON解析错误:', error)
    return { error: '无法解析JSON数据', raw: props.logData.detail }
  }
})

// 解析JSON标签数据
const parsedTags = computed(() => {
  if (!props.logData.tags) return null

  try {
    // 如果tags是字符串，尝试解析为JSON对象
    if (typeof props.logData.tags === 'string') {
      return JSON.parse(props.logData.tags)
    }
    // 如果已经是对象，直接返回
    return props.logData.tags
  } catch (error) {
    console.error('JSON解析错误:', error)
    return { error: '无法解析JSON数据', raw: props.logData.tags }
  }
})

// 获取日志级别对应的标签类型
const getLogLevelTagType = (logLevel) => {
  const levelMap = {
    DEBUG: 'info',
    INFO: 'primary',
    WARN: 'warning',
    ERROR: 'danger',
    AUDIT: 'success',
  }
  return levelMap[logLevel] || 'info'
}

// 获取用户类型对应的标签类型
const getUserTypeTagType = (userType) => {
  const typeMap = {
    SYSTEM: 'info',
    ANONYMOUS: 'warning',
    API_USER: 'primary',
    BATCH_JOB: 'success',
    EXTERNAL_SYSTEM: 'danger',
  }
  return typeMap[userType] || 'info'
}

// 获取用户类型显示文本
const getUserTypeText = (userType) => {
  const textMap = {
    SYSTEM: '系统用户',
    ANONYMOUS: '匿名用户',
    API_USER: 'API用户',
    BATCH_JOB: '批处理任务',
    EXTERNAL_SYSTEM: '外部系统',
  }
  return textMap[userType] || userType || '未知'
}

// 格式化日期
const formatDate = (timestamp) => {
  if (!timestamp) return '-'
  return dayjs(timestamp).format('YYYY-MM-DD HH:mm:ss')
}

// 关闭侧边栏
const handleClose = () => {
  emit('close')
}
</script>

<style scoped lang="scss">
.system-log-detail-container {
  padding: 20px;
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow-y: auto;
}

.el-descriptions {
  margin-bottom: 20px;
  :deep(.el-descriptions__body) {
    .el-descriptions-item__cell {
      padding: 12px 16px;
    }
  }
}

.detail-section {
  margin-top: 20px;
  h4 {
    margin-bottom: 10px;
    font-size: 14px;
    font-weight: 500;
    color: #303133;
  }
}

.json-container {
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  overflow: hidden;
  background: #f8f9fa;
  max-height: 400px;
  overflow-y: auto;
}

:deep(.json-viewer) {
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace !important;
  font-size: 13px !important;
  line-height: 1.5 !important;

  .vjs-tree {
    padding: 12px !important;

    .vjs-value {
      &.vjs-value__string {
        color: #032f62 !important;
      }

      &.vjs-value__number {
        color: #005cc5 !important;
      }

      &.vjs-value__boolean {
        color: #6f42c1 !important;
      }

      &.vjs-value__null {
        color: #d73a49 !important;
      }
    }

    .vjs-key {
      color: #22863a !important;
      font-weight: 600 !important;
    }

    .vjs-tree__node {
      margin: 2px 0 !important;
    }
  }
}

.user-agent {
  word-break: break-all;
  white-space: normal;
}

.error-message {
  color: #f56c6c;
  word-break: break-all;
  white-space: normal;
}

.drawer-footer {
  margin-top: auto;
  display: flex;
  justify-content: flex-end;
  padding-top: 16px;
  border-top: 1px solid #eee;
}

/* 优化滚动条样式 */
.system-log-detail-container::-webkit-scrollbar,
.json-container::-webkit-scrollbar {
  width: 8px;
}

.system-log-detail-container::-webkit-scrollbar-track,
.json-container::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 4px;
}

.system-log-detail-container::-webkit-scrollbar-thumb,
.json-container::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 4px;
}

.system-log-detail-container::-webkit-scrollbar-thumb:hover,
.json-container::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}
</style>
