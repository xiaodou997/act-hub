<template>
  <div class="menu-edit-form-refined">
    <el-form
      ref="formRef"
      :model="localForm"
      :rules="formRules"
      label-width="100px"
      label-position="top"
      class="custom-form"
    >
      <!-- 类型选择器：放在顶部最醒目 -->
      <div class="type-selector-wrapper">
        <el-form-item label="菜单类型" prop="type" style="margin-bottom: 0">
          <el-radio-group v-model="localForm.type" class="type-radio-group">
            <el-radio-button :value="0">
              <div class="radio-content">
                <el-icon><Folder /></el-icon> 目录
              </div>
            </el-radio-button>
            <el-radio-button :value="1">
              <div class="radio-content">
                <el-icon><Document /></el-icon> 菜单
              </div>
            </el-radio-button>
            <el-radio-button :value="2">
              <div class="radio-content">
                <el-icon><Operation /></el-icon> 按钮
              </div>
            </el-radio-button>
          </el-radio-group>
        </el-form-item>
      </div>

      <el-divider content-position="left">基础信息</el-divider>

      <el-row :gutter="24">
        <el-col :span="24">
          <el-form-item label="上级菜单" prop="parentId">
            <el-tree-select
              v-model="localForm.parentId"
              :data="treeData"
              :props="treeProps"
              check-strictly
              clearable
              placeholder="选择父菜单 (留空则为顶级菜单)"
              class="full-width"
            >
              <template #default="{ data }">
                <span class="custom-tree-option">
                  <el-icon v-if="data.type === 0"><Folder /></el-icon>
                  <el-icon v-else><Document /></el-icon>
                  {{ data.name }}
                </span>
              </template>
            </el-tree-select>
          </el-form-item>
        </el-col>

        <el-col :span="12">
          <el-form-item label="名称" prop="name">
            <el-input v-model="localForm.name" placeholder="如：系统管理" />
          </el-form-item>
        </el-col>

        <el-col :span="12">
          <el-form-item label="显示排序" prop="sortOrder">
            <el-input-number
              v-model="localForm.sortOrder"
              :min="0"
              controls-position="right"
              class="full-width"
            />
          </el-form-item>
        </el-col>

        <el-col :span="24" v-if="localForm.type !== 2">
          <el-form-item label="菜单图标" prop="icon">
            <el-input v-model="localForm.icon" placeholder="请输入Element Plus图标名称">
              <template #prefix>
                <!-- 这里可以做一个图标预览或选择器 -->
                <el-icon><component :is="localForm.icon || 'Menu'" /></el-icon>
              </template>
            </el-input>
          </el-form-item>
        </el-col>
      </el-row>

      <!-- 仅菜单和按钮显示路由/权限设置 -->
      <template v-if="localForm.type !== 0">
        <el-divider content-position="left">路由与权限</el-divider>
        <el-row :gutter="24">
          <el-col :span="12" v-if="localForm.type === 1">
            <el-form-item label="路由路径" prop="path">
              <el-input v-model="localForm.path" placeholder="/system/user" />
            </el-form-item>
          </el-col>
          <el-col :span="12" v-if="localForm.type === 1">
            <el-form-item label="组件键名" prop="componentName">
              <el-input v-model="localForm.componentName" placeholder="UserManagement" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item prop="permissionCode">
              <template #label>
                权限标识
                <el-tooltip
                  content="Controller中定义的权限字符，如: @PreAuthorize(`@ss.hasPermi('system:user:list')`)"
                  placement="top"
                >
                  <el-icon class="help-icon"><QuestionFilled /></el-icon>
                </el-tooltip>
              </template>
              <el-input v-model="localForm.permissionCode" placeholder="如：system:user:list">
                <template #prepend v-if="localForm.type === 2">Btn</template>
              </el-input>
            </el-form-item>
          </el-col>
        </el-row>
      </template>

      <el-divider content-position="left">其他设置</el-divider>

      <div class="settings-grid">
        <el-form-item label="菜单状态" prop="status" class="setting-item">
          <el-switch
            v-model="statusSwitch"
            inline-prompt
            active-text="启用"
            inactive-text="禁用"
            style="--el-switch-on-color: #13ce66; --el-switch-off-color: #ff4949"
          />
        </el-form-item>

        <el-form-item
          label="是否可见"
          prop="isVisible"
          class="setting-item"
          v-if="localForm.type !== 2"
        >
          <el-switch
            v-model="visibleSwitch"
            inline-prompt
            active-text="显示"
            inactive-text="隐藏"
          />
        </el-form-item>
      </div>

      <div class="form-footer-refined">
        <el-button @click="$emit('cancel')" class="cancel-btn">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit" class="submit-btn">
          {{ isEdit ? '保存更新' : '立即创建' }}
        </el-button>
      </div>
    </el-form>
  </div>
