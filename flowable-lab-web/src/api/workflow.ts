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
