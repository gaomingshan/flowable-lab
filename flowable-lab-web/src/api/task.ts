import request from './request'

export function getTodoTasks(params?: { assignee?: string; groupId?: string }) {
  return request.get('/task/todo', { params })
}

export function getDoneTasks(assignee: string) {
  return request.get('/task/done', { params: { assignee } })
}

export function getTask(id: string) {
  return request.get(`/task/${id}`)
}

export function completeTask(id: string, variables?: Record<string, any>) {
  return request.post(`/task/${id}/complete`, variables || {})
}

export function claimTask(id: string, userId: string) {
  return request.post(`/task/${id}/claim`, null, { params: { userId } })
}

export function unclaimTask(id: string) {
  return request.post(`/task/${id}/unclaim`)
}

export function delegateTask(id: string, userId: string) {
  return request.post(`/task/${id}/delegate`, null, { params: { userId } })
}

export function transferTask(id: string, userId: string) {
  return request.post(`/task/${id}/transfer`, null, { params: { userId } })
}

export function addComment(id: string, processInstanceId: string, message: string) {
  return request.post(`/task/${id}/comment`, null, {
    params: { processInstanceId, message }
  })
}
