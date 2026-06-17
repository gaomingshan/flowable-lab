<template>
  <div class="pm-page">
    <header class="pm-header">
      <div class="pm-header-info">
        <h2>流程编辑器</h2>
        <p v-if="processKey">编辑: {{ processKey }} (v{{ version }})</p>
        <p v-else>创建新流程定义</p>
      </div>
      <div class="pm-header-actions">
        <el-button type="primary" :loading="saving" @click="handleSave">
          <el-icon><Upload /></el-icon> 保存并部署
        </el-button>
        <el-button @click="handleNew" :disabled="saving">
          <el-icon><Plus /></el-icon> 新建
        </el-button>
        <el-upload :show-file-list="false" accept=".xml" :before-upload="handleUpload" :disabled="saving">
          <el-button><el-icon><FolderOpened /></el-icon> 打开 XML</el-button>
        </el-upload>
        <el-button @click="handleDownload" :disabled="saving">
          <el-icon><Download /></el-icon> 下载 XML
        </el-button>
      </div>
    </header>
    <div class="pm-body">
      <aside class="pm-palette">
        <div class="pm-palette-title">组件</div>
        <div
          v-for="item in paletteItems"
          :key="item.type"
          class="pm-palette-item"
          draggable="true"
          @dragstart="onDragStart($event, item.type)"
          @click="onPaletteClick(item.type)"
        >
          <div class="pm-palette-icon" v-html="item.icon"></div>
          <span class="pm-palette-label">{{ item.label }}</span>
        </div>
      </aside>
      <div ref="canvasContainer" class="pm-canvas" @drop="onDrop" @dragover.prevent></div>
      <aside class="pm-properties" v-if="selectedElement">
        <div class="pm-properties-header">
          <h4>{{ selectedTypeLabel }}</h4>
          <el-button text @click="deselectElement">
            <el-icon><Close /></el-icon>
          </el-button>
        </div>
        <div class="pm-properties-body">
          <template v-if="selectedType === 'bpmn:process'">
            <div class="pf-group">
              <div class="pf-group-title">流程属性</div>
              <el-form label-position="top" size="small">
                <el-form-item label="标识">
                  <el-input :model-value="selectedElement.id" disabled />
                </el-form-item>
                <el-form-item label="名称">
                  <el-input v-model="editForm.name" @change="updateProcessName" />
                </el-form-item>
              </el-form>
            </div>
          </template>
          <template v-else-if="isNode">
            <div class="pf-group">
              <div class="pf-group-title">基本属性</div>
              <el-form label-position="top" size="small">
                <el-form-item label="标识">
                  <el-input :model-value="selectedElement.id" disabled />
                </el-form-item>
                <el-form-item label="名称">
                  <el-input v-model="editForm.name" @change="updateNodeName" />
                </el-form-item>
              </el-form>
            </div>
            <template v-if="isUserTask">
              <div class="pf-group">
                <div class="pf-group-title">任务配置</div>
                <el-form label-position="top" size="small">
                  <el-form-item label="办理人">
                    <el-input v-model="editForm.assignee" placeholder="如 admin 或 ${applicant}" @change="updateNodeProps" />
                  </el-form-item>
                  <el-form-item label="候选组">
                    <el-input v-model="editForm.candidateGroups" placeholder="多个用逗号分隔" @change="updateNodeProps" />
                  </el-form-item>
                  <el-form-item label="候选用户">
                    <el-input v-model="editForm.candidateUsers" placeholder="多个用逗号分隔" @change="updateNodeProps" />
                  </el-form-item>
                  <el-form-item label="表单 Key">
                    <el-input v-model="editForm.formKey" @change="updateNodeProps" />
                  </el-form-item>
                  <el-form-item label="截止日期">
                    <el-input v-model="editForm.dueDate" placeholder="如 PT1H 或 ${dueDate}" @change="updateNodeProps" />
                  </el-form-item>
                  <el-form-item label="优先级">
                    <el-input-number v-model="editForm.priority" :min="0" :max="100" @change="updateNodeProps" controls-position="right" />
                  </el-form-item>
                </el-form>
              </div>
            </template>
            <template v-else-if="isServiceTask">
              <div class="pf-group">
                <div class="pf-group-title">任务配置</div>
                <el-form label-position="top" size="small">
                  <el-form-item label="委托表达式">
                    <el-input v-model="editForm.delegateExpression" placeholder="如 ${myDelegate}" @change="updateNodeProps" />
                  </el-form-item>
                  <el-form-item label="Java 类">
                    <el-input v-model="editForm['flowable:class']" placeholder="类的全限定名" @change="updateNodeProps" />
                  </el-form-item>
                  <el-form-item label="表达式">
                    <el-input v-model="editForm.expression" placeholder="如 ${myMethod()}" @change="updateNodeProps" />
                  </el-form-item>
                </el-form>
              </div>
            </template>
            <template v-else-if="isStartEvent">
              <div class="pf-group">
                <div class="pf-group-title">事件配置</div>
                <el-form label-position="top" size="small">
                  <el-form-item label="表单 Key">
                    <el-input v-model="editForm.formKey" @change="updateNodeProps" />
                  </el-form-item>
                </el-form>
              </div>
            </template>
            <template v-else-if="isGateway">
              <div class="pf-group">
                <div class="pf-group-title">网关配置</div>
                <el-form label-position="top" size="small">
                  <el-form-item label="默认流转">
                    <el-select v-model="editForm.defaultFlow" placeholder="选择默认流向" @change="updateNodeProps" clearable>
                      <el-option v-for="e in outgoingEdges" :key="e.id" :label="e.text?.value || e.id" :value="e.id" />
                    </el-select>
                  </el-form-item>
                </el-form>
              </div>
            </template>
          </template>
          <template v-else-if="isEdge">
            <div class="pf-group">
              <div class="pf-group-title">连线属性</div>
              <el-form label-position="top" size="small">
                <el-form-item label="标识">
                  <el-input :model-value="selectedElement.id" disabled />
                </el-form-item>
                <el-form-item label="名称（条件标签）">
                  <el-input v-model="editForm.name" @change="updateEdgeName" />
                </el-form-item>
                <el-form-item label="条件表达式">
                  <el-input v-model="editForm.conditionExpression" placeholder="如 ${amount > 1000}" @change="updateEdgeProps" />
                </el-form-item>
              </el-form>
            </div>
          </template>
        </div>
      </aside>
      <div v-else class="pm-properties pm-properties-empty">
        <div class="pm-empty-hint">
          <el-icon :size="32" color="#c0c4cc"><Connection /></el-icon>
          <p>点击节点或连线查看属性</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onBeforeUnmount, nextTick, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Upload, Plus, FolderOpened, Download, Close, Connection } from '@element-plus/icons-vue'
