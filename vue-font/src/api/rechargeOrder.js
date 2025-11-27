// src/api/rechargeOrder.js
import request from '@/api/request'

export const rechargeOrderApi = {
  /**
   * 获取订单分页列表
   * @param {Object} params 查询参数
   * @param {string} params.email 用户邮箱
   * @param {string} params.transactionId 微信订单号
   * @param {string} params.outTradeNo 商户订单号
   * @param {number} params.status 订单状态
   * @param {number} params.pageNum 页码
   * @param {number} params.pageSize 每页大小
   * @returns {Promise} 分页数据
   */
  getOrderList(params) {
    return request({
      url: '/api/recharge-order/list',
      method: 'get',
      params,
    })
  },

  /**
   * 根据ID获取订单详情
   * @param {string} id 订单ID
   * @returns {Promise} 订单详情
   */
  getOrderById(id) {
    return request({
      url: `/api/recharge-order/${id}`,
      method: 'get',
    })
  },

  /**
   * 创建新订单
   * @param {Object} orderData 订单数据
   * @returns {Promise} 创建结果
   */
  createOrder(orderData) {
    return request({
      url: '/api/recharge-order',
      method: 'post',
      data: orderData,
    })
  },
}
