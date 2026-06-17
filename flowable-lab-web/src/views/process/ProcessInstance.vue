<template>
  <div class="page-container">
    <div class="page-header">
      <h2>流程实例</h2>
      <p>查看和管理运行中及历史的流程实例</p>
    </div>

    <el-card>
      <template #header>
        <el-space>
          <el-radio-group v-model="viewMode" @change="loadData">
            <el-radio-button value="running">运行中</el-radio-button>
            <el-radio-button value="history">历史</el-radio-button>
          </el-radio-group>
          <el-button type="primary" size="small" @click="loadData">刷新</el-button>
        </el-space>
      </template>

      <el-table :data="instances" stripe style="width: 100%">
        <el-table-column prop="id" label="实例ID" width="260" />
        <el-table-column prop="processDefinitionKey" label="流程Key" width="120" />
        <el-table-column prop="processDefinitionName" label="流程名称" />
        <el-table-column prop="startTime" label="开始时间" width="160" />
        <el-table-column prop="endTime" label="结束时间" width="160" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.endTime" type="success" size="small">已完成</el-tag>
            <el-tag v-else-if="row.suspended" type="warning" size="small">已挂起</el-tag>
            <el-tag v-else type="primary" size="small">运行中</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button v-if="!row.endTime && !row.suspended" size="small" type="warning"
              @click="handleSuspend(row.id)">挂起</el-button>
            <el-button v-if="!row.endTime && row.suspended" size="small" type="success"
              @click="handleActivate(row.id)">恢复</el-button>
            <el-button v-if="!row.endTime" size="small" type="danger"
              @click="handleDelete(row.id)">终止</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getInstances, suspendInstance, activateInstance, deleteInstance } from '@/api/process'
import { ElMessage, ElMessageBox } from 'element-plus'

const viewMode = ref('running')
const instances = ref<any[]>([])

async function loadData() {
  const res = await getInstances(viewMode.value)
  instances.value = res.data || []
}

async function handleSuspend(id: string) {
  await suspendInstance(id)
  ElMessage.success('已挂起')
  await loadData()
}

async function handleActivate(id: string) {
  await activateInstance(id)
  ElMessage.success('已恢复')
  await loadData()
}

async function handleDelete(id: string) {
  await ElMessageBox.confirm('确定终止此流程实例？', '确认')
  await deleteInstance(id)
  ElMessage.success('已终止')
  await loadData()
}

onMounted(loadData)
</script>
