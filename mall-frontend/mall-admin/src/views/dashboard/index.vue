<template>
  <div class="dashboard">
    <el-row :gutter="20" class="stat-cards">
      <el-col :span="6" v-for="item in statCards" :key="item.label">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-info">
              <div class="stat-label">{{ item.label }}</div>
              <div class="stat-value">{{ item.value }}</div>
            </div>
            <el-icon :size="40" :style="{ color: item.color }"><component :is="item.icon" /></el-icon>
          </div>
        </el-card>
      </el-col>
    </el-row>
    <el-card class="chart-card">
      <template #header>近7天销售趋势</template>
      <div ref="chartRef" style="height: 400px;"></div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { getDashboard } from '@/api/dashboard'
import { Goods, ShoppingCart, Money, TrendCharts } from '@element-plus/icons-vue'
import * as echarts from 'echarts'

const chartRef = ref()
const data = ref({ productCount: 0, orderCount: 0, totalSales: 0, todaySales: 0, todayOrders: 0, recent7Days: [] })

const statCards = computed(() => [
  { label: '商品总数', value: data.value.productCount, icon: Goods, color: '#409EFF' },
  { label: '订单总数', value: data.value.orderCount, icon: ShoppingCart, color: '#67C23A' },
  { label: '总销售额', value: '¥' + (data.value.totalSales || 0).toFixed(2), icon: Money, color: '#E6A23C' },
  { label: '今日销售额', value: '¥' + (data.value.todaySales || 0).toFixed(2), icon: TrendCharts, color: '#F56C6C' }
])

onMounted(async () => {
  try {
    const res = await getDashboard()
    data.value = res
    initChart()
  } catch (e) { console.error(e) }
})

function initChart() {
  const chart = echarts.init(chartRef.value)
  const days = data.value.recent7Days || []
  chart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['销售额', '订单数'] },
    xAxis: { type: 'category', data: days.map(d => d.date) },
    yAxis: [{ type: 'value', name: '销售额(元)' }, { type: 'value', name: '订单数' }],
    series: [
      { name: '销售额', type: 'line', data: days.map(d => d.sales), smooth: true, itemStyle: { color: '#409EFF' } },
      { name: '订单数', type: 'bar', yAxisIndex: 1, data: days.map(d => d.orders), itemStyle: { color: '#67C23A' } }
    ]
  })
  window.addEventListener('resize', () => chart.resize())
}
</script>

<style scoped>
.stat-cards { margin-bottom: 20px; }
.stat-item { display: flex; justify-content: space-between; align-items: center; }
.stat-label { font-size: 14px; color: #909399; margin-bottom: 8px; }
.stat-value { font-size: 24px; font-weight: bold; color: #303133; }
.chart-card { margin-top: 20px; }
</style>
