import { createRouter, createWebHashHistory } from 'vue-router'
import request from '@/utils/request'
import { useUserStore } from '@/stores/user'

const routes = [
  {
    path: '/',
    redirect: '/index'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/login/index.vue')
  },
  {
    path: '/index',
    name: 'Index',
    component: () => import('../views/dashboard/index.vue'),
    meta: { requiresAuth: true }
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

// 修改路由守卫，添加token验证
router.beforeEach(async (to, from, next) => {
  const token = localStorage.getItem('token')
  const userStore = useUserStore()
  
  // 如果要访问需要认证的页面
  if (to.meta.requiresAuth) {
    if (!token) {
      next('/login')
      return
    }
    
    try {
      // 验证token
      await request.get('/auth/validate')
      next()
    } catch (error) {
      // token无效，清除用户信息并跳转到登录页
      userStore.logout()
      next('/login')
    }
  } else if (to.path === '/login' && token) {
    // 如果已登录且要访问登录页，重定向到首页
    next('/index')
  } else {
    next()
  }
})

export default router
