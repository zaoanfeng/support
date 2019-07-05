import request from '@/utils/request'

export function fetchList(query) {
  return request({
    url: 'store',
    method: 'get',
    params: query
  })
}

export function fetchStore(id) {
  return request({
    url: 'store',
    method: 'get',
    params: { id }
  })
}

export function createStore(data) {
  return request({
    url: 'store',
    method: 'post',
    data
  })
}

export function updateStore(data) {
  return request({
    url: 'store',
    method: 'put',
    data
  })
}

export function deteleStore(id) {
  return request({
    url: 'store',
    method: 'delete',
    params: { id }
  })
}
