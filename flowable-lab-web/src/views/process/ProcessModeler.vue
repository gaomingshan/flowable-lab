<template>
  <div class="pm-page">
    <div class="pm-header">
      <div>
        <h2>流程编辑器</h2>
        <p v-if="processKey">编辑: {{ processKey }} (v{{ version }})</p>
        <p v-else>创建新流程定义</p>
      </div>
      <el-space>
        <el-button type="primary" :loading="loading" @click="handleSave">保存并部署</el-button>
        <el-button @click="handleNew">新建</el-button>
        <el-upload :show-file-list="false" accept=".xml" :before-upload="handleUpload">
          <el-button>打开 XML</el-button>
        </el-upload>
        <el-button @click="handleDownload">下载 XML</el-button>
      </el-space>
    </div>
    <div ref="modelerContainer" class="pm-body">
      <div ref="canvas" class="pm-canvas" />
      <div ref="properties" class="pm-properties" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import Modeler from 'bpmn-js/lib/Modeler'
import {
  BpmnPropertiesPanelModule,
  CamundaPlatformPropertiesProviderModule
} from 'bpmn-js-properties-panel'
import camundaModdle from 'camunda-bpmn-moddle/resources/camunda.json'
import 'bpmn-js/dist/assets/diagram-js.css'
import 'bpmn-js/dist/assets/bpmn-font/css/bpmn.css'
import { getDefinitionXml, deployBpmn } from '@/api/process'

const route = useRoute()
const router = useRouter()

const modelerContainer = ref<HTMLElement>()
const canvas = ref<HTMLElement>()
const properties = ref<HTMLElement>()

const processKey = ref('')
const version = ref(0)
const processName = ref('')
const loading = ref(false)

let modeler: Modeler | null = null

function toCamundaXml(xml: string): string {
  return xml
    .replace('xmlns:flowable="http://flowable.org/bpmn"', 'xmlns:camunda="http://camunda.org/schema/1.0/bpmn"')
    .replace(/flowable:/g, 'camunda:')
}

function toFlowableXml(xml: string): string {
  return xml
    .replace('xmlns:camunda="http://camunda.org/schema/1.0/bpmn"', 'xmlns:flowable="http://flowable.org/bpmn"')
    .replace(/camunda:/g, 'flowable:')
}

async function initModeler(xml?: string) {
  if (modeler) {
    modeler.destroy()
    modeler = null
  }
  if (!canvas.value || !properties.value) return

  loading.value = true
  try {
    modeler = new Modeler({
      container: canvas.value,
      propertiesPanel: { parent: properties.value },
      additionalModules: [
        BpmnPropertiesPanelModule,
        CamundaPlatformPropertiesProviderModule
      ],
      moddleExtensions: {
        camunda: camundaModdle
      }
    })

    if (xml) {
      const camundaXml = toCamundaXml(xml)
      await modeler.importXML(camundaXml)
    } else {
      await modeler.importXML(`<?xml version="1.0" encoding="UTF-8"?>
<definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
  xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"
  xmlns:dc="http://www.omg.org/spec/DD/20100524/DC"
  id="Definitions_1" targetNamespace="http://flowable.org/bpmn">
  <process id="Process_1" isExecutable="true">
    <startEvent id="StartEvent_1" />
  </process>
  <bpmndi:BPMNDiagram id="BPMNDiagram_1">
    <bpmndi:BPMNPlane id="BPMNPlane_1" bpmnElement="Process_1">
      <bpmndi:BPMNShape id="StartEvent_1_di" bpmnElement="StartEvent_1">
        <dc:Bounds x="152" y="102" width="36" height="36" />
      </bpmndi:BPMNShape>
    </bpmndi:BPMNPlane>
  </bpmndi:BPMNDiagram>
</definitions>`)
    }
    modeler.get('canvas').zoom('fit-viewport')
  } catch (err: unknown) {
    ElMessage.error('加载流程图失败: ' + ((err as any)?.message || err))
  } finally {
    loading.value = false
  }
}

async function loadDefinition(id: string) {
  const res = await getDefinitionXml(id)
  const xml = res.data
  if (xml) {
    processKey.value = (route.query.key as string) || ''
    version.value = Number(route.query.version || 0)
    processName.value = (route.query.name as string) || ''
    await initModeler(xml)
  }
}

async function handleSave() {
  if (!modeler) return
  loading.value = true
  try {
    const { xml } = await modeler.saveXML({ format: true })
    if (!xml) return
    const name = await ElMessageBox.prompt('请输入部署名称', '部署', {
      inputValue: processName.value || '未命名流程',
      inputPlaceholder: '部署名称'
    })
    if (name.value) {
      const flowableXml = toFlowableXml(xml)
      await deployBpmn(name.value, flowableXml)
      ElMessage.success('部署成功')
      router.push('/process/definition')
    }
  } catch (err: unknown) {
    const e = err as any
    if (e !== 'cancel' && e !== 'close') {
      ElMessage.error('保存失败: ' + (e.message || e))
    }
  } finally {
    loading.value = false
  }
}

function handleNew() {
  processKey.value = ''
  version.value = 0
  processName.value = ''
  initModeler()
}

function handleUpload(file: File): boolean {
  const reader = new FileReader()
  reader.onload = async (e) => {
    const xml = e.target?.result as string
    if (xml) {
      processKey.value = ''
      version.value = 0
      processName.value = file.name
      await initModeler(xml)
    }
  }
  reader.readAsText(file)
  return false
}

async function handleDownload() {
  if (!modeler) return
  const { xml } = await modeler.saveXML({ format: true })
  if (!xml) return
  const blob = new Blob([xml], { type: 'application/xml' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `${processKey.value || 'process'}.bpmn20.xml`
  a.click()
  URL.revokeObjectURL(url)
}

onMounted(async () => {
  const defId = route.query.definitionId as string
  if (defId) {
    await loadDefinition(defId)
  } else {
    await initModeler()
  }
})

onBeforeUnmount(() => {
  if (modeler) {
    modeler.destroy()
    modeler = null
  }
})
</script>

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
}

.pm-header h2 {
  margin: 0;
}

.pm-header p {
  margin: 4px 0 0;
  color: #999;
  font-size: 13px;
}

.pm-body {
  flex: 1;
  display: flex;
  min-height: 0;
  background: #fff;
  border: 1px solid #e6e6e6;
  border-radius: 4px;
  overflow: hidden;
}

.pm-canvas {
  flex: 1;
  height: 100%;
  position: relative;
}

.pm-properties {
  width: 300px;
  flex-shrink: 0;
  overflow-y: auto;
  border-left: 1px solid #e6e6e6;
  background: #fafafa;
}
</style>
