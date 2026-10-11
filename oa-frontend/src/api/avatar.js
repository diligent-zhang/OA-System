import axios from 'axios'
import { ref } from 'vue'
import { BASE_URL } from './request'
import { auth } from '@/stores/auth'

/*
 * 头像以 blob 方式带 Token 拉取：<img src> 不会携带 Authorization 头，
 * 直接用 img 指向受 JWT 保护的 /file/avatar 接口会被拦截器拒绝。
 * 这里用独立 axios（不经过全局响应拦截器，避免未设置头像时弹错误提示）。
 */
const cache = new Map() // userId -> object URL（空串表示该用户未设置头像）

/* 头像版本号：上传成功后自增，驱动所有头像组件重新拉取 */
export const avatarVersion = ref(0)

export async function loadAvatar(userId) {
  if (userId == null || userId === '') return ''
  if (cache.has(userId)) return cache.get(userId)
  let url = ''
  try {
    const res = await axios.get(`${BASE_URL}/file/avatar/${userId}`, {
      headers: auth.token ? { Authorization: `Bearer ${auth.token}` } : {},
      responseType: 'blob'
    })
    url = URL.createObjectURL(res.data)
  } catch (_) {
    url = '' // 未设置头像或拉取失败：缓存空值，避免重复请求
  }
  cache.set(userId, url)
  return url
}

/* 头像更新后调用：清除该用户缓存并通知组件刷新 */
export function invalidateAvatar(userId) {
  if (userId != null && cache.has(userId)) {
    const old = cache.get(userId)
    if (old) URL.revokeObjectURL(old)
    cache.delete(userId)
  }
  avatarVersion.value++
}
