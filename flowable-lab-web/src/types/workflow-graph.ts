export type WorkflowNodeType =
  | 'startEvent'
  | 'endEvent'
  | 'userTask'
  | 'exclusiveGateway'
  | 'parallelGateway'
  | 'parallelJoin'

export type WorkflowEdgeType = 'sequenceFlow' | 'conditionFlow' | 'defaultFlow'

export type WorkflowGraphNode = {
  id: string
  nodeKey: string
  nodeName: string
  nodeType: WorkflowNodeType
  x: number
  y: number
}

export type WorkflowGraphEdge = {
  id: string
  edgeKey: string
  sourceNodeKey: string
  targetNodeKey: string
  edgeType: WorkflowEdgeType
  conditionExpression?: string
  defaultBranch?: boolean
}

export type WorkflowGraphViewport = {
  x: number
  y: number
  zoom: number
}

export type WorkflowGraph = {
  nodes: WorkflowGraphNode[]
  edges: WorkflowGraphEdge[]
  viewport: WorkflowGraphViewport
}

export const DEFAULT_WORKFLOW_GRAPH: WorkflowGraph = {
  nodes: [],
  edges: [],
  viewport: { x: 0, y: 0, zoom: 1 },
}

export const NODE_TYPE_OPTIONS: Array<{ value: WorkflowNodeType; label: string }> = [
  { value: 'startEvent', label: '开始' },
  { value: 'endEvent', label: '结束' },
  { value: 'userTask', label: '用户任务' },
  { value: 'exclusiveGateway', label: '排他分支' },
  { value: 'parallelGateway', label: '并行分支' },
  { value: 'parallelJoin', label: '并行汇聚' },
]

export const EDGE_TYPE_OPTIONS: Array<{ value: WorkflowEdgeType; label: string }> = [
  { value: 'sequenceFlow', label: '普通连线' },
  { value: 'conditionFlow', label: '条件连线' },
  { value: 'defaultFlow', label: '默认分支' },
]

export function cloneDefaultGraph(): WorkflowGraph {
  return {
    nodes: [],
    edges: [],
    viewport: { ...DEFAULT_WORKFLOW_GRAPH.viewport },
  }
}
