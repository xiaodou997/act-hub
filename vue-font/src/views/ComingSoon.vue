<template>
  <div class="coming-soon-container">
    <el-card class="coming-soon-card">
      <div class="coming-soon-content">
        <el-image :src="rocketImage" class="coming-soon-image" alt="开发中" :fit="'contain'">
          <template #error>
            <div class="image-slot">
              <el-icon><Picture /></el-icon>
            </div>
          </template>
        </el-image>

        <h1 class="coming-soon-title">功能开发中</h1>
        <p class="coming-soon-description">我们正在努力开发此功能，敬请期待！</p>

        <el-progress
          :percentage="percentage"
          :duration="3"
          :indeterminate="true"
          :stroke-width="8"
          class="progress-bar"
        />

        <div class="action-buttons">
          <el-button type="primary" @click="goBack">
            <el-icon><Back /></el-icon>
            返回上一页
          </el-button>
          <el-button @click="goHome">
            <el-icon><House /></el-icon>
            返回首页
          </el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { Picture, Back, House } from '@element-plus/icons-vue'
import { ElNotification } from 'element-plus'

const router = useRouter()
const percentage = ref(50)
const rocketImage = ref('/src/assets/rocket.svg') // 可以替换为您项目中的图片路径

// 模拟进度条
let progressInterval

onMounted(() => {
  ElNotification({
    title: '提示',
    message: '该功能正在开发中，敬请期待！',
    type: 'info',
    duration: 3000,
  })
})

onBeforeUnmount(() => {
  if (progressInterval) {
    clearInterval(progressInterval)
  }
})

const goBack = () => {
  router.back()
}

const goHome = () => {
  router.push('/')
}
</script>

<style scoped>
.coming-soon-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 80vh;
  padding: 20px;
}

.coming-soon-card {
  width: 100%;
  max-width: 600px;
  text-align: center;
}

.coming-soon-content {
  padding: 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.coming-soon-image {
  width: 180px;
  height: 180px;
  margin-bottom: 20px;
}

.coming-soon-title {
  font-size: 28px;
  color: #303133;
  margin-bottom: 16px;
}

.coming-soon-description {
  font-size: 16px;
  color: #606266;
  margin-bottom: 30px;
}

.progress-bar {
  width: 100%;
  margin-bottom: 30px;
}

.action-buttons {
  display: flex;
  gap: 16px;
  justify-content: center;
}

.image-slot {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
  background: #f5f7fa;
  color: #909399;
  font-size: 30px;
}
</style>
