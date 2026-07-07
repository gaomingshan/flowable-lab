<template>
  <section class="page">
    <header class="page-header">
      <div>
        <h2>流程设计</h2>
        <p>支持第一迭代节点、连线、基础校验与 graph_json 持久化。</p>
      </div>
    </header>

    <section class="panel designer-toolbar">
      <div class="toolbar-grid">
        <label class="field-block">
          <span>流程定义</span>
          <select v-model="selectedDefinitionId" @change="handleDefinitionChange">
            <option value="">请选择流程定义</option>
            <option v-for="item in store.definitions" :key="item.id" :value="item.id">
              {{ item.definitionName }} / {{ item.definitionKey }}
            </option>
          </select>
        </label>
        <label class="field-block">
          <span>版本</span>
          <select v-model.number="selectedVersionNo" :disabled="!selectedDefinitionId" @change="loadGraph">
            <option v-for="item in versionOptions" :key="item.id" :value="item.versionNo">
              V{{ item.versionNo }} / {{ item.status }}
            </option>
          </select>
        </label>
        <label class="field-block field-grow">
          <span>变更说明</span>
          <input v-model="changeNote" placeholder="例如：补充审批分支" />
        </label>
      </div>
      <div class="toolbar-actions">
        <button @click="loadGraph" :disabled="!selectedDefinitionId || !selectedVersionNo">读取图模型</button>
        <button @click="saveCurrentGraph" :disabled="!selectedDefinitionId || !selectedVersionNo">保存图模型</button>
        <button class="secondary-button" @click="createDraftVersion" :disabled="!selectedDefinitionId">新建草稿版本</button>
        <button class="secondary-button" @click="publishCurrentVersion" :disabled="!selectedDefinitionId || !selectedVersionNo">发布当前版本</button>
      </div>
    </section>

    <section class="panel panel-meta">
      <div>
        <h3>同步结果</h3>
        <p>节点骨架 {{ syncedNodes }} 条，边骨架 {{ syncedEdges }} 条。</p>
      </div>
      <div class="status-stack">
        <div class="status-text">{{ statusText }}</div>
        <div v-if="validationMessages.length" class="validation-text">
          {{ validationMessages.join('；') }}
        </div>
      </div>
    </section>

    <section class="designer-layout">
      <aside class="panel designer-sidebar">
        <div>
          <h3>节点面板</h3>
          <p class="muted-text">点击添加节点，再拖动到合适位置。</p>
        </div>
        <div class="stencil-list">
          <button
            v-for="option in NODE_TYPE_OPTIONS"
            :key="option.value"
            type="button"
            class="stencil-item secondary-button"
            @click="addNode(option.value)"
          >
            {{ option.label }}
          </button>
        </div>
        <div class="sidebar-section">
          <h4>画布摘要</h4>
          <div class="summary-item">节点 {{ graph.nodes.length }}</div>
          <div class="summary-item">连线 {{ graph.edges.length }}</div>
        </div>
      </aside>

      <section class="panel designer-canvas-panel">
        <VueFlow
          v-model:nodes="flowNodes"
          v-model:edges="flowEdges"
          class="workflow-flow"
          :fit-view-on-init="true"
          :min-zoom="0.2"
          :max-zoom="1.5"
          :nodes-draggable="true"
          :elements-selectable="true"
          :snap-to-grid="true"
          :snap-grid="[20, 20]"
          @connect="handleConnect"
          @node-click="handleNodeClick"
          @edge-click="handleEdgeClick"
          @pane-click="clearSelection"
          @move-end="handleMoveEnd"
        >
          <template #node-workflow="nodeProps">
            <WorkflowCanvasNode v-bind="nodeProps" />
          </template>

          <Background pattern-color="#d7deea" :gap="20" />
          <MiniMap pannable zoomable />
          <Controls />
        </VueFlow>
      </section>

      <aside class="panel designer-sidebar">
        <template v-if="selectedNode">
          <div class="sidebar-section">
            <h3>节点属性</h3>
            <label class="field-block">
              <span>节点Key</span>
              <input v-model="selectedNode.nodeKey" @input="syncGraphFromEditor" />
            </label>
            <label class="field-block">
              <span>节点名称</span>
              <input v-model="selectedNode.nodeName" @input="syncGraphFromEditor" />
            </label>
            <label class="field-block">
              <span>节点类型</span>
              <select v-model="selectedNode.nodeType" @change="syncGraphFromEditor">
                <option v-for="option in NODE_TYPE_OPTIONS" :key="option.value" :value="option.value">
                  {{ option.label }}
                </option>
              </select>
            </label>
            <button type="button" class="danger-button" @click="removeSelectedNode">删除节点</button>
          </div>
        </template>

        <template v-else-if="selectedEdge">
          <div class="sidebar-section">
            <h3>连线属性</h3>
            <label class="field-block">
              <span>连线Key</span>
              <input v-model="selectedEdge.edgeKey" @input="syncGraphFromEditor" />
            </label>
            <label class="field-block">
              <span>连线类型</span>
              <select v-model="selectedEdge.edgeType" @change="handleEdgeTypeChange">
                <option v-for="option in EDGE_TYPE_OPTIONS" :key="option.value" :value="option.value">
                  {{ option.label }}
                </option>
              </select>
            </label>
            <label class="field-block">
              <span>条件表达式</span>
              <textarea v-model="selectedEdge.conditionExpression" rows="4" @input="syncGraphFromEditor" />
            </label>
            <label class="inline-check">
              <input v-model="selectedEdge.defaultBranch" type="checkbox" @change="syncGraphFromEditor" />
              <span>默认分支</span>
            </label>
            <button type="button" class="danger-button" @click="removeSelectedEdge">删除连线</button>
          </div>
        </template>

        <template v-else>
          <div class="sidebar-section">
            <h3>属性面板</h3>
            <p class="muted-text">选中节点或连线后，可在这里编辑属性。</p>
          </div>
        </template>

        <div class="sidebar-section raw-json-section">
          <h4>graph_json</h4>
          <textarea v-model="graphText" class="code-block small-code-block" @change="applyGraphText" />
        </div>
      </aside>
    </section>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import type { Connection, Edge, EdgeMouseEvent, Node, NodeMouseEvent } from '@vue-flow/core'
