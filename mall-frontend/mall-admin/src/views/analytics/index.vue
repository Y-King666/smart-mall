<template>
  <div class="analytics-container">
    <!-- 标签页切换 -->
    <el-tabs v-model="activeTab" type="border-card">
      <!-- 销售统计 -->
      <el-tab-pane label="销售统计" name="sales">
        <div class="tab-content">
          <!-- 时间范围选择 -->
          <div class="filter-bar">
            <el-select v-model="salesDays" placeholder="统计天数" @change="loadSalesData">
              <el-option label="近7天" :value="7" />
              <el-option label="近30天" :value="30" />
              <el-option label="近90天" :value="90" />
            </el-select>
            <el-button type="primary" @click="loadSalesData">刷新</el-button>
          </div>

          <!-- 销售汇总卡片 -->
          <el-row :gutter="20" class="summary-cards">
            <el-col :span="6">
              <el-card class="stat-card">
                <div class="stat-value">{{ salesData.summary?.total_orders || 0 }}</div>
                <div class="stat-label">总订单数</div>
              </el-card>
            </el-col>
            <el-col :span="6">
              <el-card class="stat-card">
                <div class="stat-value">¥{{ formatMoney(salesData.summary?.total_sales) }}</div>
                <div class="stat-label">总销售额</div>
              </el-card>
            </el-col>
            <el-col :span="6">
              <el-card class="stat-card">
                <div class="stat-value">{{ salesData.summary?.total_users || 0 }}</div>
                <div class="stat-label">下单用户数</div>
              </el-card>
            </el-col>
            <el-col :span="6">
              <el-card class="stat-card">
                <div class="stat-value">¥{{ formatMoney(salesData.summary?.avg_order_amount) }}</div>
                <div class="stat-label">平均客单价</div>
              </el-card>
            </el-col>
          </el-row>

          <!-- 图表区域 -->
          <el-row :gutter="20" class="charts-row">
            <el-col :span="12">
              <el-card>
                <template #header>
                  <span>每日销售趋势</span>
                </template>
                <div ref="dailySalesChart" class="chart-container"></div>
              </el-card>
            </el-col>
            <el-col :span="12">
              <el-card>
                <template #header>
                  <span>分类销售占比</span>
                </template>
                <div ref="categoryChart" class="chart-container"></div>
              </el-card>
            </el-col>
          </el-row>

          <!-- Top10 热销商品表格 -->
          <el-card class="table-card">
            <template #header>
              <span>Top10 热销商品</span>
            </template>
            <el-table :data="salesData.topProducts" stripe style="width: 100%">
              <el-table-column type="index" label="排名" width="80" />
              <el-table-column prop="name" label="商品名称" />
              <el-table-column prop="price" label="单价" width="120">
                <template #default="scope">
                  ¥{{ scope.row.price }}
                </template>
              </el-table-column>
              <el-table-column prop="total_sales" label="销量" width="120" />
              <el-table-column prop="total_amount" label="销售额" width="150">
                <template #default="scope">
                  ¥{{ formatMoney(scope.row.total_amount) }}
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </div>
      </el-tab-pane>

      <!-- 用户分析 -->
      <el-tab-pane label="用户分析" name="users">
        <div class="tab-content">
          <!-- 用户分类统计 -->
          <el-row :gutter="20" class="summary-cards">
            <el-col :span="8">
              <el-card class="stat-card high-value">
                <div class="stat-value">{{ userTypeStats['高价值用户'] || 0 }}</div>
                <div class="stat-label">高价值用户</div>
              </el-card>
            </el-col>
            <el-col :span="8">
              <el-card class="stat-card normal">
                <div class="stat-value">{{ userTypeStats['普通用户'] || 0 }}</div>
                <div class="stat-label">普通用户</div>
              </el-card>
            </el-col>
            <el-col :span="8">
              <el-card class="stat-card low-active">
                <div class="stat-value">{{ userTypeStats['低活跃用户'] || 0 }}</div>
                <div class="stat-label">低活跃用户</div>
              </el-card>
            </el-col>
          </el-row>

          <!-- 用户分类饼图 -->
          <el-card class="chart-card">
            <template #header>
              <span>用户分类占比</span>
            </template>
            <div ref="userTypeChart" class="chart-container"></div>
          </el-card>

          <!-- Top20 高价值用户表格 -->
          <el-card class="table-card">
            <template #header>
              <span>Top20 高价值用户</span>
            </template>
            <el-table :data="topUsers" stripe style="width: 100%">
              <el-table-column type="index" label="排名" width="80" />
              <el-table-column prop="username" label="用户名" width="150" />
              <el-table-column prop="nickname" label="昵称" width="150" />
              <el-table-column prop="order_count" label="订单数" width="120" />
              <el-table-column prop="total_spent" label="总消费" width="150">
                <template #default="scope">
                  ¥{{ formatMoney(scope.row.total_spent) }}
                </template>
              </el-table-column>
              <el-table-column prop="last_order_time" label="最近下单时间" width="180">
                <template #default="scope">
                  {{ formatDate(scope.row.last_order_time) }}
                </template>
              </el-table-column>
              <el-table-column prop="user_type" label="用户类型" width="120">
                <template #default="scope">
                  <el-tag :type="getUserTypeTag(scope.row.user_type)">
                    {{ scope.row.user_type }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="110">
                <template #default="{ row }">
                  <el-button type="primary" text size="small" @click="showUserRecommendations(row)">查看推荐</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </div>
      </el-tab-pane>

      <!-- 商品推荐 -->
      <el-tab-pane label="商品推荐" name="recommend">
        <div class="tab-content">
          <!-- 筛选条件 -->
          <div class="filter-bar">
            <el-select v-model="categoryId" placeholder="选择分类" clearable @change="loadRecommendations">
              <el-option label="全部分类" :value="null" />
              <el-option
                v-for="cat in categories"
                :key="cat.id"
                :label="categoryLabel(cat)"
                :value="cat.id"
              />
            </el-select>
            <el-button type="primary" @click="loadRecommendations">刷新</el-button>
          </div>

          <!-- 热门商品表格 -->
          <el-card class="table-card">
            <template #header>
              <span>热门商品 Top20</span>
            </template>
            <el-table :data="recommendations" stripe style="width: 100%">
              <el-table-column type="index" label="排名" width="80" />
              <el-table-column prop="image" label="图片" width="100">
                <template #default="scope">
                  <el-image
                    v-if="scope.row.image"
                    :src="scope.row.image"
                    :preview-src-list="[scope.row.image]"
                    :preview-teleported="true"
                    style="width: 60px; height: 60px"
                    fit="cover"
                  />
                  <span v-else>无图片</span>
                </template>
              </el-table-column>
              <el-table-column prop="name" label="商品名称" />
              <el-table-column prop="category_name" label="分类" width="150" />
              <el-table-column prop="price" label="单价" width="120">
                <template #default="scope">
                  ¥{{ scope.row.price }}
                </template>
              </el-table-column>
              <el-table-column prop="total_sales" label="销量" width="120" />
              <el-table-column prop="total_amount" label="销售额" width="150">
                <template #default="scope">
                  ¥{{ formatMoney(scope.row.total_amount) }}
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 健康检查提示 -->
    <el-dialog v-model="showHealthDialog" title="服务状态" width="500">
      <div class="health-status">
        <el-icon v-if="pythonServiceHealthy" color="#67c23a" :size="48"><CircleCheck /></el-icon>
        <el-icon v-else color="#f56c6c" :size="48"><CircleClose /></el-icon>
        <p>{{ healthMessage }}</p>
      </div>
    </el-dialog>

    <!-- 单个用户的个性化推荐商品 -->
    <el-dialog
      v-model="userRecVisible"
      :title="`为「${userRecUser?.nickname || userRecUser?.username || ''}」推荐的商品`"
      width="700"
    >
      <el-table :data="userRecProducts" v-loading="userRecLoading" stripe style="width: 100%">
        <el-table-column type="index" label="排名" width="80" />
        <el-table-column label="封面" width="80">
          <template #default="{ row }">
            <el-image :src="row.image" style="width: 50px; height: 50px" fit="cover" />
          </template>
        </el-table-column>
        <el-table-column prop="name" label="商品名称" min-width="200" show-overflow-tooltip />
        <el-table-column prop="price" label="价格" width="110">
          <template #default="{ row }">¥{{ formatMoney(row.price) }}</template>
        </el-table-column>
        <el-table-column prop="total_sales" label="销量" width="90" />
        <template #empty>
          <span v-if="!userRecLoading">暂无可推荐的商品（该用户已买过全部热门商品）</span>
        </template>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, watch } from 'vue'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'
