<template>
  <div class="page-card-wrapper">
    <!-- 标题栏 (可选，如果页面需要标题) -->
    <div v-if="title" class="page-header">
      <h2 class="title">{{ title }}</h2>
      <div class="actions">
        <slot name="actions"></slot>
      </div>
    </div>

    <!-- 内容区域 -->
    <div class="page-content">
      <slot></slot>
    </div>
  </div>
</template>

<script setup>
defineProps({
  title: {
    type: String,
    default: ''
  }
})
</script>

<style lang="scss" scoped>
.page-card-wrapper {
  background: #ffffff;
  border-radius: 12px; // 圆角
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05); // 柔和的阴影
  min-height: calc(100vh - 48px); // 减去外边距，让卡片撑满
  padding: 32px; // 卡片内部留白
  display: flex;
  flex-direction: column;
  
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;
    
    .title {
      font-size: 20px;
      font-weight: 600;
      color: #1f2d3d;
      margin: 0;
      position: relative;
      padding-left: 12px;
      
      // 标题前的小蓝竖条（可选装饰）
      &::before {
        content: '';
        position: absolute;
        left: 0;
        top: 4px;
        bottom: 4px;
        width: 4px;
        background: #1A85F2;
        border-radius: 2px;
      }
    }
  }
  
  .page-content {
    flex: 1;
  }
}
</style>
