// src/api/order.js
import { createApi } from '@/api/baseApi'
import request from '@/api/request'

// 订单管理API接口封装
export const orderApi = {
  ...createApi('/order'),
}
