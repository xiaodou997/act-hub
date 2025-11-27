// src/api/permission.js
import request from '@/api/request'
import { createApi } from '@/api/baseApi'

// 权限管理API接口封装
export const permissionApi = {
  ...createApi('/permission'),

  /**
   * 获取所有权限列表（用于角色分配权限时的下拉选项）
   * @returns {Promise} 权限列表
   */
  list() {
    return request({
      url: '/permission/list',
      method: 'get',
    })
  },

  /**
   * 分页查询权限列表
   * @param {Object} params - 查询参数
   * @param {number} params.pageNum - 页码，默认1
   * @param {number} params.pageSize - 每页条数，默认10
   * @param {string} params.name - 权限名称（可选）
   * @param {string} params.code - 权限编码（可选）
   * @param {number} params.type - 权限类型（可选）
   * @returns {Promise} 分页数据
   */
  // getPage方法通过createApi已自动生成，这里可以省略或覆盖自定义
}