</template>

<script setup>
import { ref, watch, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Folder, Document, Operation, Menu, QuestionFilled } from '@element-plus/icons-vue'

const props = defineProps({
  formData: { type: Object, required: true, default: () => ({}) },
  isEdit: { type: Boolean, default: false },
  treeData: { type: Array, default: () => [] },
})
const emit = defineEmits(['submit', 'cancel'])

const formRef = ref()
const submitting = ref(false)
const localForm = ref({
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

watch(
  () => props.formData,
  (val) => {
    localForm.value = { ...localForm.value, ...(val || {}) }
  },
  { immediate: true, deep: true },
)

const treeProps = { label: 'name', value: 'id', children: 'children' }

const statusSwitch = computed({
  get: () => localForm.value.status === 1,
  set: (v) => (localForm.value.status = v ? 1 : 0),
})
const visibleSwitch = computed({
  get: () => localForm.value.isVisible === 1,
  set: (v) => (localForm.value.isVisible = v ? 1 : 0),
})

const formRules = {
  name: [{ required: true, message: '请输入菜单名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择类型', trigger: 'change' }],
  path: [
    {
      validator: (_, val, cb) => {
        if (localForm.value.type === 1 && !val) return cb(new Error('菜单类型时路由地址必填'))
        cb()
      },
      trigger: 'blur',
    },
  ],
  componentName: [
    {
      validator: (_, val, cb) => {
        if (localForm.value.type === 1 && !val) return cb(new Error('菜单类型时组件键名必填'))
        cb()
      },
      trigger: 'blur',
    },
  ],
}

const handleSubmit = async () => {
  try {
    const valid = await formRef.value.validate()
    if (!valid) return
    submitting.value = true
    // 创建一个新对象，过滤掉系统自动维护的字段
    const submitData = { ...localForm.value }
    // 删除系统自动维护的字段
    delete submitData.createdAt
    delete submitData.updatedAt
    emit('submit', submitData)
  } catch (e) {
    ElMessage.error('请完善表单信息')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped lang="scss">
.menu-edit-form-refined {
  padding: 0 10px;
}

.type-selector-wrapper {
  display: flex;
  justify-content: center;
  margin-bottom: 24px;

  .type-radio-group {
    :deep(.el-radio-button__inner) {
      padding: 10px 24px;
      height: auto;
      border-radius: 0;
    }
    :deep(.el-radio-button:first-child .el-radio-button__inner) {
      border-radius: 20px 0 0 20px;
    }
    :deep(.el-radio-button:last-child .el-radio-button__inner) {
      border-radius: 0 20px 20px 0;
    }
  }

  .radio-content {
    display: flex;
    align-items: center;
    gap: 6px;
    font-weight: 500;
  }
}

.full-width {
  width: 100%;
}

.custom-tree-option {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
}

.help-icon {
  margin-left: 4px;
  color: #909399;
  cursor: help;
  vertical-align: middle;
}

.settings-grid {
  display: flex;
  gap: 40px;
  background: #f8f9fa;
  padding: 16px;
  border-radius: 8px;

  .setting-item {
    margin-bottom: 0 !important;
  }
}

/* 底部按钮栏优化 */
.form-footer-refined {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 32px;
  padding-top: 20px;
  border-top: 1px solid #f0f2f5;

  .cancel-btn {
    min-width: 80px;
  }

  .submit-btn {
    min-width: 100px;
    background: var(--app-primary-gradient);
    border: none;
    box-shadow: 0 4px 12px rgba(var(--app-primary-rgb), 0.3);

    &:hover {
      opacity: 0.9;
    }
  }
}

:deep(.el-divider__text) {
  font-size: 13px;
  color: #909399;
  font-weight: 500;
}
</style>