import { CircleCheck, CircleClose } from '@element-plus/icons-vue'
import {
  getSalesStats,
  getUserAnalysis,
  getRecommendations,
  getUserRecommendations,
  checkAnalyticsHealth
} from '@/api/analytics'
import { getCategories } from '@/api/category'
import { categoryLabel, flattenCategories } from '@/utils/category'

// 当前激活的标签页
const activeTab = ref('sales')

// 销售数据
const salesDays = ref(7)
const salesData = ref({})

// 用户分析数据
const userTypeStats = ref({})
const topUsers = ref([])

// 单个用户的个性化推荐（点高价值用户表格里的「查看推荐」时弹出）
const userRecVisible = ref(false)
const userRecLoading = ref(false)
const userRecUser = ref(null)
const userRecProducts = ref([])

// 商品推荐数据
const categoryId = ref(null)
const recommendations = ref([])
const categories = ref([])

// 健康检查
const showHealthDialog = ref(false)
const pythonServiceHealthy = ref(false)
const healthMessage = ref('')

// 图表实例
const dailySalesChart = ref(null)
const categoryChart = ref(null)
const userTypeChart = ref(null)

/**
 * 格式化金额
 */
const formatMoney = (value) => {
  if (!value) return '0.00'
  return Number(value).toFixed(2)
}

