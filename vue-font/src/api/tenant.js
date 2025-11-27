// src/api/tenant.js
import { createApi } from '@/api/baseApi'
import request from '@/api/request'

// 租户管理API接口封装
export const tenantApi = {
  ...createApi('/tenant'),
}
