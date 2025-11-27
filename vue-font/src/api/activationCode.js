// src/api/order.js
import { createApi } from './baseApi'

// 订单管理API接口封装
export const activationCodeApi = {
  ...createApi('/activation-code'),
}
