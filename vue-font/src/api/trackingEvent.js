import request from '@/api/request'

export const trackingEventApi = {
  getPage: (params) => request({ url: '/admin/tracking-event/page', method: 'get', params }),
}