/**
 * 格式化日期
 */
const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  // 没有任何下单记录的用户，Python 分析服务返回的是字符串 NaT，无法被 Date 解析
  if (Number.isNaN(date.getTime())) return '-'
  return date.toLocaleString('zh-CN')
}

/**
 * 获取用户类型标签颜色
 */
const getUserTypeTag = (userType) => {
  const map = {
    '高价值用户': 'danger',
    '普通用户': 'warning',
    '低活跃用户': 'info'
  }
  return map[userType] || 'info'
}

/**
 * 加载销售数据
 */
const loadSalesData = async () => {
  try {
    const data = await getSalesStats(salesDays.value)
    salesData.value = data

    // 渲染图表
    await nextTick()
    renderDailySalesChart()
    renderCategoryChart()
  } catch (error) {
    ElMessage.error('加载销售数据失败')
  }
}

/**
 * 加载用户分析数据
 */
const loadUserData = async () => {
  try {
    const data = await getUserAnalysis()
    userTypeStats.value = data.userTypeStats || {}
    topUsers.value = data.topUsers || []

    // 渲染图表
    await nextTick()
    renderUserTypeChart()
  } catch (error) {
    ElMessage.error('加载用户分析数据失败')
  }
}

/**
 * 加载商品推荐数据
 */
const loadRecommendations = async () => {
  try {
    const data = await getRecommendations(categoryId.value)
    recommendations.value = data.hotProducts || []
  } catch (error) {
    ElMessage.error('加载商品推荐失败')
  }
}

/**
 * 查看某个用户的个性化推荐商品
 *
 * 先开弹窗再请求：弹窗里自带 loading，比等请求回来才弹更跟手
 */
const showUserRecommendations = async (user) => {
  userRecUser.value = user
  userRecProducts.value = []
  userRecVisible.value = true
  userRecLoading.value = true
  try {
    const data = await getUserRecommendations(user.id)
    userRecProducts.value = data.recommendations || []
  } catch (error) {
    ElMessage.error('加载用户推荐失败')
  } finally {
    userRecLoading.value = false
  }
}

/**
 * 加载分类列表
 */
const loadCategories = async () => {
  try {
    const data = await getCategories()
    // 拍平整棵树：原来只渲染顶层，4 个选项里 3 个是父分类，选谁都没结果
    categories.value = flattenCategories(data)
  } catch (error) {
    console.error('加载分类列表失败', error)
  }
}

