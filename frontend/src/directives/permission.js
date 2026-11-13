import { useUserStore } from '@/store/user'

/**
 * 按钮级权限指令：v-permission="'fin:record:add'"
 * 无权限时直接把元素从 DOM 移除。
 */
export default {
  mounted(el, binding) {
    const store = useUserStore()
    if (!binding.value) return
    if (!store.hasPerm(binding.value)) {
      el.parentNode && el.parentNode.removeChild(el)
    }
  }
}
