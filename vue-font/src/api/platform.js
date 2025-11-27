// src/api/role.js
import { createApi } from '@/api/baseApi'

// 角色管理API接口封装
export const platformApi = {
  ...createApi('/platform'),

  // 如果有特殊的方法，可以在这里添加
  // 例如：分配权限、获取权限等特殊操作
}
