<template>
  <div class="event-node" :class="subtype">
    <Handle type="source" :position="Position.Bottom" :connectable="!isEnd" />
    <Handle v-if="!isStart" type="target" :position="Position.Top" />
    <div class="event-node-circle">
      <div v-if="isStart" class="event-inner" />
      <div v-if="isEnd" class="event-inner end" />
      <div v-if="subtype === 'boundary'" class="event-inner boundary" />
    </div>
    <div v-if="label" class="event-label">{{ label }}</div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { Handle, Position } from '@vue-flow/core'
import type { NodeProps } from '@vue-flow/core'

const props = defineProps<NodeProps>()
const subtype = computed(() => props.data?.subtype as string)
const label = computed(() => props.data?.label as string)
const isStart = computed(() => subtype.value === 'start')
const isEnd = computed(() => subtype.value === 'end')
</script>

<style scoped>
.event-node {
  display: flex;
  flex-direction: column;
  align-items: center;
  cursor: pointer;
}

.event-node-circle {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  border: 2px solid #606266;
  display: flex;
  align-items: center;
  justify-content: center;
  background: white;
  transition: all 0.15s;
}

.event-node.start .event-node-circle {
  border-color: #67c23a;
}

.event-node.end .event-node-circle {
  border-color: #f56c6c;
  background: #fef0ef;
}

.event-node.boundary .event-node-circle {
  border-color: #e6a23c;
  border-style: dashed;
}

.event-inner {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: #67c23a;
  opacity: 0.4;
}

.event-inner.end {
  background: #f56c6c;
  opacity: 0.6;
}

.event-inner.boundary {
  width: 16px;
  height: 16px;
  border: 2px solid #e6a23c;
  background: transparent;
  opacity: 1;
}

.event-label {
  font-size: 11px;
  color: #303133;
  text-align: center;
  margin-top: 4px;
  max-width: 80px;
  line-height: 1.2;
  word-break: break-all;
}
</style>
