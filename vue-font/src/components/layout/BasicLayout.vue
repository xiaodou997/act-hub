<template>
  <div class="app-wrapper">
    <!-- 左侧侧边栏 -->
    <aside class="sidebar-container">
      <!-- 1. 用户信息面板 (固定在左上角) -->
      <div class="user-panel">
        <div class="avatar-box">
          <!-- 优先使用头像，否则显示首字 -->
          <el-avatar :size="50" :src="userStore.avatar" class="custom-avatar">
            {{ userStore.username ? userStore.username.charAt(0).toUpperCase() : 'A' }}
          </el-avatar>
        </div>
        <div class="info-box">
          <h2 class="username" :title="userStore.username">{{ userStore.username || '管理员' }}</h2>
          <p class="user-id">ID: {{ userStore.userId || '12345646898' }}</p>
        </div>
      </div>

      <!-- 2. 导航菜单 (可滚动区域) -->
      <el-scrollbar class="menu-scrollbar">
        <el-menu
          :default-active="activeMenu"
          class="sidebar-menu"
          :collapse="false"
          router
          unique-opened
        >
          <!-- 支持嵌套菜单 -->
          <template v-for="menu in menus" :key="menu.id">
            <!-- 有子菜单的情况 -->
            <el-sub-menu v-if="menu.children && menu.children.length > 0" :index="menu.path">
              <template #title>
                <el-icon class="menu-icon">
                  <component :is="resolveIcon(menu.icon)" />
                </el-icon>
                <span class="menu-title">{{ menu.title }}</span>
              </template>
              <!-- 子菜单 -->
              <el-menu-item v-for="child in menu.children" :key="child.id" :index="child.path">
                <el-icon class="menu-icon">
                  <component :is="resolveIcon(child.icon)" />
                </el-icon>
                <span class="menu-title">{{ child.title }}</span>
              </el-menu-item>
            </el-sub-menu>
            <!-- 无子菜单的情况 -->
            <el-menu-item v-else :index="menu.path">
              <el-icon class="menu-icon">
                <component :is="resolveIcon(menu.icon)" />
              </el-icon>
              <span class="menu-title">{{ menu.title }}</span>
            </el-menu-item>
          </template>
        </el-menu>
      </el-scrollbar>
      <div class="sidebar-footer">
        <el-dropdown @command="handleSettingsCommand">
          <el-button class="settings-btn" text>
            <el-icon><Setting /></el-icon>
            设置
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="changePassword">
                <el-icon><EditPen /></el-icon>
                修改密码
              </el-dropdown-item>
              <el-dropdown-item command="logout">
                <el-icon><SwitchButton /></el-icon>
                退出登录
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </aside>

    <!-- 右侧主内容区 -->
    <main class="main-container">
      <!-- 这里不需要Header了，直接是内容 -->
      <div class="content-scroll-wrapper">
        <router-view v-slot="{ Component }">
          <transition name="fade-transform" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </div>
    </main>
    <ChangePasswordDialog
      v-model:visible="passwordDialogVisible"
      @success="handlePasswordChangeSuccess"
    />
  </div>
</template>

<script setup>
import { computed, ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useMenuStore } from '@/stores/menu' // 导入菜单store
import ChangePasswordDialog from '@/components/ChangePasswordDialog.vue'
import { Setting, EditPen, SwitchButton } from '@element-plus/icons-vue'
import * as Icons from '@element-plus/icons-vue'

const route = useRoute()
const userStore = useUserStore()
const menuStore = useMenuStore() // 使用菜单store

const activeMenu = computed(() => {
  const { meta, path } = route
  if (meta.activeMenu) {
    return meta.activeMenu
  }
  return path
})

// 直接从store获取格式化后的菜单数据
const menus = computed(() => menuStore.displayMenus)

const passwordDialogVisible = ref(false)
const handleSettingsCommand = (cmd) => {
  if (cmd === 'changePassword') {
    passwordDialogVisible.value = true
  } else if (cmd === 'logout') {
    userStore.logout()
  }
}
const handlePasswordChangeSuccess = () => {
  userStore.logout()
}

const resolveIcon = (name) => {
  return Icons[name] || Icons.Grid
}

// 移除所有本地的菜单处理逻辑
// const formatMenus = (nodes) => { ... }

