<template>
  <section class="page">
    <header class="page-header">
      <div>
        <h2>流程设计</h2>
        <p>围绕流程定义、版本和 graph_json 的真实保存链路进行编辑。</p>
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
          <select v-model.number="selectedVersionNo" :disabled="!selectedDefinitionId">
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
      <div class="status-text">{{ statusText }}</div>
    </section>

    <section class="panel">
      <h3>graph_json</h3>
      <textarea v-model="graphText" class="code-block" />
    </section>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { getGraph, saveGraph } from '../api/workflow'
import { useWorkflowStore } from '../stores/workflow'

const route = useRoute()
const store = useWorkflowStore()

const selectedDefinitionId = ref('')
const selectedVersionNo = ref<number | null>(null)
const changeNote = ref('设计页创建草稿版本')
const graphText = ref(JSON.stringify({ nodes: [], edges: [], viewport: { x: 0, y: 0, zoom: 1 } }, null, 2))
const syncedNodes = ref(0)
const syncedEdges = ref(0)
const statusText = ref('等待加载')

const versionOptions = computed(() => {
  if (!selectedDefinitionId.value) {
    return []
  }
  return store.versions[selectedDefinitionId.value] ?? []
})

async function loadGraph() {
  if (!selectedDefinitionId.value || !selectedVersionNo.value) {
    return
  }
  const result = await getGraph(selectedDefinitionId.value, selectedVersionNo.value)
  graphText.value = JSON.stringify(result.graph, null, 2)
  syncedNodes.value = result.syncedNodes
  syncedEdges.value = result.syncedEdges
  statusText.value = `已加载 V${result.versionNo}`
}

async function saveCurrentGraph() {
  if (!selectedDefinitionId.value || !selectedVersionNo.value) {
    return
  }
  const graph = JSON.parse(graphText.value)
  const result = await saveGraph(selectedDefinitionId.value, selectedVersionNo.value, graph)
  graphText.value = JSON.stringify(result.graph, null, 2)
  syncedNodes.value = result.syncedNodes
  syncedEdges.value = result.syncedEdges
  statusText.value = `已保存 V${result.versionNo}`
}

async function handleDefinitionChange() {
  if (!selectedDefinitionId.value) {
    selectedVersionNo.value = null
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
  if (selectedDefinitionId.value) {
    await handleDefinitionChange()
  }
})
</script>
