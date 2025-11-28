// 组件映射配置文件
// 用于将后端返回的组件键名映射到实际的组件路径
// 遵循命名规范：
// - 菜单页面使用 PascalCase (如: MenuManagement, UserList)
// - 布局组件带 Layout 后缀 (如: BasicLayout)

const componentMap = {
  // 系统管理相关组件
  UserManagement: () => import('@/views/user/index.vue'),
  RoleManagement: () => import('@/views/role/RoleManagement.vue'),
  PermissionManagement: () => import('@/views/permission/PermissionManagement.vue'),
  MenuManagement: () => import('@/views/menu/MenuManagement.vue'),
  AiAppTypeManagement: () => import('@/views/ai-app-type/AiAppTypeManagement.vue'),
  AiApplicationManagement: () => import('@/views/ai-app/AiApplicationManagement.vue'),
  AiWorkshop: () => import('@/views/ai-workshop/AiWorkshopPage.vue'), // 新增AI工坊页面

  ComingSoon: () => import('@/views/ComingSoon.vue'),

  // 这里可以根据项目需求继续添加更多组件映射
}

// 导出组件映射配置
export default componentMap

// 导出获取组件的辅助函数
export const getComponent = (componentName) => {
  if (!componentName) {
    console.warn('组件名称为空，返回默认的ComingSoon组件')
    return componentMap['ComingSoon']
  }

  const component = componentMap[componentName]
  if (!component) {
    console.warn(`未找到名为${componentName}的组件映射，返回默认的ComingSoon组件`)
    return componentMap['ComingSoon']
  }

  return component
}
