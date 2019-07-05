import request from '@/utils/request'

export function loginByUsername(username, password) {
  const data = {
    username,
    password
  }
  return request({
    url: '/v1/login',
    method: 'post',
    data: data
  })
}

export function logout() {
  return request({
    url: 'users/logout',
    method: 'delete'
  })
}

export function getUserInfo(access_token) {
  return request({
    url: '/v1/users/info',
    method: 'get',
    params: { access_token }
  })
}

export function refreshToken(access_token) {
  return request({
    url: '/v1/users/refreshToken',
    method: 'get',
    params: { access_token }
  })
}

