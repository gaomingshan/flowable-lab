<template>
  <div class="pm-page">
    <header class="pm-header">
      <div class="pm-header-info">
        <h2>流程编辑器</h2>
        <p v-if="store.processInfo.id">编辑: {{ store.processInfo.id }}</p>
        <p v-else>创建新流程定义</p>
      </div>
      <div class="pm-header-actions">
        <el-button type="primary" :loading="store.saving" @click="handleSave">
          <el-icon><Upload /></el-icon> 保存并部署
        </el-button>
        <el-button @click="handleNew" :disabled="store.saving">
          <el-icon><Plus /></el-icon> 新建
        </el-button>
        <el-upload :show-file-list="false" accept=".json" :before-upload="handleImportJson" :disabled="store.saving">
          <el-button><el-icon><FolderOpened /></el-icon> 打开 JSON</el-button>
        </el-upload>
        <el-button @click="handleDownload" :disabled="store.saving">
          <el-icon><Download /></el-icon> 下载
        </el-button>
      </div>
    </header>
    <EditorCanvas />
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Upload, Plus, FolderOpened, Download } from '@element-plus/icons-vue'
import { deployBpmn } from '@/api/process'
import { useEditorStore } from '@/editor/stores/editorStore'
import EditorCanvas from '@/editor/EditorCanvas.vue'

const route = useRoute()
const router = useRouter()
const store = useEditorStore()

async function handleSave() {
  store.saving = true
  try {
    const json = store.toProcessJson()
    const nameResult = await ElMessageBox.prompt('请输入部署名称', '部署', {
      inputValue: store.processInfo.name || '未命名流程',
      inputPlaceholder: '部署名称',
    })
    if (nameResult.value) {
      await deployBpmn(nameResult.value, JSON.stringify(json))
      ElMessage.success('部署成功')
      router.push('/process/definition')
    }
  } catch (err: any) {
    if (err !== 'cancel' && err !== 'close') {
      ElMessage.error('保存失败: ' + (err.message || err))
    }
  } finally {
    store.saving = false
  }
}

function handleNew() {
  store.setProcessInfo({ id: 'Process_1', name: '' })
  store.nodes = []
  store.edges = []
  store.clearSelection()
}

function handleImportJson(file: File): boolean {
  const reader = new FileReader()
  reader.onload = (e) => {
    try {
      const json = JSON.parse(e.target?.result as string)
      if (!json || !json.nodes) throw new Error('缺少 nodes 字段')
      store.loadFromProcessJson(json)
      ElMessage.success('导入成功')
    } catch (err: any) {
      ElMessage.error('JSON 导入失败: ' + (err.message || err))
    }
  }
  reader.readAsText(file)
  return false
}

function handleDownload() {
  const json = store.toProcessJson()
  const blob = new Blob([JSON.stringify(json, null, 2)], { type: 'application/json' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `${store.processInfo.id || 'process'}.json`
  a.click()
  URL.revokeObjectURL(url)
}

async function loadDefinition(id: string) {
  store.saving = true
  try {
    const res = await fetch(`/api/bpmn/editor/${id}`)
    if (!res.ok) throw new Error('加载定义失败')
    const result = await res.json()
    if (!result.data || !result.data.nodes) throw new Error('响应格式异常')
    store.loadFromProcessJson(result.data)
  } catch (err: any) {
    ElMessage.error('加载定义失败: ' + (err.message || err))
  } finally {
    store.saving = false
  }
}

onMounted(async () => {
  const defId = route.query.definitionId as string
  if (defId) {
    await loadDefinition(defId)
  }
})
</script>

<style scoped>
.pm-page {
  height: calc(100vh - 100px);
  display: flex;
  flex-direction: column;
}

.pm-header {
  flex-shrink: 0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding: 0 4px;
}

.pm-header-info h2 {
  margin: 0;
  font-size: 18px;
  color: #1d1d1f;
}

.pm-header-info p {
  margin: 4px 0 0;
  color: #909399;
  font-size: 13px;
}

.pm-header-actions {
  display: flex;
  gap: 8px;
  align-items: center;
}
</style>
