import request from './request'

// User management
export function getUsers() {
  return request.get('/admin/users')
}

export function getUser(id) {
  return request.get(`/admin/users/${id}`)
}

export function updateUser(id, data) {
  return request.put(`/admin/users/${id}`, data)
}

export function deleteUser(id) {
  return request.delete(`/admin/users/${id}`)
}

// Resume management
export function getUserResumes(userId) {
  return request.get(`/admin/users/${userId}/resumes`)
}

export function deleteUserResume(userId, resumeId) {
  return request.delete(`/admin/users/${userId}/resumes/${resumeId}`)
}

// Template CRUD
export function getAdminTemplates(category) {
  return request.get('/admin/templates', { params: category ? { category } : {} })
}

export function getAdminTemplate(id) {
  return request.get(`/admin/templates/${id}`)
}

export function createTemplate(data) {
  return request.post('/admin/templates', data)
}

export function updateTemplate(id, data) {
  return request.put(`/admin/templates/${id}`, data)
}

export function deleteTemplate(id) {
  return request.delete(`/admin/templates/${id}`)
}

// Statistics
export function getStats() {
  return request.get('/admin/stats')
}

// Optimization logs
export function getLogs(page = 0, size = 20) {
  return request.get('/admin/logs', { params: { page, size } })
}

// Job analyses
export function getAnalyses(page = 0, size = 20) {
  return request.get('/admin/analyses', { params: { page, size } })
}

// Interview sessions
export function getInterviews(page = 0, size = 20) {
  return request.get('/admin/interviews', { params: { page, size } })
}
