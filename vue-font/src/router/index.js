import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useMenuStore } from '@/stores/menu' // 导入菜单store
import LoginView from '@/views/login/LoginView.vue'

const routes = [
  {
    path: '/login',
    name: 'login',
    component: LoginView,
    meta: { hidden: true },
  },
  {
    path: '/',
    name: 'main', // 为main路由添加name，方便addRoute时使用
    component: () => import('@/components/layout/BasicLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      // 只保留默认重定向，其他路由将通过动态注册添加
      { path: '', redirect: '/user' },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
})

// 路由守卫
router.beforeEach(async (to, from, next) => {
  const userStore = useUserStore()
  const menuStore = useMenuStore()

  // 处理未登录的情况
  if (to.name !== 'login' && !userStore.isAuthenticated) {
    return next({ name: 'login' })
  }

  // 已登录用户访问登录页，重定向到首页
  if (to.name === 'login' && userStore.isAuthenticated) {
    next({ name: 'main' })
    return
  }

  // 如果用户已登录但菜单未加载，则加载菜单和动态路由
  if (userStore.isAuthenticated && !menuStore.isLoaded) {
    try {
      await menuStore.loadMenus()

      // 动态路由注册后，重新导航到目标路由
      if (to.path !== '/') {
        next({ ...to, replace: true })
        return
      }
    } catch (error) {
      console.error('加载菜单失败:', error)
      // 加载失败，退出登录
      await userStore.logout()
      return next({ name: 'login' })
    }
  }

  // 处理404情况 - 如果目标路由不存在且不在主布局下，重定向到主页面
  if (to.matched.length === 0 && to.path !== '/login') {
    console.warn(`路由不存在: ${to.path}`)
    next({ path: '/', replace: true })
    return
  }

  // 页面权限控制
  const requiredRoles = to.meta?.roles
  if (requiredRoles && requiredRoles.length > 0) {
    const userRoles = userStore.roles || []
    const hasPermission = userRoles.some((role) => requiredRoles.includes(role))
    if (!hasPermission) {
      // 无权限时重定向到首页
      return next({ path: '/', replace: true })
    }
  }

  next()
})

export default router
