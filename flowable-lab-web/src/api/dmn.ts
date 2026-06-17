import request from './request'

export function getDecisions() {
  return request.get('/dmn/decision')
}

export function evaluateDecision(decisionKey: string, variables: Record<string, any>) {
  return request.post(`/dmn/evaluate/${decisionKey}`, variables)
}

export function getUsers() {
  return request.get('/identity/user')
}

export function createUser(data: Record<string, string>) {
  return request.post('/identity/user', data)
}

export function deleteUser(id: string) {
  return request.delete(`/identity/user/${id}`)
}

export function getGroups() {
  return request.get('/identity/group')
}

export function createGroup(data: Record<string, string>) {
  return request.post('/identity/group', data)
}

export function deleteGroup(id: string) {
  return request.delete(`/identity/group/${id}`)
}

export function addMembership(userId: string, groupId: string) {
  return request.post('/identity/membership', null, { params: { userId, groupId } })
}

export function removeMembership(userId: string, groupId: string) {
  return request.delete('/identity/membership', { params: { userId, groupId } })
}

export function getGroupUsers(groupId: string) {
  return request.get(`/identity/group/${groupId}/users`)
}
