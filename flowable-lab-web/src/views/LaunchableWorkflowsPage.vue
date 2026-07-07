<template>
  <section class="page">
    <header class="page-header">
      <div>
        <h2>允许启动流程</h2>
        <p>读取平台基础可启动流程列表。</p>
      </div>
      <button @click="store.fetchLaunchable">刷新</button>
    </header>

    <section class="panel">
      <table class="table">
        <thead>
          <tr>
            <th>名称</th>
            <th>Key</th>
            <th>状态</th>
            <th>发布版本</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in store.launchable" :key="item.definitionId">
            <td>{{ item.definitionName }}</td>
            <td>{{ item.definitionKey }}</td>
            <td>{{ item.status }}</td>
            <td>V{{ item.latestReleasedVersion }}</td>
          </tr>
        </tbody>
      </table>
    </section>
  </section>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useWorkflowStore } from '../stores/workflow'

const store = useWorkflowStore()

onMounted(() => {
  store.fetchLaunchable()
})
</script>
