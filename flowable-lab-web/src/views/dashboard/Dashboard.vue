<template>
  <div class="page-container">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-value">{{ stats.total }}</div>
            <div class="stat-label">流程总数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-value" style="color: #67c23a">{{ stats.running }}</div>
            <div class="stat-label">运行中</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-value" style="color: #409eff">{{ stats.finished }}</div>
            <div class="stat-label">已完成</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-value">{{ definitions.length }}</div>
            <div class="stat-label">流程定义数</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="12">
        <el-card>
          <template #header>流程定义</template>
          <el-table :data="definitions" stripe style="width: 100%">
            <el-table-column prop="key" label="Key" />
            <el-table-column prop="name" label="名称" />
            <el-table-column prop="version" label="版本" width="60" />
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>快速操作</template>
          <el-space direction="vertical" alignment="stretch" style="width: 100%">
            <el-button type="primary" @click="startLeave">启动请假流程</el-button>
            <el-button type="success" @click="startPurchase">启动采购流程</el-button>
            <el-button @click="$router.push('/process/definition')">查看流程定义</el-button>
            <el-button @click="$router.push('/task/todo')">查看待办任务</el-button>
          </el-space>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getDefinitions, startInstance } from '@/api/process'
import request from '@/api/request'

const router = useRouter()
const definitions = ref<any[]>([])
const stats = ref({ total: 0, running: 0, finished: 0 })

async function loadData() {
  const defRes = await getDefinitions()
  definitions.value = defRes.data || []

  const statRes = await request.get('/monitor/statistics')
  stats.value = statRes.data || { total: 0, running: 0, finished: 0 }
}

async function startLeave() {
  await startInstance('leave', {
    applicant: 'demo_user',
    leaveDays: 3,
    leaveReason: '样例：事假'
  })
  ElMessage.success('请假流程已启动')
  router.push('/task/todo')
}

async function startPurchase() {
  await startInstance('purchase', {
    applicant: 'demo_user',
    amount: 3000,
    purpose: '样例：采购办公设备',
    level: 'employee'
  })
  ElMessage.success('采购流程已启动')
  router.push('/task/todo')
}

onMounted(loadData)
</script>

<style scoped>
.stat-item {
  text-align: center;
  padding: 10px 0;
}
.stat-value {
  font-size: 36px;
  font-weight: bold;
  color: #303133;
}
.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 8px;
}
</style>
