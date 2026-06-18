<template>
  <div class="gateway-node" :class="subtype">
    <Handle type="target" :position="Position.Top" />
    <Handle type="source" :position="Position.Bottom" />
    <div class="gateway-node-diamond">
      <svg width="50" height="50" viewBox="0 0 50 50">
        <polygon points="25,2 48,25 25,48 2,25"
          :fill="fillColor" stroke="currentColor" stroke-width="2" />
        <line v-if="isXor" x1="18" y1="18" x2="32" y2="32" stroke="currentColor" stroke-width="2" />
        <line v-if="isXor" x1="32" y1="18" x2="18" y2="32" stroke="currentColor" stroke-width="2" />
        <line v-if="isParallel" x1="16" y1="25" x2="34" y2="25" stroke="currentColor" stroke-width="2" />
        <line v-if="isParallel" x1="25" y1="16" x2="25" y2="34" stroke="currentColor" stroke-width="2" />
        <circle v-if="isInclusive" cx="25" cy="25" r="8" fill="none" stroke="currentColor" stroke-width="2" />
      </svg>
    </div>
    <div v-if="label" class="gateway-label">{{ label }}</div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { Handle, Position } from '@vue-flow/core'
import type { NodeProps } from '@vue-flow/core'

const props = defineProps<NodeProps>()
const subtype = computed(() => props.data?.subtype as string)
const label = computed(() => props.data?.label as string)
const isXor = computed(() => subtype.value === 'exclusive')
const isParallel = computed(() => subtype.value === 'parallel')
const isInclusive = computed(() => subtype.value === 'inclusive')
const fillColor = computed(() => {
  if (isXor.value) return '#fff'
  if (isParallel.value) return '#e6f7ff'
  if (isInclusive.value) return '#fff7e6'
  return '#fff'
})
</script>

<style scoped>
.gateway-node {
  display: flex;
  flex-direction: column;
  align-items: center;
  cursor: pointer;
  color: #909399;
}

.gateway-node:hover {
  color: #409eff;
}

.gateway-label {
  font-size: 11px;
  color: #303133;
  text-align: center;
  margin-top: 4px;
  max-width: 80px;
  line-height: 1.2;
}
</style>
