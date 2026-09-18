import request from '@/utils/request'

export function getDashboard() {
  return request({ url: '/api/admin/dashboard', method: 'get' })
}
