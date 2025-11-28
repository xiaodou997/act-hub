<template>
  <view class="page">
    <view class="card">
      <input placeholder="输入作品链接" v-model="url" />
      <textarea placeholder="可选：原始文案" v-model="text"></textarea>
      <button class="primary" @click="run">生成文案</button>
    </view>
    <view v-if="result" class="card">
      <view class="title">生成结果</view>
      <view>{{format(result)}}</view>
      <button @click="copy">复制</button>
    </view>
  </view>
</template>
<script setup>
import { ref } from 'vue'
import { api } from '../utils/request'

const url = ref('')
const text = ref('')
const result = ref('')
const run = async ()=>{ result.value = await api.creativeRewrite({ url: url.value, text: text.value }) }
const format = (r)=> typeof r==='string'? r : JSON.stringify(r,null,2)
const copy = ()=> uni.setClipboardData({ data: format(result.value) })
</script>
<style>
.card{background:#fff;margin:8px;padding:12px;border-radius:8px}
.title{font-weight:bold;margin-bottom:6px}
input,textarea{width:100%;border:1px solid #eee;border-radius:6px;padding:8px;margin-top:8px}
</style>

