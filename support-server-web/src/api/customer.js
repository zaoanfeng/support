import request from '@/utils/request'

export function fetchList(query) {
  return request({
    url: 'customer',
    method: 'get',
    params: query
  })
}

export function fetchCustomer(id) {
  return request({
    url: 'customer',
    method: 'get',
    params: { id }
  })
}

export function createCustomer(data) {
  return request({
    url: 'customer',
    method: 'post',
    data
  })
}

export function updateCustomer(data) {
  return request({
    url: 'customer',
    method: 'put',
    data
  })
}

export function deteleCustomer(id) {
  return request({
    url: 'customer',
    method: 'delete',
    params: { id }
  })
}
