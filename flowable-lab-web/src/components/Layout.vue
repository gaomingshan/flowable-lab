<template>
  <el-container style="height: 100vh">
    <el-aside width="220px" style="background-color: #304156">
      <div class="logo">
        <h2>Flowable Lab</h2>
      </div>
      <el-menu
        :default-active="route.path"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409eff"
        router
      >
        <el-menu-item index="/dashboard">
          <el-icon><DataBoard /></el-icon>
          <span>仪表盘</span>
        </el-menu-item>

        <el-sub-menu index="process">
          <template #title>
            <el-icon><Connection /></el-icon>
            <span>流程管理</span>
          </template>
          <el-menu-item index="/process/definition">流程定义</el-menu-item>
          <el-menu-item index="/process/modeler">流程编辑器</el-menu-item>
          <el-menu-item index="/process/instance">流程实例</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="task">
          <template #title>
            <el-icon><List /></el-icon>
            <span>任务管理</span>
          </template>
          <el-menu-item index="/task/todo">待办任务</el-menu-item>
        </el-sub-menu>

        <el-menu-item index="/dmn/test">
          <el-icon><Operation /></el-icon>
          <span>DMN 决策</span>
        </el-menu-item>

        <el-menu-item index="/cmmn/case">
          <el-icon><FolderOpened /></el-icon>
          <span>CMMN 案例</span>
        </el-menu-item>

        <el-menu-item index="/identity">
          <el-icon><User /></el-icon>
          <span>用户组管理</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header style="background: #fff; border-bottom: 1px solid #e6e6e6; display: flex; align-items: center; justify-content: space-between">
        <h3 style="margin: 0">{{ route.meta.title }}</h3>
        <el-tag type="info">演示模式</el-tag>
      </el-header>

      <el-main style="background: #f0f2f5; padding: 20px">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { useRoute } from 'vue-router'
import { DataBoard, Connection, List, Operation, FolderOpened, User } from '@element-plus/icons-vue'

const route = useRoute()
</script>

<style scoped>
.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-bottom: 1px solid #1f2d3d;
}

.logo h2 {
  color: #fff;
  margin: 0;
  font-size: 18px;
  letter-spacing: 2px;
}
</style>
