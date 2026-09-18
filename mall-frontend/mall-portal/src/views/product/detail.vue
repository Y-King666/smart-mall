<!--
  商品详情页

  版式取自用户端设计稿的详情页：左侧大图、右侧信息与购买区，
  下方左栏是商品图文详情、右栏是同类商品。

  按约定略去了设计稿里没有数据支撑的部分：
  多图缩略图（pms_product 只有单张封面）、商品规格字段（无对应列）、
  立即购买（下单模型是购物车结算）、编辑式品牌文案与服务承诺。
-->
<template>
  <div class="detail mp-container">
    <!-- ==================== 加载中 ==================== -->
    <div v-if="loading" v-loading="true" class="detail__placeholder" />

    <!-- ==================== 商品不存在或已下架 ==================== -->
    <div v-else-if="loadError" class="empty-state">
      <p class="mp-serif empty-state__title">商品不存在或已下架</p>
      <p class="empty-state__hint">它可能已经被下架，或者链接不正确</p>
      <router-link to="/products" class="mp-btn">返回商品列表</router-link>
    </div>

    <!-- ==================== 正常内容 ==================== -->
    <template v-else-if="product">
      <!-- 面包屑 -->
      <nav class="crumb">
        <router-link to="/products" class="crumb__link">商品</router-link>
        <span class="crumb__sep">/</span>
        <router-link
          v-if="product.categoryId"
          :to="{ path: '/products', query: { categoryId: String(product.categoryId) } }"
          class="crumb__link"
        >
          {{ categoryName || '分类' }}
        </router-link>
        <span v-if="product.categoryId" class="crumb__sep">/</span>
        <span class="crumb__current">商品详情</span>
      </nav>

      <!-- ==================== 主区：左图右信息 ==================== -->
      <div class="main">
        <div class="main__media">
          <img :src="product.coverImage" :alt="product.name" />
        </div>

        <div class="main__info">
          <h1 class="mp-serif main__name">{{ product.name }}</h1>

          <div class="price-row">
            <span class="mp-price price-row__price">
              <span class="mp-price__symbol">¥</span>{{ formatAmount(product.price) }}
            </span>
            <span v-if="product.originalPrice" class="mp-price-original price-row__original">
              ¥{{ formatAmount(product.originalPrice) }}
            </span>
            <span class="price-row__sales">已售 {{ product.salesCount }} 件</span>
          </div>

          <p class="main__desc">{{ product.description || '暂无商品简介' }}</p>

          <dl class="specs">
            <div v-for="spec in specs" :key="spec.label" class="specs__row">
              <dt class="specs__label">{{ spec.label }}</dt>
              <dd class="specs__value">{{ spec.value }}</dd>
            </div>
          </dl>

          <!--
            两个入口都要先选数量（弹层里选），区别只在去向：
            加入购物车 → 购物车，立即购买 → 直接下单支付后进订单列表
          -->
          <div class="buy__actions">
            <button
              class="mp-btn mp-btn--outline"
              type="button"
              :disabled="product.stock <= 0"
              @click="openPurchaseDialog('cart')"
            >
              {{ product.stock <= 0 ? '已售罄' : '加入购物车' }}
            </button>
            <button
              class="mp-btn"
              type="button"
              :disabled="product.stock <= 0"
              @click="openPurchaseDialog('buy')"
            >
              立即购买
            </button>
          </div>
        </div>
      </div>

      <!-- ==================== 下区：图文详情 + 同类商品 ==================== -->
      <div class="body">
        <section class="detail-card">
          <h2 class="mp-serif detail-card__title">商品详情</h2>
          <!--
            商品详情是管理员录入的 HTML 富文本（实体注释即如此约定），
            这里按富文本渲染；内容只能由后台管理员写入。
          -->
          <div v-if="product.detail" class="rich" v-html="product.detail" />
          <p v-else class="detail-card__empty">该商品暂未填写图文详情</p>
        </section>

        <!--
          猜你喜欢：同分类优先、热销补齐，当前商品自身会被排除。
          三列小卡片（封面统一 1:1），比一列竖排短得多。
        -->
        <ProductSuggest
          v-if="product"
          variant="grid"
          :columns="3"
          :limit="6"
          :category-id="product.categoryId"
          :exclude-id="product.id"
        />
      </div>
    </template>

    <!-- 两个购买入口共用的选数量弹层 -->
    <QuantityDialog
      v-if="product"
      v-model:visible="dialogVisible"
      :product="product"
      :mode="dialogMode"
      @done="handlePurchased"
    />
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getCategories, getProduct } from '@/api/product'
import { formatAmount } from '@/utils/product'
import QuantityDialog from '@/components/QuantityDialog.vue'
import ProductSuggest from '@/components/ProductSuggest.vue'
import { useUserStore } from '@/store/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const product = ref(null)
const categoryNames = ref({})
const loading = ref(false)
const loadError = ref(false)
const dialogVisible = ref(false)
const dialogMode = ref('cart')

/** 当前商品所属分类名，用于面包屑 */
const categoryName = computed(() => categoryNames.value[product.value?.categoryId] || '')

/** 参数表：只用手上真实存在的字段 */
const specs = computed(() => {
  const p = product.value
  if (!p) return []
  return [
    { label: '商品编号', value: p.id },
    { label: '所属分类', value: categoryName.value || '未分类' },
    { label: '库存', value: `${p.stock} 件` },
    { label: '累计销量', value: `${p.salesCount} 件` },
    { label: '销售状态', value: p.status === 1 ? '在售' : '已下架' }
  ]
})

