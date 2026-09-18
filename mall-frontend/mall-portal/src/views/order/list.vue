<!--
  我的订单

  一张卡一笔订单：单号与状态在卡头，卡身是这笔订单买了什么（缩略图、名称、简介、条目金额），
  卡脚是订单金额与可执行的操作。

  支付与取消都以服务端返回为准：操作成功后重新拉取列表，
  避免本地状态与服务端不一致（取消会连带恢复库存，更不能只改本地）。
-->
<template>
  <div class="orders mp-container">
    <header class="orders__head">
      <h1 class="mp-serif orders__title">我的订单</h1>
      <p v-if="total" class="orders__count">共 {{ total }} 笔</p>
    </header>

    <!-- ==================== 空态 ==================== -->
    <div v-if="!loading && orders.length === 0" class="empty-state">
      <p class="mp-serif empty-state__title">还没有订单</p>
      <p class="empty-state__hint">挑几件喜欢的商品下单后，可以在这里查看</p>
      <router-link to="/products" class="mp-btn">去逛逛</router-link>
    </div>

    <!-- ==================== 订单列表 ==================== -->
    <div v-else v-loading="loading" class="orders__body">
      <article v-for="order in orders" :key="order.id" class="order">
        <div class="order__head">
          <div class="order__no">
            <span class="order__no-label">订单号</span>
            <strong class="order__no-value">{{ order.orderNo }}</strong>
            <button
              class="order__copy"
              type="button"
              title="复制订单号"
              aria-label="复制订单号"
              @click="copyOrderNo(order.orderNo)"
            >
              <el-icon :size="15"><CopyDocument /></el-icon>
            </button>
          </div>
          <el-tag :type="payStatusType(order.payStatus)" size="small" effect="light">
            {{ payStatusText(order.payStatus) }}
          </el-tag>
        </div>

        <div class="order__body">
          <div class="order__facts">
            <span class="order__fact">下单时间 {{ order.createTime }}</span>
            <span v-if="order.payTime" class="order__fact">支付时间 {{ order.payTime }}</span>
          </div>

          <!-- 这笔订单买了什么 -->
          <ul class="goods-list">
            <li v-for="item in order.items || []" :key="item.id" class="goods">
              <router-link :to="`/products/${item.productId}`" class="goods__thumb">
                <img :src="item.coverImage" :alt="item.productName" />
              </router-link>

              <div class="goods__info">
                <router-link :to="`/products/${item.productId}`" class="mp-serif goods__name">
                  {{ item.productName }}
                </router-link>
                <p v-if="item.description" class="goods__desc">{{ item.description }}</p>
              </div>

              <div class="goods__price">
                <span class="mp-price goods__unit">
                  <span class="mp-price__symbol">¥</span>{{ formatAmount(item.productPrice) }}
                </span>
                <span class="goods__qty">×{{ item.quantity }}</span>
              </div>
            </li>
          </ul>

          <div class="order__foot">
            <span class="order__amount-label">订单金额</span>
            <span class="mp-price order__amount">
              <span class="mp-price__symbol">¥</span>{{ formatAmount(order.totalAmount) }}
            </span>

            <div class="order__acts">
              <button
                v-if="canCancel(order)"
                class="mp-btn mp-btn--outline mp-btn--sm"
                type="button"
                @click="handleCancel(order)"
              >
                取消订单
              </button>
              <button
                v-if="canPay(order)"
                class="mp-btn mp-btn--sm"
                type="button"
                @click="handlePay(order)"
              >
                去支付
              </button>
            </div>
          </div>
        </div>
      </article>

      <div v-if="total > pageSize" class="orders__pager">
        <el-pagination
          layout="prev, pager, next"
          background
          :current-page="page"
          :page-size="pageSize"
          :total="total"
          @current-change="changePage"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { cancelOrder, getOrders, payOrder } from '@/api/order'
import { canCancel, canPay, payStatusText, payStatusType } from '@/utils/order'
import { formatAmount } from '@/utils/product'

const pageSize = 5

const orders = ref([])
const total = ref(0)
const page = ref(1)
const loading = ref(false)

