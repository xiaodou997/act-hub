<template>
  <div class="menu-management-container">
    <!-- 顶部操作栏 -->
    <div class="header-actions">
      <div class="title-section">
        <span class="main-title">菜单管理</span>
        <span class="sub-title">配置系统菜单、路由与按钮权限</span>
      </div>
      <el-button type="primary" class="gradient-btn" :icon="Plus" @click="handleCreateRoot">
        新建顶级菜单
      </el-button>
    </div>

    <!-- 主体内容卡片 -->
    <el-card class="main-card" shadow="never">
      <div class="toolbar">
        <el-input
          v-model="searchKeyword"
          placeholder="输入菜单名称进行过滤..."
          class="search-input"
          clearable
          @input="filterTree"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>

        <div class="toolbar-tips">
          <span class="legend-item"
            ><el-icon class="folder-icon"><Folder /></el-icon> 目录</span
          >
          <span class="legend-item"
            ><el-icon class="menu-icon"><Document /></el-icon> 菜单</span
          >
          <span class="legend-item"
            ><el-icon class="btn-icon"><Operation /></el-icon> 按钮</span
          >
        </div>
      </div>

      <el-tree
        class="custom-tree"
        :data="filteredTree"
        node-key="id"
        default-expand-all
        :props="treeProps"
        draggable
        :allow-drag="allowDrag"
        :allow-drop="allowDrop"
        @node-drop="handleNodeDrop"
        :expand-on-click-node="false"
      >
        <template #default="{ data }">
          <div class="custom-tree-node">
            <!-- 左侧：图标与名称 -->
            <div class="node-content">
              <el-icon class="type-icon" :class="getTypeIconClass(data.type)">
                <component :is="getTypeIcon(data.type)" />
              </el-icon>
              <span class="node-label">{{ data.name }}</span>

              <span class="node-tags">
                <el-tag
                  v-if="data.type === 2"
                  size="small"
                  effect="plain"
                  type="info"
                  class="code-tag"
                >
                  {{ data.permissionCode }}
                </el-tag>
                <el-tag
                  v-if="data.status === 0"
                  size="small"
                  type="danger"
                  effect="dark"
                  class="status-tag"
                  >禁用</el-tag
                >
                <el-tag
                  v-if="data.isVisible === 0 && data.type !== 2"
                  size="small"
                  type="warning"
                  effect="plain"
                  class="status-tag"
                  >隐藏</el-tag
                >
              </span>
            </div>

            <!-- 右侧：操作区 (Hover显示) -->
            <div class="node-actions">
              <el-tooltip content="显示/隐藏" placement="top" :show-after="500">
                <el-switch
                  v-model="switchMap[data.id]"
                  :active-value="1"
                  :inactive-value="0"
                  size="small"
                  class="status-switch"
                  style="--el-switch-on-color: #1a85f2"
                  @change="handleToggleStatus(data)"
                  @click.stop
                />
              </el-tooltip>

              <div class="action-buttons">
                <el-button
                  v-if="data.type !== 2"
                  type="primary"
                  link
                  :icon="Plus"
                  @click.stop="handleCreateChild(data)"
                  >新增</el-button
                >
                <el-button type="primary" link :icon="Edit" @click.stop="handleEdit(data)"
                  >编辑</el-button
                >
                <el-button type="danger" link :icon="Delete" @click.stop="handleDelete(data)"
                  >删除</el-button
                >
              </div>
            </div>
          </div>
        </template>
      </el-tree>
    </el-card>

    <!-- 弹窗 -->
    <el-dialog
      v-model="editVisible"
      :title="isEdit ? '编辑菜单' : '新建菜单'"
      width="680px"
      class="menu-dialog"
      align-center
      destroy-on-close
    >
      <MenuEditForm
        :formData="formData"
        :isEdit="isEdit"
        :treeData="menuTree"
        @submit="handleSubmit"
        @cancel="() => (editVisible = false)"
      />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete, Search, Folder, Document, Operation } from '@element-plus/icons-vue'
import { menuApi } from '@/api/menu' // 假设API路径
import MenuEditForm from './MenuEditForm.vue'

const treeProps = { children: 'children', label: 'name' }
const menuTree = ref([])
const filteredTree = ref([])
const searchKeyword = ref('')
const switchMap = reactive({})
const editVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)

