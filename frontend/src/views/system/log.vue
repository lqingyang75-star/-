<template>
  <div>
    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" :model="filters">
        <el-form-item label="关键字">
          <el-input
            v-model="filters.keyword"
            placeholder="操作人 / 动作 / 接口"
            clearable
            style="width: 200px"
            @keyup.enter="onSearch"
          />
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
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="onSearch">查询</el-button>
          <el-button @click="onReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never">
      <el-table v-loading="loading" :data="rows" border stripe empty-text="暂无操作日志">
        <el-table-column type="expand">
          <template #default="{ row }">
            <div class="detail">
              <div><span class="label">请求参数：</span>{{ row.params || '—' }}</div>
              <div v-if="row.errorMsg"><span class="label">失败原因：</span>{{ row.errorMsg }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="操作时间" width="180" />
        <el-table-column prop="username" label="操作人" width="110" />
        <el-table-column prop="module" label="模块" width="110" />
        <el-table-column prop="action" label="动作" width="130" />
        <el-table-column prop="uri" label="接口" min-width="220" show-overflow-tooltip />
        <el-table-column label="耗时" width="90">
          <template #default="{ row }">{{ row.durationMs }} ms</template>
        </el-table-column>
        <el-table-column label="结果" width="90">
          <template #default="{ row }">
            <el-tag :type="row.success ? 'success' : 'danger'" size="small">
              {{ row.success ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="ip" label="IP" width="130" />
      </el-table>
      <el-pagination
        v-model:current-page="filters.page"
        v-model:page-size="filters.size"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        class="pager"
        @current-change="loadLogs"
        @size-change="onSizeChange"
      />
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { pageOperLogs } from '@/api/system'

const loading = ref(false)
const rows = ref([])
const total = ref(0)

const filters = reactive({
  keyword: '',
  dateRange: [],
  page: 1,
  size: 10
})

function buildQuery() {
  const [startDate, endDate] = filters.dateRange || []
  return {
    keyword: filters.keyword || undefined,
    startDate: startDate || undefined,
    endDate: endDate || undefined,
    page: filters.page,
    size: filters.size
  }
}

async function loadLogs() {
  loading.value = true
  try {
    const data = await pageOperLogs(buildQuery())
    rows.value = data.items || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

function onSearch() {
  filters.page = 1
  loadLogs()
}

function onReset() {
  filters.keyword = ''
  filters.dateRange = []
  filters.page = 1
  loadLogs()
}

function onSizeChange() {
  filters.page = 1
  loadLogs()
}

onMounted(loadLogs)
</script>

<style scoped>
.filter-card {
  margin-bottom: 16px;
}

.filter-card :deep(.el-form-item) {
  margin-bottom: 0;
}

.pager {
  margin-top: 14px;
  justify-content: flex-end;
}

.detail {
  padding: 4px 16px;
  font-size: 13px;
  line-height: 1.9;
  color: #606266;
  word-break: break-all;
}

.label {
  color: #909399;
}
</style>
