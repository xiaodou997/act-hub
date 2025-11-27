// src/api/feature.js
import { createApi } from '@/api/baseApi'

// 功能管理API接口封装
export const featureApi = {
  ...createApi('/feature'),

  // 如果有特殊的方法，可以在这里添加
  // 例如：特殊查询、批量操作等
}
