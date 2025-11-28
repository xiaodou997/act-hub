<template>
  <div class="task-form-container">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
      label-position="top"
    >
      <!-- 基本信息 -->
      <div class="form-section">
        <h4 class="section-title">基本信息</h4>

        <el-form-item label="任务名称" prop="taskName">
          <el-input
            v-model="formData.taskName"
            placeholder="如：海信电视以旧换新，最高赢300购物卡"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>

        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="开始时间" prop="startTime">
              <el-date-picker
                v-model="formData.startTime"
                type="datetime"
                placeholder="选择开始时间"
                style="width: 100%"
                value-format="YYYY-MM-DD HH:mm:ss"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束时间" prop="endTime">
              <el-date-picker
                v-model="formData.endTime"
                type="datetime"
                placeholder="选择结束时间"
                style="width: 100%"
                value-format="YYYY-MM-DD HH:mm:ss"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="发布平台" prop="platforms">
          <el-checkbox-group v-model="formData.platforms">
            <el-checkbox label="XIAOHONGSHU">小红书</el-checkbox>
            <el-checkbox label="DOUYIN">抖音</el-checkbox>
            <el-checkbox label="KUAISHOU">快手</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </div>

      <!-- 任务要求 -->
      <div class="form-section">
        <h4 class="section-title">任务要求</h4>

        <el-form-item label="补充要求及规则" prop="requirements">
          <el-input
            v-model="formData.requirements"
            type="textarea"
            :rows="6"
            placeholder="请输入任务的补充要求，如必带话题、内容规范等..."
          />
        </el-form-item>

        <el-form-item label="任务图片">
          <el-upload
            v-model:file-list="imageList"
            action="#"
            list-type="picture-card"
            :auto-upload="false"
            :limit="5"
            accept="image/*"
          >
            <el-icon><Plus /></el-icon>
            <template #tip>
              <div class="upload-tip">支持jpg/png格式，最多上传5张</div>
            </template>
          </el-upload>
        </el-form-item>
      </div>

      <!-- 奖励设置 -->
      <div class="form-section">
        <h4 class="section-title">奖励设置</h4>

        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="任务奖励" prop="rewardAmount">
              <el-input-number
                v-model="formData.rewardAmount"
                :min="0"
                :precision="2"
                controls-position="right"
                style="width: 200px"
              />
              <span class="unit-text">元/条</span>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="任务总量" prop="totalQuota">
              <el-input-number
                v-model="formData.totalQuota"
                :min="1"
                controls-position="right"
                style="width: 200px"
              />
              <span class="unit-text">人</span>
            </el-form-item>
          </el-col>
        </el-row>
      </div>

      <!-- 高级设置 -->
      <div class="form-section">
        <h4 class="section-title">高级设置</h4>

        <el-form-item label="任务定向">
          <el-switch v-model="formData.isTargeted" active-text="是" inactive-text="否" />
          <span class="form-hint">开启后，只有导入的用户才能领取此任务</span>
        </el-form-item>

        <el-form-item v-if="formData.isTargeted" label="定向用户">
          <el-input
            v-model="targetUsersText"
            type="textarea"
            :rows="4"
            placeholder="请输入用户ID或手机号，每行一个"
          />
          <div class="form-hint">共 {{ targetUserCount }} 个用户</div>
        </el-form-item>

        <el-form-item label="任务权重" prop="sortOrder">
          <el-input-number
            v-model="formData.sortOrder"
            :min="0"
            :max="9999"
            controls-position="right"
            style="width: 200px"
          />
          <span class="form-hint">数值越大，任务排序越靠前</span>
        </el-form-item>
      </div>

      <!-- 表单操作 -->
      <div class="form-footer">
        <el-button @click="$emit('cancel')">取消</el-button>
        <el-button @click="handleSaveDraft">保存草稿</el-button>
        <el-button type="primary" @click="handleSubmit">
          {{ isEdit ? '保存更新' : '发布任务' }}
        </el-button>
      </div>
    </el-form>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

