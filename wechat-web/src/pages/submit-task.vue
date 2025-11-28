<template>
  <view class="page">
    <view class="field"
      ><text>报名平台</text>
      <picker
        :range="platforms"
        @change="(e) => (platform = platforms[e.detail.value])"
      >
        <view class="picker">{{ platform }}</view>
      </picker>
    </view>
    <view class="field">
      <text>作品链接 *</text>
      <input placeholder="粘贴作品链接" v-model="link" />
    </view>
    <view class="actions">
      <button class="primary" :disabled="!canSubmit" @click="submit">
        提交任务
      </button>
    </view>
  </view>
</template>
<script setup>
import { ref, computed, onLoad } from 'vue'
import { api } from '../utils/request'

const id = ref('')
const link = ref('')
const platform = ref('小红书')
const platforms = ['小红书', '抖音', '今日头条', '知乎']

const domains = {
  小红书: 'xhslink.com',
  抖音: 'v.douyin.com',
  今日头条: 'm.toutiao.com',
  知乎: 'zhuanlan.zhihu.com'
}

const canSubmit = computed(() => validate(link.value))

function validate(url) {
  if (!url || url.length < 8) return false
  const d = domains[platform.value]
  try {
    const u = url.toLowerCase()
    if (!(u.startsWith('http://') || u.startsWith('https://'))) return false
    const host = u.replace(/^https?:\/\//, '').split('/', 2)[0]
    return host.endsWith(d)
  } catch (e) {
    return false
  }
}

const submit = async () => {
  if (!canSubmit.value) {
    uni.showToast({ title: '请填写正确链接', icon: 'none' })
    return
  }
  await api.submitParticipation(id.value, { link: link.value, content: '' })
  uni.showToast({ title: '提交成功', icon: 'success' })
  uni.switchTab({ url: '/pages/my-tasks' })
}

onLoad((opt) => {
  id.value = opt.id
})
</script>
<style>
.field {
  background: #fff;
  margin: 8px;
  padding: 12px;
  border-radius: 8px;
}
.picker {
  padding: 8px;
  background: #eee;
  border-radius: 6px;
  margin-top: 6px;
}
.actions {
  padding: 12px;
}
</style>
