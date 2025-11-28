<template>
  <view class="page">
    <view v-if="list.length === 0" class="empty"
      >您的奖品空空如也，快去参加任务赢奖励吧~</view
    >
    <view v-else>
      <view class="card" v-for="item in list" :key="item.id">
        <image
          v-if="item.imageUrl"
          :src="item.imageUrl"
          mode="aspectFill"
          style="width: 100%; height: 140px"
        />
        <view class="title">{{ item.rewardName }}</view>
        <view>活动：{{ item.taskId }}</view>
        <view>时间：{{ format(item.issuedAt) }}</view>
        <button class="primary" @click="detail(item)">兑换</button>
      </view>
    </view>
  </view>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import dayjs from 'dayjs'
import { api } from '../utils/request'

const list = ref([])
const format = (ts) => (ts ? dayjs(ts).format('YYYY-MM-DD HH:mm') : '-')
const load = async () => {
  const data = await api.myRewards({ pageNum: 1, pageSize: 20 })
  list.value = data.records || []
}
const detail = (item) =>
  uni.navigateTo({ url: '/pages/reward-detail?id=' + item.id })
onMounted(load)
</script>
<style>
.card {
  background: #fff;
  margin: 8px;
  padding: 12px;
  border-radius: 8px;
}
.title {
  font-weight: bold;
  margin-top: 8px;
}
.empty {
  padding: 24px;
  text-align: center;
  color: #999;
}
</style>
