import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { authApi } from '@/api/auth'
const apiBaseUrl = import.meta.env.VITE_API_BASE_URL

// create an axios instance
const service = axios.create({
  baseURL: apiBaseUrl,
  timeout: 30000, // request timeout
})

// 添加默认请求头
service.defaults.headers.common['X-Client-Type'] = 'admin'
let refreshPromise = null

// request interceptor
service.interceptors.request.use(
  async (config) => {
    const userStore = useUserStore()
    // 设置 Authorization 头
    if (userStore.accessToken) {
      config.headers['Authorization'] = `Bearer ${userStore.accessToken}`
    }

    return config
  },
  (error) => {
    console.log(error) // for debug
    return Promise.reject(error)
  },
)

// response interceptor
service.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code !== 0) {
      ElMessage.error(res.message || '请求出错')
      return Promise.reject(new Error(res.message || 'Error'))
    }
    return res.data
  },
  async (error) => {
    console.log('error', error)
    const userStore = useUserStore()
    const status = error?.response?.status
    const originalRequest = error?.config || {}
    console.log('originalRequest', originalRequest)
    console.log('status', status, userStore.refreshToken, originalRequest._retry)

    if ((status === 401 || status === 403) && userStore.refreshToken && !originalRequest._retry) {
      console.log('refreshToken', userStore.refreshToken)
      try {
        if (!refreshPromise) {
          refreshPromise = (async () => {
            const res = await authApi.refresh(userStore.refreshToken)
            userStore.accessToken = res.accessToken
            userStore.refreshToken = res.refreshToken
            localStorage.setItem('accessToken', res.accessToken)
            localStorage.setItem('refreshToken', res.refreshToken)
          })().finally(() => {
            refreshPromise = null
          })
        }
        await refreshPromise
        originalRequest._retry = true
        originalRequest.headers = originalRequest.headers || {}
        originalRequest.headers['Authorization'] = `Bearer ${userStore.accessToken}`
        return service(originalRequest)
      } catch (e) {
        userStore.logout()
      }
    }

    return Promise.reject(error)
  },
)

export default service
