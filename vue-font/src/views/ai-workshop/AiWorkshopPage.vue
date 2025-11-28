<template>
  <div class="ai-workshop-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>AI工坊</h2>
      <p class="subtitle">选择分类和应用，快速生成高质量内容</p>
    </div>

    <!-- 步骤指示器 -->
    <div class="steps-indicator">
      <el-steps :active="currentStep" finish-status="success" align-center>
        <el-step title="选择分类" description="选择应用类型" />
        <el-step title="选择应用" description="选择AI应用" />
        <el-step title="填写参数" description="输入生成参数" />
        <el-step title="生成结果" description="查看并选择内容" />
      </el-steps>
    </div>

    <!-- 步骤内容区域 -->
    <div class="step-content">
      <!-- 第一步：选择分类 -->
      <div v-show="currentStep === 0" class="step-panel">
        <h3 class="step-title">请选择应用分类</h3>
        <div class="category-grid" v-loading="loadingCategories">
          <div
            v-for="category in categories"
            :key="category.id"
            class="category-card"
            :class="{ active: selectedCategory?.id === category.id }"
            @click="selectCategory(category)"
          >
            <div class="category-icon">
              <el-icon :size="32"><component :is="getCategoryIcon(category.name)" /></el-icon>
            </div>
            <div class="category-name">{{ category.name }}</div>
            <div class="category-desc">{{ category.description || '暂无描述' }}</div>
          </div>

          <!-- 自定义输入卡片 -->
          <div
            class="category-card custom-card"
            :class="{ active: isCustomMode }"
            @click="enableCustomMode"
          >
            <div class="category-icon">
              <el-icon :size="32"><Edit /></el-icon>
            </div>
            <div class="category-name">自定义</div>
            <div class="category-desc">输入自定义内容</div>
          </div>
        </div>

        <!-- 自定义输入区域 -->
        <div v-if="isCustomMode" class="custom-input-area">
          <el-input
            v-model="customContent"
            type="textarea"
            :rows="4"
            placeholder="请输入您想要生成的内容描述..."
          />
        </div>

        <div class="step-actions">
          <el-button type="primary" size="large" :disabled="!canProceedStep1" @click="nextStep">
            下一步
            <el-icon class="el-icon--right"><ArrowRight /></el-icon>
          </el-button>
        </div>
      </div>

      <!-- 第二步：选择应用 -->
      <div v-show="currentStep === 1" class="step-panel">
        <h3 class="step-title">
          选择AI应用
          <span class="step-subtitle">（{{ selectedCategory?.name }} 分类）</span>
        </h3>

        <div class="app-grid" v-loading="loadingApps">
          <el-empty
            v-if="applications.length === 0 && !loadingApps"
            description="该分类下暂无可用应用"
          />

          <div
            v-for="app in applications"
            :key="app.id"
            class="app-card"
            :class="{ active: selectedApp?.id === app.id }"
            @click="selectApplication(app)"
          >
            <div class="app-name">{{ app.name }}</div>
            <div class="app-desc">{{ app.description || '暂无描述' }}</div>
          </div>
        </div>

        <div class="step-actions">
          <el-button size="large" @click="prevStep">
            <el-icon class="el-icon--left"><ArrowLeft /></el-icon>
            上一步
          </el-button>
          <el-button type="primary" size="large" :disabled="!selectedApp" @click="nextStep">
            下一步
            <el-icon class="el-icon--right"><ArrowRight /></el-icon>
          </el-button>
        </div>
      </div>

      <!-- 第三步：填写参数 -->
      <div v-show="currentStep === 2" class="step-panel">
        <h3 class="step-title">
          填写生成参数
          <span class="step-subtitle">（{{ selectedApp?.name }}）</span>
        </h3>

        <div class="params-form-container">
          <DynamicParamForm
            ref="paramFormRef"
            :schema="selectedApp?.paramSchema"
            :custom-content="isCustomMode ? customContent : ''"
            v-model="formParams"
          />
        </div>

        <div class="step-actions">
          <el-button size="large" @click="prevStep">
            <el-icon class="el-icon--left"><ArrowLeft /></el-icon>
            上一步
          </el-button>
          <el-button type="primary" size="large" :loading="generating" @click="handleExecute">
            <el-icon class="el-icon--left"><MagicStick /></el-icon>
            开始生成
          </el-button>
        </div>
      </div>

      <!-- 第四步：生成结果 -->
      <div v-show="currentStep === 3" class="step-panel">
        <h3 class="step-title">生成结果</h3>

        <div class="result-toolbar">
          <el-checkbox
            v-model="selectAll"
            :indeterminate="isIndeterminate"
            @change="handleSelectAll"
          >
            全选
          </el-checkbox>
          <span class="selected-count"
            >已选 {{ selectedResults.length }} / {{ generatedResults.length }} 条</span
          >
          <div class="toolbar-actions">
            <el-button
              :icon="CopyDocument"
              :disabled="selectedResults.length === 0"
              @click="handleCopy"
            >
              复制选中
            </el-button>
            <el-button
              :icon="Download"
              :disabled="selectedResults.length === 0"
              @click="handleDownload"
            >
              下载选中
            </el-button>
            <!-- 第二期功能：任务相关按钮 -->
            <!-- <el-button type="primary" :icon="FolderAdd" :disabled="selectedResults.length === 0">
              导入任务
            </el-button>
            <el-button type="success" :icon="Plus" :disabled="selectedResults.length === 0">
              创建任务
            </el-button> -->
          </div>
        </div>

        <div class="result-list" v-loading="generating">
          <el-empty
            v-if="generatedResults.length === 0 && !generating"
            description="暂无生成结果"
          />

          <div
            v-for="(item, index) in generatedResults"
            :key="index"
            class="result-item"
            :class="{ selected: item.selected }"
          >
            <el-checkbox v-model="item.selected" @change="updateSelectState" />
            <div class="result-content">
              <div class="result-index">#{{ index + 1 }}</div>
              <div class="result-text">{{ item.content }}</div>
            </div>
            <div class="result-actions">
              <el-button type="primary" link :icon="CopyDocument" @click="copySingle(item)">
                复制
              </el-button>
            </div>
          </div>
        </div>

        <div class="step-actions">
          <el-button size="large" @click="handleRegenerate">
            <el-icon class="el-icon--left"><RefreshRight /></el-icon>
            重新生成
          </el-button>
          <el-button size="large" @click="handleReset">
            <el-icon class="el-icon--left"><Refresh /></el-icon>
            返回首页
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import {
  ArrowRight,
  ArrowLeft,
  Edit,
  MagicStick,
  CopyDocument,
  Download,
  RefreshRight,
  Refresh,
  Sunny,
  House,
  Location,
  TrendCharts,
  Food,
  Star,
  Coffee,
  Reading,
} from '@element-plus/icons-vue'
import { aiWorkshopApi } from '@/api/aiWorkshop'
import DynamicParamForm from './components/DynamicParamForm.vue'

