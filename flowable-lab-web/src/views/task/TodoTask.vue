<template>
  <div class="page-container">
    <div class="page-header">
      <h2>待办任务</h2>
      <p>查看和处理待办任务</p>
    </div>

    <el-card>
      <template #header>
        <el-space>
          <span>待办任务列表</span>
          <el-button size="small" @click="loadData">刷新</el-button>
          <el-input v-model="assignee" placeholder="指定办理人" size="small" style="width: 150px" />
          <el-button size="small" type="primary" @click="loadData">查询</el-button>
        </el-space>
      </template>

      <el-table :data="tasks" stripe style="width: 100%">
        <el-table-column prop="id" label="任务ID" width="260" />
        <el-table-column prop="name" label="任务名称" />
        <el-table-column prop="processDefinitionId" label="流程" width="200" />
        <el-table-column prop="assignee" label="办理人" width="100" />
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="goDetail(row.id)">办理</el-button>
            <el-button v-if="!row.assignee" size="small" @click="handleClaim(row.id)">认领</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getTodoTasks, claimTask } from '@/api/task'
import { ElMessage } from 'element-plus'

const router = useRouter()
const tasks = ref<any[]>([])
const assignee = ref('')

async function loadData() {
  const params: Record<string, string> = {}
  if (assignee.value) params.assignee = assignee.value
  const res = await getTodoTasks(params)
  tasks.value = res.data || []
}

function goDetail(taskId: string) {
  router.push(`/task/detail/${taskId}`)
}

async function handleClaim(taskId: string) {
  await claimTask(taskId, 'demo_user')
  ElMessage.success('认领成功')
  await loadData()
}

onMounted(loadData)
</script>
