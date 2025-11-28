import request from '@/api/request'

export const rewardPayoutApi = {
  distribute: (data) =>
    request({ url: '/reward-payout/distribute', method: 'post', data }),
  getPage: (params) =>
    request({ url: '/reward-payout/page', method: 'get', params }),
}

