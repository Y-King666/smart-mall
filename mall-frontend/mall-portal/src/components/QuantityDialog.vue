<!--
  选择购买数量弹层

  商品详情页的两个入口共用：加入购物车与立即购买都要先选数量，
  区别只在按下弹层里那颗按钮之后——一个是把商品放进购物车，一个是直接下单支付。

  版式参考常见购物软件的规格弹层，但本商城的商品没有规格可选
  （pms_product 没有对应字段），所以弹层里只剩数量一项可填。
-->
<template>
  <Teleport to="body">
    <transition name="fade">
      <div v-if="visible" class="modal" @click.self="close">
        <div class="modal__card">
          <button class="modal__close" type="button" aria-label="关闭" @click="close">✕</button>

          <!-- 商品信息 -->
          <div class="goods">
            <img :src="product.coverImage" :alt="product.name" class="goods__thumb" />
            <div class="goods__info">
              <p class="mp-price goods__price">
                <span class="mp-price__symbol">¥</span>{{ formatAmount(product.price) }}
              </p>
              <p class="mp-serif goods__name">{{ product.name }}</p>
              <p class="goods__stock">
                {{ product.stock > 0 ? `库存 ${product.stock} 件` : '暂无库存' }}
              </p>
            </div>
          </div>

          <!-- 数量 -->
          <div class="row">
            <span class="row__label">购买数量</span>
            <QuantityStepper v-model="quantity" :max="maxQuantity" />
          </div>

          <button class="mp-btn modal__submit" type="button" :disabled="!canSubmit" @click="submit">
            {{ submitting ? submittingText : submitText }}
          </button>
        </div>
      </div>
    </transition>
  </Teleport>
</template>

<script setup>
import { computed, onUnmounted, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { addToCart } from '@/api/cart'
import { submitDirectOrder } from '@/api/order'
import { formatAmount } from '@/utils/product'
import QuantityStepper from '@/components/QuantityStepper.vue'
import { useCartStore } from '@/store/cart'

/** 去向：cart=加入购物车，buy=直接下单支付 */
const MODE_CART = 'cart'

const props = defineProps({
  visible: { type: Boolean, default: false },
  product: { type: Object, required: true },
  mode: { type: String, default: MODE_CART }
})

const emit = defineEmits(['update:visible', 'done'])

const cartStore = useCartStore()

const quantity = ref(1)
const submitting = ref(false)

const isCartMode = computed(() => props.mode === MODE_CART)

const submitText = computed(() => (isCartMode.value ? '加入购物车' : '立即支付'))

const submittingText = computed(() => (isCartMode.value ? '加入中…' : '支付中…'))

const maxQuantity = computed(() => Math.max(1, props.product?.stock ?? 1))

const canSubmit = computed(() => !submitting.value && props.product?.stock > 0)

// 每次打开都回到 1 件，不沿用上次弹层里的残留值；
// 同时锁住页面滚动，避免弹层背后的长页面跟着滚
watch(
  () => props.visible,
  (open) => {
    if (open) {
      quantity.value = 1
    }
    document.body.style.overflow = open ? 'hidden' : ''
  }
)

function close() {
  if (!submitting.value) {
    emit('update:visible', false)
  }
}

/** 按当前数量执行对应动作：加购只入购物车，直接买则下单并支付 */
async function submit() {
  if (!canSubmit.value) {
    return
  }

  submitting.value = true
  try {
    if (isCartMode.value) {
      await addToCart({ productId: props.product.id, quantity: quantity.value })
      await cartStore.refresh()
      ElMessage.success('已加入购物车')
    } else {
      const orderNo = await submitDirectOrder({
        productId: props.product.id,
        quantity: quantity.value
      })
      ElMessage.success(`支付成功，订单号 ${orderNo}`)
    }
    emit('update:visible', false)
    // 由页面决定之后去哪：加购去购物车，直接买去订单列表
    emit('done', props.mode)
  } catch (e) {
    // 库存不足、已下架等错误已由请求拦截器提示
  } finally {
    submitting.value = false
  }
}

onUnmounted(() => {
  document.body.style.overflow = ''
})
</script>

<style scoped>
.modal {
  position: fixed;
  inset: 0;
  z-index: 300;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--mp-space-lg);
  background: rgba(31, 32, 34, 0.45);
}

.modal__card {
  position: relative;
  width: 100%;
  max-width: 420px;
  padding: var(--mp-space-xl) var(--mp-space-lg) var(--mp-space-lg);
  border-radius: var(--mp-radius-lg);
  background: var(--mp-surface);
  box-shadow: var(--mp-shadow-overlay);
}

.modal__close {
  position: absolute;
  top: var(--mp-space-sm);
  right: var(--mp-space-sm);
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  border-radius: var(--mp-radius-full);
  background: transparent;
  color: var(--mp-ink-muted);
  font-size: 14px;
  cursor: pointer;
  transition: background-color 0.2s, color 0.2s;
}

.modal__close:hover {
  background: var(--mp-surface-subdued);
  color: var(--mp-ink);
}

/* ==================== 商品信息 ==================== */
.goods {
  display: flex;
  gap: var(--mp-space-md);
}

.goods__thumb {
  flex-shrink: 0;
  width: 80px;
  height: 80px;
  border-radius: var(--mp-radius-sm);
  object-fit: cover;
  background: var(--mp-surface-subdued);
}

.goods__info {
  min-width: 0;
}

.goods__price {
  font-size: 20px;
}

/* 名称最多两行，长名字不会把弹层撑高 */
.goods__name {
  margin-top: var(--mp-space-2xs);
  font-size: 15px;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.goods__stock {
  margin-top: var(--mp-space-2xs);
  font-size: 12px;
  color: var(--mp-ink-muted);
}

/* ==================== 数量与提交 ==================== */
.row {
  margin-top: var(--mp-space-lg);
  padding-top: var(--mp-space-md);
  border-top: 1px solid var(--mp-separator);
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.row__label {
  font-size: 13px;
  color: var(--mp-ink-secondary);
}

.modal__submit {
  width: 100%;
  height: 44px;
  margin-top: var(--mp-space-lg);
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.18s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
