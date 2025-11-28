import request from '@/api/request'

export const articleApi = {
  getPage: (params) => request({ url: '/admin/article/page', method: 'get', params }),
  getById: (id) => request({ url: `/admin/article/${id}`, method: 'get' }),
  create: (data) => request({ url: '/admin/article', method: 'post', data }),
  update: (data) => request({ url: '/admin/article', method: 'put', data }),
  delete: (id) => request({ url: `/admin/article/${id}`, method: 'delete' }),
}
