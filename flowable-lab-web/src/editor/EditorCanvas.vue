<template>
  <div class="editor-body">
    <EditorPalette @add-node="addNodeAtCenter" />
    <div class="canvas-wrapper">
      <VueFlow
        v-model:nodes="store.nodes"
        v-model:edges="store.edges"
        :node-types="nodeTypes"
        :default-edge-options="defaultEdgeOptions"
        :fit-view-on-init="true"
        @node-click="onNodeClick"
        @edge-click="onEdgeClick"
        @pane-click="onPaneClick"
        @drop="onDrop"
        @dragover="onDragOver"
        @connect="onConnect"
        class="flow-canvas"
      >
        <Background :gap="20" color="#f0f0f0" />
        <Controls :show-zoom="true" :show-fit-view="true" position="bottom-right" />
        <MiniMap />
        <template #node-event="nodeProps">
          <EventNode v-bind="nodeProps" />
        </template>
        <template #node-task="nodeProps">
          <TaskNode v-bind="nodeProps" />
        </template>
        <template #node-gateway="nodeProps">
          <GatewayNode v-bind="nodeProps" />
        </template>
        <template #node-subProcess="nodeProps">
          <SubProcessNode v-bind="nodeProps" />
        </template>
      </VueFlow>
    </div>
    <PropertiesPanel />
  </div>
</template>

<script setup lang="ts">
import { markRaw } from 'vue'
import { VueFlow, useVueFlow } from '@vue-flow/core'
import { Background } from '@vue-flow/background'
import { Controls } from '@vue-flow/controls'
import { MiniMap } from '@vue-flow/minimap'
import type { Node, Edge, Connection } from '@vue-flow/core'
import '@vue-flow/core/dist/style.css'
import '@vue-flow/core/dist/theme-default.css'
import '@vue-flow/controls/dist/style.css'
import '@vue-flow/minimap/dist/style.css'
import EventNode from './nodes/EventNode.vue'
import TaskNode from './nodes/TaskNode.vue'
import GatewayNode from './nodes/GatewayNode.vue'
import SubProcessNode from './nodes/SubProcessNode.vue'
import EditorPalette from './EditorPalette.vue'
import PropertiesPanel from './PropertiesPanel.vue'
import { useEditorStore } from './stores/editorStore'
import type { PaletteItem } from './EditorPalette.vue'

const store = useEditorStore()

const nodeTypes = {
  event: markRaw(EventNode),
  task: markRaw(TaskNode),
  gateway: markRaw(GatewayNode),
  subProcess: markRaw(SubProcessNode),
}

const defaultEdgeOptions: Partial<Edge> = {
  type: 'smoothstep',
  style: { stroke: '#409eff', strokeWidth: 2 },
}

const { screenToFlowCoordinate } = useVueFlow({ id: 'editor' })

function onNodeClick({ node }: { node: Node }) {
  store.selectNode(node.id)
}

function onEdgeClick({ edge }: { edge: Edge }) {
  store.selectEdge(edge.id)
}

function onPaneClick() {
  store.clearSelection()
}

function onConnect(connection: Connection) {
  if (!connection.source || !connection.target) return
  const id = `flow_${Date.now()}`
  store.edges.push({
    id,
    source: connection.source,
    target: connection.target,
    type: 'smoothstep',
    style: { stroke: '#409eff', strokeWidth: 2 },
  } as any)
}

function onDragOver(event: DragEvent) {
  event.preventDefault()
  if (event.dataTransfer) {
    event.dataTransfer.dropEffect = 'copy'
  }
}

function onDrop(event: DragEvent) {
  event.preventDefault()
  const raw = event.dataTransfer?.getData('application/flowable-node')
  if (!raw) return
  const item: PaletteItem = JSON.parse(raw)
  const position = screenToFlowCoordinate({ x: event.clientX, y: event.clientY })
  addNodeAt(item, position.x, position.y)
}

function addNodeAtCenter(item: PaletteItem) {
  addNodeAt(item, window.innerWidth / 2 + (Math.random() - 0.5) * 200, 300 + (Math.random() - 0.5) * 200)
}

function addNodeAt(item: PaletteItem, x: number, y: number) {
  const id = `${item.type}_${Date.now()}`
  store.nodes.push({
    id,
    type: item.type,
    position: { x, y },
    data: {
      subtype: item.subtype,
      label: item.label,
      properties: {},
      extensions: {},
    },
  } as any)
  store.selectNode(id)
}
</script>

<style scoped>
.editor-body {
  flex: 1;
  display: flex;
  min-height: 0;
  background: #fff;
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  overflow: hidden;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
}

.canvas-wrapper {
  flex: 1;
  min-width: 0;
  position: relative;
}

.flow-canvas {
  width: 100%;
  height: 100%;
}
</style>
