<template>
  <section class="page">
    <header class="page-header">
      <div>
        <h2>流程定义</h2>
        <p>创建并查看第一期流程定义。</p>
      </div>
    </header>

    <form class="panel form-grid" @submit.prevent="handleSubmit">
      <input v-model="form.definitionKey" placeholder="definition key" />
      <input v-model="form.definitionName" placeholder="流程名称" />
      <input v-model="form.categoryCode" placeholder="分类编码" />
      <input v-model="form.description" placeholder="描述" />
      <button type="submit">创建流程定义</button>
    </form>

    <section class="panel">
      <h3>定义列表</h3>
      <div v-if="store.loading">加载中...</div>
      <table v-else class="table">
        <thead>
          <tr>
            <th>名称</th>
            <th>Key</th>
            <th>分类</th>
            <th>状态</th>
            <th>草稿版本</th>
            <th>发布版本</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in store.definitions" :key="item.id">
            <td>{{ item.definitionName }}</td>
            <td>{{ item.definitionKey }}</td>
            <td>{{ item.categoryCode }}</td>
            <td>{{ item.status }}</td>
            <td>{{ item.latestDraftVersion }}</td>
            <td>{{ item.latestReleasedVersion }}</td>
            <td>
              <div class="row-actions">
                <button type="button" class="secondary-button" @click="openDesigner(item.id)">设计</button>
                <button type="button" class="secondary-button" @click="createNewVersion(item.id)">新建版本</button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </section>
  </section>
</template>

<script setup lang="ts">
import { onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useWorkflowStore } from '../stores/workflow'

const store = useWorkflowStore()
const router = useRouter()

const form = reactive({
  definitionKey: '',
  definitionName: '',
  categoryCode: '',
  description: '',
})

async function handleSubmit() {
  if (!form.definitionKey || !form.definitionName || !form.categoryCode) {
    return
  }
  await store.addDefinition({ ...form })
  form.definitionKey = ''
  form.definitionName = ''
  form.categoryCode = ''
  form.description = ''
}

function openDesigner(definitionId: string) {
  router.push({ path: '/designer', query: { definitionId } })
}

async function createNewVersion(definitionId: string) {
  await store.addVersion(definitionId, '页面创建新草稿版本')
}

onMounted(() => {
  store.fetchDefinitions()
})
</script>