import { VueFlow } from '@vue-flow/core'
import { Background } from '@vue-flow/background'
import { Controls } from '@vue-flow/controls'
import { MiniMap } from '@vue-flow/minimap'
import '@vue-flow/core/dist/style.css'
import '@vue-flow/core/dist/theme-default.css'
import '@vue-flow/controls/dist/style.css'
import '@vue-flow/minimap/dist/style.css'
import { useRoute } from 'vue-router'
import WorkflowCanvasNode from '../components/workflow/WorkflowCanvasNode.vue'
import { getGraph, saveGraph } from '../api/workflow'
import { useWorkflowStore } from '../stores/workflow'
import {
  cloneDefaultGraph,
  DEFAULT_WORKFLOW_GRAPH,
  EDGE_TYPE_OPTIONS,
  NODE_TYPE_OPTIONS,
  type WorkflowEdgeType,
  type WorkflowGraph,
  type WorkflowGraphEdge,
  type WorkflowGraphNode,
  type WorkflowNodeType,
} from '../types/workflow-graph'

const route = useRoute()
const store = useWorkflowStore()

const selectedDefinitionId = ref('')
const selectedVersionNo = ref<number | null>(null)
const changeNote = ref('设计页创建草稿版本')
const graph = ref<WorkflowGraph>(cloneDefaultGraph())
const graphText = ref(JSON.stringify(DEFAULT_WORKFLOW_GRAPH, null, 2))
const syncedNodes = ref(0)
const syncedEdges = ref(0)
const statusText = ref('等待加载')
const validationMessages = ref<string[]>([])
const flowNodes = ref<Node[]>([])
const flowEdges = ref<Edge[]>([])
const selectedNodeId = ref<string | null>(null)
const selectedEdgeId = ref<string | null>(null)

const versionOptions = computed(() => {
  if (!selectedDefinitionId.value) {
    return []
  }
  return store.versions[selectedDefinitionId.value] ?? []
})

const selectedNode = computed<WorkflowGraphNode | undefined>(() =>
  graph.value.nodes.find((item) => item.id === selectedNodeId.value),
)

const selectedEdge = computed<WorkflowGraphEdge | undefined>(() =>
  graph.value.edges.find((item) => item.id === selectedEdgeId.value),
)

function typeLabel(nodeType: WorkflowNodeType) {
  return NODE_TYPE_OPTIONS.find((item) => item.value === nodeType)?.label ?? nodeType
}

function edgeLabel(edge: WorkflowGraphEdge) {
  if (edge.edgeType === 'conditionFlow') {
    return edge.conditionExpression || '条件分支'
  }
  if (edge.edgeType === 'defaultFlow' || edge.defaultBranch) {
    return '默认分支'
  }
  return '连线'
}

