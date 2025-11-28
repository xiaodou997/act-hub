import request from '@/api/request'

export const blacklistApi = {
  getPage: (params) => request({ url: '/admin/blacklist/page', method: 'get', params }),
  add: (data) => request({ url: '/admin/blacklist', method: 'post', data }),
  remove: (id) => request({ url: `/admin/blacklist/${id}`, method: 'delete' }),
  check: (userId) => request({ url: `/admin/blacklist/check/${userId}`, method: 'get' }),
}
