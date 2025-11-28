<template>
  <view class="page">
    <view class="banner">
      <button @click="goCopyAssistant">创作助手</button>
    </view>
    <view class="search">
      <input
        placeholder="搜索任务"
        confirm-type="search"
        v-model="query"
        @confirm="search"
      />
      <button @click="cancel">取消</button>
    </view>
    <view class="tabs">
      <view
        v-for="t in tabs"
        :key="t"
        :class="['tab', activeTab === t ? 'active' : '']"
        @click="activeTab = t"
        >{{ t }}</view
      >
    </view>
    <view v-if="list.length === 0" class="empty">搜索无结果</view>
    <view v-else>
      <view class="card" v-for="item in list" :key="item.id">
        <view class="title">{{ item.taskName }}</view>
        <view class="desc">{{ item.requirements }}</view>
        <button
          class="primary"
          :disabled="claimedIds.has(item.id)"
          @click="claim(item)"
        >
          {{ claimedIds.has(item.id) ? '已领取' : '领取任务' }}
        </button>
        <button @click="detail(item)">查看详情</button>
      </view>
    </view>
  </view>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import { api } from '../utils/request'

const tabs = ['全部', '小红书', '抖音']
const activeTab = ref('全部')
const query = ref('')
const list = ref([])
const claimedIds = new Set()

const load = async () => {
  const data = await api.tasks({
    pageNum: 1,
    pageSize: 20,
    tag: activeTab.value === '全部' ? undefined : activeTab.value
  })
  list.value = data.records || []
}
const search = () => load()
const cancel = () => {
  query.value = ''
  load()
}
const detail = (item) =>
  uni.navigateTo({ url: '/pages/task-detail?id=' + item.id })
const claim = async (item) => {
  await api.claimTask(item.id)
  claimedIds.add(item.id)
  uni.showToast({ title: '已领取', icon: 'success' })
}
const goCopyAssistant = () => uni.navigateTo({ url: '/pages/copy-assistant' })
onMounted(load)
</script>
<style>
.banner {
  padding: 12px;
  background: #fff;
  margin-bottom: 8px;
}
.search {
  display: flex;
  gap: 8px;
  padding: 8px;
  background: #fff;
}
.tabs {
  display: flex;
  gap: 12px;
  padding: 8px;
  background: #fff;
  margin-top: 8px;
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
.title {
  font-weight: bold;
  margin-bottom: 6px;
}
.empty {
  padding: 24px;
  text-align: center;
  color: #999;
}
</style>
