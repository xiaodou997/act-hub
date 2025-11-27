// 验证规则
export const validateEmail = (rule, value, callback) => {
  // 基本邮箱格式正则
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

  // 常见邮箱服务提供商域名
  const commonDomains = [
    'qq.com',
    'gmail.com',
    'icloud.com',
    '163.com',
    '126.com',
    'foxmail.com',
    'sina.com',
    'sohu.com',
    'outlook.com',
    'hotmail.com',
    'yahoo.com',
    'live.com',
    'me.com',
    'mail.com',
  ]

  if (!value) {
    callback(new Error('请输入邮箱'))
  } else if (!emailRegex.test(value)) {
    callback(new Error('请输入有效的邮箱地址'))
  } else {
    // 提取域名部分
    const domain = value.split('@')[1].toLowerCase()
    console.log(domain)
    // 检查是否是常见域名（可选验证，如果需要严格限制）
    if (!commonDomains.some((d) => domain === d || domain.endsWith('.' + d))) {
      callback(new Error('请输入支持的邮箱类型'))
    } else {
      callback()
    }
  }
}
