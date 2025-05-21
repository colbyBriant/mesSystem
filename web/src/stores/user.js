import { defineStore } from 'pinia'
import { login, register } from '@/api/auth'

export const useUserStore = defineStore('user', {
  state: () => ({
    userInfo: JSON.parse(localStorage.getItem('userInfo')) || null,
    token: localStorage.getItem('token') || '',
    role: localStorage.getItem('role') || ''
  }),
  
  getters: {
    isLoggedIn: (state) => !!state.token,
    userRole: (state) => state.role
  },

  actions: {
    async login(data) {
      try {
        const res = await login(data)
        this.userInfo = res.user
        this.token = res.token
        this.role = res.user.role
        // 持久化存储
        localStorage.setItem('userInfo', JSON.stringify(res.user))
        localStorage.setItem('token', res.token)
        localStorage.setItem('role', res.user.role)
        return res
      } catch (error) {
        throw error
      }
    },

    async register(data) {
      try {
        const res = await register(data)
        return res
      } catch (error) {
        throw error
      }
    },

    logout() {
      this.userInfo = null
      this.token = ''
      this.role = ''
      localStorage.removeItem('userInfo')
      localStorage.removeItem('token')
      localStorage.removeItem('role')
    }
  }
})