import LogicFlow from '@logicflow/core'
import { BpmnElement, BpmnXmlAdapter, DndPanel as DndPanelPlugin } from '@logicflow/extension'
import '@logicflow/core/dist/index.css'
import '@logicflow/extension/lib/index.css'
import { getDefinitionXml, deployBpmn } from '@/api/process'

const route = useRoute()
const router = useRouter()

const canvasContainer = ref<HTMLElement>()
const processKey = ref('')
const version = ref(0)
const processName = ref('')
const saving = ref(false)

let lf: InstanceType<typeof LogicFlow> | null = null
let bpmnAdapter: BpmnXmlAdapter | null = null
let currentProcessId = 'Process_1'

const selectedElement = ref<any>(null)
const selectedType = ref('')
const editForm = reactive<Record<string, any>>({})

const paletteItems = [
  {
    type: 'bpmn:startEvent',
    label: '开始事件',
    icon: `<svg width="20" height="20" viewBox="0 0 20 20"><circle cx="10" cy="10" r="8" fill="none" stroke="#67C23A" stroke-width="1.5"/><circle cx="10" cy="10" r="4" fill="#67C23A" opacity="0.3"/></svg>`,
  },
  {
    type: 'bpmn:endEvent',
    label: '结束事件',
    icon: `<svg width="20" height="20" viewBox="0 0 20 20"><circle cx="10" cy="10" r="8" fill="#F56C6C" stroke="#F56C6C" stroke-width="1.5" opacity="0.8"/><circle cx="10" cy="10" r="4" fill="#fff"/></svg>`,
  },
  {
    type: 'bpmn:userTask',
    label: '用户任务',
    icon: `<svg width="20" height="20" viewBox="0 0 20 20"><rect x="2" y="2" width="16" height="16" rx="3" fill="none" stroke="#409EFF" stroke-width="1.5"/><path d="M7 8a3 3 0 1 0 6 0 3 3 0 0 0-6 0m-2 7a6 6 0 0 1 10 0" fill="none" stroke="#409EFF" stroke-width="1.2"/></svg>`,
  },
  {
    type: 'bpmn:serviceTask',
    label: '服务任务',
    icon: `<svg width="20" height="20" viewBox="0 0 20 20"><rect x="2" y="2" width="16" height="16" rx="3" fill="none" stroke="#E6A23C" stroke-width="1.5"/><path d="M10 5v10M5 10h10" stroke="#E6A23C" stroke-width="1.2"/></svg>`,
  },
  {
    type: 'bpmn:exclusiveGateway',
    label: '排他网关',
    icon: `<svg width="20" height="20" viewBox="0 0 20 20"><polygon points="10,1 19,10 10,19 1,10" fill="none" stroke="#909399" stroke-width="1.5"/><path d="M7 7l6 6m0-6l-6 6" stroke="#909399" stroke-width="1.2"/></svg>`,
  },
  {
    type: 'bpmn:parallelGateway',
    label: '并行网关',
    icon: `<svg width="20" height="20" viewBox="0 0 20 20"><polygon points="10,1 19,10 10,19 1,10" fill="none" stroke="#909399" stroke-width="1.5"/><path d="M5 10h10M10 5v10" stroke="#909399" stroke-width="1.3"/></svg>`,
  },
]

