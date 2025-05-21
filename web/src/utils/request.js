import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

const service = axios.create({
  baseURL: 'http://localhost:8888/api',  // 直接指向后端服务地址
  timeout: 0  // 设置为0表示不设置超时限制
})

// 请求拦截器
service.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers['Authorization'] = `Bearer ${token}`
  }
  return config
}, error => {
  return Promise.reject(error)
})

// 响应拦截器
service.interceptors.response.use(response => {
  return response.data
}, error => {
  if (error.response) {
    if (error.response.status === 401) {
      ElMessage.error('登录已过期，请重新登录')
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      router.push('/login')
    } else if (error.response.status === 500) {
      const errorMsg = error.response.data?.message || '服务器内部错误'
      ElMessage.error(errorMsg)
    }
  }
  return Promise.reject(error)
})

export default service