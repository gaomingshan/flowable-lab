import { http, type ApiResponse } from './http'
import type { WorkflowGraph } from '../types/workflow-graph'

export type WorkflowDefinitionRequest = {
  definitionKey: string
  definitionName: string
  categoryCode: string
  description?: string
}

export type WorkflowDefinition = {
  id: string
  definitionKey: string
  definitionName: string
  categoryCode: string
  description?: string
  status: string
  latestDraftVersion: number
  latestReleasedVersion: number
}

export type WorkflowVersion = {
  id: string
  definitionId: string
  versionNo: number
  changeNote?: string
  status: string
  releaseComment?: string
  deploymentId?: string
  processDefinitionId?: string
}

export type WorkflowGraphResponse = {
  definitionId: string
  versionNo: number
  graph: WorkflowGraph
  syncedNodes: number
  syncedEdges: number
}

export type LaunchableWorkflow = {
  definitionId: string
  definitionKey: string
  definitionName: string
  status: string
  latestReleasedVersion: number
}

export type WorkflowTaskSummary = {
  taskId: string
  taskName: string
  taskDefinitionKey: string
  processInstanceId: string
  processDefinitionId: string
  processDefinitionKey: string
  businessKey?: string | null
  assignee?: string | null
  candidateUsers: string[]
  candidateGroups: string[]
  taskStatus: string
  starterId?: string | null
  starterDeptId?: string | null
  formTitle?: string | null
  createdAt?: string | null
  completedAt?: string | null
}

export type WorkflowInstanceSummary = {
  processInstanceId: string
  processDefinitionId: string
  processDefinitionKey: string
  businessKey?: string | null
  starterId?: string | null
  starterDeptId?: string | null
  formTitle?: string | null
  status: string
  startTime?: string | null
  endTime?: string | null
}

export type WorkflowTaskDetail = {
  task: WorkflowTaskSummary
  instance: WorkflowInstanceSummary
  variables: Record<string, unknown>
  candidateUsers: string[]
  candidateGroups: string[]
}

export type WorkflowTaskActionResponse = {
  taskId: string
  action: string
  assignee?: string | null
  processInstanceId: string
  status: string
}

export type WorkflowInstanceStartPayload = {
  definitionId: string
  businessKey: string
  title: string
  variables?: Record<string, unknown>
}

export type WorkflowInstanceStartResponse = {
  instanceId: string
  engineProcessInstanceId: string
  definitionId: string
  versionNo: number
  businessKey: string
  title: string
  status: string
  processDefinitionId: string
  processDefinitionKey: string
}

export async function listDefinitions() {
  const { data } = await http.get<ApiResponse<WorkflowDefinition[]>>('/api/platform/workflow-definitions')
  return data.data
}

export async function createDefinition(payload: WorkflowDefinitionRequest) {
  const { data } = await http.post<ApiResponse<WorkflowDefinition>>('/api/platform/workflow-definitions', payload)
  return data.data
}

export async function getGraph(definitionId: string, versionNo: number) {
  const { data } = await http.get<ApiResponse<WorkflowGraphResponse>>(
    `/api/platform/workflow-definitions/${definitionId}/versions/${versionNo}/graph`,
  )
  return data.data
}

export async function saveGraph(definitionId: string, versionNo: number, graph: WorkflowGraph) {
  const { data } = await http.put<ApiResponse<WorkflowGraphResponse>>(
    `/api/platform/workflow-definitions/${definitionId}/versions/${versionNo}/graph`,
    { graph },
  )
  return data.data
}

export async function listVersions(definitionId: string) {
  const { data } = await http.get<ApiResponse<WorkflowVersion[]>>(
    `/api/platform/workflow-definitions/${definitionId}/versions`,
  )
  return data.data
}

export async function createVersion(definitionId: string, changeNote: string) {
  const { data } = await http.post<ApiResponse<WorkflowVersion>>(
    `/api/platform/workflow-definitions/${definitionId}/versions`,
    { changeNote },
  )
  return data.data
}

export async function releaseVersion(definitionId: string, versionNo: number, releaseComment: string) {
  const { data } = await http.post<ApiResponse<Record<string, unknown>>>(
    `/api/platform/workflow-definitions/${definitionId}/versions/${versionNo}/release`,
    { releaseComment },
  )
  return data.data
}

export async function listLaunchableWorkflows() {
  const { data } = await http.get<ApiResponse<LaunchableWorkflow[]>>('/api/platform/launchable-workflows')
  return data.data
}

export async function listUnclaimedTasks(userId?: string) {
  const { data } = await http.get<ApiResponse<WorkflowTaskSummary[]>>('/api/platform/query/unclaimed', { params: { userId } })
  return data.data
}

export async function listTodoTasks(userId?: string) {
  const { data } = await http.get<ApiResponse<WorkflowTaskSummary[]>>('/api/platform/query/todo', { params: { userId } })
  return data.data
}

export async function listDoneTasks(userId?: string) {
  const { data } = await http.get<ApiResponse<WorkflowTaskSummary[]>>('/api/platform/query/done', { params: { userId } })
  return data.data
}

export async function listInitiatedInstances(userId?: string) {
  const { data } = await http.get<ApiResponse<WorkflowInstanceSummary[]>>('/api/platform/query/initiated', { params: { userId } })
  return data.data
}

export async function getTaskDetail(taskId: string) {
  const { data } = await http.get<ApiResponse<WorkflowTaskDetail>>(`/api/platform/query/tasks/${taskId}`)
  return data.data
}

export async function claimTask(taskId: string, userId?: string) {
  const { data } = await http.post<ApiResponse<WorkflowTaskActionResponse>>('/api/platform/workflow-instances/claim', { taskId, userId })
  return data.data
}

export async function completeTask(taskId: string, userId?: string, variables?: Record<string, unknown>) {
  const { data } = await http.post<ApiResponse<WorkflowTaskActionResponse>>('/api/platform/workflow-instances/complete', { taskId, userId, variables })
  return data.data
}

export async function startWorkflow(payload: WorkflowInstanceStartPayload) {
  const { data } = await http.post<ApiResponse<WorkflowInstanceStartResponse>>('/api/platform/workflow-instances', payload)
  return data.data
}