const nodeTypeLabels: Record<string, string> = {
  'bpmn:startEvent': '开始事件',
  'bpmn:endEvent': '结束事件',
  'bpmn:userTask': '用户任务',
  'bpmn:serviceTask': '服务任务',
  'bpmn:exclusiveGateway': '排他网关',
  'bpmn:parallelGateway': '并行网关',
  'bpmn:sequenceFlow': '顺序流',
  'bpmn:process': '流程属性',
}

const selectedTypeLabel = computed(() => nodeTypeLabels[selectedType.value] || selectedType.value)
const isNode = computed(() => selectedElement.value?.id && !isEdge.value && selectedType.value !== 'bpmn:process')
const isEdge = computed(() => selectedType.value === 'bpmn:sequenceFlow')
const isUserTask = computed(() => selectedType.value === 'bpmn:userTask')
const isServiceTask = computed(() => selectedType.value === 'bpmn:serviceTask')
const isStartEvent = computed(() => selectedType.value === 'bpmn:startEvent')
const isGateway = computed(() => ['bpmn:exclusiveGateway', 'bpmn:parallelGateway', 'bpmn:inclusiveGateway'].includes(selectedType.value))
const outgoingEdges = computed(() => {
  if (!lf || !selectedElement.value) return []
  const graphData = lf.getGraphData() as any
  const edges = graphData.edges || []
  return edges.filter((e: any) => e.sourceNodeId === selectedElement.value.id)
})

