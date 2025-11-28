<template>
  <view class="page" v-if="task">
    <view class="title">{{ task.taskName }}</view>
    <view class="content">{{ task.requirements }}</view>
    <button class="primary" @click="goSubmit">参加任务</button>
  </view>
</template>
<script setup>
import { ref, onLoad } from 'vue'
import { api } from '../utils/request'

const task = ref(null)
const id = ref('')
onLoad(async (opt) => {
  id.value = opt.id
  task.value = await api.taskDetail(id.value)
})
const goSubmit = () =>
  uni.navigateTo({ url: '/pages/submit-task?id=' + id.value })
</script>
<style>
.title {
  font-weight: bold;
  font-size: 18px;
  margin: 12px;
}
.content {
  background: #fff;
  margin: 12px;
  padding: 12px;
  border-radius: 8px;
}
</style>
