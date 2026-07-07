import { defineStore } from 'pinia'
import {
  createDefinition,
  createVersion,
  listDefinitions,
  listLaunchableWorkflows,
  listVersions,
  releaseVersion,
  type WorkflowDefinition,
  type WorkflowDefinitionRequest,
  type LaunchableWorkflow,
  type WorkflowVersion,
} from '../api/workflow'

export const useWorkflowStore = defineStore('workflow', {
  state: () => ({
    definitions: [] as WorkflowDefinition[],
    launchable: [] as LaunchableWorkflow[],
    versions: {} as Record<string, WorkflowVersion[]>,
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
  },
})