function toFlowNode(node: WorkflowGraphNode): Node {
  return {
    id: node.id,
    type: 'workflow',
    position: { x: node.x, y: node.y },
    data: {
      nodeKey: node.nodeKey,
      nodeName: node.nodeName,
      nodeType: node.nodeType,
      typeLabel: typeLabel(node.nodeType),
    },
  }
}

function toFlowEdge(edge: WorkflowGraphEdge): Edge {
  return {
    id: edge.id,
    source: graph.value.nodes.find((node) => node.nodeKey === edge.sourceNodeKey)?.id ?? edge.sourceNodeKey,
    target: graph.value.nodes.find((node) => node.nodeKey === edge.targetNodeKey)?.id ?? edge.targetNodeKey,
    label: edgeLabel(edge),
    animated: edge.edgeType !== 'sequenceFlow',
    style: {
      stroke: edge.defaultBranch ? '#ea580c' : edge.edgeType === 'conditionFlow' ? '#2563eb' : '#64748b',
      strokeWidth: edge.defaultBranch ? 2.4 : 2,
    },
    labelStyle: { fill: '#334155', fontWeight: 600 },
  }
}

function syncFlowFromGraph() {
  flowNodes.value = graph.value.nodes.map(toFlowNode)
  flowEdges.value = graph.value.edges.map(toFlowEdge)
  graphText.value = JSON.stringify(graph.value, null, 2)
  validationMessages.value = validateGraph(graph.value)
}

function syncGraphNodePositionsFromCanvas() {
  const positionMap = new Map<string, { x: number; y: number }>()
  for (const item of flowNodes.value) {
    positionMap.set(String(item.id), { x: item.position.x, y: item.position.y })
  }
  graph.value.nodes = graph.value.nodes.map((node) => {
    const position = positionMap.get(node.id)
    if (!position) {
      return node
    }
    return {
      ...node,
      x: position.x,
      y: position.y,
    }
  })
}

function syncGraphFromEditor() {
  syncFlowFromGraph()
}

function normalizeGraph(input: unknown): WorkflowGraph {
  const source = typeof input === 'object' && input ? (input as Record<string, unknown>) : {}
  const nodes = Array.isArray(source.nodes) ? source.nodes : []
  const edges = Array.isArray(source.edges) ? source.edges : []
  const viewportInput = typeof source.viewport === 'object' && source.viewport ? (source.viewport as Record<string, unknown>) : {}

  return {
    nodes: nodes.map((item, index) => normalizeNode(item, index)),
    edges: edges.map((item, index) => normalizeEdge(item, index)),
    viewport: {
      x: Number(viewportInput.x ?? 0),
      y: Number(viewportInput.y ?? 0),
      zoom: Number(viewportInput.zoom ?? 1),
    },
  }
}

function normalizeNode(value: unknown, index: number): WorkflowGraphNode {
  const node = (typeof value === 'object' && value ? value : {}) as Record<string, unknown>
  const nodeType = resolveNodeType(node.nodeType)
  const fallbackX = 120 + (index % 3) * 240
  const fallbackY = 80 + Math.floor(index / 3) * 180
  return {
    id: String(node.id ?? `node-${index + 1}`),
    nodeKey: String(node.nodeKey ?? node.id ?? `node_${index + 1}`),
    nodeName: String(node.nodeName ?? typeLabel(nodeType)),
    nodeType,
    x: Number(node.x ?? fallbackX),
    y: Number(node.y ?? fallbackY),
  }
}

function normalizeEdge(value: unknown, index: number): WorkflowGraphEdge {
  const edge = (typeof value === 'object' && value ? value : {}) as Record<string, unknown>
  const edgeType = resolveEdgeType(edge.edgeType, edge.defaultBranch)
  return {
    id: String(edge.id ?? `edge-${index + 1}`),
    edgeKey: String(edge.edgeKey ?? edge.id ?? `edge_${index + 1}`),
    sourceNodeKey: String(edge.sourceNodeKey ?? ''),
    targetNodeKey: String(edge.targetNodeKey ?? ''),
    edgeType,
    conditionExpression: String(edge.conditionExpression ?? ''),
    defaultBranch: Boolean(edge.defaultBranch ?? edgeType === 'defaultFlow'),
  }
}

function resolveNodeType(value: unknown): WorkflowNodeType {
  const type = String(value ?? 'userTask') as WorkflowNodeType
  return NODE_TYPE_OPTIONS.some((item) => item.value === type) ? type : 'userTask'
}

