// src/api/task.js
import request from '@/api/request'

/**
 * 任务管理API接口
 */
export const taskApi = {
  /**
   * 分页查询任务列表
   * @param {Object} params - 查询参数
   * @returns {Promise} 分页结果
   */
  page(params) {
    return request({
      url: '/task/page',
      method: 'get',
      params,
    })
  },

  /**
   * 获取任务详情
   * @param {string} id - 任务ID
   * @returns {Promise} 任务详情
   */
  getById(id) {
    return request({
      url: `/task/${id}`,
      method: 'get',
    })
  },

  /**
   * 创建任务
   * @param {Object} data - 任务数据
   * @returns {Promise}
   */
  create(data) {
    return request({
      url: '/task',
      method: 'post',
      data,
    })
  },

  /**
   * 更新任务
   * @param {string} id - 任务ID
   * @param {Object} data - 任务数据
   * @returns {Promise}
   */
  update(id, data) {
    return request({
      url: `/task/${id}`,
      method: 'put',
      data,
    })
  },

  /**
   * 删除任务
   * @param {string} id - 任务ID
   * @returns {Promise}
   */
  delete(id) {
    return request({
      url: `/task/${id}`,
      method: 'delete',
    })
  },

  /**
   * 变更任务状态
   * @param {string} id - 任务ID
   * @param {number} status - 目标状态
   * @returns {Promise}
   */
  changeStatus(id, status) {
    return request({
      url: `/task/${id}/status`,
      method: 'put',
      params: { status },
    })
  },

  /**
   * 导入定向用户
   * @param {string} id - 任务ID
   * @param {Object} data - 用户列表数据
   * @returns {Promise}
   */
  importTargetUsers(id, data) {
    return request({
      url: `/task/${id}/target-users`,
      method: 'post',
      data,
    })
  },

  /**
   * 获取定向用户列表
   * @param {string} id - 任务ID
   * @param {Object} params - 分页参数
   * @returns {Promise}
   */
  getTargetUsers(id, params) {
    return request({
      url: `/task/${id}/target-users`,
      method: 'get',
      params,
    })
  },
}

/**
 * 任务参与记录API接口
 */
export const taskParticipationApi = {
  /**
   * 分页查询参与记录
   * @param {Object} params - 查询参数
   * @returns {Promise} 分页结果
   */
  page(params) {
    return request({
      url: '/task-participation/page',
      method: 'get',
      params,
    })
  },

  /**
   * 获取参与记录详情
   * @param {string} id - 记录ID
   * @returns {Promise}
   */
  getById(id) {
    return request({
      url: `/task-participation/${id}`,
      method: 'get',
    })
  },

  /**
   * 审核参与记录
   * @param {string} id - 记录ID
   * @param {Object} data - 审核数据
   * @returns {Promise}
   */
  audit(id, data) {
    return request({
      url: `/task-participation/${id}/audit`,
      method: 'post',
      data,
    })
  },

  /**
   * 批量审核
   * @param {Array} ids - 记录ID列表
   * @param {number} auditStatus - 审核状态
   * @param {string} auditNotes - 审核备注
   * @returns {Promise}
   */
  batchAudit(ids, auditStatus, auditNotes) {
    return request({
      url: '/task-participation/batch-audit',
      method: 'post',
      params: { auditStatus, auditNotes },
      data: ids,
    })
  },

  /**
   * 导出参与记录
   * @param {Object} params - 查询参数
   * @returns {Promise}
   */
  export(params) {
    return request({
      url: '/task-participation/export',
      method: 'get',
      params,
      responseType: 'blob',
    })
  },

  /**
   * 获取统计信息
   * @param {string} taskId - 任务ID
   * @returns {Promise}
   */
  getStatistics(taskId) {
    return request({
      url: '/task-participation/statistics',
      method: 'get',
      params: { taskId },
    })
  },
}
