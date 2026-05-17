import request from './request'

export function getResumes() {
  return request.get('/resumes')
}

export function getResume(id) {
  return request.get(`/resumes/${id}`)
}

export function createResume(data) {
  return request.post('/resumes', data)
}

export function updateResume(id, data) {
  return request.put(`/resumes/${id}`, data)
}

export function deleteResume(id) {
  return request.delete(`/resumes/${id}`)
}

export function createVersion(id) {
  return request.post(`/resumes/${id}/versions`)
}