function initModeler() {
  if (!canvasContainer.value) return
  destroyModeler()

  LogicFlow.use(BpmnElement)

  lf = new LogicFlow({
    container: canvasContainer.value,
    grid: {
      type: 'mesh',
      size: 20,
      visible: true,
    },
    width: canvasContainer.value.clientWidth,
    height: canvasContainer.value.clientHeight,
    stopZoomGraph: false,
    stopScrollGraph: false,
    plugins: [DndPanelPlugin],
    guard: (_event: any, model: any) => {
      if (model.isSelected || model.text?.value) return false
      return true
    },
  })


  lf.on('node:click', ({ data }: any) => selectElement(data, 'node'))
  lf.on('edge:click', ({ data }: any) => selectElement(data, 'edge'))
  lf.on('blank:click', () => {
    selectProcessElement()
  })
  lf.on('node:delete', () => deselectElement())
  lf.on('edge:delete', () => deselectElement())

  bpmnAdapter = new BpmnXmlAdapter({ lf })
  bpmnAdapter.definitionAttributes['-xmlns:flowable'] = 'http://flowable.org/bpmn'
  bpmnAdapter.definitionAttributes['-targetNamespace'] = 'http://flowable.org/bpmn'

  lf.render({})

  lf.setTheme({
    rect: { stroke: '#409EFF', strokeWidth: 1.5, radius: 4 },
    circle: { stroke: '#409EFF', strokeWidth: 1.5 },
    polygon: { stroke: '#409EFF', strokeWidth: 1.5 },
    polyline: { stroke: '#409EFF', strokeWidth: 2 },
    text: { color: '#303133', fontSize: 12 },
    edgeText: { color: '#606266', fontSize: 11, textWidth: 80 },
    anchor: { stroke: '#409EFF', r: 4, fill: '#fff', hover: { stroke: '#66B1FF', r: 6 } },
  })
}

function destroyModeler() {
  if (lf) {
    lf.destroy()
    lf = null
    bpmnAdapter = null
  }
}

function selectElement(data: any, type: 'node' | 'edge') {
  if (!lf) return
  const model = lf.getNodeModelById?.(data.id) || lf.getEdgeModelById?.(data.id)
  if (model) {
    selectedElement.value = { id: data.id, type: data.type, model }
    selectedType.value = data.type
  }
  editForm.name = data.text?.value || ''
  editForm.assignee = data.properties?.assignee || ''
  editForm.candidateGroups = data.properties?.candidateGroups || ''
  editForm.candidateUsers = data.properties?.candidateUsers || ''
  editForm.formKey = data.properties?.formKey || ''
  editForm.dueDate = data.properties?.dueDate || ''
  editForm.priority = data.properties?.priority ?? 50
  editForm.delegateExpression = data.properties?.delegateExpression || ''
  editForm['flowable:class'] = data.properties?.['flowable:class'] || ''
  editForm.expression = data.properties?.expression || ''
  editForm.conditionExpression = data.properties?.conditionExpression || ''
  editForm.defaultFlow = data.properties?.defaultFlow || ''
  editForm.documentation = data.properties?.documentation || ''
}

function selectProcessElement() {
  selectedElement.value = { id: currentProcessId, type: 'bpmn:process', model: null }
  selectedType.value = 'bpmn:process'
  if (!lf) return
  const graphData = lf.getGraphData() as any
  const processProps = graphData.properties || {}
  editForm.name = processProps.name || graphData.processName || ''
}

function deselectElement() {
  selectedElement.value = null
  selectedType.value = ''
  Object.keys(editForm).forEach(k => delete editForm[k])
}

function updateNodeName() {
  updateNodeModel({ text: editForm.name })
}

function updateEdgeName() {
  updateNodeModel({ text: editForm.name })
}

function updateProcessName() {
  if (!lf) return
  const graphData = lf.getGraphData()
  ;(graphData as any).processName = editForm.name
}

function updateNodeModel(data: any) {
  if (!lf || !selectedElement.value) return
  try {
    const id = selectedElement.value.id
    const model = lf.getNodeModelById(id) || lf.getEdgeModelById(id)
    if (model) {
      if (data.text !== undefined) {
        model.setProperty('text', data.text)
      }
    }
    lf.setProperties(id, data)
  } catch {
    // ignored
  }
}

