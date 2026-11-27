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
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="onDiagnose(row)">诊断</el-button>
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

    <el-drawer v-model="diagVisible" :title="diagTitle" size="420px" destroy-on-close>
      <div v-loading="diagLoading" class="diag">
        <template v-if="analysis">
          <el-descriptions :column="1" border>
            <el-descriptions-item label="综合评分">{{ format(analysis.score, 2) }}</el-descriptions-item>
            <el-descriptions-item label="评级">
              <el-tag :type="ratingType(analysis.rating)">{{ analysis.rating }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="估值评价">{{ analysis.valuation || '—' }}</el-descriptions-item>
          </el-descriptions>
          <div class="diag-section-title">风险提示</div>
          <ul v-if="analysis.risks && analysis.risks.length" class="diag-list">
            <li v-for="(risk, index) in analysis.risks" :key="index">{{ risk }}</li>
          </ul>
          <el-empty v-else description="暂无风险提示" :image-size="70" />
        </template>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { addWatch, analyzeStock, listWatchlist, removeWatch } from '@/api/stock'

const loading = ref(false)
const adding = ref(false)
const code = ref('')
const rows = ref([])

const diagVisible = ref(false)
const diagLoading = ref(false)
const diagTitle = ref('个股诊断')
const analysis = ref(null)

const RATING_TYPES = {
  强烈推荐: 'success',
  推荐: 'success',
  中性: 'info',
  谨慎: 'warning',
  回避: 'danger'
}

function ratingType(rating) {
  return RATING_TYPES[rating] || 'info'
}

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

async function onDiagnose(row) {
  if (diagLoading.value) return
  diagVisible.value = true
  diagLoading.value = true
  analysis.value = null
  diagTitle.value = `${row.stock.name}（${row.stock.code}）个股诊断`
  try {
    analysis.value = await analyzeStock(row.stock.code)
  } catch (e) {
    ElMessage.error('诊断失败，请稍后重试')
    diagVisible.value = false
  } finally {
    diagLoading.value = false
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

.diag {
  min-height: 120px;
}

.diag-section-title {
  margin: 18px 0 8px;
  font-weight: 600;
}

.diag-list {
  margin: 0;
  padding-left: 18px;
  color: #606266;
  line-height: 1.9;
}
</style>
