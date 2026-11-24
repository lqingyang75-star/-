<template>
  <div>
    <el-card shadow="never" class="tool-card">
      <el-form :inline="true" @submit.prevent>
        <el-form-item label="股票代码">
          <el-input
            v-model="code"
            placeholder="输入 6 位代码，如 600519"
            clearable
            style="width: 220px"
            @keyup.enter="onAdd"
          />
        </el-form-item>
        <el-form-item>
          <el-button v-permission="'stock:watch:add'" type="primary" :loading="adding" @click="onAdd">
            加入自选
          </el-button>
          <el-button :loading="loading" @click="load">刷新行情</el-button>
        </el-form-item>
      </el-form>
      <div class="hint">自选列表按加入顺序展示，行情与评分在每次查询时实时刷新，最多 50 只。</div>
    </el-card>

    <el-card shadow="never">
      <el-table v-loading="loading" :data="rows" border stripe empty-text="还没有自选股，输入股票代码加入">
        <el-table-column type="index" label="#" width="56" />
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
        <el-table-column prop="reason" label="命中理由" min-width="220" show-overflow-tooltip />
        <el-table-column label="操作" width="110" fixed="right">
          <template #default="{ row }">
            <el-button
              v-permission="'stock:watch:delete'"
              link
              type="danger"
              @click="onRemove(row)"
            >
              移除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { addWatch, listWatchlist, removeWatch } from '@/api/stock'

const loading = ref(false)
const adding = ref(false)
const code = ref('')
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
    rows.value = await listWatchlist()
  } catch (e) {
    rows.value = []
  } finally {
    loading.value = false
  }
}

async function onAdd() {
  if (adding.value) return
  const value = code.value.trim()
  if (!/^\d{6}$/.test(value)) {
    ElMessage.warning('请输入 6 位数字股票代码')
    return
  }
  adding.value = true
  try {
    await addWatch(value)
    ElMessage.success('已加入自选')
    code.value = ''
    await load()
  } finally {
    adding.value = false
  }
}

async function onRemove(row) {
  try {
    await ElMessageBox.confirm(`确定把 ${row.stock.name}（${row.stock.code}）移出自选吗？`, '移除自选', {
      type: 'warning'
    })
  } catch (e) {
    return
  }
  await removeWatch(row.stock.code)
  ElMessage.success('已移除')
  await load()
}

onMounted(load)
</script>

<style scoped>
.tool-card {
  margin-bottom: 16px;
}

.tool-card :deep(.el-form-item) {
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
