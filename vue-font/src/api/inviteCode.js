import request from '@/api/request'

// 邀请码管理API接口封装
export const inviteCodeApi = {
  /**
   * 创建邀请码
   * @param {Object} data - 邀请码创建参数
   * @param {number} data.maxUses - 最大使用次数
   * @param {string} data.expireAt - 过期时间
   * @returns {Promise} 请求Promise
   */
  create(data) {
    return request({
      url: '/api/invite-code',
      method: 'post',
      data,
    })
  },

  /**
   * 更新邀请码
   * @param {Object} data - 邀请码更新参数
   * @param {string} data.id - 邀请码ID
   * @param {number} data.maxUses - 最大使用次数
   * @param {string} data.expireAt - 过期时间
   * @returns {Promise} 请求Promise
   */
  update(data) {
    return request({
      url: '/api/invite-code',
      method: 'put',
      data,
    })
  },

  /**
   * 删除邀请码
   * @param {string} id - 邀请码ID
   * @returns {Promise} 请求Promise
   */
  delete(id) {
    return request({
      url: `/api/invite-code/${id}`,
      method: 'delete',
    })
  },

  /**
   * 分页查询邀请码列表
   * @param {Object} params - 查询参数
   * @param {number} params.pageNum - 页码，默认为1
   * @param {number} params.pageSize - 每页数量，默认为20
   * @param {string} params.code - 邀请码名称（模糊查询）
   * @param {number} params.status - 状态（0-禁用，1-启用）
   * @returns {Promise} 请求Promise
   */
  getPage(params) {
    return request({
      url: '/api/invite-code/page',
      method: 'get',
      params,
    })
  },
}