function updateNodeProps() {
  if (!lf || !selectedElement.value) return
  const id = selectedElement.value.id
  const props: Record<string, any> = {}
  if (isUserTask.value) {
    props.assignee = editForm.assignee
    props.candidateGroups = editForm.candidateGroups
    props.candidateUsers = editForm.candidateUsers
    props.formKey = editForm.formKey
    props.dueDate = editForm.dueDate
    props.priority = editForm.priority
  } else if (isServiceTask.value) {
    props.delegateExpression = editForm.delegateExpression
    props['flowable:class'] = editForm['flowable:class']
    props.expression = editForm.expression
  } else if (isStartEvent.value) {
    props.formKey = editForm.formKey
  } else if (isGateway.value) {
    props.defaultFlow = editForm.defaultFlow
  }
  lf.setProperties(id, props)
}

function updateEdgeProps() {
  if (!lf || !selectedElement.value) return
  const id = selectedElement.value.id
  lf.setProperties(id, {
    conditionExpression: editForm.conditionExpression,
    conditionType: editForm.conditionExpression ? 'expression' : '',
  })
}

async function loadAndRender(xml: string, meta?: { key: string; version: number; name: string }) {
  if (!lf || !bpmnAdapter) return
  try {
    const graphData = bpmnAdapter.adapterXmlIn(xml)
    if (!graphData || (!graphData.nodes?.length && !graphData.edges?.length)) {
      ElMessage.warning('流程图为空或格式不支持')
      return
    }
    processKey.value = meta?.key || route.query.key as string || ''
    version.value = meta?.version || Number(route.query.version || 0)
    processName.value = meta?.name || route.query.name as string || ''
    lf.render(graphData)
    const process = xml.match(/process\s+id="([^"]+)"/)
    if (process) currentProcessId = process[1]
    nextTick(() => {
      lf?.zoomToFit()
    })
  } catch (err: any) {
    ElMessage.error('加载失败: ' + (err.message || err))
  }
}

async function loadDefinition(id: string) {
  if (!lf || !bpmnAdapter) return
  saving.value = true
  try {
    const res = await getDefinitionXml(id)
    const xml = res.data
    if (xml) await loadAndRender(xml)
  } catch (err: any) {
    ElMessage.error('加载定义失败: ' + (err.message || err))
  } finally {
    saving.value = false
  }
}

async function handleSave() {
  if (!lf || !bpmnAdapter) return
  saving.value = true
  try {
    const graphData = lf.getGraphData() as any
    graphData.processName = editForm.name || processName.value
    const xml = bpmnAdapter.adapterXmlOut(graphData)
    if (!xml) return
    const name = await ElMessageBox.prompt('请输入部署名称', '部署', {
      inputValue: processName.value || '未命名流程',
      inputPlaceholder: '部署名称',
    })
    if (name.value) {
      await deployBpmn(name.value, xml)
      ElMessage.success('部署成功')
      router.push('/process/definition')
    }
  } catch (err: any) {
    if (err !== 'cancel' && err !== 'close') {
      ElMessage.error('保存失败: ' + (err.message || err))
    }
  } finally {
    saving.value = false
  }
}

function handleNew() {
  processKey.value = ''
  version.value = 0
  processName.value = ''
  currentProcessId = 'Process_1'
  deselectElement()
  if (lf) {
    lf.clearData()
    const graphData = {
      nodes: [],
      edges: [],
      processName: '',
    }
    lf.render(graphData)
  }
}

function handleUpload(file: File): boolean {
  const reader = new FileReader()
  reader.onload = async (e) => {
    const xml = e.target?.result as string
    if (xml) {
      await loadAndRender(xml, { key: file.name.replace(/\.bpmn20\.xml$/, ''), version: 1, name: file.name })
    }
  }
  reader.readAsText(file)
  return false
}

