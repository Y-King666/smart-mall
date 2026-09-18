<!--
  确认订单

  版式取自用户端设计稿的结算区块，但按约定做了简化：
  本项目经营的是虚拟商品（课程、软件、会员），无需收货地址与物流信息，
  因此只保留「商品清单 + 金额汇总 + 提交订单」。

  商品来源是购物车中「已勾选且未下架」的项——下单接口不接受商品参数，
  它自己去读购物车的勾选状态。
-->
<template>
  <div class="checkout mp-container">
    <h1 class="mp-serif checkout__title">确认订单</h1>

    <!-- ==================== 没有可结算的商品 ==================== -->
    <div v-if="!loading && items.length === 0" class="empty-state">
      <p class="mp-serif empty-state__title">没有待结算的商品</p>
      <p class="empty-state__hint">请先在购物车中选中要购买的商品</p>
      <router-link to="/cart" class="mp-btn">返回购物车</router-link>
    </div>

    <!-- ==================== 正常内容 ==================== -->
    <div v-else class="checkout__body">
      <section class="panel">
        <h2 class="mp-serif panel__title">商品清单</h2>

        <div v-loading="loading" class="list">
          <div v-for="item in items" :key="item.id" class="item">
            <img :src="item.coverImage" :alt="item.name" class="item__thumb" />
            <div class="item__info">
              <p class="mp-serif item__name">{{ item.name }}</p>
              <p class="item__unit">¥{{ formatAmount(item.price) }} × {{ item.quantity }}</p>
            </div>
            <span class="mp-price item__subtotal">
              <span class="mp-price__symbol">¥</span>{{ formatAmount(item.price * item.quantity) }}
            </span>
          </div>
        </div>
      </section>

      <aside class="summary">
        <h2 class="mp-serif summary__title">金额汇总</h2>

        <dl class="summary__rows">
          <div class="summary__row">
            <dt>商品件数</dt>
            <dd>{{ totalQuantity }} 件</dd>
          </div>
          <div class="summary__row">
            <dt>商品总额</dt>
            <dd>
              <span class="mp-price summary__amount">
                <span class="mp-price__symbol">¥</span>{{ formatAmount(totalAmount) }}
              </span>
            </dd>
          </div>
        </dl>

        <button class="mp-btn summary__submit" type="button" :disabled="submitting" @click="handleSubmit">
          {{ submitting ? '提交中…' : '提交订单' }}
        </button>

        <p class="summary__note">提交后可在「我的订单」中支付或取消</p>
      </aside>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getCart } from '@/api/cart'
import { submitOrder } from '@/api/order'
import { formatAmount } from '@/utils/product'
import { useCartStore } from '@/store/cart'

const router = useRouter()
const cartStore = useCartStore()

const items = ref([])
const loading = ref(false)
const submitting = ref(false)

const totalQuantity = computed(() => items.value.reduce((sum, i) => sum + i.quantity, 0))
const totalAmount = computed(() =>
  items.value.reduce((sum, i) => sum + Number(i.price) * i.quantity, 0)
)

async function loadCheckedItems() {
  loading.value = true
  try {
    const cart = await getCart()
    // 与后端下单口径保持一致：只结算已勾选且未下架的
    items.value = cart.filter((i) => i.checked === 1 && i.status === 1)
  } catch (e) {
    items.value = []
  } finally {
    loading.value = false
  }
}

async function handleSubmit() {
  if (!items.value.length) {
    return
  }
  submitting.value = true
  try {
    await submitOrder()
    await cartStore.refresh()
    ElMessage.success('下单成功')
    // 下单接口不返回订单号，直接去订单列表——列表按下单时间倒序，新订单在最前
    router.replace('/orders')
  } catch (e) {
    // 库存不足、商品下架等错误已由拦截器提示；重新拉一次以同步最新状态
    await loadCheckedItems()
  } finally {
    submitting.value = false
  }
}

onMounted(loadCheckedItems)
</script>

<style scoped>
.checkout {
  padding-top: var(--mp-space-xl);
  padding-bottom: var(--mp-space-2xl);
}

.checkout__title {
  font-size: 30px;
}

.checkout__body {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 320px;
  gap: var(--mp-space-xl);
  margin-top: var(--mp-space-lg);
  align-items: start;
}

/* ==================== 商品清单 ==================== */
.panel__title,
.summary__title {
  font-size: 20px;
  padding-bottom: var(--mp-space-sm);
  border-bottom: 1px solid var(--mp-separator);
}

.list {
  margin-top: var(--mp-space-xs);
  min-height: 80px;
}

.item {
  display: flex;
  align-items: center;
  gap: var(--mp-space-md);
  padding: var(--mp-space-md) 0;
  border-bottom: 1px solid var(--mp-separator);
}

.item__thumb {
  width: 64px;
  height: 64px;
  flex-shrink: 0;
  border-radius: var(--mp-radius-sm);
  object-fit: cover;
  background: var(--mp-surface-subdued);
}

.item__info {
  min-width: 0;
}

.item__name {
  font-size: 15px;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.item__unit {
  margin-top: 2px;
  font-size: 13px;
  color: var(--mp-ink-secondary);
}

.item__subtotal {
  margin-left: auto;
  font-size: 18px;
}

/* ==================== 金额汇总 ==================== */
.summary {
  padding: var(--mp-space-lg);
  border-radius: var(--mp-radius-lg);
  background: var(--mp-surface);
  box-shadow: var(--mp-shadow-card);
}

.summary__rows {
  margin: var(--mp-space-md) 0 0;
}

.summary__row {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  padding: 8px 0;
  font-size: 14px;
}

.summary__row dt {
  color: var(--mp-ink-secondary);
}

.summary__row dd {
  margin: 0;
  color: var(--mp-ink);
}

.summary__amount {
  font-size: 24px;
}

.summary__submit {
  width: 100%;
  height: 46px;
  margin-top: var(--mp-space-md);
  font-size: 15px;
}

.summary__note {
  margin-top: var(--mp-space-sm);
  font-size: 12px;
  color: var(--mp-ink-muted);
  text-align: center;
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

/* ==================== 窄屏 ==================== */
@media (max-width: 900px) {
  .checkout__body {
    grid-template-columns: 1fr;
  }
}
</style>