onMounted(async () => {
  try {
    // 使用store加载菜单数据，会自动注册路由
    await menuStore.loadMenus()
  } catch (e) {
    console.error('加载菜单失败:', e)
  }
})
</script>

<style lang="scss" scoped>
/* ================= 变量定义 ================= */
$sidebar-width: 260px; // 侧边栏宽度，比原来稍微宽一点更大气
$primary-gradient: var(--app-primary-gradient);
$primary-color: var(--el-color-primary);
$bg-color: var(--app-bg-color);
$menu-text-color: #566a82;
$menu-active-bg: var(--el-color-primary-light-9);

.app-wrapper {
  display: flex;
  width: 100%;
  height: 100vh;
  background-color: $bg-color;
  overflow: hidden;
}

/* ================= 左侧样式 ================= */
.sidebar-container {
  width: $sidebar-width;
  height: 100%;
  background: #ffffff;
  display: flex;
  flex-direction: column;
  box-shadow: 4px 0 16px rgba(0, 0, 0, 0.04); // 侧边栏右侧轻微阴影
  z-index: 100;

  // 1. 用户面板区
  .user-panel {
    flex-shrink: 0; // 防止被压缩
    height: 140px; // 保持高度
    background: $primary-gradient;
    display: flex;
    align-items: center;
    padding: 0 24px;
    color: #ffffff;
    position: relative;
    overflow: hidden;

    // 增加一点背景装饰纹理（可选，模仿图片右上角的淡光）
    &::after {
      content: '';
      position: absolute;
      top: -20px;
      right: -20px;
      width: 100px;
      height: 100px;
      background: rgba(255, 255, 255, 0.1);
      border-radius: 50%;
      filter: blur(20px);
    }

    .avatar-box {
      margin-right: 16px;

      .custom-avatar {
        border: 3px solid rgba(255, 255, 255, 0.3);
        background: #fff;
        color: $primary-color;
        font-weight: bold;
        font-size: 20px;
      }
    }

    .info-box {
      overflow: hidden; // 防止文字溢出

      .username {
        margin: 0 0 4px 0;
        font-size: 18px;
        font-weight: 600;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
      }

      .user-id {
        margin: 0;
        font-size: 12px;
        opacity: 0.85;
        font-family: 'Roboto Mono', monospace; // 数字用等宽字体更好看
      }
    }
  }

  // 2. 菜单区
  .menu-scrollbar {
    flex: 1; // 占据剩余高度
    background: #fff;
  }

  .sidebar-menu {
    border-right: none;
    padding: 16px 0;

    :deep(.el-menu-item) {
      height: 56px;
      line-height: 56px;
      margin: 4px 0;
      color: $menu-text-color;
      border-left: 4px solid transparent; // 左侧高亮条占位

      .menu-icon {
        font-size: 20px;
        margin-right: 12px;
        color: #8996a6; // 图标默认灰色
        transition: color 0.3s;
      }

      .menu-title {
        font-size: 15px;
        font-weight: 500;
      }

      &:hover {
        background-color: #f7f8fa;
        color: $primary-color;
        .menu-icon {
          color: $primary-color;
        }
      }

      // 选中状态样式 (完全复刻图片)
      &.is-active {
        background-color: $menu-active-bg;
        color: $primary-color;
        border-left-color: $primary-color; // 左侧蓝色竖条出现

        .menu-icon {
          color: $primary-color;
        }
      }
    }
  }

  .sidebar-footer {
    flex-shrink: 0;
    padding: 12px 16px;
    border-top: 1px solid #eef2f6;
    background: #fff;
    display: flex;
    justify-content: center;
  }
}

/* ================= 右侧样式 ================= */
.main-container {
  flex: 1;
  height: 100%;
  background-color: $bg-color;
  position: relative;

  .content-scroll-wrapper {
    height: 100%;
    overflow-y: auto; // 内容区独立滚动
    padding: 24px; // 给内容留出边距
    box-sizing: border-box;
  }
}

/* 页面切换动画 */
.fade-transform-enter-active,
.fade-transform-leave-active {
  transition: all 0.3s;
}
.fade-transform-enter-from {
  opacity: 0;
  transform: translateX(-20px);
}
.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(20px);
}
</style>
