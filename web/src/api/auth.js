import request from '@/utils/request'

export function login(data) {
  return request({
    url: '/users/login',
    method: 'post',
    data: {
      username: data.username,
      password: data.password
    }
  })
}

export function register(data) {
  return request({
    url: '/users/register',
    method: 'post',
    data: {
      username: data.username,
      password: data.password,
      name: data.username,  // 如果没有专门的name输入框，可以暂时用username
      role: data.role || 'USER'
    }
  })
}

export function logout() {
  return request({
    url: '/users/logout',
    method: 'post'
  })
}