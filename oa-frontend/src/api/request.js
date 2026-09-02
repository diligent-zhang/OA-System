import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'
import { auth } from '@/stores/auth'

const service = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 15000
})

/* 请求拦截：自动携带 Token */
service.interceptors.request.use(
  (config) => {
    if (auth.token) {
      config.headers.Authorization = `Bearer ${auth.token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

/* 响应拦截：统一结果处理 / Token 续期 / 401 跳登录 */
service.interceptors.response.use(
  (response) => {
    const renew = response.headers['x-renew-token']
    if (renew) {
      auth.setToken(renew)
    }

    const res = response.data
    if (res.code === 200) {
      return res
    }
    if (res.code === 401) {
      auth.clear()
      router.push('/login')
      return Promise.reject(new Error(res.message))
    }
    ElMessage.error(res.message || '操作失败')
    return Promise.reject(new Error(res.message || '操作失败'))
  },
  (error) => {
    ElMessage.error(error.response?.status === 404 ? '接口不存在' : '网络异常，请检查后端服务是否启动')
    return Promise.reject(error)
  }
)

export const get = (url, params) => service.get(url, { params })
export const post = (url, data) => service.post(url, data)
export const put = (url, data, params) => service.put(url, data, { params })
export const del = (url, data) => service.delete(url, { data })

export default service
