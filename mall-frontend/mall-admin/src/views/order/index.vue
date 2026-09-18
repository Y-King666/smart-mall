<template>
  <div class="order-page">
    <div class="stat-cards">
      <el-card v-for="item in statCards" :key="item.label" shadow="hover">
        <div class="stat-item">
          <div class="stat-info">
            <div class="stat-label">{{ item.label }}</div>
            <div class="stat-value">{{ item.value }}</div>
          </div>
          <el-icon :size="40" :style="{ color: item.color }"><component :is="item.icon" /></el-icon>
        </div>
      </el-card>
    </div>

    <el-card>
      <div class="filter-bar">
        <el-input v-model="query.orderNo" placeholder="订单号" clearable style="width: 200px" @keyup.enter="loadData" />
        <el-select v-model="query.payStatus" placeholder="支付状态" clearable @change="loadData" style="width: 140px">
          <el-option label="待支付" :value="0" />
          <el-option label="已支付" :value="1" />
          <el-option label="已取消" :value="2" />
        </el-select>
        <el-button type="primary" @click="loadData">搜索</el-button>
      </div>
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="orderNo" label="订单号" width="220" />
        <el-table-column prop="totalAmount" label="金额" width="120">
          <template #default="{ row }">¥{{ row.totalAmount }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType(row.payStatus)">{{ statusText(row.payStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column prop="payTime" label="支付时间" width="180" />
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button type="primary" text size="small" @click="showDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-model:current-page="query.page"
        v-model:page-size="query.size"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @change="loadData"
        style="margin-top: 16px; justify-content: flex-end;"
      />
    </el-card>

    <el-dialog v-model="detailVisible" title="订单详情" width="600px">
      <div v-if="currentOrder">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单号">{{ currentOrder.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusType(currentOrder.payStatus)">{{ statusText(currentOrder.payStatus) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="金额">¥{{ currentOrder.totalAmount }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ currentOrder.createTime }}</el-descriptions-item>
        </el-descriptions>
        <el-table :data="currentOrder.items || []" style="margin-top: 16px;">
          <el-table-column prop="productName" label="商品" />
          <el-table-column prop="productPrice" label="单价" width="100">
            <template #default="{ row }">¥{{ row.productPrice }}</template>
          </el-table-column>
          <el-table-column prop="quantity" label="数量" width="80" />
        </el-table>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { getOrders, getOrderDetail, getOrderStats } from '@/api/order'
import { Tickets, Clock, CircleCheck, CircleClose, Money } from '@element-plus/icons-vue'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const detailVisible = ref(false)
const currentOrder = ref(null)
const query = reactive({ page: 1, size: 10, orderNo: '', payStatus: '' })

/** 订单统计汇总：全量口径，不随上方筛选条件变化 */
const stats = ref({ totalCount: 0, pendingCount: 0, paidCount: 0, cancelledCount: 0, totalAmount: 0 })

const statCards = computed(() => [
  { label: '订单总数', value: stats.value.totalCount, icon: Tickets, color: '#409EFF' },
  { label: '待支付', value: stats.value.pendingCount, icon: Clock, color: '#E6A23C' },
  { label: '已支付', value: stats.value.paidCount, icon: CircleCheck, color: '#67C23A' },
  { label: '已取消', value: stats.value.cancelledCount, icon: CircleClose, color: '#909399' },
  { label: '订单总额', value: '¥' + Number(stats.value.totalAmount || 0).toFixed(2), icon: Money, color: '#F56C6C' }
])

onMounted(() => {
  loadData()
  loadStats()
})

async function loadData() {
  loading.value = true
  try {
    const res = await getOrders(query)
    tableData.value = res.records
    total.value = res.total
  } finally { loading.value = false }
}

/**
 * 统计与列表各拉各的。
 * 统计失败只影响卡片数字，不该连带把订单列表也拦掉，所以单独 try。
 */
async function loadStats() {
  try {
    const res = await getOrderStats()
    if (res) stats.value = res
  } catch (e) { console.error(e) }
}

async function showDetail(row) {
  const res = await getOrderDetail(row.id)
  currentOrder.value = res
  detailVisible.value = true
}

function statusText(s) { return ['待支付', '已支付', '已取消'][s] || '未知' }
function statusType(s) { return ['warning', 'success', 'info'][s] || 'info' }
</script>

<style scoped>
/* 5 张卡片用 grid 均分：Element Plus 的 24 栅格除不尽 5，用 el-col 会在右侧留空档 */
.stat-cards { display: grid; grid-template-columns: repeat(5, 1fr); gap: 20px; margin-bottom: 20px; }
.stat-item { display: flex; justify-content: space-between; align-items: center; }
.stat-label { font-size: 14px; color: #909399; margin-bottom: 8px; }
.stat-value { font-size: 24px; font-weight: bold; color: #303133; }

.filter-bar { display: flex; gap: 12px; margin-bottom: 16px; flex-wrap: wrap; }
</style>
