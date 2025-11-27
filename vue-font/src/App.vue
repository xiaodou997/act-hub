<template>
  <router-view />
</template>

<script setup>
// 可加入全局监听或初始化逻辑
</script>

<style lang="scss">
/* 设置全局布局和字体 */
body,
html,
#app {
  margin: 0;
  padding: 0;
  height: 100%;
  font-family:
    -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  background-color: #f5f7fa;
}

* {
  box-sizing: border-box;
}
// 建议覆盖 Element Plus 的主题色变量
:root {
  --el-color-primary: #1a85f2;
  --el-color-primary-light-3: #5cb1f8;
  --el-color-primary-light-9: #ebf6ff;
  --el-font-family:
    -apple-system, BlinkMacSystemFont, 'PingFang SC', 'Helvetica Neue', Arial, sans-serif;
  --app-bg-color: #f5f7fa;
  --app-primary-gradient: linear-gradient(135deg, #3ab5f8 0%, #1a85f2 100%);
  --app-primary-rgb: 26, 133, 242;
}

body {
  margin: 0;
  background-color: var(--app-bg-color);
}

// 优化滚动条样式
::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}
::-webkit-scrollbar-thumb {
  background: #c0c4cc;
  border-radius: 3px;
}
::-webkit-scrollbar-track {
  background: transparent;
}
</style>