/**
 * 健康检查
 */
const checkHealth = async () => {
  try {
    const data = await checkAnalyticsHealth()
    pythonServiceHealthy.value = data.pythonServiceHealthy
    healthMessage.value = data.message

    if (!pythonServiceHealthy.value) {
      showHealthDialog.value = true
    }
  } catch (error) {
    console.error('健康检查失败', error)
  }
}

/**
 * 渲染每日销售趋势图
 */
const renderDailySalesChart = () => {
  if (!dailySalesChart.value) return

  const chart = echarts.init(dailySalesChart.value)
  const dailySales = salesData.value.dailySales || []

  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' }
    },
    legend: {
      data: ['销售额', '订单数']
    },
    xAxis: {
      type: 'category',
      data: dailySales.map(item => item.sale_date)
    },
    yAxis: [
      {
        type: 'value',
        name: '销售额(元)',
        position: 'left'
      },
      {
        type: 'value',
        name: '订单数',
        position: 'right'
      }
    ],
    series: [
      {
        name: '销售额',
        type: 'line',
        data: dailySales.map(item => item.total_sales),
        smooth: true,
        itemStyle: { color: '#409EFF' }
      },
      {
        name: '订单数',
        type: 'bar',
        yAxisIndex: 1,
        data: dailySales.map(item => item.order_count),
        itemStyle: { color: '#67C23A' }
      }
    ]
  }

  chart.setOption(option)
}

/**
 * 渲染分类销售占比图
 */
const renderCategoryChart = () => {
  if (!categoryChart.value) return

  const chart = echarts.init(categoryChart.value)
  const categoryStats = salesData.value.categoryStats || []

  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: ¥{c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left'
    },
    series: [
      {
        name: '分类销售占比',
        type: 'pie',
        radius: '50%',
        data: categoryStats.map(item => ({
          name: item.category_name,
          value: item.total_amount
        })),
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }
    ]
  }

  chart.setOption(option)
}

/**
 * 渲染用户分类饼图
 */
const renderUserTypeChart = () => {
  if (!userTypeChart.value) return

  const chart = echarts.init(userTypeChart.value)

  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c}人 ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left'
    },
    series: [
      {
        name: '用户分类',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: true,
          formatter: '{b}: {c}人'
        },
        data: [
          {
            name: '高价值用户',
            value: userTypeStats.value['高价值用户'] || 0,
            itemStyle: { color: '#F56C6C' }
          },
          {
            name: '普通用户',
            value: userTypeStats.value['普通用户'] || 0,
            itemStyle: { color: '#E6A23C' }
          },
          {
            name: '低活跃用户',
            value: userTypeStats.value['低活跃用户'] || 0,
            itemStyle: { color: '#909399' }
          }
        ]
      }
    ]
  }

  chart.setOption(option)
}

// 监听标签页切换
watch(activeTab, (newTab) => {
  if (newTab === 'sales') {
    loadSalesData()
  } else if (newTab === 'users') {
    loadUserData()
  } else if (newTab === 'recommend') {
    loadRecommendations()
  }
})

// 页面加载时初始化
onMounted(() => {
  checkHealth()
  loadSalesData()
  loadCategories()
})
</script>

<style scoped>
.analytics-container {
  padding: 20px;
}

.tab-content {
  padding: 20px 0;
}

.filter-bar {
  margin-bottom: 20px;
  display: flex;
  gap: 10px;
}

.summary-cards {
  margin-bottom: 20px;
}

.stat-card {
  text-align: center;
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  color: #409EFF;
  margin-bottom: 10px;
}

.stat-card.high-value .stat-value {
  color: #F56C6C;
}

.stat-card.normal .stat-value {
  color: #E6A23C;
}

.stat-card.low-active .stat-value {
  color: #909399;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.charts-row {
  margin-bottom: 20px;
}

.chart-container {
  height: 400px;
  width: 100%;
}

.chart-card {
  margin-bottom: 20px;
}

.table-card {
  margin-top: 20px;
}

.health-status {
  text-align: center;
  padding: 20px;
}

.health-status p {
  margin-top: 20px;
  font-size: 16px;
  color: #606266;
}
</style>
