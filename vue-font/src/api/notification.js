import request from '@/api/request'

export const notificationApi = {
  getPage: (params) => request({ url: '/notification/page', method: 'get', params }),
  markRead: (id) => request({ url: `/notification/${id}/read`, method: 'post' }),
}
