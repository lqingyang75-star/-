<template>
  <div>
    <!-- 统计卡片 -->
    <el-row :gutter="16" class="stat-row">
      <el-col :span="8">
        <el-card shadow="never">
          <div class="stat-label">收入合计</div>
          <div class="stat-value income">{{ formatMoney(stat.totalIncome) }}</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never">
          <div class="stat-label">支出合计</div>
          <div class="stat-value expense">{{ formatMoney(stat.totalExpense) }}</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never">
          <div class="stat-label">结余</div>
          <div class="stat-value" :class="Number(stat.balance) >= 0 ? 'income' : 'expense'">
            {{ formatMoney(stat.balance) }}
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 筛选区 -->
    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" :model="filters">
        <el-form-item label="类型">
          <el-select v-model="filters.type" placeholder="全部" clearable style="width: 110px">
            <el-option label="收入" :value="1" />
            <el-option label="支出" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="filters.categoryId" placeholder="全部" clearable style="width: 140px">
            <el-option
              v-for="item in filterCategories"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="日期">
          <el-date-picker
            v-model="filters.dateRange"
            type="daterange"
            value-format="YYYY-MM-DD"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            style="width: 240px"
          />
        </el-form-item>
        <el-form-item label="关键字">
          <el-input
            v-model="filters.keyword"
            placeholder="备注或股票代码"
            clearable
            style="width: 160px"
            @keyup.enter="onSearch"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="onSearch">查询</el-button>
          <el-button @click="onReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 列表 -->
    <el-card shadow="never">
      <div class="toolbar">
        <el-button v-permission="'fin:record:add'" type="primary" @click="openCreate">新增记录</el-button>
      </div>
      <el-table v-loading="loading" :data="rows" border stripe>
        <el-table-column prop="recordDate" label="日期" width="110" />
        <el-table-column label="类型" width="80">
          <template #default="{ row }">
            <el-tag :type="row.type === 1 ? 'success' : 'danger'" size="small">
              {{ row.type === 1 ? '收入' : '支出' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="categoryName" label="分类" width="110" />
        <el-table-column label="金额" width="130" align="right">
          <template #default="{ row }">
            <span :class="row.type === 1 ? 'income' : 'expense'">
              {{ row.type === 1 ? '+' : '-' }}{{ formatMoney(row.amount) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="交易明细" min-width="200">
          <template #default="{ row }">
            <span v-if="row.tradeSide">
              {{ row.stockCode }} {{ row.tradeSide === 'BUY' ? '买入' : '卖出' }}
              {{ row.quantity }} 股 @ {{ row.tradePrice }}
              <span class="fee">费用 {{ formatMoney(feeOf(row)) }}</span>
            </span>
            <span v-else class="muted">—</span>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="160" show-overflow-tooltip />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button v-permission="'fin:record:edit'" link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button v-permission="'fin:record:delete'" link type="danger" @click="onDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-model:current-page="filters.page"
        v-model:page-size="filters.size"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        class="pager"
        @current-change="loadRecords"
        @size-change="onSizeChange"
      />
    </el-card>

    <!-- 新增/编辑 -->
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑记录' : '新增记录'" width="620px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="记录模式">
          <el-radio-group v-model="form.mode">
            <el-radio-button label="normal">普通收支</el-radio-button>
            <el-radio-button label="trade">证券交易</el-radio-button>
          </el-radio-group>
        </el-form-item>

        <template v-if="form.mode === 'trade'">
          <el-form-item label="股票代码">
            <el-input v-model="form.stockCode" placeholder="如 600519" clearable />
          </el-form-item>
          <el-form-item label="买卖方向">
            <el-radio-group v-model="form.tradeSide" @change="onTradeSideChange">
              <el-radio label="BUY">买入</el-radio>
              <el-radio label="SELL">卖出</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="成交数量">
            <el-input-number v-model="form.quantity" :min="1" :step="100" style="width: 180px" />
            <span class="unit">股</span>
          </el-form-item>
          <el-form-item label="成交价">
            <el-input-number v-model="form.tradePrice" :min="0" :precision="3" :step="0.01" style="width: 180px" />
            <span class="unit">元</span>
          </el-form-item>
          <el-form-item label="佣金">
            <el-input-number v-model="form.commission" :min="0" :precision="2" :step="1" style="width: 180px" />
          </el-form-item>
          <el-form-item label="印花税">
            <el-input-number
              v-model="form.stampDuty"
              :min="0"
              :precision="2"
              :step="1"
              :disabled="form.tradeSide === 'BUY'"
              style="width: 180px"
            />
            <span v-if="form.tradeSide === 'BUY'" class="unit muted">买入不收印花税</span>
          </el-form-item>
          <el-form-item label="过户费">
            <el-input-number v-model="form.transferFee" :min="0" :precision="2" :step="0.5" style="width: 180px" />
          </el-form-item>
          <el-form-item label="记账金额">
            <span class="amount-preview">{{ previewAmount }}</span>
            <span class="unit muted">成交额{{ form.tradeSide === 'BUY' ? '加' : '减' }}费用，由系统计算</span>
          </el-form-item>
        </template>

        <template v-else>
          <el-form-item label="收支类型" prop="type">
            <el-radio-group v-model="form.type" @change="onTypeChange">
              <el-radio :label="1">收入</el-radio>
              <el-radio :label="2">支出</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="金额" prop="amount">
            <el-input-number v-model="form.amount" :min="0.01" :precision="2" :step="10" style="width: 180px" />
            <span class="unit">元</span>
          </el-form-item>
        </template>

        <el-form-item label="分类" prop="categoryId">
          <el-select v-model="form.categoryId" placeholder="请选择分类" style="width: 220px">
            <el-option v-for="item in formCategories" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="记账日期" prop="recordDate">
          <el-date-picker v-model="form.recordDate" type="date" value-format="YYYY-MM-DD" style="width: 220px" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" maxlength="255" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="onSubmit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  createRecord,
  deleteRecord,
  listCategories,
  pageRecords,
  statRecords,
  updateRecord
} from '@/api/finance'

const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const formRef = ref(null)
const rows = ref([])
const total = ref(0)
const categories = ref([])
const stat = reactive({ totalIncome: 0, totalExpense: 0, balance: 0 })

const filters = reactive({
  type: null,
  categoryId: null,
  dateRange: [],
  keyword: '',
  page: 1,
  size: 10
})

const form = reactive(newForm())

function newForm() {
  return {
    id: null,
    mode: 'normal',
    type: 2,
    amount: null,
    categoryId: null,
    recordDate: today(),
    remark: '',
    stockCode: '',
    tradeSide: 'BUY',
    quantity: 100,
    tradePrice: null,
    commission: 5,
    stampDuty: 0,
    transferFee: 0
  }
}

function today() {
  const d = new Date()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${d.getFullYear()}-${m}-${day}`
}

const rules = {
  type: [{ required: true, message: '请选择收支类型', trigger: 'change' }],
  amount: [{ required: true, message: '请输入金额', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  recordDate: [{ required: true, message: '请选择日期', trigger: 'change' }]
}

// 交易模式下收支类型由买卖方向决定
const effectiveType = computed(() => {
  if (form.mode === 'trade') {
    return form.tradeSide === 'BUY' ? 2 : 1
  }
  return form.type
})

const filterCategories = computed(() =>
  filters.type ? categories.value.filter((c) => c.type === filters.type) : categories.value
)

const formCategories = computed(() => categories.value.filter((c) => c.type === effectiveType.value))

const previewAmount = computed(() => {
  const turnover = Number(form.tradePrice || 0) * Number(form.quantity || 0)
  const fees = Number(form.commission || 0) + Number(form.stampDuty || 0) + Number(form.transferFee || 0)
  const value = form.tradeSide === 'BUY' ? turnover + fees : turnover - fees
  return formatMoney(value)
})

function formatMoney(value) {
  const num = Number(value || 0)
  return num.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

function feeOf(row) {
  return Number(row.commission || 0) + Number(row.stampDuty || 0) + Number(row.transferFee || 0)
}

function buildQuery() {
  const [startDate, endDate] = filters.dateRange || []
  return {
    type: filters.type || undefined,
    categoryId: filters.categoryId || undefined,
    startDate: startDate || undefined,
    endDate: endDate || undefined,
    keyword: filters.keyword || undefined,
    page: filters.page,
    size: filters.size
  }
}

async function loadCategories() {
  categories.value = await listCategories()
}

async function loadRecords() {
  loading.value = true
  try {
    const data = await pageRecords(buildQuery())
    rows.value = data.items || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

async function loadStat() {
  const data = await statRecords(buildQuery())
  Object.assign(stat, data)
}

function onSearch() {
  filters.page = 1
  loadRecords()
  loadStat()
}

function onReset() {
  filters.type = null
  filters.categoryId = null
  filters.dateRange = []
  filters.keyword = ''
  filters.page = 1
  loadRecords()
  loadStat()
}

function onSizeChange() {
  filters.page = 1
  loadRecords()
}

function onTypeChange() {
  // 类型切换后原分类可能不匹配，清空重选
  if (!formCategories.value.some((c) => c.id === form.categoryId)) {
    form.categoryId = null
  }
}

function onTradeSideChange() {
  if (form.tradeSide === 'BUY') {
    form.stampDuty = 0
  }
  if (!formCategories.value.some((c) => c.id === form.categoryId)) {
    form.categoryId = null
  }
}

function resetForm(row) {
  Object.assign(form, newForm())
  if (row) {
    form.id = row.id
    form.recordDate = row.recordDate
    form.remark = row.remark || ''
    form.categoryId = row.categoryId
    if (row.tradeSide) {
      form.mode = 'trade'
      form.stockCode = row.stockCode
      form.tradeSide = row.tradeSide
      form.quantity = row.quantity
      form.tradePrice = row.tradePrice
      form.commission = Number(row.commission || 0)
      form.stampDuty = Number(row.stampDuty || 0)
      form.transferFee = Number(row.transferFee || 0)
    } else {
      form.mode = 'normal'
      form.type = row.type
      form.amount = Number(row.amount)
    }
  }
}

function openCreate() {
  resetForm(null)
  dialogVisible.value = true
}

function openEdit(row) {
  resetForm(row)
  dialogVisible.value = true
}

async function onSubmit() {
  if (saving.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  const base = {
    categoryId: form.categoryId,
    recordDate: form.recordDate,
    remark: form.remark || null
  }
  let payload
  if (form.mode === 'trade') {
    payload = {
      ...base,
      stockCode: form.stockCode || null,
      tradeSide: form.tradeSide,
      quantity: form.quantity,
      tradePrice: form.tradePrice,
      commission: form.commission || 0,
      stampDuty: form.tradeSide === 'SELL' ? form.stampDuty || 0 : 0,
      transferFee: form.transferFee || 0
    }
  } else {
    payload = { ...base, type: form.type, amount: form.amount }
  }

  saving.value = true
  try {
    if (form.id) {
      await updateRecord(form.id, payload)
      ElMessage.success('已保存')
    } else {
      await createRecord(payload)
      ElMessage.success('已新增')
    }
    dialogVisible.value = false
    await Promise.all([loadRecords(), loadStat()])
  } finally {
    saving.value = false
  }
}

async function onDelete(row) {
  try {
    await ElMessageBox.confirm(`确定删除 ${row.recordDate} 的这条记录吗？`, '确认删除', { type: 'warning' })
  } catch (e) {
    return
  }
  await deleteRecord(row.id)
  ElMessage.success('已删除')
  await Promise.all([loadRecords(), loadStat()])
}

onMounted(async () => {
  await loadCategories()
  await Promise.all([loadRecords(), loadStat()])
})
</script>

<style scoped>
.stat-row {
  margin-bottom: 16px;
}

.stat-label {
  font-size: 13px;
  color: #909399;
}

.stat-value {
  margin-top: 8px;
  font-size: 22px;
  font-weight: 600;
}

.income {
  color: #16a34a;
}

.expense {
  color: #dc2626;
}

.filter-card {
  margin-bottom: 16px;
}

.filter-card :deep(.el-form-item) {
  margin-bottom: 0;
}

.toolbar {
  margin-bottom: 12px;
}

.pager {
  margin-top: 14px;
  justify-content: flex-end;
}

.fee {
  margin-left: 6px;
  color: #909399;
  font-size: 12px;
}

.muted {
  color: #c0c4cc;
}

.unit {
  margin-left: 8px;
  font-size: 12px;
  color: #909399;
}

.amount-preview {
  font-size: 16px;
  font-weight: 600;
  color: #409eff;
}
</style>
