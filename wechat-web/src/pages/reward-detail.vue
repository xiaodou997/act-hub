<template>
  <view class="page" v-if="detail">
    <image v-if="detail.imageUrl" :src="detail.imageUrl" mode="aspectFill" style="width:100%;height:160px" />
    <view class="card">
      <view class="title">{{detail.rewardName}}</view>
      <view>使用说明：{{detail.rules || '—'}}</view>
    </view>
    <view class="actions">
      <button v-if="detail.rewardForm===3" class="primary" @click="redeemCode">立即使用</button>
      <button v-else class="primary" @click="fillAddress">填写收货地址</button>
    </view>
  </view>
</template>
<script setup>
import { ref, onLoad } from 'vue'
import { api } from '../utils/request'

const id = ref('')
const detail = ref(null)
onLoad(async (opt)=>{ id.value = opt.id; detail.value = await api.rewardDetail(id.value) })
const redeemCode = async ()=>{
  const res = await api.redeemReward(id.value, {})
  uni.showModal({ title:'激活码', content: res||'已兑换', showCancel:false })
}
const fillAddress = async ()=>{
  uni.navigateTo({ url:'/pages/address?id='+id.value })
}
</script>
<style>
.card{background:#fff;margin:8px;padding:12px;border-radius:8px}
.title{font-weight:bold;margin-bottom:8px}
.actions{padding:12px}
</style>