async function handleDownload() {
  if (!lf || !bpmnAdapter) return
  const graphData = lf.getGraphData() as any
  graphData.processName = editForm.name || processName.value
  const xml = bpmnAdapter.adapterXmlOut(graphData)
  if (!xml) return
  const blob = new Blob([xml], { type: 'application/xml' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `${processKey.value || 'process'}.bpmn20.xml`
  a.click()
  URL.revokeObjectURL(url)
}

function onDragStart(event: DragEvent, type: string) {
  if (event.dataTransfer) {
    event.dataTransfer.setData('lf-node-type', type)
    event.dataTransfer.effectAllowed = 'copy'
    if (canvasContainer.value) {
      const rect = canvasContainer.value.getBoundingClientRect()
      event.dataTransfer.setDragImage(
        new Image(),
        rect.width / 2,
        rect.height / 2,
      )
    }
  }
}

function onDrop(event: DragEvent) {
  if (!lf) return
  const type = event.dataTransfer?.getData('lf-node-type')
  if (!type) return
  const rect = canvasContainer.value?.getBoundingClientRect()
  if (!rect) return
  const x = event.clientX - rect.left
  const y = event.clientY - rect.top
  const point = lf.getPointByClient(event.clientX, event.clientY)
  const label = paletteItems.find(p => p.type === type)?.label || type
  const { canvasOverlayPosition } = point
  lf.addNode({
    type,
    x: canvasOverlayPosition.x,
    y: canvasOverlayPosition.y,
    text: label,
  })
}

function onPaletteClick(type: string) {
  if (!lf) return
  const rect = canvasContainer.value?.getBoundingClientRect()
  if (!rect) return
  const label = paletteItems.find(p => p.type === type)?.label || type
  lf.addNode({
    type,
    x: rect.width / 2 + (Math.random() - 0.5) * 100,
    y: rect.height / 2 + (Math.random() - 0.5) * 100,
    text: label,
  })
}

onMounted(async () => {
  initModeler()
  if (!canvasContainer.value) return
  await new Promise(resolve => setTimeout(resolve, 200))
  const resizeHandler = () => {
    if (canvasContainer.value && lf) {
      lf.resize(canvasContainer.value.clientWidth, canvasContainer.value.clientHeight)
    }
  }
  window.addEventListener('resize', resizeHandler)
  const defId = route.query.definitionId as string
  if (defId) {
    await loadDefinition(defId)
  }
})

onBeforeUnmount(() => {
  destroyModeler()
})
</script>

<style>
/* LogicFlow global style overrides */
.logic-flow .lf-canvas-overlay {
  background: #f8f9fb;
}

.logic-flow .lf-grid line {
  stroke: #ebeef5;
  stroke-width: 0.5;
}

.lf-dnd-panel {
  display: none !important;
}

/* Connection styling */
.logic-flow .lf-edge path {
  stroke: #409EFF !important;
  stroke-width: 2 !important;
  fill: none !important;
}

.logic-flow .lf-edge:hover path {
  stroke: #66B1FF !important;
  stroke-width: 2.5 !important;
}

.logic-flow .lf-edge.active path {
  stroke: #409EFF !important;
  stroke-width: 2.5 !important;
}

/* Node styling */
.logic-flow .lf-node rect,
.logic-flow .lf-node circle,
.logic-flow .lf-node polygon {
  transition: all 0.15s ease;
}

.logic-flow .lf-node:hover rect,
.logic-flow .lf-node:hover circle,
.logic-flow .lf-node:hover polygon {
  filter: drop-shadow(0 2px 6px rgba(64, 158, 255, 0.25));
}

.logic-flow .lf-node.select rect,
.logic-flow .lf-node.select circle,
.logic-flow .lf-node.select polygon {
  filter: drop-shadow(0 2px 8px rgba(64, 158, 255, 0.35));
}

/* Anchor styling */
.lf-anchor {
  transition: all 0.15s ease !important;
}

.lf-anchor:hover {
  r: 6 !important;
  stroke: #66B1FF !important;
}

/* Text styling */
.logic-flow .lf-text {
  font-family: -apple-system, BlinkMacSystemFont, "PingFang SC", "Microsoft YaHei", sans-serif !important;
  fill: #303133 !important;
}

.logic-flow .lf-edge-text {
  font-family: -apple-system, BlinkMacSystemFont, "PingFang SC", "Microsoft YaHei", sans-serif !important;
  fill: #606266 !important;
}

/* Background mode */
.logic-flow .lf-canvas-overlay .lf-background {
  background: #f8f9fb;
}

/* Selection box */
.lf-multiple-select {
  border: 1.5px dashed #409EFF !important;
  background: rgba(64, 158, 255, 0.06) !important;
}
</style>

<style scoped>
.pm-page {
  height: calc(100vh - 100px);
  display: flex;
  flex-direction: column;
}

.pm-header {
  flex-shrink: 0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding: 0 4px;
}

.pm-header-info h2 {
  margin: 0;
  font-size: 18px;
  color: #1d1d1f;
}

.pm-header-info p {
  margin: 4px 0 0;
  color: #909399;
  font-size: 13px;
}

.pm-header-actions {
  display: flex;
  gap: 8px;
  align-items: center;
}

.pm-body {
  flex: 1;
  display: flex;
  min-height: 0;
  gap: 0;
  background: #fff;
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  overflow: hidden;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
}

.pm-palette {
  width: 160px;
  flex-shrink: 0;
  background: #fafafa;
  border-right: 1px solid #dcdfe6;
  padding: 12px;
  overflow-y: auto;
}

.pm-palette-title {
  font-size: 12px;
  font-weight: 600;
  color: #909399;
  text-transform: uppercase;
  letter-spacing: 1px;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid #ebeef5;
}

.pm-palette-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 10px;
  border-radius: 6px;
  cursor: grab;
  transition: all 0.15s;
  user-select: none;
  margin-bottom: 2px;
}

.pm-palette-item:hover {
  background: #ecf5ff;
}

.pm-palette-item:active {
  cursor: grabbing;
  background: #d9ecff;
}

.pm-palette-icon {
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.pm-palette-label {
  font-size: 13px;
  color: #303133;
  line-height: 1;
}

.pm-canvas {
  flex: 1;
  height: 100%;
  min-width: 0;
  position: relative;
  background: #f8f9fb;
}

.pm-properties {
  width: 300px;
  flex-shrink: 0;
  border-left: 1px solid #dcdfe6;
  background: #fff;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.pm-properties-empty {
  justify-content: center;
  align-items: center;
}

.pm-empty-hint {
  text-align: center;
  color: #c0c4cc;
}

.pm-empty-hint p {
  margin: 8px 0 0;
  font-size: 13px;
}

.pm-properties-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid #ebeef5;
  background: #fafafa;
  flex-shrink: 0;
}

.pm-properties-header h4 {
  margin: 0;
  font-size: 14px;
  color: #303133;
}

.pm-properties-body {
  flex: 1;
  overflow-y: auto;
  padding: 12px 16px;
}

.pf-group {
  margin-bottom: 16px;
}

.pf-group-title {
  font-size: 12px;
  font-weight: 600;
  color: #909399;
  margin-bottom: 10px;
  padding-bottom: 6px;
  border-bottom: 1px solid #ebeef5;
}

.pf-group :deep(.el-form-item) {
  margin-bottom: 12px;
}

.pf-group :deep(.el-form-item__label) {
  font-size: 12px;
  padding-bottom: 2px;
  color: #606266;
}

.pf-group :deep(.el-input__inner),
.pf-group :deep(.el-textarea__inner) {
  font-size: 13px;
}

.pf-group :deep(.el-input-number--small) {
  width: 100%;
}

.pf-group :deep(.el-select) {
  width: 100%;
}
</style>
