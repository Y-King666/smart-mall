<!--
  购物车

  版式取自用户端设计稿的购物车页：列表 + 底部的选择与结算栏。
  勾选状态与数量都存在服务端（oms_cart_item.checked / quantity），
  页面改动后以服务端返回为准，避免本地状态与真实购物车脱节。

  结算走购物车中「已勾选」的商品，因此已下架的勾选项不参与结算，
  这里直接禁用其勾选框，避免用户勾了却在提交时被后端拒绝。
-->
<template>
  <div class="cart mp-container">
    <header class="cart__head">
      <h1 class="mp-serif cart__title">我的购物车</h1>
      <p v-if="items.length" class="cart__count">共 {{ totalQuantity }} 件宝贝</p>
    </header>

    <!-- ==================== 空态 ==================== -->
    <div v-if="!loading && items.length === 0" class="empty-state">
      <p class="mp-serif empty-state__title">购物车还是空的</p>
      <p class="empty-state__hint">去挑几件喜欢的商品吧</p>
      <router-link to="/products" class="mp-btn">去逛逛</router-link>
    </div>

    <!-- ==================== 列表 ==================== -->
    <template v-else>
      <div v-loading="loading" class="cart__body">
        <div class="row row--head">
          <el-checkbox
            :model-value="allChecked"
            :indeterminate="partiallyChecked"
            @change="toggleAll"
          >
            全选
          </el-checkbox>
          <span>商品信息</span>
          <span>单价</span>
          <span>数量</span>
          <span>小计</span>
          <span>操作</span>
        </div>

        <div v-for="item in items" :key="item.id" class="row" :class="{ 'row--invalid': item.status !== 1 }">
          <div class="row__check">
            <el-checkbox
              :model-value="item.checked === 1"
              :disabled="item.status !== 1"
              @change="onCheckedChange(item, $event)"
            />
          </div>

          <div class="row__product">
            <router-link :to="`/products/${item.productId}`" class="row__thumb">
              <img :src="item.coverImage" :alt="item.name" />
            </router-link>
            <div class="row__info">
              <router-link :to="`/products/${item.productId}`" class="mp-serif row__name">
                {{ item.name }}
              </router-link>
              <span v-if="item.status !== 1" class="mp-tag row__invalid-tag">已下架</span>
            </div>
          </div>

          <div class="row__price" data-label="单价">¥{{ formatAmount(item.price) }}</div>

          <div class="row__quantity" data-label="数量">
            <QuantityStepper
              :model-value="item.quantity"
              :max="item.stock"
              @update:model-value="onQuantityChange(item, $event)"
            />
            <span class="row__stock">库存 {{ item.stock }} 件</span>
          </div>

          <div class="row__subtotal" data-label="小计">
            <span class="mp-price row__subtotal-price">
              <span class="mp-price__symbol">¥</span>{{ formatAmount(item.price * item.quantity) }}
            </span>
          </div>

          <div class="row__actions">
            <button class="row__remove" type="button" @click="removeItem(item)">删除</button>
          </div>
        </div>
      </div>

      <!-- ==================== 底部结算栏 ==================== -->
      <div class="bar">
        <el-checkbox
          :model-value="allChecked"
          :indeterminate="partiallyChecked"
          @change="toggleAll"
        >
          全选
        </el-checkbox>
        <button class="bar__remove" type="button" :disabled="!checkedItems.length" @click="removeChecked">
          删除已选
        </button>
        <div class="bar__summary">
          <span class="bar__picked">已选 {{ checkedQuantity }} 件</span>
          <span class="bar__total">
            合计
            <span class="mp-price bar__total-price">
              <span class="mp-price__symbol">¥</span>{{ formatAmount(totalAmount) }}
            </span>
          </span>
        </div>
        <button class="mp-btn bar__submit" type="button" :disabled="!checkedItems.length" @click="goCheckout">
          去结算{{ checkedQuantity ? `（${checkedQuantity}）` : '' }}
        </button>
      </div>
    </template>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { deleteCartItem, getCart, updateCartItem } from '@/api/cart'