const formData = reactive({
  id: '',
  parentId: '',
  name: '',
  type: 1,
  path: '',
  componentName: '',
  permissionCode: '',
  icon: '',
  sortOrder: 0,
  status: 1,
  isVisible: 1,
})

// 图标逻辑
const getTypeIcon = (type) => {
  if (type === 0) return Folder
  if (type === 1) return Document
  return Operation
}
const getTypeIconClass = (type) => {
  if (type === 0) return 'text-folder'
  if (type === 1) return 'text-menu'
  return 'text-btn'
}

const loadTree = async () => {
  const res = await menuApi.getMenuTree()
  menuTree.value = Array.isArray(res) ? res : []
  filteredTree.value = menuTree.value
  switchMapInit()
}

const switchMapInit = () => {
  switchMapClear()
  const walk = (nodes) => {
    nodes.forEach((n) => {
      switchMap[n.id] = n.status
      if (n.children && n.children.length) walk(n.children)
    })
  }
  walk(menuTree.value)
}
const switchMapClear = () => {
  Object.keys(switchMap).forEach((k) => delete switchMap[k])
}

const filterTree = () => {
  const kw = searchKeyword.value.trim().toLowerCase()
  if (!kw) {
    filteredTree.value = menuTree.value
    return
  }
  const match = (node) => node.name?.toLowerCase().includes(kw)
  const dfs = (nodes) => {
    const result = []
    nodes.forEach((n) => {
      const children = n.children ? dfs(n.children) : []
      if (match(n) || children.length) {
        result.push({ ...n, children })
      }
    })
    return result
  }
  filteredTree.value = dfs(menuTree.value)
}

const resetForm = () => {
  Object.assign(formData, {
    id: '',
    parentId: '',
    name: '',
    type: 1,
    path: '',
    componentName: '',
    permissionCode: '',
    icon: '',
    sortOrder: 0,
    status: 1,
    isVisible: 1,
  })
}

const handleCreateRoot = () => {
  isEdit.value = false
  resetForm()
  editVisible.value = true
}
const handleCreateChild = (parent) => {
  isEdit.value = false
  resetForm()
  formData.parentId = parent.id
  editVisible.value = true
}
const handleEdit = async (row) => {
  isEdit.value = true
  const res = await menuApi.getById(row.id)
  Object.assign(formData, res || {})
  editVisible.value = true
}
const handleDelete = (row) => {
  ElMessageBox.confirm(`确定删除"${row.name}"吗？`, '提示', { type: 'warning' }).then(async () => {
    await menuApi.delete(row.id)
    ElMessage.success('删除成功')
    loadTree()
  })
}
const handleToggleStatus = async (row) => {
  try {
    await menuApi.updateStatus(row.id, switchMap[row.id])
    ElMessage.success('状态已更新')
    loadTree()
  } catch {
    ElMessage.error('更新失败')
  }
}

const allowDrag = () => true
const allowDrop = (draggingNode, dropNode, type) => {
  if (type === 'none') return false
  return true
}

const findNodeById = (nodes, id) => {
  for (const n of nodes) {
    if (n.id === id) return n
    if (n.children && n.children.length) {
      const f = findNodeById(n.children, id)
      if (f) return f
    }
  }
  return null
}

const handleNodeDrop = async (draggingNode, dropNode, dropType) => {
  try {
    let targetParentId = null
    if (dropType === 'inner') {
      targetParentId = dropNode.data.id
    } else {
      targetParentId = dropNode.parent?.data?.id || null
    }
    const parent = targetParentId
      ? findNodeById(menuTree.value, targetParentId)
      : { children: menuTree.value }
    const orderedIds = (parent.children || []).map((c) => c.id)
    await menuApi.sort(targetParentId, orderedIds)
    ElMessage.success('排序已更新')
    loadTree()
  } catch (e) {
    ElMessage.error('排序更新失败')
  }
}

const handleSubmit = async (payload) => {
  try {
    submitting.value = true
    if (isEdit.value) {
      await menuApi.update({ ...payload })
      ElMessage.success('更新成功')
    } else {
      await menuApi.create({ ...payload })
      ElMessage.success('创建成功')
    }
    editVisible.value = false
    loadTree()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('提交失败')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadTree()
})
</script>

<style scoped lang="scss">
.menu-management-container {
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;
}

