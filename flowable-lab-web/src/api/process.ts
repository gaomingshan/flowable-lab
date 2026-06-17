import request from './request'

export function getDefinitions() {
  return request.get('/bpmn/definition')
}

export function getDefinition(id: string) {
  return request.get(`/bpmn/definition/${id}`)
}

export function getDefinitionByKey(key: string) {
  return request.get(`/bpmn/definition/key/${key}`)
}

export function suspendDefinition(id: string) {
  return request.post(`/bpmn/definition/${id}/suspend`)
}

export function activateDefinition(id: string) {
  return request.post(`/bpmn/definition/${id}/activate`)
}

export function getDeployments() {
  return request.get('/bpmn/definition/deployments')
}

export function deleteDeployment(id: string) {
  return request.delete(`/bpmn/definition/deployments/${id}`)
}

export function startInstance(processKey: string, variables?: Record<string, any>) {
  return request.post(`/bpmn/instance/start/${processKey}`, variables || {})
}

export function getInstances(status = 'running', processKey?: string) {
  const params: Record<string, string> = { status }
  if (processKey) params.processKey = processKey
  return request.get('/bpmn/instance', { params })
}

export function getInstance(id: string, type = 'runtime') {
  return request.get(`/bpmn/instance/${id}`, { params: { type } })
}

export function suspendInstance(id: string) {
  return request.post(`/bpmn/instance/${id}/suspend`)
}

export function activateInstance(id: string) {
  return request.post(`/bpmn/instance/${id}/activate`)
}

export function deleteInstance(id: string, reason = '手动删除') {
  return request.delete(`/bpmn/instance/${id}`, { params: { reason } })
}

export function getInstanceVariables(id: string) {
  return request.get(`/bpmn/instance/${id}/variables`)
}

export function setInstanceVariables(id: string, variables: Record<string, any>) {
  return request.post(`/bpmn/instance/${id}/variables`, variables)
}

export function getDiagram(definitionId: string) {
  return `/api/diagram/${definitionId}`
}

export function getHighlightedDiagram(definitionId: string, instanceId: string) {
  return `/api/diagram/${definitionId}/${instanceId}`
}

export function getDefinitionXml(id: string) {
  return request.get(`/bpmn/definition/${id}/xml`)
}

export function deployBpmn(name: string, bpmnXml: string) {
  return request.post('/bpmn/definition/deploy', { name, bpmnXml })
}