// 步骤控制
const currentStep = ref(0)

// 数据状态
const loadingCategories = ref(false)
const loadingApps = ref(false)
const generating = ref(false)

// 分类相关
const categories = ref([])
const selectedCategory = ref(null)
const isCustomMode = ref(false)
const customContent = ref('')

// 应用相关
const applications = ref([])
const selectedApp = ref(null)

// 参数表单
const paramFormRef = ref(null)
const formParams = ref({})

// 生成结果
const generatedResults = ref([])
const selectAll = ref(false)

// 计算属性
const canProceedStep1 = computed(() => {
  return selectedCategory.value || (isCustomMode.value && customContent.value.trim())
})

const selectedResults = computed(() => {
  return generatedResults.value.filter((item) => item.selected)
})

const isIndeterminate = computed(() => {
  const selectedCount = selectedResults.value.length
  return selectedCount > 0 && selectedCount < generatedResults.value.length
})

// 获取分类图标
const getCategoryIcon = (name) => {
  const iconMap = {
    情感: Sunny,
    生活: Coffee,
    家居: House,
    旅行: Location,
    励志: TrendCharts,
    美食: Food,
    阅读: Reading,
  }
  return iconMap[name] || Star
}

// 加载分类列表
const loadCategories = async () => {
  loadingCategories.value = true
  try {
    const data = await aiWorkshopApi.getCategories()
    categories.value = data || []
  } catch (error) {
    console.error('加载分类失败:', error)
    ElMessage.error('加载分类失败')
  } finally {
    loadingCategories.value = false
  }
}

