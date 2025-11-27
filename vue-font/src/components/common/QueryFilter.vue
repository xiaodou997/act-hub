<template>
  <el-row type="flex" justify="space-between" align="middle" class="query-filter">
    <!-- 左侧筛选表单 -->
    <el-col :span="20" class="left-col">
      <el-form :inline="true" :model="modelValue" class="query-form">
        <slot name="filters" />
        <el-form-item>
          <el-button type="primary" @click="$emit('search')">查询</el-button>
          <el-button @click="$emit('reset')">重置</el-button>
        </el-form-item>
      </el-form>
    </el-col>

    <!-- 右侧操作区 -->
    <el-col :span="4" class="right-col">
      <slot name="actions" />
    </el-col>
  </el-row>
</template>

<script setup>
defineProps({
  modelValue: Object,
})
defineEmits(['search', 'reset'])
</script>

<style scoped>
.query-filter {
  margin-bottom: 16px;
}
/* 关键修改部分 */
.left-col {
  display: flex;
  align-items: center;
}

.right-col {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  height: 100%;
}

.query-form {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px;
}

/* 确保所有表单项目垂直居中 */
:deep(.el-form-item) {
  margin-bottom: 0 !important;
  display: flex;
  align-items: center;
}
</style>
