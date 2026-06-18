<template>
  <aside v-if="hasSelection" class="properties">
    <div class="properties-header">
      <h4>{{ selectedLabel }}</h4>
      <el-button text @click="store.clearSelection">
        <el-icon><Close /></el-icon>
      </el-button>
    </div>
    <div class="properties-body">
      <!-- Process properties -->
      <template v-if="mode === 'process'">
        <FormGroup title="流程属性">
          <el-form label-position="top" size="small">
            <el-form-item label="标识">
              <el-input :model-value="store.processInfo.id" disabled />
            </el-form-item>
            <el-form-item label="名称">
              <el-input v-model="store.processInfo.name" />
            </el-form-item>
          </el-form>
        </FormGroup>
      </template>

      <!-- Edge properties -->
      <template v-else-if="mode === 'edge' && edgeData">
        <FormGroup title="连线属性">
          <el-form label-position="top" size="small">
            <el-form-item label="标识">
              <el-input :model-value="store.selectedEdgeId || ''" disabled />
            </el-form-item>
            <el-form-item label="条件表达式">
              <el-input v-model="edgeData.conditionExpression" placeholder="如 ${amount > 1000}" @change="onEdgeChanged" />
            </el-form-item>
          </el-form>
        </FormGroup>
      </template>

      <!-- Node properties -->
      <template v-else-if="mode === 'node' && nodeData">
        <FormGroup title="基本属性">
          <el-form label-position="top" size="small">
            <el-form-item label="标识">
              <el-input :model-value="store.selectedNodeId || ''" disabled />
            </el-form-item>
            <el-form-item label="名称">
              <el-input v-model="nodeData.label" @change="onNodeChanged" />
            </el-form-item>
          </el-form>
        </FormGroup>

        <FormGroup v-if="nodeData.subtype === 'user'" title="任务配置">
          <el-form label-position="top" size="small">
            <el-form-item label="办理人">
              <el-input v-model="nodeData.assignee" placeholder="如 admin 或 ${applicant}" @change="onNodeChanged" />
            </el-form-item>
            <el-form-item label="候选组">
              <el-input v-model="nodeData.candidateGroups" placeholder="多个用逗号分隔" @change="onNodeChanged" />
            </el-form-item>
            <el-form-item label="候选用户">
              <el-input v-model="nodeData.candidateUsers" placeholder="多个用逗号分隔" @change="onNodeChanged" />
            </el-form-item>
            <el-form-item label="表单 Key">
              <el-input v-model="nodeData.formKey" @change="onNodeChanged" />
            </el-form-item>
            <el-form-item label="截止日期">
              <el-input v-model="nodeData.dueDate" placeholder="如 PT1H 或 ${dueDate}" @change="onNodeChanged" />
            </el-form-item>
            <el-form-item label="优先级">
              <el-input-number v-model="nodeData.priority" :min="0" :max="100" @change="onNodeChanged" controls-position="right" />
            </el-form-item>
          </el-form>
        </FormGroup>
      </template>
    </div>
  </aside>
  <aside v-else class="properties properties-empty">
    <div class="properties-hint">
      <el-icon :size="32" color="#c0c4cc"><Connection /></el-icon>
      <p>点击节点或连线查看属性</p>
    </div>
  </aside>
</template>

<script setup lang="ts">
import { reactive, computed, watch } from 'vue'
import { Close, Connection } from '@element-plus/icons-vue'
import { useEditorStore } from './stores/editorStore'
import FormGroup from './FormGroup.vue'

const store = useEditorStore()

type EditorMode = 'process' | 'node' | 'edge'

const mode = computed<EditorMode>(() => {
  if (store.selectedNodeId) return 'node'
  if (store.selectedEdgeId) return 'edge'
  return 'process'
})

const hasSelection = computed(() => mode.value !== 'process')

const selectedLabel = computed(() => {
  if (mode.value === 'process') return '流程属性'
  if (mode.value === 'edge') return '连线'
  const n = findNode()
  return n?.data?.label as string || '节点'
})

function findNode() {
  return store.nodes.find(n => n.id === store.selectedNodeId) || null
}

function findEdge() {
  return store.edges.find(e => e.id === store.selectedEdgeId) || null
}

interface NodeForm {
  label: string
  subtype: string
  assignee: string
  candidateGroups: string
  candidateUsers: string
  formKey: string
  dueDate: string
  priority: number
}

interface EdgeForm {
  conditionExpression: string
}

const nodeData = reactive<NodeForm>({
  label: '',
  subtype: '',
  assignee: '',
  candidateGroups: '',
  candidateUsers: '',
  formKey: '',
  dueDate: '',
  priority: 50,
})

const edgeData = reactive<EdgeForm>({
  conditionExpression: '',
})

watch(() => store.selectedNodeId, (id) => {
  if (!id) return
  const n = store.nodes.find(x => x.id === id)
  if (!n?.data) return
  const d = n.data
  nodeData.label = (d.label as string) || ''
  nodeData.subtype = (d.subtype as string) || ''
  nodeData.assignee = ((d.properties as any)?.assignee as string) || ''
  nodeData.candidateGroups = ((d.properties as any)?.candidateGroups as string) || ''
  nodeData.candidateUsers = ((d.properties as any)?.candidateUsers as string) || ''
  nodeData.formKey = ((d.properties as any)?.formKey as string) || ''
  nodeData.dueDate = ((d.properties as any)?.dueDate as string) || ''
  nodeData.priority = ((d.properties as any)?.priority as number) ?? 50
})

watch(() => store.selectedEdgeId, (id) => {
  if (!id) return
  const e = store.edges.find(x => x.id === id)
  edgeData.conditionExpression = ((e?.data as any)?.conditionExpression as string) || ''
})

function onNodeChanged() {
  const n = store.nodes.find(x => x.id === store.selectedNodeId)
  if (!n?.data) return
  n.data.label = nodeData.label
  if (!n.data.properties) n.data.properties = {}
  const p = n.data.properties as any
  p.assignee = nodeData.assignee || undefined
  p.candidateGroups = nodeData.candidateGroups || undefined
  p.candidateUsers = nodeData.candidateUsers || undefined
  p.formKey = nodeData.formKey || undefined
  p.dueDate = nodeData.dueDate || undefined
  p.priority = nodeData.priority
}

function onEdgeChanged() {
  const e = store.edges.find(x => x.id === store.selectedEdgeId)
  if (!e) return
  if (!e.data) e.data = {}
  const d = e.data as any
  d.conditionExpression = edgeData.conditionExpression || undefined
}
</script>

<style scoped>
.properties {
  width: 300px;
  flex-shrink: 0;
  border-left: 1px solid #dcdfe6;
  background: #fff;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.properties-empty {
  justify-content: center;
  align-items: center;
}

.properties-hint {
  text-align: center;
  color: #c0c4cc;
}

.properties-hint p {
  margin: 8px 0 0;
  font-size: 13px;
}

.properties-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid #ebeef5;
  background: #fafafa;
  flex-shrink: 0;
}

.properties-header h4 {
  margin: 0;
  font-size: 14px;
  color: #303133;
}

.properties-body {
  flex: 1;
  overflow-y: auto;
  padding: 12px 16px;
}
</style>