/* 顶部区域 */
.header-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;

  .title-section {
    display: flex;
    flex-direction: column;

    .main-title {
      font-size: 24px;
      font-weight: 600;
      color: #1f2d3d;
      margin-bottom: 4px;
    }
    .sub-title {
      font-size: 14px;
      color: #909399;
    }
  }

  .gradient-btn {
    background: var(--app-primary-gradient);
    border: none;
    padding: 10px 24px;
    box-shadow: 0 4px 14px rgba(var(--app-primary-rgb), 0.3);
    transition:
      transform 0.2s,
      box-shadow 0.2s;

    &:hover {
      transform: translateY(-1px);
      box-shadow: 0 6px 20px rgba(var(--app-primary-rgb), 0.4);
    }
    &:active {
      transform: translateY(0);
    }
  }
}

/* 主卡片 */
.main-card {
  border-radius: 12px;
  border: 1px solid #ebeef5;
  background: #ffffff;

  :deep(.el-card__body) {
    padding: 20px;
  }
}

/* 工具栏 */
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;

  .search-input {
    width: 300px;
    :deep(.el-input__wrapper) {
      border-radius: 20px;
      box-shadow: 0 0 0 1px #dcdfe6 inset;
      &:hover {
        box-shadow: 0 0 0 1px var(--el-color-primary) inset;
      }
      &.is-focus {
        box-shadow: 0 0 0 1px var(--el-color-primary) inset;
      }
    }
  }

  .toolbar-tips {
    display: flex;
    gap: 20px;
    font-size: 13px;
    color: #606266;

    .legend-item {
      display: flex;
      align-items: center;
      gap: 6px;

      .el-icon {
        font-size: 16px;
      }
      .folder-icon {
        color: #e6a23c;
      }
      .menu-icon {
        color: #409eff;
      }
      .btn-icon {
        color: #909399;
      }
    }
  }
}

/* 树形结构美化 */
.custom-tree {
  /* 去除默认Focus边框 */
  :deep(.el-tree-node:focus > .el-tree-node__content) {
    background-color: transparent;
  }

  :deep(.el-tree-node__content) {
    height: 48px; /* 增加行高 */
    border-radius: 8px;
    margin-bottom: 4px;
    transition: background-color 0.2s;
    border: 1px solid transparent;

    &:hover {
      background-color: var(--el-color-primary-light-9);
      .node-actions {
        opacity: 1;
        visibility: visible;
      }
    }
  }

  /* 选中状态 */
  :deep(.el-tree-node.is-current > .el-tree-node__content) {
    background-color: var(--el-color-primary-light-9);
    color: var(--el-color-primary);
    font-weight: 500;
  }
}

.custom-tree-node {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-right: 8px;
  font-size: 14px;
  width: 100%;
}

.node-content {
  display: flex;
  align-items: center;
  gap: 10px;
  overflow: hidden;

  .type-icon {
    font-size: 18px;
    &.text-folder {
      color: #e6a23c;
    }
    &.text-menu {
      color: #409eff;
    }
    &.text-btn {
      color: #909399;
    }
  }

  .node-label {
    margin-right: 12px;
    color: #303133;
  }

  .node-tags {
    display: flex;
    gap: 8px;

    .code-tag {
      font-family: monospace;
      background-color: #f4f4f5;
      color: #909399;
      border: none;
    }
    .status-tag {
      border: none;
    }
  }
}

.node-actions {
  display: flex;
  align-items: center;
  gap: 16px;
  opacity: 0; /* 默认隐藏 */
  visibility: hidden;
  transition: all 0.2s ease;

  .action-buttons {
    display: flex;
    align-items: center;

    .el-button {
      padding: 4px 8px;
      height: auto;
      font-size: 13px;

      & + .el-button {
        margin-left: 0;
        position: relative;

        &::before {
          content: '';
          position: absolute;
          left: 0;
          top: 3px;
          bottom: 3px;
          width: 1px;
          background-color: #dcdfe6;
        }
      }
    }
  }
}

/* 响应式调整 */
@media (max-width: 768px) {
  .header-actions {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
  .toolbar {
    flex-direction: column;
    align-items: stretch;
    gap: 12px;

    .search-input {
      width: 100%;
    }
  }
  .node-tags {
    display: none;
  }
  .node-actions {
    opacity: 1;
    visibility: visible;
  }
}
</style>
