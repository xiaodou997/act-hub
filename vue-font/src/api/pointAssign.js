import request from '@/api/request'

// 积分分配管理API接口封装
export const pointAssignApi = {
  /**
   * 发放积分
   * @param {Object} data - 积分分配参数
   * @param {string} data.toUserId - 被发放者用户ID
   * @param {number} data.point - 分配积分数量
   * @param {number} [data.amount] - 成员支付的金额（可选）
   * @param {string} [data.remark] - 备注
   * @param {number} data.validDays - 充值积分有效期（天数）
   * @returns {Promise} 请求Promise
   */
  create(data) {
    return request({
      url: '/api/point-assign',
      method: 'post',
      data,
    })
  },

  /**
   * 获取积分分配记录详情
   * @param {string} id - 积分分配记录ID
   * @returns {Promise} 请求Promise
   */
  getById(id) {
    return request({
      url: `/api/point-assign/${id}`,
      method: 'get',
    })
  },

  /**
   * 更新积分分配记录
   * @param {Object} data - 积分分配记录数据
   * @param {string} data.id - 记录ID
   * @param {string} [data.teamId] - 团队ID
   * @param {string} [data.fromUserId] - 发放者用户ID
   * @param {string} [data.toUserId] - 被发放者用户ID
   * @param {number} [data.point] - 积分数量
   * @param {number} [data.usedPoints] - 已使用的积分数
   * @param {number} [data.amount] - 支付金额
   * @param {string} [data.remark] - 备注
   * @param {string} [data.expireAt] - 过期时间
   * @returns {Promise} 请求Promise
   */
  update(data) {
    return request({
      url: '/api/point-assign',
      method: 'put',
      data,
    })
  },

  /**
   * 删除积分分配记录
   * @param {string} id - 积分分配记录ID
   * @returns {Promise} 请求Promise
   */
  delete(id) {
    return request({
      url: `/api/point-assign/${id}`,
      method: 'delete',
    })
  },

  /**
   * 分页查询积分分配记录列表
   * @param {Object} params - 查询参数
   * @param {string} [params.teamId] - 团队ID
   * @param {string} [params.teamName] - 团队名称
   * @param {string} [params.fromUserName] - 发放者名称
   * @param {string} [params.toUserName] - 被发放者名称
   * @param {string} [params.remark] - 备注
   * @param {number} [params.pageNum=1] - 页码，默认为1
   * @param {number} [params.pageSize=20] - 每页数量，默认为20
   * @returns {Promise} 请求Promise
   */
  getList(params) {
    return request({
      url: '/api/point-assign/list',
      method: 'get',
      params,
    })
  },
}
