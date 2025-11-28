<template>
  <view class="page">
    <view class="card">
      <input placeholder="收件人" v-model="form.recipientName" />
      <input placeholder="手机号" v-model="form.phone" />
      <input placeholder="地区" v-model="form.region" />
      <input placeholder="详细地址" v-model="form.addressLine" />
      <input placeholder="邮编" v-model="form.zip" />
      <button class="primary" @click="submit">提交地址</button>
    </view>
  </view>
</template>
<script setup>
import { reactive } from 'vue'
import { request } from '../utils/request'

const form = reactive({
  recipientName: '',
  phone: '',
  region: '',
  addressLine: '',
  zip: ''
})
const submit = async () => {
  await request({ url: '/address', method: 'POST', data: form })
  uni.showToast({ title: '已提交', icon: 'success' })
}
</script>
<style>
.card {
  background: #fff;
  margin: 8px;
  padding: 12px;
  border-radius: 8px;
}
input {
  width: 100%;
  border: 1px solid #eee;
  border-radius: 6px;
  padding: 8px;
  margin-top: 8px;
}
</style>
