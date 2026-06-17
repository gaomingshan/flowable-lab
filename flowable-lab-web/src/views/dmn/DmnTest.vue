<template>
  <div class="page-container">
    <div class="page-header">
      <h2>DMN 决策测试</h2>
      <p>测试业务决策规则引擎</p>
    </div>

    <el-row :gutter="20">
      <el-col :span="12">
        <el-card>
          <template #header>费用审批决策</template>
          <el-form :model="form" label-width="120px">
            <el-form-item label="采购金额">
              <el-input-number v-model="form.amount" :min="1" :max="100000" />
            </el-form-item>
            <el-form-item label="申请人职级">
              <el-select v-model="form.level">
                <el-option label="普通员工" value="employee" />
                <el-option label="经理" value="manager" />
                <el-option label="总监" value="director" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleEvaluate" :loading="loading">
                执行决策
              </el-button>
            </el-form-item>
          </el-form>

          <el-divider />

          <div>
            <h4>决策规则</h4>
            <el-table :data="rules" stripe size="small">
              <el-table-column prop="condition" label="条件" />
              <el-table-column prop="result" label="结果" />
            </el-table>
          </div>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card v-if="result">
          <template #header>决策结果</template>
          <el-result :status="resultIcon" :title="resultTitle">
            <template #extra>
              <el-descriptions :column="1" border>
                <el-descriptions-item v-for="(v, k) in result" :key="k" :label="k">
                  {{ v }}
                </el-descriptions-item>
              </el-descriptions>
            </template>
          </el-result>
        </el-card>
        <el-card v-else>
          <template #header>决策结果</template>
          <el-empty description="请先执行决策" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { evaluateDecision } from '@/api/dmn'

const form = ref({
  amount: 3000,
  level: 'employee'
})

const loading = ref(false)
const result = ref<Record<string, any> | null>(null)

const rules = [
  { condition: '金额 < 1000', result: '自动通过' },
  { condition: '1000 ≤ 金额 ≤ 5000，经理以上', result: '自动通过' },
  { condition: '1000 ≤ 金额 ≤ 5000，普通员工', result: '主管审批' },
  { condition: '金额 > 5000', result: '总监审批' }
]

const resultIcon = computed(() => {
  if (!result.value) return 'info'
  const decision = result.value['decision']
  if (decision === 'auto_approve') return 'success'
  if (decision === 'need_approval') return 'warning'
  if (decision === 'reject') return 'error'
  return 'info'
})

const resultTitle = computed(() => {
  if (!result.value) return ''
  const decision = result.value['decision']
  if (decision === 'auto_approve') return '自动通过'
  if (decision === 'need_approval') return '需人工审批'
  if (decision === 'reject') return '驳回'
  return '未知'
})

async function handleEvaluate() {
  loading.value = true
  try {
    const res = await evaluateDecision('expense-approval', {
      amount: form.value.amount,
      level: form.value.level
    })
    result.value = res.data || res
  } finally {
    loading.value = false
  }
}
</script>
