<template>
  <section class="page">
    <header class="page-header">
      <div>
        <h2>流程联调台 Demo</h2>
        <p>用于串起流程发起、认领、办理和实例结果查看，不作为正式流程运维台。</p>
      </div>
      <button @click="refreshAll">刷新全链路</button>
    </header>

    <section class="demo-layout">
      <article class="panel demo-start-panel">
        <div class="task-column-header">
          <h3>发起流程</h3>
          <span>{{ store.launchable.length }}</span>
        </div>
        <div class="form-grid">
          <label class="field-block">
            <span>当前用户</span>
            <input v-model="userId" placeholder="默认 system-admin" />
          </label>
          <label class="field-block">
            <span>可发起流程</span>
            <select v-model="startForm.definitionId">
              <option value="">请选择流程</option>
              <option v-for="item in store.launchable" :key="item.definitionId" :value="item.definitionId">
                {{ item.definitionName }} / {{ item.definitionKey }}
              </option>
            </select>
          </label>
          <label class="field-block">
            <span>业务 Key</span>
            <input v-model="startForm.businessKey" placeholder="例如 CLAIM-20260708-002" />
          </label>
          <label class="field-block">
            <span>标题</span>
            <input v-model="startForm.title" placeholder="例如 认领审批联调单" />
          </label>
        </div>
        <label class="field-block">
          <span>流程变量 JSON</span>
          <textarea v-model="startVariablesText" class="code-block small-code-block" />
        </label>
        <div class="toolbar-actions">
          <button @click="handleStartWorkflow">发起流程</button>
        </div>
        <div v-if="store.latestStartedInstance" class="demo-result-card">
          <strong>最近发起实例</strong>
          <span>{{ store.latestStartedInstance.businessKey }}</span>
          <span>{{ store.latestStartedInstance.processDefinitionKey }}</span>
          <span>{{ store.latestStartedInstance.status }}</span>
        </div>
      </article>

      <article class="panel demo-overview-panel">
        <div class="task-column-header">
          <h3>联调概览</h3>
          <span>{{ workbenchCount }}</span>
        </div>
        <div class="overview-grid">
          <div class="overview-card">
            <strong>未认领</strong>
            <span>{{ store.unclaimedTasks.length }}</span>
          </div>
          <div class="overview-card">
            <strong>待办</strong>
            <span>{{ store.todoTasks.length }}</span>
          </div>
          <div class="overview-card">
            <strong>已办</strong>
            <span>{{ store.doneTasks.length }}</span>
          </div>
          <div class="overview-card">
            <strong>我的发起</strong>
            <span>{{ store.initiatedInstances.length }}</span>
          </div>
        </div>
      </article>

      <section class="task-layout demo-task-layout">
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
              <h4>完成变量</h4>
              <textarea v-model="completeVariablesText" class="code-block small-code-block" />
            </section>

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
              <p>从左侧任务列表选择一项，执行认领或完成，观察整条流程链路。</p>
            </div>
          </template>
        </aside>
      </section>
    </section>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useWorkflowStore } from '../stores/workflow'

const store = useWorkflowStore()
const userId = ref('system-admin')
const startForm = reactive({
  definitionId: '',
  businessKey: 'CLAIM-20260708-002',
  title: '流程联调台发起单',
})
const startVariablesText = ref(JSON.stringify({ applicant: 'demo-user', days: 1, note: 'demo start' }, null, 2))
const completeVariablesText = ref(JSON.stringify({ approved: true, comment: 'demo complete' }, null, 2))

const detailVariablesText = computed(() => JSON.stringify(store.taskDetail?.variables ?? {}, null, 2))
const workbenchCount = computed(
  () => store.unclaimedTasks.length + store.todoTasks.length + store.doneTasks.length + store.initiatedInstances.length,
)

async function refreshAll() {
  await Promise.all([store.fetchLaunchable(), store.fetchTaskWorkbench(userId.value || undefined)])
  if (!startForm.definitionId && store.launchable.length > 0) {
    startForm.definitionId = store.launchable[0].definitionId
  }
}

async function selectTask(taskId: string) {
  await store.fetchTaskDetail(taskId)
}

async function handleClaim(taskId: string) {
  await store.claimTask(taskId, userId.value || undefined)
  await store.fetchTaskDetail(taskId)
}

async function handleComplete(taskId: string) {
  const variables = JSON.parse(completeVariablesText.value)
  await store.completeTask(taskId, userId.value || undefined, variables)
}

async function handleStartWorkflow() {
  const variables = JSON.parse(startVariablesText.value)
  await store.startWorkflow(
    {
      definitionId: startForm.definitionId,
      businessKey: startForm.businessKey,
      title: startForm.title,
      variables,
    },
    userId.value || undefined,
  )
  await refreshAll()
}

onMounted(async () => {
  await refreshAll()
})
</script>
