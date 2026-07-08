<template>
  <section class="page">
    <header class="page-header">
      <div>
        <h2>我的任务</h2>
        <p>围绕未认领、待办、已办和我的发起构成一期任务工作台。</p>
      </div>
      <button @click="refreshWorkbench">刷新</button>
    </header>

    <section class="panel task-toolbar">
      <label class="field-block task-filter">
        <span>当前用户</span>
        <input v-model="userId" placeholder="默认 system-admin" />
      </label>
      <div class="toolbar-actions">
        <button @click="refreshWorkbench">查询工作台</button>
      </div>
    </section>

    <section class="task-layout">
      <section class="task-columns">
        <article class="panel task-column">
          <div class="task-column-header">
            <h3>未认领</h3>
            <span>{{ store.unclaimedTasks.length }}</span>
          </div>
          <div class="task-list">
            <button
              v-for="task in store.unclaimedTasks"
              :key="task.taskId"
              type="button"
              class="task-card"
              @click="selectTask(task.taskId)"
            >
              <strong>{{ task.taskName }}</strong>
              <span>{{ task.processDefinitionKey }}</span>
              <span>{{ task.formTitle || task.businessKey || task.processInstanceId }}</span>
            </button>
            <div v-if="!store.unclaimedTasks.length" class="empty-hint">暂无未认领任务</div>
          </div>
        </article>

        <article class="panel task-column">
          <div class="task-column-header">
            <h3>待办</h3>
            <span>{{ store.todoTasks.length }}</span>
          </div>
          <div class="task-list">
            <button
              v-for="task in store.todoTasks"
              :key="task.taskId"
              type="button"
              class="task-card"
              @click="selectTask(task.taskId)"
            >
              <strong>{{ task.taskName }}</strong>
              <span>{{ task.assignee || '未认领' }}</span>
              <span>{{ task.formTitle || task.businessKey || task.processInstanceId }}</span>
            </button>
            <div v-if="!store.todoTasks.length" class="empty-hint">暂无待办任务</div>
          </div>
        </article>

        <article class="panel task-column">
          <div class="task-column-header">
            <h3>已办</h3>
            <span>{{ store.doneTasks.length }}</span>
          </div>
          <div class="task-list compact-list">
            <button
              v-for="task in store.doneTasks"
              :key="task.taskId"
              type="button"
              class="task-card"
              @click="selectTask(task.taskId)"
            >
              <strong>{{ task.taskName }}</strong>
              <span>{{ task.completedAt || task.businessKey || task.processInstanceId }}</span>
            </button>
            <div v-if="!store.doneTasks.length" class="empty-hint">暂无已办任务</div>
          </div>
        </article>

        <article class="panel task-column">
          <div class="task-column-header">
            <h3>我的发起</h3>
            <span>{{ store.initiatedInstances.length }}</span>
          </div>
          <div class="task-list compact-list">
            <div v-for="instance in store.initiatedInstances" :key="instance.processInstanceId" class="instance-card">
              <strong>{{ instance.formTitle || instance.businessKey || instance.processInstanceId }}</strong>
              <span>{{ instance.processDefinitionKey }}</span>
              <span>{{ instance.status }}</span>
            </div>
            <div v-if="!store.initiatedInstances.length" class="empty-hint">暂无我发起的流程</div>
          </div>
        </article>
      </section>

      <aside class="panel task-detail-panel">
        <template v-if="store.taskDetail">
          <div class="task-detail-header">
            <div>
              <h3>{{ store.taskDetail.task.taskName }}</h3>
              <p>{{ store.taskDetail.task.taskStatus }} / {{ store.taskDetail.task.processDefinitionKey }}</p>
            </div>
            <div class="row-actions">
              <button
                v-if="store.taskDetail.task.taskStatus === 'UNCLAIMED'"
                @click="handleClaim(store.taskDetail.task.taskId)"
              >
                认领任务
              </button>
              <button
                v-if="store.taskDetail.task.taskStatus === 'CLAIMED'"
                @click="handleComplete(store.taskDetail.task.taskId)"
              >
                完成任务
              </button>
            </div>
          </div>

          <div class="detail-grid">
            <div><span>任务ID</span><strong>{{ store.taskDetail.task.taskId }}</strong></div>
            <div><span>流程实例</span><strong>{{ store.taskDetail.task.processInstanceId }}</strong></div>
            <div><span>业务Key</span><strong>{{ store.taskDetail.task.businessKey || '-' }}</strong></div>
            <div><span>办理人</span><strong>{{ store.taskDetail.task.assignee || '-' }}</strong></div>
            <div><span>候选人</span><strong>{{ store.taskDetail.candidateUsers.join(', ') || '-' }}</strong></div>
            <div><span>标题</span><strong>{{ store.taskDetail.task.formTitle || '-' }}</strong></div>
          </div>

          <section class="task-detail-section">
            <h4>实例摘要</h4>
            <div class="detail-grid">
              <div><span>状态</span><strong>{{ store.taskDetail.instance.status }}</strong></div>
              <div><span>发起人</span><strong>{{ store.taskDetail.instance.starterId || '-' }}</strong></div>
              <div><span>发起部门</span><strong>{{ store.taskDetail.instance.starterDeptId || '-' }}</strong></div>
              <div><span>开始时间</span><strong>{{ store.taskDetail.instance.startTime || '-' }}</strong></div>
            </div>
          </section>

          <section class="task-detail-section">
            <h4>流程变量</h4>
            <textarea :value="detailVariablesText" class="code-block small-code-block" readonly />
          </section>
        </template>

        <template v-else>
          <div class="empty-detail">
            <h3>任务详情</h3>
            <p>从左侧任务列表选择一项，查看详情并执行认领或完成。</p>
          </div>
        </template>
      </aside>
    </section>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useWorkflowStore } from '../stores/workflow'

const store = useWorkflowStore()
const userId = ref('system-admin')

const detailVariablesText = computed(() => JSON.stringify(store.taskDetail?.variables ?? {}, null, 2))

async function refreshWorkbench() {
  await store.fetchTaskWorkbench(userId.value || undefined)
}

async function selectTask(taskId: string) {
  await store.fetchTaskDetail(taskId)
}

async function handleClaim(taskId: string) {
  await store.claimTask(taskId, userId.value || undefined)
  await store.fetchTaskDetail(taskId)
}

async function handleComplete(taskId: string) {
  await store.completeTask(taskId, userId.value || undefined, { approved: true, handledBy: userId.value || 'system-admin' })
}

onMounted(async () => {
  await refreshWorkbench()
})
</script>