// 选择分类
const selectCategory = (category) => {
  selectedCategory.value = category
  isCustomMode.value = false
  customContent.value = ''
}

// 启用自定义模式
const enableCustomMode = () => {
  isCustomMode.value = true
  selectedCategory.value = null
}

// 加载应用列表
const loadApplications = async () => {
  if (!selectedCategory.value && !isCustomMode.value) return

  loadingApps.value = true
  try {
    if (isCustomMode.value) {
      // 自定义模式：加载所有应用或特定的通用应用
      // 这里可以根据业务需求调整
      const data = await aiWorkshopApi.getCategories()
      if (data && data.length > 0) {
        // 默认使用第一个分类的应用
        const apps = await aiWorkshopApi.getApplicationsByType(data[0].id)
        applications.value = apps || []
      }
    } else {
      const data = await aiWorkshopApi.getApplicationsByType(selectedCategory.value.id)
      applications.value = data || []
    }
  } catch (error) {
    console.error('加载应用失败:', error)
    ElMessage.error('加载应用失败')
  } finally {
    loadingApps.value = false
  }
}

// 选择应用
const selectApplication = (app) => {
  selectedApp.value = app
}

// 步骤导航
const nextStep = () => {
  if (currentStep.value === 0) {
    loadApplications()
  }
  currentStep.value++
}

const prevStep = () => {
  currentStep.value--
}

// 执行生成
const handleExecute = async () => {
  // 验证表单
  if (paramFormRef.value && !paramFormRef.value.validate()) {
    ElMessage.warning('请填写必填参数')
    return
  }

  generating.value = true
  currentStep.value = 3 // 跳转到结果页

  try {
    const result = await aiWorkshopApi.execute(selectedApp.value.id, formParams.value)

    // 解析生成结果
    parseExecuteResult(result)

    ElMessage.success('生成成功')
  } catch (error) {
    console.error('生成失败:', error)
    ElMessage.error('生成失败: ' + (error.message || '未知错误'))
  } finally {
    generating.value = false
  }
}

// 解析执行结果
const parseExecuteResult = (result) => {
  // 根据实际返回格式解析
  // 假设返回格式为: { data: { contents: ["内容1", "内容2", ...] } }
  // 或者: { data: "内容1\n---\n内容2\n---\n内容3" }

  let contents = []

  if (result?.data) {
    const data = result.data

    if (Array.isArray(data)) {
      contents = data
    } else if (typeof data === 'string') {
      // 尝试按分隔符拆分
      contents = data.split(/\n---\n|\n\n\n/).filter((s) => s.trim())
    } else if (data.contents && Array.isArray(data.contents)) {
      contents = data.contents
    } else if (data.content) {
      contents = [data.content]
    }
  } else if (typeof result === 'string') {
    contents = result.split(/\n---\n|\n\n\n/).filter((s) => s.trim())
  }

  // 如果还是解析不出来，把整个结果当作一条
  if (contents.length === 0 && result) {
    contents = [JSON.stringify(result, null, 2)]
  }

  generatedResults.value = contents.map((content) => ({
    content: typeof content === 'string' ? content.trim() : JSON.stringify(content),
    selected: false,
  }))
}

// 全选/取消全选
const handleSelectAll = (val) => {
  generatedResults.value.forEach((item) => {
    item.selected = val
  })
}

// 更新选择状态
const updateSelectState = () => {
  const allSelected = generatedResults.value.every((item) => item.selected)
  const noneSelected = generatedResults.value.every((item) => !item.selected)
  selectAll.value = allSelected && generatedResults.value.length > 0
}

// 复制单条
const copySingle = async (item) => {
  try {
    await navigator.clipboard.writeText(item.content)
    ElMessage.success('复制成功')
  } catch {
    ElMessage.error('复制失败')
  }
}

// 复制选中
const handleCopy = async () => {
  const text = selectedResults.value.map((item) => item.content).join('\n\n---\n\n')
  try {
    await navigator.clipboard.writeText(text)
    ElMessage.success(`已复制 ${selectedResults.value.length} 条内容`)
  } catch {
    ElMessage.error('复制失败')
  }
}

