import request from './request'

export function startInterview(data) {
  return request.post('/interview/start', data)
}

export function submitAnswer(data) {
  return request.post('/interview/answer', data)
}

export function endInterview(id) {
  return request.post(`/interview/${id}/end`)
}

export function getSessionDetail(id) {
  return request.get(`/interview/${id}`)
}

export function getInterviewHistory() {
  return request.get('/interview/history')
}

export function deleteInterviewSession(id) {
  return request.delete(`/interview/${id}`)
}
