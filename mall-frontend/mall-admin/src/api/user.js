import request from '@/utils/request'

export function getUsers(params) {
  return request({ url: '/api/admin/users', method: 'get', params })
}

export function toggleUserStatus(id) {
  return request({ url: `/api/admin/users/${id}/status`, method: 'put' })
}