function resolveEdgeType(value: unknown, defaultBranch?: unknown): WorkflowEdgeType {
  if (Boolean(defaultBranch)) {
    return 'defaultFlow'
  }
  const type = String(value ?? 'sequenceFlow') as WorkflowEdgeType
  return EDGE_TYPE_OPTIONS.some((item) => item.value === type) ? type : 'sequenceFlow'
}

function validateGraph(currentGraph: WorkflowGraph) {
  const messages: string[] = []
  const startNodes = currentGraph.nodes.filter((node) => node.nodeType === 'startEvent')
  const endNodes = currentGraph.nodes.filter((node) => node.nodeType === 'endEvent')
  const nodeKeys = new Set(currentGraph.nodes.map((node) => node.nodeKey))

  if (startNodes.length > 1) {
    messages.push('只能存在一个开始节点')
  }
  if (startNodes.length === 0) {
    messages.push('至少需要一个开始节点')
  }
  if (endNodes.length === 0) {
    messages.push('至少需要一个结束节点')
  }

  const defaultBranchCounter = new Map<string, number>()
  for (const edge of currentGraph.edges) {
    if (!nodeKeys.has(edge.sourceNodeKey) || !nodeKeys.has(edge.targetNodeKey)) {
      messages.push(`连线 ${edge.edgeKey} 存在无效端点`)
    }
    if ((edge.edgeType === 'conditionFlow' || edge.defaultBranch) && !edge.conditionExpression && edge.edgeType === 'conditionFlow') {
      messages.push(`条件连线 ${edge.edgeKey} 缺少条件表达式`)
    }
    if (edge.defaultBranch) {
      defaultBranchCounter.set(edge.sourceNodeKey, (defaultBranchCounter.get(edge.sourceNodeKey) ?? 0) + 1)
    }
  }

  for (const node of currentGraph.nodes) {
    const incoming = currentGraph.edges.filter((edge) => edge.targetNodeKey === node.nodeKey)
    const outgoing = currentGraph.edges.filter((edge) => edge.sourceNodeKey === node.nodeKey)

    if (node.nodeType === 'startEvent' && incoming.length > 0) {
      messages.push(`开始节点 ${node.nodeName} 不能有入边`)
    }
    if (node.nodeType === 'endEvent' && outgoing.length > 0) {
      messages.push(`结束节点 ${node.nodeName} 不能有出边`)
    }
    if ((defaultBranchCounter.get(node.nodeKey) ?? 0) > 1) {
      messages.push(`节点 ${node.nodeName} 只能有一个默认分支`)
    }
  }

  return Array.from(new Set(messages))
}

function addNode(nodeType: WorkflowNodeType) {
  const index = graph.value.nodes.length + 1
  const label = typeLabel(nodeType)
  const node: WorkflowGraphNode = {
    id: crypto.randomUUID(),
    nodeKey: `${nodeType}_${index}`,
    nodeName: `${label}${index}`,
    nodeType,
    x: 120 + ((index - 1) % 3) * 240,
    y: 80 + Math.floor((index - 1) / 3) * 180,
  }
  graph.value.nodes.push(node)
  selectedNodeId.value = node.id
  selectedEdgeId.value = null
  syncFlowFromGraph()
}

function handleConnect(connection: Connection) {
  if (!connection.source || !connection.target) {
    return
  }
  const sourceNode = graph.value.nodes.find((item) => item.id === connection.source)
  const targetNode = graph.value.nodes.find((item) => item.id === connection.target)
  if (!sourceNode || !targetNode) {
    return
  }

  const edge: WorkflowGraphEdge = {
    id: crypto.randomUUID(),
    edgeKey: `edge_${graph.value.edges.length + 1}`,
    sourceNodeKey: sourceNode.nodeKey,
    targetNodeKey: targetNode.nodeKey,
    edgeType: 'sequenceFlow',
    conditionExpression: '',
    defaultBranch: false,
  }
  graph.value.edges.push(edge)
  selectedEdgeId.value = edge.id
  selectedNodeId.value = null
  syncFlowFromGraph()
}

function handleNodeClick({ event, node }: NodeMouseEvent) {
  event.stopPropagation()
  selectedNodeId.value = String(node.id)
  selectedEdgeId.value = null
}

function handleEdgeClick({ event, edge }: EdgeMouseEvent) {
  event.stopPropagation()
  selectedEdgeId.value = String(edge.id)
  selectedNodeId.value = null
}

function clearSelection() {
  selectedNodeId.value = null
  selectedEdgeId.value = null
}

