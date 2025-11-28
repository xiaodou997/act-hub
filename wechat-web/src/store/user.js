export const userStore = {
  state: {
    accessToken: uni.getStorageSync('accessToken') || '',
    refreshToken: uni.getStorageSync('refreshToken') || '',
    profile: uni.getStorageSync('profile') || null,
  },
  setTokens(a, r) { this.state.accessToken = a; this.state.refreshToken = r; uni.setStorageSync('accessToken', a); uni.setStorageSync('refreshToken', r) },
  setProfile(p) { this.state.profile = p; uni.setStorageSync('profile', p) },
  logout() { this.setTokens('', ''); uni.removeStorageSync('profile') },
}

