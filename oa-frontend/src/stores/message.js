import { reactive } from 'vue'
import { messageUnreadCount } from '@/api'

/*
 * 未读消息数共享状态：顶部导航红标与消息中心共用，
 * 消息中心标记已读/全部已读后即时刷新，红标立即消失。
 */
export const messageState = reactive({
  unread: 0,

  async refresh() {
    try {
      const res = await messageUnreadCount()
      this.unread = Number(res.data) || 0
    } catch (_) {
      /* 静默失败：保持原值 */
    }
  },

  setUnread(n) {
    this.unread = Math.max(0, Number(n) || 0)
  }
})
