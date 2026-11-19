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
          <el-button type="primary" :loading="loading" @click="load">查询行情</el-button>
          <el-button @click="onReset">重置</el-button>
        </el-form-item>
      </el-form>
      <div class="hint">留空则查询内置股票池；行情取自实时接口，首次查询可能稍慢。</div>
    </el-card>

    <el-card shadow="never">
      <el-table v-loading="loading" :data="rows" border stripe empty-text="没有查询到行情数据">
        <el-table-column prop="stock.code" label="代码" width="100" />
        <el-table-column prop="stock.name" label="名称" width="120" />
        <el-table-column prop="stock.price" label="最新价" width="110" sortable>
          <template #default="{ row }">{{ format(row.stock.price, 2) }}</template>
        </el-table-column>
        <el-table-column prop="stock.changePercent" label="涨跌幅" width="110" sortable>
          <template #default="{ row }">
            <span :class="changeClass(row.stock.changePercent)">
              {{ format(row.stock.changePercent, 2) }}%
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="stock.pe" label="市盈率" width="100" sortable>
          <template #default="{ row }">{{ format(row.stock.pe, 2) }}</template>
        </el-table-column>
        <el-table-column prop="stock.pb" label="市净率" width="100" sortable>
          <template #default="{ row }">{{ format(row.stock.pb, 2) }}</template>
        </el-table-column>
        <el-table-column prop="stock.roe" label="ROE(%)" width="110" sortable>
          <template #default="{ row }">{{ format(row.stock.roe, 2) }}</template>
        </el-table-column>
        <el-table-column prop="score" label="综合评分" width="110" sortable>
          <template #default="{ row }">{{ format(row.score, 2) }}</template>
        </el-table-column>
        <el-table-column prop="reason" label="命中理由" min-width="240" show-overflow-tooltip />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { pickStocks } from '@/api/stock'

const loading = ref(false)
const codes = ref('')
const rows = ref([])

function format(value, digits) {
  return Number.isFinite(Number(value)) ? Number(value).toFixed(digits) : '—'
}

function changeClass(value) {
  const num = Number(value)
  if (!Number.isFinite(num) || num === 0) return 'flat'
  return num > 0 ? 'up' : 'down'
}

async function load() {
  if (loading.value) return
  loading.value = true
  try {
    const keyword = codes.value.trim()
    rows.value = await pickStocks({
      limit: 20,
      codes: keyword || undefined
    })
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

.up {
  color: #f56c6c;
}

.down {
  color: #67c23a;
}

.flat {
  color: #606266;
}
</style>
