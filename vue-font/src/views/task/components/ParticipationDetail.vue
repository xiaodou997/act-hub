<template>
  <div class="participation-detail">
    <el-descriptions :column="1" border>
      <el-descriptions-item label="用户ID">
        {{ record?.userId }}
      </el-descriptions-item>

      <el-descriptions-item label="用户昵称">
        {{ record?.userName || '-' }}
      </el-descriptions-item>

      <el-descriptions-item label="参与状态">
        <el-tag :type="getStatusType(record?.status)">
          {{ record?.statusDesc }}
        </el-tag>
      </el-descriptions-item>

      <el-descriptions-item label="提交链接">
        <template v-if="record?.submittedLink">
          <el-link :href="record.submittedLink" target="_blank" type="primary">
            {{ record.submittedLink }}
          </el-link>
        </template>
        <span v-else>未提交</span>
      </el-descriptions-item>

      <el-descriptions-item label="提交内容" v-if="record?.submittedContent">
        <div class="content-box">{{ record.submittedContent }}</div>
      </el-descriptions-item>

      <el-descriptions-item label="提交时间">
        {{ formatDate(record?.submittedAt) }}
      </el-descriptions-item>

      <el-descriptions-item label="审核状态" v-if="record?.status === 3 || record?.status === 4">
        <el-tag :type="record.status === 3 ? 'success' : 'danger'">
          {{ record.status === 3 ? '已通过' : '已拒绝' }}
        </el-tag>
      </el-descriptions-item>

      <el-descriptions-item label="审核备注" v-if="record?.auditNotes">
        {{ record.auditNotes }}
      </el-descriptions-item>

      <el-descriptions-item label="审核时间" v-if="record?.auditedAt">
        {{ formatDate(record.auditedAt) }}
      </el-descriptions-item>

      <el-descriptions-item label="奖励状态">
        <el-tag :type="getRewardStatusType(record?.rewardStatus)">
          {{ record?.rewardStatusDesc || '待发放' }}
        </el-tag>
      </el-descriptions-item>

      <el-descriptions-item label="领取时间">
        {{ formatDate(record?.createdAt) }}
      </el-descriptions-item>
    </el-descriptions>

    <!-- 页面快照 -->
    <div class="snapshot-section" v-if="record?.snapshotUrl">
      <h4>页面快照</h4>
      <el-image
        :src="record.snapshotUrl"
        fit="contain"
        :preview-src-list="[record.snapshotUrl]"
        class="snapshot-image"
      />
    </div>

    <!-- 提交历史 -->
    <div class="history-section" v-if="submissionHistory.length > 0">
      <h4>提交历史</h4>
      <el-timeline>
        <el-timeline-item
          v-for="(item, index) in submissionHistory"
          :key="index"
          :type="item.auditStatus === 3 ? 'success' : item.auditStatus === 4 ? 'danger' : 'primary'"
          :timestamp="formatDate(item.submittedAt)"
          placement="top"
        >
          <div class="history-item">
            <div class="history-link">
              <el-link :href="item.submittedLink" target="_blank" type="primary">
                {{ item.submittedLink }}
              </el-link>
            </div>
            <div class="history-audit" v-if="item.auditStatus">
              <el-tag :type="item.auditStatus === 3 ? 'success' : 'danger'" size="small">
                {{ item.auditStatus === 3 ? '通过' : '拒绝' }}
              </el-tag>
              <span class="audit-time">{{ formatDate(item.auditedAt) }}</span>
              <span class="audit-notes" v-if="item.auditNotes">- {{ item.auditNotes }}</span>
            </div>
          </div>
        </el-timeline-item>
      </el-timeline>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import dayjs from 'dayjs'

const props = defineProps({
  record: {
    type: Object,
    default: null,
  },
})

// 解析提交历史
const submissionHistory = computed(() => {
  if (!props.record?.submissionHistory) return []
  try {
    return JSON.parse(props.record.submissionHistory)
  } catch {
    return []
  }
})

// 状态类型
const getStatusType = (status) => {
  const map = {
    1: 'info',
    2: 'warning',
    3: 'success',
    4: 'danger',
  }
  return map[status] || 'info'
}

// 奖励状态类型
const getRewardStatusType = (status) => {
  const map = {
    0: 'info',
    1: 'success',
    2: 'danger',
  }
  return map[status] || 'info'
}

// 格式化日期
const formatDate = (timestamp) => {
  if (!timestamp) return '-'
  return dayjs(timestamp).format('YYYY-MM-DD HH:mm:ss')
}
</script>

<style scoped lang="scss">
.participation-detail {
  padding: 16px 0;
}

.content-box {
  padding: 12px;
  background: #f5f7fa;
  border-radius: 4px;
  white-space: pre-wrap;
  word-break: break-word;
}

.snapshot-section,
.history-section {
  margin-top: 24px;

  h4 {
    font-size: 14px;
    font-weight: 500;
    color: #303133;
    margin: 0 0 16px;
    padding-left: 12px;
    border-left: 3px solid #409eff;
  }
}

.snapshot-image {
  max-width: 100%;
  max-height: 400px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
}

.history-item {
  .history-link {
    margin-bottom: 8px;
  }

  .history-audit {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 12px;
    color: #909399;

    .audit-notes {
      color: #606266;
    }
  }
}
</style>
