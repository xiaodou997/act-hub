// src/api/user.js
import { createApi } from '@/api/baseApi'
import request from '@/api/request'

// 用户管理API接口封装
export const userApi = {
  // 使用通用API工厂函数处理标准CRUD操作
  ...createApi('/user'),

  // 修改密码
  changePassword(data) {
    return request({
      url: '/user/password',
      method: 'put',
      data,
    })
  },

  // 重置用户密码
  resetPassword(id) {
    return request({
      url: `/user/reset-password/${id}`,
      method: 'put',
    })
  },

  // 分配角色给用户
  assignRoles(userId, roleIds) {
    return request({
      url: `/user/${userId}/roles`,
      method: 'post',
      params: { roleIds },
    })
  },

  // 获取用户角色
  getUserRoles(userId) {
    return request({
      url: `/user/${userId}/roles`,
      method: 'get',
    })
  },

  // 为了保持向后兼容性，可以保留原来的方法名
  getUserPage: createApi('/user').getPage,
  getUserById: createApi('/user').getById,
  createUser: createApi('/user').create,
  deleteUser: createApi('/user').delete,
}
