<template>
  <div class="app-shell">
    <aside class="sidebar">
      <h1>Flowable Lab</h1>
      <nav>
        <RouterLink to="/definitions">流程定义</RouterLink>
        <RouterLink to="/designer">流程设计</RouterLink>
        <RouterLink to="/launchable">允许启动流程</RouterLink>
        <RouterLink to="/tasks">我的任务</RouterLink>
      </nav>
    </aside>
    <main class="content">
      <RouterView />
    </main>
  </div>
</template>

<script setup lang="ts">
</script>
