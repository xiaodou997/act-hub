import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { menuApi } from '@/api/menu'
import router from '@/router'
import { getComponent } from '@/router/componentMap'

// 使用Vue 3推荐的组合式API写法
export const useMenuStore = defineStore('menu', () => {
  // 状态定义（替代state对象）
  const menus = ref([]) // 完整的菜单数据
  const formattedMenus = ref([]) // 格式化后的菜单数据，用于前端展示
  const routeMap = ref({}) // 路由映射表，用于快速查找路由信息
  const loaded = ref(false) // 菜单数据是否已加载

  // 计算属性（替代getters对象）
  const allMenus = computed(() => menus.value)
  const displayMenus = computed(() => formattedMenus.value)
  const isLoaded = computed(() => loaded.value)
  const getRouteByPath = computed(() => (path) => routeMap.value[path])

  // 方法（替代actions对象）
  
  // 获取菜单数据并动态注册路由
  const loadMenus = async () => {
    try {
      // 从后端获取菜单数据
      const nav = await menuApi.getUserNav()
      
      if (Array.isArray(nav)) {
        // 存储原始菜单数据
        menus.value = [...nav]
        
        // 构建路由映射表
        buildRouteMap(nav)
        
        // 格式化菜单数据用于前端展示
        formattedMenus.value = formatMenus(
          [...nav].sort((a, b) => (a.sortOrder ?? 0) - (b.sortOrder ?? 0)),
        )
        
        // 动态注册路由
        await registerDynamicRoutes(nav)
        
        // 标记为已加载
        loaded.value = true
      }
      
      return menus.value
    } catch (error) {
      console.error('获取菜单失败:', error)
      menus.value = []
      formattedMenus.value = []
      loaded.value = false
      return []
    }
  }
  
  // 构建路由映射表
  const buildRouteMap = (menuItems) => {
    const processItem = (item) => {
      // 使用componentName替代component
      if (item.path && item.componentName) {
        routeMap.value[item.path] = {
          path: item.path,
          name: item.name,
          componentName: item.componentName,
          meta: {
            title: item.name,
            icon: item.icon,
            roles: item.roles || [],
            ...item.meta,
          },
        }
      }

      // 递归处理子菜单
      if (Array.isArray(item.children) && item.children.length > 0) {
        item.children.forEach(processItem)
      }
    }

    menuItems.forEach(processItem)
  }
  
  // 格式化菜单数据
  const formatMenus = (nodes) => {
    return nodes.map((node) => {
      const formattedNode = {
        id: node.id,
        path: node.path,
        title: node.name,
        icon: node.icon || 'Grid',
        // 保留componentName供后续使用
        componentName: node.componentName,
      }

      // 处理子菜单
      if (Array.isArray(node.children) && node.children.length > 0) {
        formattedNode.children = formatMenus(
          [...node.children].sort((a, b) => (a.sortOrder ?? 0) - (b.sortOrder ?? 0)),
        )
      }

      return formattedNode
    })
  }
  
  // 动态注册路由
  const registerDynamicRoutes = async (menuItems) => {
    const routes = generateRoutesFromMenu(menuItems)

    // 注册路由
    routes.forEach((route) => {
      router.addRoute('main', route)
    })

    // 确保404路由存在
    ensureNotFoundRoute()
  }
  
  // 从菜单数据生成路由配置
  const generateRoutesFromMenu = (menuItems, basePath = '') => {
    const routes = []

    const processItem = (item, parentPath = '') => {
      // 构建完整路径
      const fullPath = parentPath ? `${parentPath}/${item.path}` : item.path

      // 使用componentName替代component
      if (item.componentName) {
        const route = {
          path: item.path,
          name: item.name || fullPath,
          component: getComponent(item.componentName), // 使用组件映射函数获取组件
          meta: {
            title: item.name,
            icon: item.icon,
            roles: item.roles || [],
            ...item.meta,
          },
        }

        routes.push(route)
      }

      // 递归处理子菜单
      if (Array.isArray(item.children) && item.children.length > 0) {
        item.children.forEach((child) => processItem(child, fullPath))
      }
    }

    menuItems.forEach((item) => processItem(item, basePath))
    return routes
  }
  
  // 确保404路由存在
  const ensureNotFoundRoute = () => {
    // 检查是否已存在404路由
    const hasNotFound = router.getRoutes().some((route) => route.path === '/:pathMatch(.*)*')

    if (!hasNotFound) {
      router.addRoute({
        path: '/:pathMatch(.*)*',
        redirect: '/', // 重定向到首页，避免重定向循环
      })
    }
  }
  
  // 重置菜单数据
  const reset = () => {
    menus.value = []
    formattedMenus.value = []
    routeMap.value = {}
    loaded.value = false
  }

  // 导出所有状态和方法
  return {
    // 状态
    menus,
    formattedMenus,
    routeMap,
    loaded,
    // 计算属性
    allMenus,
    displayMenus,
    isLoaded,
    getRouteByPath,
    // 方法
    loadMenus,
    buildRouteMap,
    formatMenus,
    registerDynamicRoutes,
    generateRoutesFromMenu,
    ensureNotFoundRoute,
    reset,
  }
})