async function loadOrders() {
  loading.value = true
  try {
    const data = await getOrders({ page: page.value, size: pageSize })
    orders.value = data.records || []
    total.value = data.total || 0
  } catch (e) {
    orders.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function changePage(next) {
  page.value = next
  loadOrders()
}

async function copyOrderNo(orderNo) {
  try {
    await navigator.clipboard.writeText(orderNo)
    ElMessage.success('订单号已复制')
  } catch (e) {
    ElMessage.error('复制失败，请手动复制')
  }
}

async function handlePay(order) {
  try {
    await ElMessageBox.confirm(
      `确认支付订单 ${order.orderNo}（¥${formatAmount(order.totalAmount)}）吗？本项目为模拟支付，点确认即视为支付成功。`,
      '确认支付',
      { confirmButtonText: '确认支付', cancelButtonText: '再想想', type: 'info' }
    )
  } catch (e) {
    return
  }
  try {
    await payOrder(order.id)
    ElMessage.success('支付成功')
    await loadOrders()
  } catch (e) {
    // 状态已变化等错误由拦截器提示，重新拉取以同步
    await loadOrders()
  }
}

async function handleCancel(order) {
  const paid = order.payStatus === 1
  try {
    await ElMessageBox.confirm(
      paid
        ? `该订单已支付，取消后将退回库存并扣回商品销量。确定取消订单 ${order.orderNo} 吗？`
        : `确定取消订单 ${order.orderNo} 吗？取消后库存会退回。`,
      '取消订单',
      { confirmButtonText: '确定取消', cancelButtonText: '再想想', type: 'warning' }
    )
  } catch (e) {
    return
  }
  try {
    await cancelOrder(order.id)
    ElMessage.success('订单已取消')
    await loadOrders()
  } catch (e) {
    await loadOrders()
  }
}

onMounted(loadOrders)
</script>

<style scoped>
.orders {
  padding-top: var(--mp-space-xl);
  padding-bottom: var(--mp-space-2xl);
}

.orders__head {
  display: flex;
  align-items: baseline;
  gap: var(--mp-space-sm);
}

.orders__title {
  font-size: 30px;
}

.orders__count {
  font-size: 13px;
  color: var(--mp-ink-secondary);
}

.orders__body {
  margin-top: var(--mp-space-lg);
  min-height: 120px;
}

/* ==================== 订单卡 ==================== */
.order {
  border-radius: var(--mp-radius-lg);
  overflow: hidden;
  background: var(--mp-surface);
  box-shadow: var(--mp-shadow-card);
}

.order + .order {
  margin-top: var(--mp-space-md);
}

/* 卡头用低调底色与卡身区分，与设计规范里"靠色调差分区、不靠重边框"的取向一致 */
.order__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--mp-space-md);
  padding: var(--mp-space-sm) var(--mp-space-lg);
  background: var(--mp-surface-subdued);
}

.order__no {
  display: flex;
  align-items: center;
  gap: var(--mp-space-sm);
  min-width: 0;
  font-size: 13px;
  color: var(--mp-ink-secondary);
}

.order__no-label {
  flex-shrink: 0;
  letter-spacing: 0.02em;
}

.order__no-value {
  color: var(--mp-primary);
  font-weight: 700;
  font-variant-numeric: tabular-nums;
  letter-spacing: 0.03em;
}

.order__copy {
  flex-shrink: 0;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  padding: 0;
  border: 1px solid var(--mp-outline);
  border-radius: var(--mp-radius-sm);
  background: var(--mp-surface);
  color: var(--mp-ink-secondary);
  cursor: pointer;
  transition: color 0.2s, border-color 0.2s, background-color 0.2s;
}

.order__copy:hover {
  border-color: var(--mp-primary);
  background: var(--mp-surface-subdued);
  color: var(--mp-primary);
}

.order__copy:focus-visible {
  outline: 2px solid var(--mp-primary);
  outline-offset: 2px;
}

.order__body {
  padding: var(--mp-space-md) var(--mp-space-lg) var(--mp-space-lg);
}

.order__facts {
  display: flex;
  flex-wrap: wrap;
  gap: var(--mp-space-lg);
}

.order__fact {
  font-size: 13px;
  color: var(--mp-ink-secondary);
}

/* ==================== 订单内的商品条目 ==================== */
.goods-list {
  margin: var(--mp-space-md) 0 0;
  padding: 0;
  list-style: none;
  display: flex;
  flex-direction: column;
  gap: var(--mp-space-sm);
}

.goods {
  display: flex;
  align-items: center;
  gap: var(--mp-space-md);
}

/* 缩略图用 4:5，与下单页、购物车里的商品图保持同一比例 */
.goods__thumb {
  flex-shrink: 0;
  width: 56px;
  height: 56px;
  border-radius: var(--mp-radius-sm);
  overflow: hidden;
  background: var(--mp-surface-subdued);
}

.goods__thumb img {
  width: 100%;
  height: 100%;
  display: block;
  object-fit: cover;
}

.goods__info {
  flex: 1;
  min-width: 0;
}

/* 商品名与简介都只占一行，条目高度因此恒定 */
.goods__name {
  display: block;
  font-size: 15px;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  transition: color 0.2s;
}

.goods__name:hover {
  color: var(--mp-primary);
}

/* 简介只留一行，长文案不会把条目撑高、把卡片拉得长短不一 */
.goods__desc {
  margin-top: 3px;
  font-size: 12px;
  color: var(--mp-ink-secondary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 单价对齐商品名那行，数量对齐简介那行 */
.goods__price {
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 5px;
}

.goods__unit {
  font-size: 15px;
}

.goods__qty {
  font-size: 12px;
  color: var(--mp-ink-muted);
}

.order__foot {
  display: flex;
  align-items: baseline;
  gap: var(--mp-space-sm);
  margin-top: var(--mp-space-md);
  padding-top: var(--mp-space-md);
  border-top: 1px solid var(--mp-separator);
}

.order__amount-label {
  font-size: 13px;
  color: var(--mp-ink-secondary);
}

.order__amount {
  font-size: 24px;
}

.order__acts {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: var(--mp-space-xs);
}

.orders__pager {
  margin-top: var(--mp-space-xl);
  display: flex;
  justify-content: center;
}

/* ==================== 空态 ==================== */
.empty-state {
  padding: var(--mp-space-2xl) 0;
  text-align: center;
}

.empty-state__title {
  font-size: 22px;
}

.empty-state__hint {
  margin: var(--mp-space-xs) 0 var(--mp-space-lg);
  font-size: 13px;
  color: var(--mp-ink-secondary);
}
</style>
