<template>
  <div class="page-container">
    <div class="page-header">
      <h2>任务详情</h2>
      <p>任务办理与流程跟踪</p>
    </div>

    <el-row :gutter="20">
      <el-col :span="12">
        <el-card>
          <template #header>任务信息</template>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="任务ID">{{ task?.id }}</el-descriptions-item>
            <el-descriptions-item label="任务名称">{{ task?.name }}</el-descriptions-item>
            <el-descriptions-item label="流程实例">{{ task?.processInstanceId }}</el-descriptions-item>
            <el-descriptions-item label="办理人">{{ task?.assignee || '未分配' }}</el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ task?.createTime }}</el-descriptions-item>
          </el-descriptions>

          <div style="margin-top: 20px">
            <el-form :model="form" label-width="100px">
              <el-form-item label="审批意见">
                <el-input v-model="form.comment" type="textarea" rows="3" />
              </el-form-item>
              <el-form-item label="是否同意">
                <el-switch v-model="form.approved" active-text="同意" inactive-text="驳回" />
              </el-form-item>
              <el-form-item>
                <el-space>
                  <el-button type="primary" @click="handleComplete">完成任务</el-button>
                  <el-button @click="handleClaim" v-if="!task?.assignee">认领任务</el-button>
                  <el-button @click="handleTransfer" v-if="task?.assignee">转办</el-button>
                </el-space>
              </el-form-item>
            </el-form>
          </div>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card>
          <template #header>流程图</template>
          <el-image
            v-if="diagramUrl"
            :src="diagramUrl"
            fit="contain"
            style="width: 100%; height: 400px"
          />
          <el-empty v-else description="无流程图" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getTask, completeTask, claimTask, transferTask } from '@/api/task'
import { getDiagram, getHighlightedDiagram } from '@/api/process'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()
const task = ref<any>(null)
const form = ref({
  comment: '',
  approved: true
})

const diagramUrl = computed(() => {
  if (!task.value) return ''
  if (task.value.processInstanceId) {
    const defId = task.value.processDefinitionId
    return getHighlightedDiagram(defId, task.value.processInstanceId)
  }
  return ''
})

async function loadData() {
  const taskId = route.params.taskId as string
  const res = await getTask(taskId)
  task.value = res.data
}

async function handleComplete() {
  const variables: Record<string, any> = {
    approved: form.value.approved,
    comment: form.value.comment
  }
  await completeTask(route.params.taskId as string, variables)
  ElMessage.success('任务已完成')
  router.push('/task/todo')
}

async function handleClaim() {
  await claimTask(route.params.taskId as string, 'demo_user')
  ElMessage.success('认领成功')
  await loadData()
}

async function handleTransfer() {
  const { value } = await ElMessageBox.prompt('请输入新办理人ID', '转办')
  if (value) {
    await transferTask(route.params.taskId as string, value)
    ElMessage.success('已转办')
    await loadData()
  }
}

onMounted(loadData)
</script>
