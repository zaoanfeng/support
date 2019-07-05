import request from '@/utils/request'

export function search(keyword, offset, limit) {
  return request({
    url: '/v1/search',
    method: 'get',
    params: { keyword, offset, limit }
  })
}

export function detail(type, id) {
  return request({
    url: '/v1/search/detail',
    method: 'get',
    params: { type, id }
  })
}
