<template>
  <view class="container">
    <view class="logo">ActHub</view>
    <button class="primary" @click="login">微信授权登录</button>
  </view>
</template>
<script setup>
import { api } from '../utils/request'
import { userStore } from '../store/user'

const login = () => {
  // 申请用户信息与code
  uni
    .getUserProfile({ desc: '用于完善资料' })
    .then((profileRes) => {
      const profile = profileRes.userInfo
      uni
        .login({ provider: 'weixin' })
        .then(async (codeRes) => {
          const code = codeRes.code
          const data = await api.login(code, profile)
          userStore.setTokens(data.accessToken, data.refreshToken)
          userStore.setProfile(profile)
          uni.switchTab({ url: '/pages/index' })
        })
        .catch(() => uni.showToast({ title: '登录失败', icon: 'none' }))
    })
    .catch(() => uni.showToast({ title: '需要授权用户信息', icon: 'none' }))
}
</script>
<style>
.container {
  padding: 24px;
}
.logo {
  font-size: 24px;
  margin-bottom: 16px;
}
</style>