function removeSelectedNode() {
  if (!selectedNode.value) {
    return
  }
  const nodeKey = selectedNode.value.nodeKey
  graph.value.nodes = graph.value.nodes.filter((item) => item.id !== selectedNode.value?.id)
  graph.value.edges = graph.value.edges.filter(
    (edge) => edge.sourceNodeKey !== nodeKey && edge.targetNodeKey !== nodeKey,
  )
  clearSelection()
  syncFlowFromGraph()
}

function removeSelectedEdge() {
  if (!selectedEdge.value) {
    return
  }
  graph.value.edges = graph.value.edges.filter((item) => item.id !== selectedEdge.value?.id)
  clearSelection()
  syncFlowFromGraph()
}

function handleEdgeTypeChange() {
  if (!selectedEdge.value) {
    return
  }
  selectedEdge.value.defaultBranch = selectedEdge.value.edgeType === 'defaultFlow'
  if (selectedEdge.value.edgeType === 'sequenceFlow') {
    selectedEdge.value.conditionExpression = ''
  }
  syncGraphFromEditor()
}

function handleMoveEnd({ flowTransform }: { event: Event | null; flowTransform: { x: number; y: number; zoom: number } }) {
  graph.value.viewport = { x: flowTransform.x, y: flowTransform.y, zoom: flowTransform.zoom }
  syncGraphNodePositionsFromCanvas()
  graphText.value = JSON.stringify(graph.value, null, 2)
}

function applyGraphText() {
  try {
    graph.value = normalizeGraph(JSON.parse(graphText.value))
    syncFlowFromGraph()
    statusText.value = '已从 JSON 应用到画布'
  } catch {
    statusText.value = 'graph_json 不是有效的 JSON'
  }
}

async function loadGraph() {
  if (!selectedDefinitionId.value || !selectedVersionNo.value) {
    return
  }
  const result = await getGraph(selectedDefinitionId.value, selectedVersionNo.value)
  graph.value = normalizeGraph(result.graph)
  syncedNodes.value = result.syncedNodes
  syncedEdges.value = result.syncedEdges
  statusText.value = `已加载 V${result.versionNo}`
  clearSelection()
  syncFlowFromGraph()
}

async function saveCurrentGraph() {
  if (!selectedDefinitionId.value || !selectedVersionNo.value) {
    return
  }
  syncGraphNodePositionsFromCanvas()
  const messages = validateGraph(graph.value)
  validationMessages.value = messages
  if (messages.length > 0) {
    statusText.value = '存在校验问题，请先修正'
    return
  }
  const result = await saveGraph(selectedDefinitionId.value, selectedVersionNo.value, graph.value)
  graph.value = normalizeGraph(result.graph)
  syncedNodes.value = result.syncedNodes
  syncedEdges.value = result.syncedEdges
  statusText.value = `已保存 V${result.versionNo}`
  syncFlowFromGraph()
}

async function handleDefinitionChange() {
  if (!selectedDefinitionId.value) {
    selectedVersionNo.value = null
    graph.value = cloneDefaultGraph()
    syncFlowFromGraph()
    return
  }
  const versions = await store.fetchVersions(selectedDefinitionId.value)
  selectedVersionNo.value = versions[0]?.versionNo ?? null
  if (selectedVersionNo.value) {
    await loadGraph()
  }
}

async function createDraftVersion() {
  if (!selectedDefinitionId.value) {
    return
  }
  const version = await store.addVersion(selectedDefinitionId.value, changeNote.value || '设计页创建草稿版本')
  selectedVersionNo.value = version.versionNo
  await loadGraph()
  statusText.value = `已创建草稿 V${version.versionNo}`
}

async function publishCurrentVersion() {
  if (!selectedDefinitionId.value || !selectedVersionNo.value) {
    return
  }
  await saveCurrentGraph()
  if (validationMessages.value.length > 0) {
    return
  }
  await store.publishVersion(selectedDefinitionId.value, selectedVersionNo.value, '设计页发布版本')
  await handleDefinitionChange()
  statusText.value = `已发布 V${selectedVersionNo.value}`
}

onMounted(async () => {
  await store.fetchDefinitions()
  selectedDefinitionId.value = String(route.query.definitionId ?? '')
  if (!selectedDefinitionId.value && store.definitions.length > 0) {
    selectedDefinitionId.value = store.definitions[0].id
  }
  syncFlowFromGraph()
  if (selectedDefinitionId.value) {
    await handleDefinitionChange()
  }
})
</script>