// 下载选中
const handleDownload = () => {
  const text = selectedResults.value
    .map((item, index) => {
      return `【内容 ${index + 1}】\n${item.content}`
    })
    .join('\n\n' + '='.repeat(50) + '\n\n')

  const blob = new Blob([text], { type: 'text/plain;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `AI工坊_${new Date().toLocaleDateString()}.txt`
  a.click()
  URL.revokeObjectURL(url)

  ElMessage.success('下载成功')
}

// 重新生成
const handleRegenerate = () => {
  currentStep.value = 2
  generatedResults.value = []
}

// 返回首页
const handleReset = () => {
  currentStep.value = 0
  selectedCategory.value = null
  selectedApp.value = null
  isCustomMode.value = false
  customContent.value = ''
  formParams.value = {}
  generatedResults.value = []
}

// 初始化
loadCategories()
</script>

<style scoped lang="scss">
.ai-workshop-container {
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  text-align: center;
  margin-bottom: 32px;

  h2 {
    margin: 0 0 8px;
    font-size: 28px;
    font-weight: 600;
    color: #303133;
  }

  .subtitle {
    margin: 0;
    color: #909399;
    font-size: 14px;
  }
}

.steps-indicator {
  margin-bottom: 40px;
  padding: 0 40px;
}

.step-content {
  min-height: 400px;
}

.step-panel {
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.step-title {
  font-size: 18px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 24px;

  .step-subtitle {
    font-size: 14px;
    font-weight: 400;
    color: #909399;
    margin-left: 8px;
  }
}

// 分类卡片网格
.category-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  gap: 16px;
  margin-bottom: 24px;
}

.category-card {
  padding: 24px 16px;
  border: 2px solid #e4e7ed;
  border-radius: 12px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
  background: #fff;

  &:hover {
    border-color: #409eff;
    box-shadow: 0 4px 12px rgba(64, 158, 255, 0.15);
    transform: translateY(-2px);
  }

  &.active {
    border-color: #409eff;
    background: linear-gradient(135deg, #ecf5ff 0%, #f5f7fa 100%);

    .category-icon {
      color: #409eff;
    }
  }

  .category-icon {
    color: #909399;
    margin-bottom: 12px;
    transition: color 0.3s;
  }

  .category-name {
    font-size: 16px;
    font-weight: 500;
    color: #303133;
    margin-bottom: 8px;
  }

  .category-desc {
    font-size: 12px;
    color: #909399;
    line-height: 1.4;
  }
}

.custom-card {
  border-style: dashed;
}

.custom-input-area {
  margin-bottom: 24px;

  :deep(.el-textarea__inner) {
    border-radius: 8px;
  }
}

// 应用卡片网格
.app-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 16px;
  margin-bottom: 24px;
  min-height: 200px;
}

.app-card {
  padding: 20px;
  border: 2px solid #e4e7ed;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s;
  background: #fff;

  &:hover {
    border-color: #409eff;
    box-shadow: 0 4px 12px rgba(64, 158, 255, 0.15);
  }

  &.active {
    border-color: #409eff;
    background: linear-gradient(135deg, #ecf5ff 0%, #f5f7fa 100%);
  }

  .app-name {
    font-size: 16px;
    font-weight: 500;
    color: #303133;
    margin-bottom: 8px;
  }

  .app-desc {
    font-size: 13px;
    color: #909399;
    line-height: 1.5;
  }
}

// 参数表单容器
.params-form-container {
  background: #fafafa;
  padding: 24px;
  border-radius: 12px;
  margin-bottom: 24px;
}

// 结果区域
.result-toolbar {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
  margin-bottom: 16px;

  .selected-count {
    color: #909399;
    font-size: 14px;
  }

  .toolbar-actions {
    margin-left: auto;
    display: flex;
    gap: 8px;
  }
}

.result-list {
  min-height: 300px;
}

.result-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 16px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  margin-bottom: 12px;
  background: #fff;
  transition: all 0.2s;

  &:hover {
    border-color: #c0c4cc;
  }

  &.selected {
    border-color: #409eff;
    background: #ecf5ff;
  }

  .result-content {
    flex: 1;
    min-width: 0;
  }

  .result-index {
    font-size: 12px;
    color: #909399;
    margin-bottom: 4px;
  }

  .result-text {
    font-size: 14px;
    line-height: 1.8;
    color: #303133;
    white-space: pre-wrap;
    word-break: break-word;
  }

  .result-actions {
    flex-shrink: 0;
  }
}

// 步骤操作按钮
.step-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid #ebeef5;
}
</style>
