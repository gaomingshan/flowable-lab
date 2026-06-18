<template>
  <div class="subprocess-node">
    <Handle type="target" :position="Position.Top" />
    <Handle type="source" :position="Position.Bottom" />
    <div class="subprocess-rect">
      <div class="subprocess-header">{{ label }}</div>
      <div class="subprocess-body">
        <span class="subprocess-hint">{{ childCount }} 个节点</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { Handle, Position } from '@vue-flow/core'
import type { NodeProps } from '@vue-flow/core'

const props = defineProps<NodeProps>()
const label = computed(() => props.data?.label as string)
const childCount = computed(() => {
  return props.data?.process?.nodes?.length || 0
})
</script>

<style scoped>
.subprocess-node {
  cursor: pointer;
}

.subprocess-rect {
  width: 200px;
  min-height: 100px;
  border: 2px solid #8b5cf6;
  border-radius: 8px;
  background: white;
  overflow: hidden;
  transition: all 0.15s;
}

.subprocess-node:hover .subprocess-rect {
  box-shadow: 0 2px 8px rgba(139, 92, 246, 0.2);
}

.subprocess-header {
  background: #f5f3ff;
  padding: 4px 8px;
  font-size: 11px;
  font-weight: 600;
  color: #6d28d9;
  border-bottom: 1px solid #e9d5ff;
  text-align: center;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.subprocess-body {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 12px;
}

.subprocess-hint {
  font-size: 11px;
  color: #a78bfa;
}
</style>
