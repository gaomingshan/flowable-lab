import { defineStore } from 'pinia'
import {
  createDefinition,
  createVersion,
  getTaskDetail,
  claimTask,
  completeTask,
  listDefinitions,
  listDoneTasks,
  listInitiatedInstances,
  listLaunchableWorkflows,
  listTodoTasks,
  listUnclaimedTasks,
  listVersions,
  releaseVersion,
  startWorkflow,
  type WorkflowDefinition,
  type WorkflowDefinitionRequest,
  type WorkflowInstanceStartPayload,
  type WorkflowInstanceStartResponse,
  type LaunchableWorkflow,
  type WorkflowInstanceSummary,
  type WorkflowTaskDetail,
  type WorkflowTaskSummary,
  type WorkflowVersion,
} from '../api/workflow'

export const useWorkflowStore = defineStore('workflow', {
  state: () => ({
    definitions: [] as WorkflowDefinition[],
    launchable: [] as LaunchableWorkflow[],
    versions: {} as Record<string, WorkflowVersion[]>,
    unclaimedTasks: [] as WorkflowTaskSummary[],
    todoTasks: [] as WorkflowTaskSummary[],
    doneTasks: [] as WorkflowTaskSummary[],
    initiatedInstances: [] as WorkflowInstanceSummary[],
    taskDetail: null as WorkflowTaskDetail | null,
    latestStartedInstance: null as WorkflowInstanceStartResponse | null,
    loading: false,
  }),
  actions: {
    async fetchDefinitions() {
      this.loading = true
      try {
        this.definitions = await listDefinitions()
      } finally {
        this.loading = false
      }
    },
    async addDefinition(payload: WorkflowDefinitionRequest) {
      const definition = await createDefinition(payload)
      this.definitions.unshift(definition)
      return definition
    },
    async fetchLaunchable() {
      this.launchable = await listLaunchableWorkflows()
    },
    async fetchVersions(definitionId: string) {
      this.versions[definitionId] = await listVersions(definitionId)
      return this.versions[definitionId]
    },
    async addVersion(definitionId: string, changeNote: string) {
      const version = await createVersion(definitionId, changeNote)
      const current = this.versions[definitionId] ?? []
      this.versions[definitionId] = [version, ...current]
      await this.fetchDefinitions()
      return version
    },
    async publishVersion(definitionId: string, versionNo: number, releaseComment: string) {
      const release = await releaseVersion(definitionId, versionNo, releaseComment)
      await Promise.all([this.fetchDefinitions(), this.fetchVersions(definitionId), this.fetchLaunchable()])
      return release
    },
    async fetchTaskWorkbench(userId?: string) {
      const [unclaimed, todo, done, initiated] = await Promise.all([
        listUnclaimedTasks(userId),
        listTodoTasks(userId),
        listDoneTasks(userId),
        listInitiatedInstances(userId),
      ])
      this.unclaimedTasks = unclaimed
      this.todoTasks = todo
      this.doneTasks = done
      this.initiatedInstances = initiated
    },
    async fetchTaskDetail(taskId: string) {
      this.taskDetail = await getTaskDetail(taskId)
      return this.taskDetail
    },
    async claimTask(taskId: string, userId?: string) {
      const result = await claimTask(taskId, userId)
      await this.fetchTaskWorkbench(userId)
      if (this.taskDetail?.task.taskId === taskId) {
        await this.fetchTaskDetail(taskId)
      }
      return result
    },
    async completeTask(taskId: string, userId?: string, variables?: Record<string, unknown>) {
      const result = await completeTask(taskId, userId, variables)
      await this.fetchTaskWorkbench(userId)
      if (this.taskDetail?.task.taskId === taskId) {
        this.taskDetail = null
      }
      return result
    },
    async startWorkflow(payload: WorkflowInstanceStartPayload, userId?: string) {
      const result = await startWorkflow(payload)
      this.latestStartedInstance = result
      await this.fetchTaskWorkbench(userId)
      return result
    },
  },
})
