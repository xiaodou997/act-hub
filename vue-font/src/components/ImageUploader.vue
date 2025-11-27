<template>
  <div class="image-uploader">
    <el-upload
      class="uploader"
      :show-file-list="false"
      :before-upload="beforeUpload"
      :http-request="handleUpload"
      :accept="accept"
      action="#"
    >
      <div class="uploader-trigger">
        <div v-if="previewUrl || file" class="image-preview">
          <img :src="getPreviewUrl()" />
          <div class="image-actions">
            <el-icon><ZoomIn /></el-icon>
            <el-icon><Refresh /></el-icon>
            <el-icon><Delete @click.stop="handleRemove" /></el-icon>
          </div>
        </div>
        <el-icon v-else class="uploader-icon"><Plus /></el-icon>
      </div>
    </el-upload>
    <div class="upload-tip">{{ tip }}</div>
  </div>
</template>

<script setup>
import { ref, watch, onUnmounted } from 'vue'
import { Plus, ZoomIn, Refresh, Delete } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const props = defineProps({
  previewUrl: String,
  accept: {
    type: String,
    default: 'image/*',
  },
  tip: {
    type: String,
    default: '',
  },
})

const emit = defineEmits(['update:file']) // 移除update:md5

const file = ref(null)

const getPreviewUrl = () => {
  if (file.value) {
    try {
      return URL.createObjectURL(file.value)
    } catch (e) {
      console.error('Failed to create object URL:', e)
      return ''
    }
  }
  if (props.previewUrl) return props.previewUrl
  return ''
}

const beforeUpload = (rawFile) => {
  if (rawFile.type.indexOf('image/') === -1) {
    ElMessage.error('请上传图片文件')
    return false
  }

  const isLt5M = rawFile.size / 1024 / 1024 < 5
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过5MB')
    return false
  }

  return true
}

const handleUpload = async (options) => {
  const rawFile = options.file

  file.value = rawFile
  emit('update:file', rawFile)
}

const handleRemove = (e) => {
  e.stopPropagation()
  file.value = null
  emit('update:file', null)
}

watch(file, (newVal) => {
  emit('update:file', newVal)
})

onUnmounted(() => {
  if (file.value) {
    URL.revokeObjectURL(URL.createObjectURL(file.value))
  }
})
</script>

<style scoped>
.image-uploader {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.uploader {
  border: 1px dashed var(--el-border-color);
  border-radius: 12px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  width: 150px;
  height: 150px;
  transition:
    border-color 0.3s ease,
    box-shadow 0.3s ease,
    transform 0.2s ease;
  backdrop-filter: blur(6px);
  background-color: rgba(255, 255, 255, 0.15);
}

.uploader:hover {
  border-color: var(--el-color-primary);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  transform: translateY(-2px);
}

/* 让 slot 区域和 el-upload 容器一样大 */
:deep(.el-upload) {
  width: 100%;
  height: 100%;
}

.uploader-trigger {
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  position: relative;
  cursor: pointer;
}

/* 加号样式 */
.uploader-icon {
  font-size: 36px;
  color: #8c939d;
  display: flex;
  justify-content: center;
  align-items: center;
  transition:
    transform 0.25s ease,
    color 0.25s ease,
    opacity 0.25s ease;
  opacity: 0.85;
}

/* Hover 效果：缩放 + 微光亮 */
.uploader:hover .uploader-icon {
  transform: scale(1.15);
  color: var(--el-color-primary);
  opacity: 1;
}

.image-preview {
  width: 100%;
  height: 100%;
  position: relative;
}

.image-preview img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.image-actions {
  position: absolute;
  inset: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: none;
  justify-content: center;
  align-items: center;
  gap: 12px;
  color: white;
}

.image-preview:hover .image-actions {
  display: flex;
}

.upload-tip {
  margin-top: 8px;
  color: var(--el-text-color-secondary);
  font-size: 12px;
  text-align: center;
}
</style>
