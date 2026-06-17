<template>
  <div class="page-container">
    <div class="page-header">
      <h2>CMMN 案例管理</h2>
      <p>弹性案例管理，无固定流程顺序</p>
    </div>

    <el-card>
      <template #header>
        <el-space>
          <span>运行中案例</span>
          <el-button type="primary" size="small" @click="startCase">启动投诉案例</el-button>
          <el-button size="small" @click="loadData">刷新</el-button>
        </el-space>
      </template>

      <el-table :data="cases" stripe style="width: 100%">
        <el-table-column prop="id" label="案例ID" width="260" />
        <el-table-column prop="caseDefinitionKey" label="定义Key" width="140" />
        <el-table-column prop="caseDefinitionName" label="名称" />
        <el-table-column prop="startTime" label="开始时间" width="160" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag type="primary" size="small">运行中</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button size="small" type="danger" @click="handleTerminate(row.id)">终止</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import request from '@/api/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const cases = ref<any[]>([])

async function loadData() {
  const res = await request.get('/cmmn/case')
  cases.value = res.data || []
}

async function startCase() {
  const res = await request.post('/cmmn/case/start/complaint-handling', {
    initiator: 'demo_user'
  })
  ElMessage.success('投诉案例已启动')
  await loadData()
}

async function handleTerminate(id: string) {
  await ElMessageBox.confirm('确定终止此案例？', '确认')
  await request.delete(`/cmmn/case/${id}`)
  ElMessage.success('已终止')
  await loadData()
}

onMounted(loadData)
</script>
