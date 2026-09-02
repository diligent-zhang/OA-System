import { reactive } from 'vue'

const storedToken = localStorage.getItem('oa_token') || ''
const storedUser = JSON.parse(localStorage.getItem('oa_user') || 'null')

/* 轻量登录态（localStorage 持久化），两端共用 */
export const auth = reactive({
  token: storedToken,
  user: storedUser,

  get isLogin() {
    return !!this.token
  },
  get role() {
    return this.user?.role || ''
  },
  get isAdmin() {
    return this.role === 'ADMIN'
  },

  setToken(token) {
    this.token = token
    localStorage.setItem('oa_token', token)
  },
  setUser(user) {
    this.user = user
    localStorage.setItem('oa_user', JSON.stringify(user))
  },
  clear() {
    this.token = ''
    this.user = null
    localStorage.removeItem('oa_token')
    localStorage.removeItem('oa_user')
  }
})
