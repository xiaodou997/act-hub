// src/api/baseApi.js
import request from '@/api/request'

/**
 * 通用API工厂函数
 * @param {string} prefix - API路径前缀
 * @param {Object} options - 可选配置项
 * @returns {Object} 包含标准CRUD操作的API对象
 */
export function createApi(prefix, options = {}) {
  // 默认配置
  const defaultOptions = {
    // 是否使用复数形式的资源名（如 /users 而不是 /user）
    usePlural: false,
    // 自定义路径映射
    pathMap: {},
    // 自定义方法名映射
    methodMap: {},
  }

  const config = { ...defaultOptions, ...options }

  // 处理前缀，确保以 / 开头但不以 / 结尾
  const normalizedPrefix = prefix.startsWith('/')
    ? prefix.replace(/\/$/, '')
    : `/${prefix.replace(/\/$/, '')}`

  return {
    /**
     * 分页查询
     * @param {Object} params - 查询参数
     * @returns {Promise}
     */
    getPage(params) {
      const url = config.pathMap.getPage || `${normalizedPrefix}/page`
      return request({
        url,
        method: 'get',
        params,
      })
    },

    /**
     * 根据ID获取详情
     * @param {string|number} id - 资源ID
     * @returns {Promise}
     */
    getById(id) {
      const url = config.pathMap.getById || `${normalizedPrefix}/${id}`
      return request({
        url,
        method: 'get',
      })
    },

    /**
     * 创建资源
     * @param {Object} data - 资源数据
     * @returns {Promise}
     */
    create(data) {
      const url = config.pathMap.create || normalizedPrefix
      return request({
        url,
        method: 'post',
        data,
      })
    },

    /**
     * 更新资源
     * @param {Object} data - 资源数据
     * @returns {Promise}
     */
    update(data) {
      const url = config.pathMap.update || normalizedPrefix
      return request({
        url,
        method: 'put',
        data,
      })
    },

    /**
     * 删除资源
     * @param {string|number} id - 资源ID
     * @returns {Promise}
     */
    delete(id) {
      const url = config.pathMap.delete || `${normalizedPrefix}/${id}`
      return request({
        url,
        method: 'delete',
      })
    },
  }
}
