<template>
  <el-dialog
    v-model="visible"
    :title="props.title"
    width="80%"
    :before-close="handleClose"
    custom-class="announcement-modal"
    :close-on-click-modal="false"
    :show-close="true"
  >
    <div class="announcement-content" v-html="content"></div>

    <template #footer>
      <div class="modal-footer">
        <el-checkbox v-model="doNotShowAgain" label="不再显示此公告" size="large" />
        <el-button type="primary" @click="confirm" size="large">我知道了</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElDialog, ElButton, ElCheckbox } from 'element-plus'
import { getAnnouncement } from '@/api/announcement' // 假设这是你的API接口

const props = defineProps({
  title: {
    type: String,
    default: '系统公告',
  },
})

const visible = ref(false)
const content = ref('')
const doNotShowAgain = ref(false)
const announcementId = ref(null)

// 从本地存储检查是否已经选择不再显示
const shouldShowAnnouncement = (id) => {
  const hiddenAnnouncements = JSON.parse(localStorage.getItem('hiddenAnnouncements')) || []
  return !hiddenAnnouncements.includes(id)
}

// 获取公告数据
const fetchAnnouncement = async () => {
  try {
    const res = await getAnnouncement()
    if (res.data.show && res.data.content && shouldShowAnnouncement(res.data.id)) {
      content.value = res.data.content
      announcementId.value = res.data.id
      visible.value = true
    }
  } catch (error) {
    console.error('获取公告失败:', error)
  }
}

// 确认按钮点击事件
const confirm = () => {
  if (doNotShowAgain.value && announcementId.value) {
    const hiddenAnnouncements = JSON.parse(localStorage.getItem('hiddenAnnouncements') || '[]')
    hiddenAnnouncements.push(announcementId.value)
    localStorage.setItem('hiddenAnnouncements', JSON.stringify(hiddenAnnouncements))
  }
  visible.value = false
}

// 关闭弹框
const handleClose = (done) => {
  confirm()
  done()
}

// 组件挂载时获取公告
onMounted(() => {
  fetchAnnouncement()
})
</script>

<style scoped>
.announcement-content {
  max-height: 60vh;
  overflow-y: auto;
  line-height: 1.6;
  font-size: 16px;
  padding: 0 10px;
}

.announcement-content :deep(p) {
  margin-bottom: 1em;
}

.announcement-content :deep(a) {
  color: var(--el-color-primary);
  text-decoration: none;
}

.announcement-content :deep(a:hover) {
  text-decoration: underline;
}

.modal-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 20px;
  border-top: 1px solid var(--el-border-color-light);
}
</style>

<style>
/* 全局样式，因为el-dialog的样式需要全局生效 */
.announcement-modal .el-dialog__header {
  border-bottom: 1px solid var(--el-border-color-light);
  padding-bottom: 15px;
}

.announcement-modal .el-dialog__title {
  font-size: 20px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.announcement-modal .el-dialog__body {
  padding: 20px;
}
</style>
