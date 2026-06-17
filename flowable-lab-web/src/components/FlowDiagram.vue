<template>
  <el-image
    :src="diagramUrl"
    fit="contain"
    style="width: 100%; height: 500px"
  >
    <template #error>
      <div class="diagram-error">流程图加载失败</div>
    </template>
  </el-image>
</template>

<script setup lang="ts">
defineProps<{
  diagramUrl: string
}>()
</script>

<style scoped>
.diagram-error {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 500px;
  color: #999;
  font-size: 14px;
}
</style>
