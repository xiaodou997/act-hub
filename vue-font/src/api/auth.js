import request from '@/api/request'

export const authApi = {
  login(username, password) {
    return request.post('/admin/auth/login', { username, password })
  },
  logout() {
    return request.post('/admin/auth/logout')
  },
  refresh(refreshToken) {
    return request.post('/admin/auth/refresh', { refreshToken })
  },
  kick(userId) {
    return request.post(`/admin/auth/kick/${userId}`)
  },
}
