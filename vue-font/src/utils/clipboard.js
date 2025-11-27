// /src/utils/clipboard.js
import { ElMessage } from 'element-plus'

/**
 * 复制文本到剪贴板
 * @param {string} text - 要复制的文本
 * @returns {boolean} 是否复制成功
 */
export const copyToClipboard = (text) => {
  // 方法1: 使用现代 Clipboard API
  if (navigator.clipboard) {
    navigator.clipboard
      .writeText(text)
      .then(() => true)
      .catch(() => fallbackCopyToClipboard(text))
    return true
  }

  // 方法2: 使用 document.execCommand 作为降级方案
  return fallbackCopyToClipboard(text)
}

/**
 * 降级方案 - 使用 document.execCommand 复制文本
 * @param {string} text - 要复制的文本
 * @returns {boolean} 是否复制成功
 */
function fallbackCopyToClipboard(text) {
  try {
    const textarea = document.createElement('textarea')
    textarea.value = text
    textarea.style.position = 'fixed'
    document.body.appendChild(textarea)
    textarea.select()
    textarea.setSelectionRange(0, textarea.value.length)
    const successful = document.execCommand('copy')
    document.body.removeChild(textarea)
    return successful
  } catch (err) {
    console.error('复制失败:', err)
    return false
  }
}

/**
 * 复制文本到剪贴板并显示提示消息
 * @param {string} text - 要复制的文本
 * @param {string} successMsg - 成功提示消息
 * @param {string} errorMsg - 错误提示消息
 */
export const copyWithFeedback = (text, successMsg = '已复制', errorMsg = '复制失败') => {
  const success = copyToClipboard(text)
  if (success && successMsg) {
    ElMessage.success(successMsg)
  } else if (!success && errorMsg) {
    ElMessage.error(errorMsg)
  }
  return success
}