import { formatAmount } from '@/utils/product'
import QuantityStepper from '@/components/QuantityStepper.vue'
import { useCartStore } from '@/store/cart'

const router = useRouter()
const cartStore = useCartStore()

const items = ref([])
const loading = ref(false)

/** 可结算的商品：已勾选且未下架 */
const checkedItems = computed(() => items.value.filter((i) => i.checked === 1 && i.status === 1))
const checkedQuantity = computed(() => checkedItems.value.reduce((sum, i) => sum + i.quantity, 0))
const totalAmount = computed(() =>
  checkedItems.value.reduce((sum, i) => sum + Number(i.price) * i.quantity, 0)
)
const totalQuantity = computed(() => items.value.reduce((sum, i) => sum + i.quantity, 0))

/** 可勾选的商品（下架的不参与全选） */
const selectableItems = computed(() => items.value.filter((i) => i.status === 1))
const allChecked = computed(
  () => selectableItems.value.length > 0 && selectableItems.value.every((i) => i.checked === 1)
)
const partiallyChecked = computed(
  () => selectableItems.value.some((i) => i.checked === 1) && !allChecked.value
)

async function loadCart() {
  loading.value = true
  try {
    items.value = await getCart()
  } catch (e) {
    items.value = []
  } finally {
    loading.value = false
  }
}

/** 勾选/取消勾选：先改本地让反馈即时，再以服务端为准 */
async function onCheckedChange(item, checked) {
  const next = checked ? 1 : 0
  const previous = item.checked
  item.checked = next
  try {
    await updateCartItem({ id: item.id, checked: next })
  } catch (e) {
    item.checked = previous
  }
}

async function toggleAll(checked) {
  const next = checked ? 1 : 0
  const targets = selectableItems.value
  if (!targets.length) {
    return
  }
  targets.forEach((i) => {
    i.checked = next
  })
  // 接口只支持逐项更新，这里并发提交后以服务端结果为准
  await Promise.all(targets.map((i) => updateCartItem({ id: i.id, checked: next }).catch(() => {})))
  await loadCart()
}

async function onQuantityChange(item, quantity) {
  const previous = item.quantity
  item.quantity = quantity
  try {
    await updateCartItem({ id: item.id, quantity })
    // 数量会影响顶栏角标
    await cartStore.refresh()
  } catch (e) {
    item.quantity = previous
  }
}

