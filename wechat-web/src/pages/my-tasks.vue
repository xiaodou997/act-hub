<template>
  <view class="page">
    <view class="tabs">
      <view :class="['tab', status === 1 ? 'active' : '']" @click="status = 1"
        >进行中</view
      >
      <view :class="['tab', status === 3 ? 'active' : '']" @click="status = 3"
        >已完成</view
      >
      <view :class="['tab', status === 4 ? 'active' : '']" @click="status = 4"
        >未通过</view
      >
    </view>
    <view v-if="list.length === 0" class="empty">暂无任务</view>
    <view v-else>
      <view class="card" v-for="item in list" :key="item.id">
        <view>{{ item.taskId }}</view>
        <view>状态：{{ item.status }}</view>
        <view v-if="item.submittedLink">链接：{{ item.submittedLink }}</view>
      </view>
    </view>
  </view>
</template>
<script setup>
import { ref, watch, onMounted } from 'vue'
import { api } from '../utils/request'

const status = ref(1)
const list = ref([])
const load = async () => {
  const data = await api.myTasks({
    pageNum: 1,
    pageSize: 50,
    status: status.value
  })
  list.value = data.records || []
}
watch(status, load)
onMounted(load)
</script>
<style>
.tabs {
  display: flex;
  gap: 12px;
  background: #fff;
  padding: 8px;
}
.tab {
  padding: 6px 10px;
  border-radius: 12px;
  background: #eee;
}
.tab.active {
  background: #1a85f2;
  color: #fff;
}
.card {
  background: #fff;
  margin: 8px;
  padding: 12px;
  border-radius: 8px;
}
.empty {
  padding: 24px;
  text-align: center;
  color: #999;
}
</style>
