import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { ProcessJson, NodeJson, EdgeJson, ProcessInfo, VisualNodeType } from '../types'

export interface FlowNode {
  id: string
  type: string
  position: { x: number; y: number }
  data?: Record<string, unknown>
}

export interface FlowEdge {
  id: string
  source: string
  target: string
  type?: string
  label?: string
  style?: Record<string, string | number>
  data?: Record<string, unknown>
}

export const useEditorStore = defineStore('editor', () => {
  const processInfo = ref<ProcessInfo>({ id: '', name: '' })
  const selectedNodeId = ref<string | undefined>(undefined)
  const selectedEdgeId = ref<string | undefined>(undefined)
  const saving = ref(false)
  const nodes = ref<FlowNode[]>([])
  const edges = ref<FlowEdge[]>([])

  const selectedElement = computed(() => {
    if (selectedNodeId.value) return { id: selectedNodeId.value, type: 'node' as const }
    if (selectedEdgeId.value) return { id: selectedEdgeId.value, type: 'edge' as const }
    return null
  })

  function selectNode(id: string | undefined) {
    selectedNodeId.value = id
    selectedEdgeId.value = undefined
  }

  function selectEdge(id: string | undefined) {
    selectedEdgeId.value = id
    selectedNodeId.value = undefined
  }

  function clearSelection() {
    selectedNodeId.value = undefined
    selectedEdgeId.value = undefined
  }

  function setProcessInfo(info: ProcessInfo) {
    processInfo.value = info
  }

  function toProcessJson(): ProcessJson {
    return {
      process: { ...processInfo.value },
      nodes: nodes.value.map(toNodeJson),
      edges: edges.value.map(toEdgeJson),
    }
  }

  function toNodeJson(node: FlowNode): NodeJson {
    return {
      id: node.id,
      type: (node.type || 'task') as VisualNodeType,
      subtype: (node.data?.subtype as string) || '',
      label: (node.data?.label as string) || '',
      x: node.position.x,
      y: node.position.y,
      properties: (node.data?.properties as Record<string, unknown>) || {},
      extensions: (node.data?.extensions as Record<string, unknown>) || {},
      process: node.data?.process as ProcessJson | undefined,
    }
  }

  function toEdgeJson(edge: FlowEdge): EdgeJson {
    return {
      id: edge.id,
      source: edge.source,
      target: edge.target,
      label: edge.label || (edge.data?.label as string) || undefined,
      properties: (edge.data?.properties as Record<string, unknown>) || {},
      extensions: (edge.data?.extensions as Record<string, unknown>) || {},
    }
  }

  function loadFromProcessJson(json: ProcessJson) {
    if (!json) {
      console.error('loadFromProcessJson: json is null')
      return
    }
    processInfo.value = { ...(json.process || { id: '', name: '' }) }
    clearSelection()
    const nodeList = json.nodes || []
    const edgeList = json.edges || []
    nodes.value = nodeList.map((nj: NodeJson) => ({
      id: nj.id,
      type: nj.type,
      position: { x: nj.x || 0, y: nj.y || 0 },
      data: {
        subtype: nj.subtype,
        label: nj.label,
        properties: nj.properties || {},
        extensions: nj.extensions || {},
        process: nj.process,
      },
    }))
    edges.value = edgeList.map((ej: EdgeJson) => ({
      id: ej.id,
      source: ej.source,
      target: ej.target,
      type: 'smoothstep',
      label: ej.label || '',
      style: { stroke: '#409eff', strokeWidth: 2 },
      data: {
        properties: ej.properties || {},
        extensions: ej.extensions || {},
      },
    }))
  }

  return {
    processInfo,
    selectedNodeId,
    selectedEdgeId,
    selectedElement,
    saving,
    nodes,
    edges,
    selectNode,
    selectEdge,
    clearSelection,
    setProcessInfo,
    toProcessJson,
    loadFromProcessJson,
  }
})
