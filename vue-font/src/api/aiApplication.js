import { createApi } from '@/api/baseApi'
import request from '@/api/request'

export const aiApplicationApi = {
  ...createApi('/ai-application'),

  getPage(params) {
    return request({
      url: '/ai-application',
      method: 'get',
      params,
    })
  },

  update(id, data) {
    return request({
      url: `/ai-application/${id}`,
      method: 'put',
      data,
    })
  },

  run(appId, params) {
    return request({
      url: `/ai-application/v1/run/${appId}`,
      method: 'post',
      data: params,
    })
  },
}
