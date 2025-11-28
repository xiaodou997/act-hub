// src/api/aiAppRecord.js
import request from '@/api/request'

/**
 * AI应用执行记录API接口
 */
export const aiAppRecordApi = {
  /**
   * 分页查询执行记录（管理端）
   * @param {Object} params - 查询参数
   * @param {number} params.pageNum - 页码（默认1）
   * @param {number} params.pageSize - 每页条数（默认20）
   * @param {string} params.aiApplicationId - 应用ID（可选）
   * @param {number} params.status - 执行状态 1-成功,2-失败,3-进行中（可选）
   * @param {string} params.userId - 用户ID，超管可用（可选）
   * @returns {Promise} 分页结果
   */
  page(params) {
    return request({
      url: '/ai-app-record/page',
      method: 'get',
      params: {
        pageNum: params.pageNum || params.page || 1,
        pageSize: params.pageSize || 20,
        aiApplicationId: params.aiApplicationId || params.appId,
        status: params.status,
        userId: params.userId,
      },
    })
  },

  /**
   * 查询当前用户的执行记录（AI工坊使用）
   * @param {Object} params - 查询参数
   * @param {number} params.pageNum - 页码（默认1）
   * @param {number} params.pageSize - 每页条数（默认20）
   * @param {string} params.aiApplicationId - 应用ID（可选）
   * @param {number} params.status - 执行状态（可选）
   * @returns {Promise} 分页结果
   */
  myRecords(params = {}) {
    return request({
      url: '/ai-app-record/my',
      method: 'get',
      params: {
        pageNum: params.pageNum || params.page || 1,
        pageSize: params.pageSize || 20,
        aiApplicationId: params.aiApplicationId || params.appId,
        status: params.status,
      },
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
   * 根据执行ID查询记录（用于异步任务）
   * @param {string} executeId - 执行ID
   * @returns {Promise} 记录详情
   */
  getByExecuteId(executeId) {
    return request({
      url: `/ai-app-record/execute/${executeId}`,
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
}
