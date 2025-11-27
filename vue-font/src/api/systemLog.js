// src/api/systemLog.js
import { createApi } from '@/api/baseApi'

// 系统日志管理API接口封装
export const systemLogApi = {
  // 使用通用API工厂函数处理标准CRUD操作中的分页查询和详情查询
  ...createApi('/system-log'),
}
