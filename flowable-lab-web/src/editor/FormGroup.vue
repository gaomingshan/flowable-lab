<template>
  <div class="form-group">
    <div class="form-group-title">{{ title }}</div>
    <slot />
  </div>
</template>

<script setup lang="ts">
defineProps<{ title: string }>()
</script>

<style scoped>
.form-group {
  margin-bottom: 16px;
}

.form-group-title {
  font-size: 12px;
  font-weight: 600;
  color: #909399;
  margin-bottom: 10px;
  padding-bottom: 6px;
  border-bottom: 1px solid #ebeef5;
}

.form-group :deep(.el-form-item) {
  margin-bottom: 12px;
}

.form-group :deep(.el-form-item__label) {
  font-size: 12px;
  padding-bottom: 2px;
  color: #606266;
}

.form-group :deep(.el-input__inner),
.form-group :deep(.el-textarea__inner) {
  font-size: 13px;
}

.form-group :deep(.el-input-number--small) {
  width: 100%;
}
</style>
