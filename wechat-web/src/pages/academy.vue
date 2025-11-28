<template>
  <view class="page">
    <view class="card">
      <picker :range="categories" @change="(e)=>sel=categories[e.detail.value]">
        <view class="picker">{{sel?sel.name:'选择分类'}}</view>
      </picker>
    </view>
    <view v-for="a in articles" :key="a.id" class="card">
      <view class="title">{{a.title}}</view>
      <view class="content">{{a.content}}</view>
    </view>
  </view>
</template>
<script setup>
import { ref, watch, onMounted } from 'vue'
import { api } from '../utils/request'

const categories = ref([])
const sel = ref(null)
const articles = ref([])
const loadCats = async ()=>{ categories.value = await api.academyCategories() }
const loadArticles = async ()=>{ articles.value = await api.academyArticles({ categoryId: sel.value?.id }) }
watch(sel, loadArticles)
onMounted(async ()=>{ await loadCats(); await loadArticles() })
</script>
<style>
.card{background:#fff;margin:8px;padding:12px;border-radius:8px}
.picker{padding:8px;background:#eee;border-radius:6px}
.title{font-weight:bold;margin-bottom:6px}
</style>

