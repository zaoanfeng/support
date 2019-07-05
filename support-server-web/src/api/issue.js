import request from '@/utils/request'

export function fetchList(query) {
  return request({
    url: '/v1/issue',
    method: 'get',
    params: query
  })
}

export function fetchIssue(id) {
  return request({
    url: '/v1/issue/' + id,
    method: 'get'
  })
}

export function createIssue(data) {
  return request({
    url: '/v1/issue',
    method: 'post',
    data
  })
}

export function updateIssue(data) {
  return request({
    url: '/v1/issue',
    method: 'put',
    data
  })
}

export function deleteIssue(id) {
  return request({
    url: '/v1/issue/' + id,
    method: 'delete'
  })
}
