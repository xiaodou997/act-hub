<template>
  <div class="task-detail" v-loading="loading">
    <template v-if="taskData">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="任务名称" :span="2">
          {{ taskData.taskName }}
        </el-descriptions-item>

        <el-descriptions-item label="任务状态">
          <el-tag :type="getStatusType(taskData.status)">
            {{ taskData.statusDesc }}
          </el-tag>
        </el-descriptions-item>

        <el-descriptions-item label="是否定向">
          <el-tag :type="taskData.isTargeted ? 'warning' : 'info'">
            {{ taskData.isTargeted ? '是' : '否' }}
          </el-tag>
        </el-descriptions-item>

        <el-descriptions-item label="开始时间">
          {{ formatDate(taskData.startTime) }}
        </el-descriptions-item>

        <el-descriptions-item label="结束时间">
          {{ formatDate(taskData.endTime) }}
        </el-descriptions-item>

        <el-descriptions-item label="发布平台" :span="2">
          <template v-if="taskData.platforms && taskData.platforms.length > 0">
            <el-tag
              v-for="platform in taskData.platforms"
              :key="platform"
              style="margin-right: 8px"
            >
              {{ getPlatformName(platform) }}
            </el-tag>
          </template>
          <span v-else>-</span>
        </el-descriptions-item>

        <el-descriptions-item label="任务奖励">
          <span class="highlight-text">{{ taskData.rewardAmount || 0 }} 元/条</span>
        </el-descriptions-item>

        <el-descriptions-item label="任务总量">
          {{ taskData.totalQuota || 0 }} 人
        </el-descriptions-item>

        <el-descriptions-item label="已领取">
          {{ taskData.claimedCount || 0 }} 人
        </el-descriptions-item>

        <el-descriptions-item label="已完成">
          <span class="highlight-text">{{ taskData.completedCount || 0 }} 人</span>
        </el-descriptions-item>

        <el-descriptions-item label="任务权重">
          {{ taskData.sortOrder || 0 }}
        </el-descriptions-item>

        <el-descriptions-item label="创建时间">
          {{ formatDate(taskData.createdAt) }}
        </el-descriptions-item>
      </el-descriptions>

      <div class="detail-section" v-if="taskData.requirements">
        <h4>补充要求</h4>
        <div class="requirements-content" v-html="taskData.requirements"></div>
      </div>

      <div class="detail-section" v-if="taskData.images && taskData.images.length > 0">
        <h4>任务图片</h4>
        <div class="image-list">
          <el-image
            v-for="(url, index) in taskData.images"
            :key="index"
            :src="url"
            :preview-src-list="taskData.images"
            :initial-index="index"
            fit="cover"
            class="task-image"
          />
        </div>
      </div>

      <!-- 进度统计 -->
      <div class="detail-section">
        <h4>完成进度</h4>
        <div class="progress-stats">
          <el-progress
            :percentage="getProgressPercentage()"
            :stroke-width="20"
            :format="() => `${taskData.completedCount || 0}/${taskData.totalQuota || 0}`"
          />
        </div>
      </div>
    </template>

    <el-empty v-else description="暂无数据" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { taskApi } from '@/api/task'
import dayjs from 'dayjs'

const props = defineProps({
  taskId: {
    type: String,
    required: true,
  },
})

const loading = ref(false)
const taskData = ref(null)

// 状态类型映射
const getStatusType = (status) => {
  const map = {
    0: 'info',
    1: 'success',
    2: 'warning',
    3: 'danger',
  }
  return map[status] || 'info'
}

// 平台名称映射
const getPlatformName = (platform) => {
  const map = {
    XIAOHONGSHU: '小红书',
    DOUYIN: '抖音',
    KUAISHOU: '快手',
  }
  return map[platform] || platform
}

// 格式化日期
const formatDate = (timestamp) => {
  if (!timestamp) return '-'
  return dayjs(timestamp).format('YYYY-MM-DD HH:mm:ss')
}

// 计算进度百分比
const getProgressPercentage = () => {
  if (!taskData.value?.totalQuota || taskData.value.totalQuota === 0) return 0
  return Math.round(((taskData.value.completedCount || 0) / taskData.value.totalQuota) * 100)
}

// 加载任务详情
const loadTaskDetail = async () => {
  loading.value = true
  try {
    const data = await taskApi.getById(props.taskId)
    taskData.value = data
  } catch (error) {
    console.error('加载任务详情失败:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadTaskDetail()
})
</script>

<style scoped lang="scss">
.task-detail {
  padding: 16px 0;
}

.detail-section {
  margin-top: 24px;

  h4 {
    font-size: 14px;
    font-weight: 500;
    color: #303133;
    margin: 0 0 12px;
    padding-left: 12px;
    border-left: 3px solid #409eff;
  }
}

.requirements-content {
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
  line-height: 1.8;
  color: #606266;
}

.image-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;

  .task-image {
    width: 120px;
    height: 120px;
    border-radius: 8px;
    cursor: pointer;
  }
}

.progress-stats {
  padding: 16px 0;
}

.highlight-text {
  color: #e6a23c;
  font-weight: 500;
}
</style>
