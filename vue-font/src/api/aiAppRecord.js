// src/api/aiAppRecord.js
import request from '@/api/request'

/**
 * AI应用执行记录API接口
 */
export const aiAppRecordApi = {
  /**
   * 分页查询执行记录
   * @param {Object} params - 查询参数
   * @param {number} params.page - 页码
   * @param {number} params.pageSize - 每页条数
   * @param {string} params.appId - 应用ID（可选）
   * @param {string} params.status - 执行状态（可选）
   * @param {string} params.startTime - 开始时间（可选）
   * @param {string} params.endTime - 结束时间（可选）
   * @returns {Promise} 分页结果
   */
  page(params) {
    return request({
      url: '/ai-app-record/page',
      method: 'get',
      params,
    })
  },

  /**
   * 获取执行记录详情
   * @param {string} id - 记录ID
   * @returns {Promise} 记录详情
   */
  getById(id) {
    return request({
      url: `/ai-app-record/${id}`,
      method: 'get',
    })
  },

  /**
   * 删除执行记录
   * @param {string} id - 记录ID
   * @returns {Promise}
   */
  delete(id) {
    return request({
      url: `/ai-app-record/${id}`,
      method: 'delete',
    })
  },

  /**
   * 批量删除执行记录
   * @param {Array<string>} ids - 记录ID数组
   * @returns {Promise}
   */
  batchDelete(ids) {
    return request({
      url: '/ai-app-record/batch',
      method: 'delete',
      data: ids,
    })
  },

  /**
   * 获取用户的执行记录列表（简化版，用于AI工坊）
   * @param {Object} params - 查询参数
   * @returns {Promise}
   */
  myRecords(params) {
    return request({
      url: '/ai-app-record/my',
      method: 'get',
      params,
    })
  },
}
