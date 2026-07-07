<template>
  <div class="wf-node" :class="[`wf-node-${data.nodeType}`, { 'wf-node-selected': selected }]">
    <Handle id="target" type="target" :position="Position.Top" class="wf-handle" />
    <div class="wf-node-type">{{ data.typeLabel }}</div>
    <div class="wf-node-name">{{ data.nodeName }}</div>
    <div class="wf-node-key">{{ data.nodeKey }}</div>
    <Handle id="source" type="source" :position="Position.Bottom" class="wf-handle" />
  </div>
</template>

<script setup lang="ts">
import { Handle, Position } from '@vue-flow/core'

defineProps<{
  data: {
    nodeKey: string
    nodeName: string
    nodeType: string
    typeLabel: string
  }
  selected?: boolean
}>()
</script>
