<template>
  <div>
    <el-row :gutter="16">
      <el-col :span="8">
        <el-card shadow="never">
          <div class="stat-label">当前登录人</div>
          <div class="stat-value">{{ store.userInfo?.realName || store.userInfo?.username }}</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never">
          <div class="stat-label">拥有权限码</div>
          <div class="stat-value">{{ store.perms.length }}</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never">
          <div class="stat-label">可见菜单</div>
          <div class="stat-value">{{ menuCount }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="chart-card">
      <template #header>近 6 个月收支趋势</template>
      <div ref="trendRef" v-loading="loading" class="chart" />
      <el-empty v-if="!loading && isEmpty(trendData)" description="暂无收支趋势数据" :image-size="80" />
    </el-card>

    <el-row :gutter="16">
      <el-col :span="12">
        <el-card shadow="never" class="chart-card">
          <template #header>近 7 天收支</template>
          <div ref="weeklyRef" v-loading="loading" class="chart" />
          <el-empty v-if="!loading && isEmpty(weeklyData)" description="近 7 天暂无收支记录" :image-size="80" />
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never" class="chart-card">
          <template #header>分类占比</template>
          <div ref="pieRef" v-loading="loading" class="chart" />
          <el-empty v-if="!loading && isEmpty(pieData)" description="暂无分类数据" :image-size="80" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import * as echarts from 'echarts'
import { useUserStore } from '@/store/user'
import { categoryPie, monthlyTrend, weeklyBar } from '@/api/finance'

const store = useUserStore()
const loading = ref(false)
const trendRef = ref(null)
const weeklyRef = ref(null)
const pieRef = ref(null)
const trendData = ref([])
const weeklyData = ref([])
const pieData = ref([])
let charts = []
let resizeTimer = null

const menuCount = computed(() => {
  let count = 0
  const walk = (nodes) => {
    (nodes || []).forEach((node) => {
      if (node.type === 2) count += 1
      if (node.children && node.children.length) walk(node.children)
    })
  }
  walk(store.menuTree)
  return count
})

// 空数据判断：数组为空，或所有数值项加起来都是 0
function isEmpty(list) {
  if (!list || !list.length) return true
  return list.every((item) => !Number(item.income) && !Number(item.expense) && !Number(item.value))
}

function moneyAxis() {
  return { type: 'value', axisLabel: { formatter: (v) => Number(v).toLocaleString('zh-CN') } }
}

function lineOption(points) {
  return {
    tooltip: {
      trigger: 'axis',
      valueFormatter: (v) => `${Number(v || 0).toLocaleString('zh-CN')} 元`
    },
    legend: { data: ['收入', '支出'], right: 0 },
    grid: { left: 10, right: 10, bottom: 0, top: 36, containLabel: true },
    xAxis: { type: 'category', data: points.map((p) => p.label) },
    yAxis: moneyAxis(),
    series: [
      {
        name: '收入',
        type: 'line',
        smooth: true,
        showSymbol: false,
        data: points.map((p) => Number(p.income || 0)),
        itemStyle: { color: '#16a34a' }
      },
      {
        name: '支出',
        type: 'line',
        smooth: true,
        showSymbol: false,
        data: points.map((p) => Number(p.expense || 0)),
        itemStyle: { color: '#dc2626' }
      }
    ]
  }
}

function barOption(points) {
  return {
    tooltip: {
      trigger: 'axis',
      valueFormatter: (v) => `${Number(v || 0).toLocaleString('zh-CN')} 元`
    },
    legend: { data: ['收入', '支出'], right: 0 },
    grid: { left: 10, right: 10, bottom: 0, top: 36, containLabel: true },
    xAxis: { type: 'category', data: points.map((p) => p.label) },
    yAxis: moneyAxis(),
    series: [
      { name: '收入', type: 'bar', data: points.map((p) => Number(p.income || 0)), itemStyle: { color: '#16a34a' } },
      { name: '支出', type: 'bar', data: points.map((p) => Number(p.expense || 0)), itemStyle: { color: '#dc2626' } }
    ]
  }
}

function pieOption(points) {
  return {
    tooltip: { trigger: 'item', formatter: '{b}<br/>{c} 元（{d}%）' },
    legend: { bottom: 0, type: 'scroll' },
    series: [
      {
        type: 'pie',
        radius: ['40%', '60%'],
        center: ['50%', '42%'],
        avoidLabelOverlap: true,
        label: { formatter: '{b}\n{d}%' },
        data: points.map((p) => ({ name: p.name, value: Number(p.value || 0) }))
      }
    ]
  }
}

function renderChart(container, option) {
  if (!container) return
  const chart = echarts.init(container)
  chart.setOption(option)
  charts.push(chart)
}

// 窗口连续拖动会高频触发 resize，加 200ms 防抖
function onResize() {
  if (resizeTimer) clearTimeout(resizeTimer)
  resizeTimer = setTimeout(() => {
    charts.forEach((chart) => chart.resize())
    resizeTimer = null
  }, 200)
}

async function loadCharts() {
  loading.value = true
  try {
    const [trend, weekly, pie] = await Promise.all([
      monthlyTrend(6),
      weeklyBar(),
      categoryPie({})
    ])
    trendData.value = trend || []
    weeklyData.value = weekly || []
    pieData.value = pie || []
    renderChart(trendRef.value, lineOption(trendData.value))
    renderChart(weeklyRef.value, barOption(weeklyData.value))
    renderChart(pieRef.value, pieOption(pieData.value))
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  await loadCharts()
  window.addEventListener('resize', onResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', onResize)
  if (resizeTimer) clearTimeout(resizeTimer)
  charts.forEach((chart) => chart.dispose())
  charts = []
})
</script>

<style scoped>
.stat-label {
  font-size: 13px;
  color: #909399;
}

.stat-value {
  margin-top: 8px;
  font-size: 24px;
  font-weight: 600;
}

.chart-card {
  margin-top: 16px;
}

.chart {
  height: 300px;
}

.chart-card :deep(.el-empty) {
  padding: 24px 0;
}
</style>
