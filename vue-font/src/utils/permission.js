import { useUserStore } from '@/stores/user'

/**
 * 校验是否有权限
 * @param {string|string[]} required 权限标识或标识数组
 * @returns {boolean}
 */
export function checkPermission(required) {
  const store = useUserStore()
  const permissions = store.roles || []

  if (Array.isArray(required) && required.length > 0) {
    return required.some((r) => permissions.includes(r))
  }
  if (typeof required === 'string') {
    return permissions.includes(required)
  }
  console.error('checkPermission 参数必须是 string 或 string[]')
  return false
}
