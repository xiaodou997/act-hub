// src/api/aiWorkshop.js
import request from '@/api/request'

/**
 * AI工坊API接口
 */
export const aiWorkshopApi = {
  /**
   * 获取AI应用分类列表
   * @returns {Promise} 分类列表
   */
  getCategories() {
    return request({
      url: '/ai-workshop/categories',
      method: 'get',
    })
  },

  /**
   * 根据分类ID获取AI应用列表
   * @param {string} typeId - 分类ID
   * @returns {Promise} AI应用列表
   */
  getApplicationsByType(typeId) {
    return request({
      url: '/ai-workshop/applications',
      method: 'get',
      params: { typeId },
    })
  },

  /**
   * 获取AI应用详情（包含参数Schema）
   * @param {string} appId - 应用ID
   * @returns {Promise} 应用详情
   */
  getApplicationDetail(appId) {
    return request({
      url: `/ai-workshop/application/${appId}`,
      method: 'get',
    })
  },

  /**
   * 执行AI应用（生成内容）
   * @param {string} appId - 应用ID
   * @param {Object} params - 执行参数
   * @returns {Promise} 执行结果
   */
  execute(appId, params) {
    return request({
      url: `/ai-workshop/execute/${appId}`,
      method: 'post',
      data: params,
    })
  },

  /**
   * 查询执行记录
   * @param {string} recordId - 记录ID
   * @returns {Promise} 记录详情
   */
  getRecord(recordId) {
    return request({
      url: `/ai-workshop/record/${recordId}`,
      method: 'get',
    })
  },
}
