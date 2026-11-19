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
    </el-card>

    <el-row :gutter="16">
      <el-col :span="12">
        <el-card shadow="never" class="chart-card">
          <template #header>近 7 天收支</template>
          <div ref="weeklyRef" v-loading="loading" class="chart" />
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never" class="chart-card">
          <template #header>分类占比</template>
          <div ref="pieRef" v-loading="loading" class="chart" />
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
let charts = []

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

function moneyAxis() {
  return { type: 'value', axisLabel: { formatter: (v) => `${v}` } }
}

function lineOption(points) {
  return {
    tooltip: { trigger: 'axis' },
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
    tooltip: { trigger: 'axis' },
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
    legend: { bottom: 0 },
    series: [
      {
        type: 'pie',
        radius: ['40%', '65%'],
        center: ['50%', '45%'],
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

function onResize() {
  charts.forEach((chart) => chart.resize())
}

async function loadCharts() {
  loading.value = true
  try {
    const [trend, weekly, pie] = await Promise.all([
      monthlyTrend(6),
      weeklyBar(),
      categoryPie({})
    ])
    renderChart(trendRef.value, lineOption(trend || []))
    renderChart(weeklyRef.value, barOption(weekly || []))
    renderChart(pieRef.value, pieOption(pie || []))
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
</style>
