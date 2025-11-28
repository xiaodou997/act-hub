import request from '@/api/request'

export const rewardItemApi = {
  importItems: (data) => request({ url: '/reward-item/import', method: 'post', data }),
  getPage: (params) => request({ url: '/reward-item/page', method: 'get', params }),
}
