import { checkPermission } from '@/utils/permission'

export default {
  mounted(el, binding) {
    const { value } = binding
    const hasPermission = checkPermission(value)

    if (!hasPermission) {
      el.parentNode?.removeChild(el)
    }
  },
}
