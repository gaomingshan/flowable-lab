<template>
  <div class="page-container">
    <div class="page-header">
      <h2>流程定义</h2>
      <p>查看和管理已部署的 BPMN 流程定义</p>
    </div>

    <el-card>
      <template #header>
        <el-space>
          <span>流程定义列表</span>
          <el-button type="primary" size="small" @click="loadData">刷新</el-button>
        </el-space>
      </template>

      <el-table :data="definitions" stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="280" />
        <el-table-column prop="key" label="Key" width="120" />
        <el-table-column prop="name" label="名称" />
        <el-table-column prop="version" label="版本" width="60" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.suspended ? 'danger' : 'success'" size="small">
              {{ row.suspended ? '已挂起' : '激活' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button size="small" :type="row.suspended ? 'success' : 'warning'"
              @click="toggleSuspend(row)">
              {{ row.suspended ? '激活' : '挂起' }}
            </el-button>
            <el-button size="small" @click="showDiagram(row.id)">流程图</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-card style="margin-top: 20px">
      <template #header>部署列表</template>
      <el-table :data="deployments" stripe style="width: 100%">
        <el-table-column prop="id" label="部署ID" width="280" />
        <el-table-column prop="name" label="名称" />
        <el-table-column prop="deploymentTime" label="部署时间" width="180" />
        <el-table-column label="操作" width="80">
          <template #default="{ row }">
            <el-button size="small" type="danger" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="diagramVisible" title="流程图" width="800px">
      <FlowDiagram :diagram-url="diagramUrl" />
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getDefinitions, getDeployments, deleteDeployment, suspendDefinition, activateDefinition, getDiagram } from '@/api/process'
import { ElMessage, ElMessageBox } from 'element-plus'
import FlowDiagram from '@/components/FlowDiagram.vue'

const definitions = ref<any[]>([])
const deployments = ref<any[]>([])
const diagramVisible = ref(false)
const diagramUrl = ref('')

async function loadData() {
  const defRes = await getDefinitions()
  definitions.value = defRes.data || []
  const depRes = await getDeployments()
  deployments.value = depRes.data || []
}

async function toggleSuspend(row: any) {
  if (row.suspended) {
    await activateDefinition(row.id)
    ElMessage.success('已激活')
  } else {
    await suspendDefinition(row.id)
    ElMessage.success('已挂起')
  }
  await loadData()
}

function showDiagram(id: string) {
  diagramUrl.value = getDiagram(id)
  diagramVisible.value = true
}

async function handleDelete(id: string) {
  await ElMessageBox.confirm('确定删除此部署？关联的流程定义也会被删除。', '确认')
  await deleteDeployment(id)
  ElMessage.success('已删除')
  await loadData()
}

onMounted(loadData)
</script>
