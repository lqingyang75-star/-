<template>
  <div>
    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" @submit.prevent>
        <el-form-item label="股票代码">
          <el-input
            v-model="codes"
            placeholder="多个代码用英文逗号分隔，如 600519,000651"
            clearable
            style="width: 320px"
            @keyup.enter="load"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="load">开始对比</el-button>
          <el-button @click="onReset">重置</el-button>
        </el-form-item>
      </el-form>
      <div class="hint">对同一批股票分别按价值/成长/均衡三种策略打分，留空则对比内置股票池。</div>
    </el-card>

    <el-card shadow="never">
      <el-table v-loading="loading" :data="rows" border stripe empty-text="没有可对比的数据">
        <el-table-column type="index" label="#" width="56" />
        <el-table-column prop="stock.code" label="代码" width="100" />
        <el-table-column prop="stock.name" label="名称" width="120" />
        <el-table-column
          v-for="col in strategyColumns"
          :key="col.code"
          :label="col.label"
          width="110"
          sortable
          :sort-method="(a, b) => scoreOf(a, col.code) - scoreOf(b, col.code)"
        >
          <template #default="{ row }">{{ formatScore(scoreOf(row, col.code)) }}</template>
        </el-table-column>
        <el-table-column label="最优策略" width="110">
          <template #default="{ row }">
            <el-tag :type="bestTag(bestOf(row))" effect="plain">{{ bestOf(row) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="对比说明" min-width="240" show-overflow-tooltip>
          <template #default="{ row }">{{ summaryOf(row) }}</template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { compareStocks } from '@/api/stock'

const loading = ref(false)
const codes = ref('')
const rows = ref([])

// 与后端 Strategy 枚举顺序一致：价值/成长/均衡
const strategyColumns = [
  { code: 'value', label: '价值型' },
  { code: 'growth', label: '成长型' },
  { code: 'balanced', label: '均衡型' }
]

function scoreOf(row, strategyCode) {
  const hit = (row.strategies || []).find((s) => s.strategy === strategyCode)
  return hit ? hit.score : 0
}

function bestOf(row) {
  let best = null
  for (const s of row.strategies || []) {
    if (!best || s.score > best.score) best = s
  }
  return best ? best.strategyLabel : '—'
}

function summaryOf(row) {
  const list = row.strategies || []
  if (!list.length) return '—'
  return list.map((s) => `${s.strategyLabel} ${Number(s.score).toFixed(2)}（${s.reason}）`).join('；')
}

function formatScore(value) {
  return Number.isFinite(Number(value)) ? Number(value).toFixed(2) : '—'
}

function bestTag(best) {
  const map = { 价值型: 'warning', 成长型: 'success', 均衡型: 'primary' }
  return map[best] || 'info'
}

async function load() {
  if (loading.value) return
  loading.value = true
  try {
    const keyword = codes.value.trim()
    rows.value = await compareStocks(keyword || undefined)
  } catch (e) {
    rows.value = []
  } finally {
    loading.value = false
  }
}

function onReset() {
  codes.value = ''
  load()
}

onMounted(load)
</script>

<style scoped>
.filter-card {
  margin-bottom: 16px;
}

.filter-card :deep(.el-form-item) {
  margin-bottom: 0;
}

.hint {
  margin-top: 10px;
  font-size: 12px;
  color: #909399;
}
</style>