/** 分类树拍平成 id → 名称，面包屑与同类商品都要用 */
async function loadCategories() {
  try {
    const tree = await getCategories()
    const map = {}
    const walk = (nodes) => {
      nodes.forEach((node) => {
        map[node.id] = node.name
        walk(node.children || [])
      })
    }
    walk(tree)
    categoryNames.value = map
  } catch (e) {
    categoryNames.value = {}
  }
}

async function loadProduct(id) {
  loading.value = true
  loadError.value = false
  product.value = null
  try {
    const data = await getProduct(id)
    product.value = data
  } catch (e) {
    // 商品不存在或已下架时后端返回业务错误，这里降级为友好提示页
    loadError.value = true
  } finally {
    loading.value = false
  }
}

// 同类商品之间互相跳转时路由参数变化但组件复用，需要重新加载
watch(
  () => route.params.id,
  (id) => {
    if (id) {
      loadProduct(id)
    }
  },
  { immediate: true }
)

loadCategories()

/** 打开选数量弹层：两个入口都要先登录，差别只在 mode（去向不同） */
function openPurchaseDialog(mode) {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return
  }
  dialogMode.value = mode
  dialogVisible.value = true
}

/** 弹层里动作完成后的去向：加购去购物车，直接买去订单列表 */
function handlePurchased(mode) {
  router.push(mode === 'cart' ? '/cart' : '/orders')
}
</script>

<style scoped>
.detail {
  padding-top: var(--mp-space-lg);
  padding-bottom: var(--mp-space-2xl);
}

.detail__placeholder {
  min-height: 360px;
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

/* ==================== 面包屑 ==================== */
.crumb {
  display: flex;
  align-items: center;
  gap: var(--mp-space-xs);
  font-size: 13px;
  color: var(--mp-ink-secondary);
}

.crumb__link:hover {
  color: var(--mp-primary);
}

.crumb__sep {
  color: var(--mp-ink-muted);
}

.crumb__current {
  color: var(--mp-ink-muted);
}

/* ==================== 主区 ==================== */
.main {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
  gap: var(--mp-space-2xl);
  margin-top: var(--mp-space-lg);
}

/* 主图用全站统一的 1:1，与卡片、缩略图裁切一致 */
.main__media {
  aspect-ratio: 1 / 1;
  border-radius: var(--mp-radius-lg);
  overflow: hidden;
  background: var(--mp-surface-subdued);
}

.main__media img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.main__info {
  display: flex;
  flex-direction: column;
}

.main__name {
  font-size: 30px;
  line-height: 1.4;
}

.price-row {
  display: flex;
  align-items: baseline;
  gap: var(--mp-space-sm);
  margin-top: var(--mp-space-md);
  padding-bottom: var(--mp-space-md);
  border-bottom: 1px solid var(--mp-separator);
}

.price-row__price {
  font-size: 34px;
}

.price-row__original {
  font-size: 15px;
}

.price-row__sales {
  margin-left: auto;
  font-size: 13px;
  color: var(--mp-ink-muted);
}

.main__desc {
  margin-top: var(--mp-space-md);
  font-size: 14px;
  line-height: 1.7;
  color: var(--mp-ink-secondary);
}

/* ==================== 参数表 ==================== */
.specs {
  margin: var(--mp-space-md) 0 0;
  padding: var(--mp-space-sm) var(--mp-space-md);
  border-radius: var(--mp-radius);
  background: var(--mp-surface-subdued);
}

.specs__row {
  display: flex;
  gap: var(--mp-space-md);
  padding: 6px 0;
  font-size: 13px;
}

.specs__row + .specs__row {
  border-top: 1px solid rgba(31, 32, 34, 0.06);
}

.specs__label {
  width: 80px;
  flex-shrink: 0;
  color: var(--mp-ink-secondary);
}

.specs__value {
  margin: 0;
  color: var(--mp-ink);
}

/* ==================== 购买区 ==================== */
/*
  购买区吸底：主图是正方形（约 596px），比右栏内容高，
  让按钮贴着右栏底部就能与图片底边齐平，中间多出来的空白落在规格表与购买区之间。
*/
.buy__actions {
  margin-top: auto;
  padding-top: var(--mp-space-lg);
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--mp-space-sm);
}

.buy__actions .mp-btn {
  height: 48px;
  font-size: 15px;
}

/* ==================== 下区 ==================== */
/*
  下区：左图文详情、右猜你喜欢。
  推荐栏给到 440px（两列卡片需要这个宽度），商品详情相应收窄到 768px 上下，
  仍在舒适阅读宽度内。
*/
.body {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 440px;
  gap: var(--mp-space-xl);
  margin-top: var(--mp-space-2xl);
}

.detail-card__title {
  font-size: 20px;
  padding-bottom: var(--mp-space-sm);
  border-bottom: 1px solid var(--mp-separator);
}

.detail-card__empty {
  margin-top: var(--mp-space-md);
  font-size: 13px;
  color: var(--mp-ink-muted);
}

/* 富文本内容由 v-html 注入，不带作用域属性，需要 :deep 才能命中 */
.rich {
  margin-top: var(--mp-space-md);
  font-size: 14px;
  line-height: 1.9;
  color: var(--mp-ink-secondary);
}

.rich :deep(h2) {
  font-family: var(--mp-font-serif);
  font-size: 18px;
  color: var(--mp-ink);
  margin: var(--mp-space-lg) 0 var(--mp-space-xs);
}

.rich :deep(h2:first-child) {
  margin-top: 0;
}

.rich :deep(p) {
  margin: 0 0 var(--mp-space-xs);
}

.rich :deep(img) {
  border-radius: var(--mp-radius);
  margin: var(--mp-space-sm) 0;
}

/* ==================== 响应式 ==================== */
@media (max-width: 900px) {
  .main {
    grid-template-columns: 1fr;
    gap: var(--mp-space-lg);
  }

  .body {
    grid-template-columns: 1fr;
  }
}
</style>
