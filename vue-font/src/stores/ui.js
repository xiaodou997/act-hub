// src/stores/ui.js
import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUiStore = defineStore('ui', () => {
  // 定义一个状态，用于表示是否有抽屉或对话框等覆盖层正在显示
  const isOverlayVisible = ref(false)

  // 提供一个方法来设置这个状态
  const setOverlayVisible = (visible) => {
    isOverlayVisible.value = visible
  }

  return {
    isOverlayVisible,
    setOverlayVisible,
  }
})
