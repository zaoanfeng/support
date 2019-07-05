import request from '@/utils/request'

export function fetchList(query) {
  return request({
    url: '/v1/article',
    method: 'get',
    params: query
  })
}

export function fetchArticle(id) {
  return request({
    url: '/v1/article/' + id,
    method: 'get'
  })
}

export function fetchPv(pv) {
  return request({
    url: '/v1/article/pv',
    method: 'get',
    params: { pv }
  })
}

export function createArticle(data) {
  return request({
    url: '/v1/article',
    method: 'post',
    data
  })
}

export function updateArticle(data) {
  return request({
    url: '/v1/article',
    method: 'put',
    data
  })
}

export function deleteArticle(id) {
  return request({
    url: '/v1/article/' + id,
    method: 'delete'
  })
}
