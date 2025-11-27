// /utils/date.js
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'

// 使用相对时间插件
dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

/**
 * 给日期添加指定月数
 * @param {Date} date - 原始日期
 * @param {number} months - 要添加的月数
 * @returns {Date} 新日期
 */
export function addMonthsToDate(date, months) {
  const result = new Date(date)
  result.setMonth(result.getMonth() + months)
  return result
}

/**
 * 计算剩余时间并返回相应的标签信息
 * @param {string|Date} expireTime - 过期时间
 * @returns {Object} 包含文本和样式信息的对象
 */
export function getRemainingTimeInfo(expireTime) {
  if (!expireTime) {
    return {
      text: '永久有效',
      type: 'success',
      tagType: 'success',
    }
  }

  const now = dayjs()
  const expire = dayjs(expireTime)
  const diffDays = expire.diff(now, 'day')
  const diffHours = expire.diff(now, 'hour')

  // 已经过期
  if (diffDays < 0) {
    return {
      text: '已过期',
      type: 'danger',
      tagType: 'danger',
    }
  }

  // 24小时内过期
  if (diffHours < 24) {
    return {
      text: `剩余${diffHours}小时`,
      type: 'danger',
      tagType: 'danger',
    }
  }

  // 7天内过期
  if (diffDays <= 7) {
    return {
      text: `剩余${diffDays}天`,
      type: 'warning',
      tagType: 'warning',
    }
  }

  // 30天内过期
  if (diffDays <= 30) {
    return {
      text: `剩余${diffDays}天`,
      type: 'default',
      tagType: 'default',
    }
  }

  // 超过30天
  return {
    text: `剩余${diffDays}天`,
    type: 'success',
    tagType: 'success',
  }
}
