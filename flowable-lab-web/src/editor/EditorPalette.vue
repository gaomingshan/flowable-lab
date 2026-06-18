<template>
  <aside class="palette">
    <div class="palette-title">组件</div>
    <div
      v-for="item in items"
      :key="`${item.type}-${item.subtype}`"
      class="palette-item"
      draggable="true"
      @dragstart="onDragStart($event, item)"
      @click="$emit('add-node', item)"
    >
      <div class="palette-icon">
        <svg v-if="item.type === 'event'" width="20" height="20" viewBox="0 0 20 20">
          <circle cx="10" cy="10" r="8" :fill="item.subtype === 'start' ? '#e1f3d8' : '#fde2e2'"
            :stroke="item.subtype === 'start' ? '#67c23a' : '#f56c6c'" stroke-width="1.5" />
          <circle cx="10" cy="10" r="4" :fill="item.subtype === 'start' ? '#67c23a' : '#f56c6c'" opacity="0.4" />
        </svg>
        <svg v-else-if="item.type === 'task'" width="20" height="20" viewBox="0 0 20 20">
          <rect x="2" y="2" width="16" height="16" rx="3" fill="none" stroke="#409eff" stroke-width="1.5" />
        </svg>
        <svg v-else-if="item.type === 'gateway'" width="20" height="20" viewBox="0 0 20 20">
          <polygon points="10,1 19,10 10,19 1,10" fill="none" stroke="#909399" stroke-width="1.5" />
        </svg>
        <svg v-else width="20" height="20" viewBox="0 0 20 20">
          <rect x="2" y="2" width="16" height="16" rx="4" fill="none" stroke="#8b5cf6" stroke-width="1.5" />
        </svg>
      </div>
      <span class="palette-label">{{ item.label }}</span>
    </div>
  </aside>
</template>

<script setup lang="ts">
import type { VisualNodeType } from './types'
import { PALETTE_ITEMS } from './types'

defineEmits<{ 'add-node': [item: PaletteItem] }>()

export interface PaletteItem {
  type: VisualNodeType
  subtype: string
  label: string
}

const items = PALETTE_ITEMS

function onDragStart(event: DragEvent, item: PaletteItem) {
  if (!event.dataTransfer) return
  event.dataTransfer.setData('application/flowable-node', JSON.stringify(item))
  event.dataTransfer.effectAllowed = 'copy'
}
</script>

<style scoped>
.palette {
  width: 160px;
  flex-shrink: 0;
  background: #fafafa;
  border-right: 1px solid #dcdfe6;
  padding: 12px;
  overflow-y: auto;
}

.palette-title {
  font-size: 12px;
  font-weight: 600;
  color: #909399;
  text-transform: uppercase;
  letter-spacing: 1px;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid #ebeef5;
}

.palette-item {
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

.palette-item:hover {
  background: #ecf5ff;
}

.palette-item:active {
  cursor: grabbing;
  background: #d9ecff;
}

.palette-icon {
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.palette-label {
  font-size: 13px;
  color: #303133;
  line-height: 1;
}
</style>
