export interface ProcessInfo {
  id: string
  name: string
  isExecutable?: boolean
  documentation?: string
  extensions?: Record<string, unknown>
}

export interface NodeJson {
  id: string
  type: VisualNodeType
  subtype: string
  label: string
  x: number
  y: number
  width?: number
  height?: number
  collapsed?: boolean
  properties: Record<string, unknown>
  extensions: Record<string, unknown>
  process?: ProcessJson
}

export type VisualNodeType = 'event' | 'task' | 'gateway' | 'subProcess' | 'pool'

export interface EdgeJson {
  id: string
  source: string
  target: string
  label?: string
  properties: Record<string, unknown>
  extensions: Record<string, unknown>
}

export interface ProcessJson {
  process: ProcessInfo
  nodes: NodeJson[]
  edges: EdgeJson[]
}

export const PALETTE_ITEMS: Array<{
  type: VisualNodeType
  subtype: string
  label: string
}> = [
  { type: 'event', subtype: 'start', label: '开始' },
  { type: 'event', subtype: 'end', label: '结束' },
  { type: 'task', subtype: 'user', label: '用户任务' },
  { type: 'task', subtype: 'service', label: '服务任务' },
  { type: 'task', subtype: 'script', label: '脚本任务' },
  { type: 'task', subtype: 'receive', label: '接收任务' },
  { type: 'task', subtype: 'callActivity', label: '调用活动' },
  { type: 'gateway', subtype: 'exclusive', label: '排他网关' },
  { type: 'gateway', subtype: 'parallel', label: '并行网关' },
  { type: 'gateway', subtype: 'inclusive', label: '包容网关' },
  { type: 'subProcess', subtype: 'embedded', label: '子流程' },
]
