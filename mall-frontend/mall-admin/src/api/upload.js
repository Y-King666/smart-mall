/**
 * 文件上传 API
 */
import request from '@/utils/request'

/**
 * 上传图片文件
 * @param {File} file - 图片文件对象
 * @returns {Promise<string>} 上传成功返回图片访问 URL 路径
 */
export function uploadImage(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/api/admin/upload/image',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}
