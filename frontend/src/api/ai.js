import request from './request'

export function optimizeSection(data) {
  return request.post('/ai/optimize', data)
}

export function optimizeFull(data) {
  return request.post('/ai/optimize-full', data)
}

export function matchWithJob(data) {
  return request.post('/ai/match', data)
}

export function getMatchHistory() {
  return request.get('/ai/match-history')
}
