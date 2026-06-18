<template>
  <div class="task-node" :class="subtype">
    <Handle type="target" :position="Position.Top" />
    <Handle type="source" :position="Position.Bottom" />
    <div class="task-node-rect">
      <div class="task-icon">
        <svg v-if="isUser" width="16" height="16" viewBox="0 0 20 20">
          <path d="M7 8a3 3 0 1 0 6 0 3 3 0 0 0-6 0m-2 7a6 6 0 0 1 10 0" fill="none" stroke="currentColor" stroke-width="1.5" />
        </svg>
        <svg v-if="isService" width="16" height="16" viewBox="0 0 20 20">
          <path d="M10 5v10M5 10h10" stroke="currentColor" stroke-width="1.5" />
        </svg>
        <svg v-if="isScript" width="16" height="16" viewBox="0 0 20 20">
          <path d="M6 4l-3 6 3 6M14 4l3 6-3 6" fill="none" stroke="currentColor" stroke-width="1.5" />
        </svg>
      </div>
      <div class="task-label">{{ label }}</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { Handle, Position } from '@vue-flow/core'
import type { NodeProps } from '@vue-flow/core'

const props = defineProps<NodeProps>()
const subtype = computed(() => props.data?.subtype as string)
const label = computed(() => props.data?.label as string)
const isUser = computed(() => subtype.value === 'user')
const isService = computed(() => subtype.value === 'service')
const isScript = computed(() => subtype.value === 'script')
</script>

<style scoped>
.task-node {
  cursor: pointer;
}

.task-node-rect {
  width: 100px;
  min-height: 60px;
  border: 2px solid #409eff;
  border-radius: 6px;
  background: white;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 6px 8px;
  transition: all 0.15s;
}

.task-node:hover .task-node-rect {
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.2);
}

.task-icon {
  color: #409eff;
  margin-bottom: 2px;
}

.task-label {
  font-size: 11px;
  color: #303133;
  text-align: center;
  line-height: 1.2;
  word-break: break-all;
}
</style>