const props = defineProps({
  taskData: {
    type: Object,
    default: () => ({}),
  },
  isEdit: {
    type: Boolean,
    default: false,
  },
})

const emit = defineEmits(['submit', 'cancel'])

const formRef = ref(null)
const imageList = ref([])
const targetUsersText = ref('')

// 表单数据
const formData = reactive({
  taskName: '',
  startTime: '',
  endTime: '',
  platforms: [],
  requirements: '',
  images: [],
  rewardAmount: 0,
  totalQuota: 100,
  isTargeted: false,
  sortOrder: 0,
  status: 0,
})

// 表单校验规则
const formRules = {
  taskName: [
    { required: true, message: '请输入任务名称', trigger: 'blur' },
    { min: 2, max: 100, message: '任务名称长度在2-100个字符', trigger: 'blur' },
  ],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }],
  platforms: [
    { required: true, message: '请选择发布平台', trigger: 'change' },
    { type: 'array', min: 1, message: '请至少选择一个平台', trigger: 'change' },
  ],
  rewardAmount: [{ required: true, message: '请输入任务奖励', trigger: 'change' }],
  totalQuota: [{ required: true, message: '请输入任务总量', trigger: 'change' }],
}

// 计算定向用户数量
const targetUserCount = computed(() => {
  if (!targetUsersText.value) return 0
  return targetUsersText.value.split('\n').filter((line) => line.trim()).length
})

// 监听外部数据变化
watch(
  () => props.taskData,
  (newData) => {
    if (newData && Object.keys(newData).length > 0) {
      Object.assign(formData, {
        taskName: newData.taskName || '',
        startTime: newData.startTime || '',
        endTime: newData.endTime || '',
        platforms: newData.platforms || [],
        requirements: newData.requirements || '',
        images: newData.images || [],
        rewardAmount: newData.rewardAmount || 0,
        totalQuota: newData.totalQuota || 100,
        isTargeted: newData.isTargeted || false,
        sortOrder: newData.sortOrder || 0,
        status: newData.status || 0,
      })
    }
  },
  { immediate: true, deep: true },
)

// 保存草稿
const handleSaveDraft = async () => {
  formData.status = 0 // 草稿状态
  await handleSubmit()
}

// 提交表单
const handleSubmit = async () => {
  try {
    const valid = await formRef.value.validate()
    if (!valid) return

    // 验证时间
    if (new Date(formData.endTime) <= new Date(formData.startTime)) {
      ElMessage.warning('结束时间必须晚于开始时间')
      return
    }

    // 处理定向用户
    let targetUserIdentifiers = []
    if (formData.isTargeted && targetUsersText.value) {
      targetUserIdentifiers = targetUsersText.value
        .split('\n')
        .map((line) => line.trim())
        .filter(Boolean)
    }

    const submitData = {
      ...formData,
      // 如果是发布任务，状态设为上线
      status: formData.status === 0 && !props.isEdit ? 1 : formData.status,
    }

    emit('submit', submitData, targetUserIdentifiers)
  } catch (error) {
    console.error('表单验证失败:', error)
  }
}

onMounted(() => {
  // 初始化逻辑
})
</script>

<style scoped lang="scss">
.task-form-container {
  max-height: 70vh;
  overflow-y: auto;
  padding-right: 12px;
}

.form-section {
  margin-bottom: 24px;
  padding-bottom: 24px;
  border-bottom: 1px solid #ebeef5;

  &:last-child {
    border-bottom: none;
  }

  .section-title {
    font-size: 16px;
    font-weight: 500;
    color: #303133;
    margin: 0 0 16px;
    padding-left: 12px;
    border-left: 3px solid #409eff;
  }
}

.unit-text {
  margin-left: 8px;
  color: #909399;
}

.form-hint {
  margin-left: 12px;
  font-size: 12px;
  color: #909399;
}

.upload-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 8px;
}

.form-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding-top: 24px;
  border-top: 1px solid #ebeef5;
}
</style>
