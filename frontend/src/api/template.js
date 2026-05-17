import request from './request'

export function getTemplates(category) {
  return request.get('/templates', { params: category ? { category } : {} })
}

export function getTemplate(id) {
  return request.get(`/templates/${id}`)
}
