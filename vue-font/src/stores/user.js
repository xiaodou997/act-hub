import { defineStore } from 'pinia'
import { authApi } from '@/api/auth'
import router from '@/router'

export const useUserStore = defineStore('user', {
  state: () => ({
    accessToken: localStorage.getItem('accessToken') || '',
    refreshToken: localStorage.getItem('refreshToken') || '',
    userInfo: JSON.parse(localStorage.getItem('userInfo') || '{}'),
    rememberedUsername: localStorage.getItem('rememberedUsername') || '', // 新增：记住的用户名
  }),
  getters: {
    isAuthenticated: (state) => !!state.accessToken,
    username: (state) => state.userInfo.username || '',
    roles: (state) => state.userInfo.roles || [],
  },
  actions: {
    async login({ username, password, remember = false }) {
      const response = await authApi.login(username, password)
      console.log('login response', response)
      this.accessToken = response.accessToken
      this.refreshToken = response.refreshToken
      this.userInfo = response.userInfo

      console.log(this.userInfo)
      localStorage.setItem('accessToken', response.accessToken)
      localStorage.setItem('refreshToken', response.refreshToken)
      localStorage.setItem('userInfo', JSON.stringify(response.userInfo))
      console.log('rememberedUsername', this.rememberedUsername)
      // 处理记住我功能
      if (remember) {
        localStorage.setItem('rememberedUsername', username)
        this.rememberedUsername = username
      } else {
        localStorage.removeItem('rememberedUsername')
        this.rememberedUsername = ''
      }

      return response
    },
    async logout() {
      try {
        await authApi.logout()
      } catch (e) {}
      this.accessToken = ''
      this.refreshToken = ''
      this.userInfo = {}
      localStorage.removeItem('accessToken')
      localStorage.removeItem('refreshToken')
      localStorage.removeItem('userInfo')

      router.push('/login').then(() => {
        window.location.reload()
      })
    },
  },
})