async function removeItem(item) {
  try {
    await ElMessageBox.confirm(`确定从购物车移除「${item.name}」吗？`, '移除商品', {
      confirmButtonText: '移除',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch (e) {
    return   // 用户取消
  }
  try {
    await deleteCartItem(item.id)
    ElMessage.success('已移除')
    await loadCart()
    await cartStore.refresh()
  } catch (e) {
    // 错误已由拦截器提示
  }
}

async function removeChecked() {
  const targets = checkedItems.value
  if (!targets.length) {
    return
  }
  try {
    await ElMessageBox.confirm(`确定移除已选中的 ${targets.length} 件商品吗？`, '批量移除', {
      confirmButtonText: '移除',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch (e) {
    return
  }
  try {
    await Promise.all(targets.map((i) => deleteCartItem(i.id).catch(() => {})))
    ElMessage.success('已移除')
    await loadCart()
    await cartStore.refresh()
  } catch (e) {
    // 错误已由拦截器提示
  }
}

function goCheckout() {
  if (!checkedItems.value.length) {
    ElMessage.warning('请先选中要结算的商品')
    return
  }
  router.push('/checkout')
}

onMounted(loadCart)
</script>

<style scoped>
.cart {
  padding-top: var(--mp-space-xl);
  padding-bottom: var(--mp-space-2xl);
}

.cart__head {
  display: flex;
  align-items: baseline;
  gap: var(--mp-space-sm);
}

.cart__title {
  font-size: 30px;
}

.cart__count {
  font-size: 13px;
  color: var(--mp-ink-secondary);
}

.cart__body {
  margin-top: var(--mp-space-lg);
  min-height: 120px;
}

/* ==================== 列表 ==================== */
.row {
  display: grid;
  /* 首列要放得下表头的「勾选框 + 全选」两个字，否则会挤到商品信息列上 */
  grid-template-columns: 88px minmax(0, 1fr) 110px 150px 120px 70px;
  align-items: center;
  gap: var(--mp-space-md);
  padding: var(--mp-space-md) 0;
  border-bottom: 1px solid var(--mp-separator);
}

.row--head {
  padding: var(--mp-space-sm) 0;
  font-size: 13px;
  color: var(--mp-ink-secondary);
  border-bottom: 1px solid var(--mp-ink-muted);
}

.row--invalid .row__product,
.row--invalid .row__price,
.row--invalid .row__quantity,
.row--invalid .row__subtotal {
  opacity: 0.55;
}

.row__product {
  display: flex;
  align-items: center;
  gap: var(--mp-space-md);
  min-width: 0;
}

.row__thumb {
  width: 80px;
  height: 80px;
  flex-shrink: 0;
  border-radius: var(--mp-radius);
  overflow: hidden;
  background: var(--mp-surface-subdued);
}

.row__thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.row__info {
  min-width: 0;
}

.row__name {
  font-size: 16px;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.row__name:hover {
  color: var(--mp-primary);
}

.row__invalid-tag {
  margin-top: var(--mp-space-2xs);
  background: rgba(31, 32, 34, 0.06);
  color: var(--mp-ink-secondary);
}

.row__price {
  font-size: 14px;
  color: var(--mp-ink-secondary);
}

.row__quantity {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.row__stock {
  font-size: 11px;
  color: var(--mp-ink-muted);
}

.row__subtotal-price {
  font-size: 18px;
}

.row__remove {
  border: none;
  background: transparent;
  color: var(--mp-ink-secondary);
  font-family: var(--mp-font-sans);
  font-size: 13px;
  cursor: pointer;
}

.row__remove:hover {
  color: var(--mp-primary);
}

/* ==================== 底部结算栏 ==================== */
.bar {
  display: flex;
  align-items: center;
  gap: var(--mp-space-lg);
  margin-top: var(--mp-space-lg);
  padding: var(--mp-space-md) var(--mp-space-lg);
  border-radius: var(--mp-radius-lg);
  background: var(--mp-surface);
  box-shadow: var(--mp-shadow-card);
}

.bar__remove {
  border: none;
  background: transparent;
  color: var(--mp-ink-secondary);
  font-family: var(--mp-font-sans);
  font-size: 13px;
  cursor: pointer;
}

.bar__remove:hover:not(:disabled) {
  color: var(--mp-primary);
}

.bar__remove:disabled {
  color: var(--mp-ink-muted);
  cursor: not-allowed;
}

.bar__summary {
  margin-left: auto;
  display: flex;
  align-items: baseline;
  gap: var(--mp-space-lg);
}

.bar__picked {
  font-size: 13px;
  color: var(--mp-ink-secondary);
}

.bar__total {
  font-size: 13px;
  color: var(--mp-ink-secondary);
}

.bar__total-price {
  font-size: 26px;
}

.bar__submit {
  height: 46px;
  padding: 0 var(--mp-space-2xl);
  font-size: 15px;
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
  .row {
    grid-template-columns: 80px minmax(0, 1fr) 100px 90px;
    row-gap: var(--mp-space-sm);
  }

  /* 窄屏下单价与小计合并展示，操作列并入商品信息行 */
  .row--head,
  .row__price,
  .row__actions {
    display: none;
  }

  .row__subtotal {
    grid-column: 3 / 5;
    text-align: right;
  }

  .bar {
    flex-wrap: wrap;
    gap: var(--mp-space-sm);
  }

  .bar__summary {
    margin-left: 0;
  }
}
</style>
