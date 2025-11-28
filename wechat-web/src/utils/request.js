const BASE_URL = process.env.MINI_BASE_URL || 'http://localhost:8080'

export function request({ url, method = 'GET', data = {}, header = {} }) {
  const token = uni.getStorageSync('accessToken')
  const h = { 'Content-Type': 'application/json', ...header }
  if (token) h['Authorization'] = `Bearer ${token}`
  return new Promise((resolve, reject) => {
    uni.request({
      url: BASE_URL + url,
      method,
      data,
      header: h,
      success: (res) => {
        const body = res.data || {}
        if (res.statusCode === 401 || res.statusCode === 403) {
          uni.navigateTo({ url: '/pages/login' })
          return reject(new Error('unauthorized'))
        }
        if (body.code !== 0) return reject(new Error(body.message || 'error'))
        resolve(body.data)
      },
      fail: reject,
    })
  })
}

export const api = {
  login(code, profile) { return request({ url: '/wx/login', method: 'POST', data: { code, profile } }) },
  tasks(params) { return request({ url: '/api/tasks', method: 'GET', data: params }) },
  taskDetail(id) { return request({ url: `/api/task/${id}`, method: 'GET' }) },
  claimTask(id) { return request({ url: `/api/task/${id}/claim`, method: 'POST' }) },
  submitParticipation(id, payload) { return request({ url: `/api/task-participation/${id}/submit`, method: 'POST', data: payload }) },
  myTasks(params) { return request({ url: '/api/my/tasks', method: 'GET', data: params }) },
  creativeRewrite(payload) { return request({ url: '/api/creative/rewrite', method: 'POST', data: payload }) },
  academyCategories() { return request({ url: '/api/academy/categories', method: 'GET' }) },
  academyArticles(params) { return request({ url: '/api/academy/articles', method: 'GET', data: params }) },
  myRewards(params) { return request({ url: '/api/reward/my', method: 'GET', data: params }) },
  rewardDetail(id) { return request({ url: `/api/reward/${id}`, method: 'GET' }) },
  redeemReward(id, payload) { return request({ url: `/api/reward/${id}/redeem`, method: 'POST', data: payload }) },
}

