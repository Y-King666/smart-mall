<!--
  商品页（全部好物，站点的落地页，地址 /products）

  设计取自用户端设计稿的首页，去掉了没有数据支撑的部分
  （编辑式营销大图、商品标签、AI 悬浮顾问等），只保留可用的筛选、搜索与商品网格。

  筛选条件全部写进地址栏（categoryId / keyword / page）：
  刷新、前进后退、分享链接都能还原同一份结果，也避免组件内再维护一套分页状态。
-->
<template>
  <div class="home mp-container">
    <!--
      开屏：热销推荐层叠轮播
      一旦有筛选条件（搜索或选分类）就收起它：
      否则顶栏搜索框与搜索结果之间隔着大半个轮播，用户必须下滚才能看到自己搜到了什么。
      清空筛选后自动恢复。
    -->
    <RecommendCarousel v-if="!hasFilter" />

    <!-- ==================== 页头 ==================== -->
    <header class="home__head">
      <div>
        <p class="home__eyebrow">ALL GOODS</p>
        <h1 class="mp-serif home__title">{{ currentCategoryName || '全部好物' }}</h1>
      </div>
      <p class="home__count">共 {{ total }} 款商品</p>
    </header>

    <!--
      当前生效的筛选条件
      实时搜索没有"提交"这个动作，必须把筛选状态显式摆出来，
      用户才知道自己正被什么条件过滤、以及怎么单独撤掉某一项
    -->
    <div v-if="hasFilter" class="filters">
      <span class="filters__label">筛选条件</span>
      <button v-if="queryKeyword" class="filters__chip" type="button" @click="clearKeyword">
        关键词：{{ queryKeyword }}
        <el-icon :size="12"><Close /></el-icon>
      </button>
      <button v-if="queryCategoryId" class="filters__chip" type="button" @click="clearCategory">
        分类：{{ selectedCategoryName }}
        <el-icon :size="12"><Close /></el-icon>
      </button>
      <button class="filters__reset" type="button" @click="clearFilter">全部清除</button>
    </div>

    <!-- ==================== 分类筛选 ==================== -->
    <div class="chips">
      <button
        class="chip"
        :class="{ 'chip--active': !queryCategoryId }"
        type="button"
        @click="selectCategory(null)"
      >
        全部好物
      </button>
      <button
        v-for="category in categoryOptions"
        :key="category.id"
        class="chip"
        :class="{ 'chip--active': queryCategoryId === category.id }"
        type="button"
        @click="selectCategory(category.id)"
      >
        {{ category.name }}
      </button>
    </div>

    <!-- ==================== 商品网格 ==================== -->
    <div v-loading="loading" class="home__body">
      <div v-if="!loading && products.length === 0" class="empty">
        <p class="mp-serif empty__title">没有找到相关商品</p>
        <p class="empty__hint">换个分类或关键词试试</p>
        <button v-if="hasFilter" class="mp-btn mp-btn--outline" type="button" @click="clearFilter">
          清除筛选条件
        </button>
      </div>

      <div v-else class="grid">
        <article v-for="product in products" :key="product.id" class="mp-card product">
          <router-link :to="`/products/${product.id}`" class="product__media">
            <img :src="product.coverImage" :alt="product.name" />
            <span v-if="product.stock <= 0" class="product__soldout">已售罄</span>
          </router-link>

          <div class="product__body">
            <h3 class="mp-serif product__name">
              <router-link :to="`/products/${product.id}`">{{ product.name }}</router-link>
            </h3>
            <p class="product__desc">{{ product.description || '暂无商品描述' }}</p>

            <div class="product__meta">
              <span class="mp-price product__price">
                <span class="mp-price__symbol">¥</span>{{ formatAmount(product.price) }}
              </span>
              <span v-if="hasDiscount(product)" class="product__original-group">
                <span class="mp-price-original">¥{{ formatAmount(product.originalPrice) }}</span>
                <!--
                  折扣信息挂在划线原价旁边，而不是做成独立角标：
                  折扣率在这批商品里几乎雷同（都在 5 折上下），立省金额才有区分度
                -->
                <span class="mp-tag product__saving">省¥{{ savingOf(product) }}</span>
              </span>
              <span class="product__sales">已售 {{ product.salesCount }} 件</span>
            </div>

            <!-- 购买动作统一放在商品详情页，卡片上只保留封面与标题两个进详情的入口 -->
          </div>
        </article>
      </div>
    </div>

    <!-- ==================== 分页 ==================== -->
    <div v-if="total > pageSize" class="home__pager">
      <el-pagination
        layout="prev, pager, next"
        background
        :current-page="queryPage"
        :page-size="pageSize"
        :total="total"
        @current-change="changePage"
      />
    </div>
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getCategories, getProducts } from '@/api/product'
import { formatAmount, hasDiscount, savingOf } from '@/utils/product'
import RecommendCarousel from '@/components/RecommendCarousel.vue'

const route = useRoute()
const router = useRouter()

const pageSize = 12

const products = ref([])
const categories = ref([])
const total = ref(0)
const loading = ref(false)

// ==================== 筛选条件（唯一来源是地址栏） ====================
const queryPage = computed(() => Number(route.query.page) || 1)
const queryCategoryId = computed(() => (route.query.categoryId ? Number(route.query.categoryId) : null))
const queryKeyword = computed(() => route.query.keyword || '')
const hasFilter = computed(() => Boolean(queryCategoryId.value) || Boolean(queryKeyword.value))

/**
 * 筛选用的分类：只取叶子分类
 *
 * 商品记录挂在叶子分类上（如"编程开发"），用顶级分类（如"数字课程"）作为
 * 查询条件一件商品也查不到。因此把分类树拍平成叶子列表，而不是直接展示顶级分类。
 */
const categoryOptions = computed(() => {
  const options = []
  const walk = (nodes) => {
    nodes.forEach((node) => {
      if (node.children && node.children.length) {
        walk(node.children)
      } else {
        options.push({ id: node.id, name: node.name })
      }
    })
  }
  walk(categories.value)
  return options
})

/** 当前选中的分类名 */
const selectedCategoryName = computed(() => {
  const found = categoryOptions.value.find((c) => c.id === queryCategoryId.value)
  return found ? found.name : ''
})

/** 页头标题：搜索中显示搜索说明，选中分类时显示分类名，否则显示"全部好物" */
const currentCategoryName = computed(() => {
  if (queryKeyword.value) return `“${queryKeyword.value}” 的搜索结果`
  return selectedCategoryName.value
})

// ==================== 数据 ====================
async function fetchProducts() {
  loading.value = true
  try {
    const data = await getProducts({
      page: queryPage.value,
      size: pageSize,
      keyword: queryKeyword.value || undefined,
      categoryId: queryCategoryId.value || undefined
    })
    products.value = data.records || []
    total.value = data.total || 0
  } catch (e) {
    products.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

async function fetchCategories() {
  try {
    categories.value = await getCategories()
  } catch (e) {
    categories.value = []
  }
}

// 地址栏是筛选条件的唯一来源，query 一变就重新拉取
watch(() => route.query, fetchProducts, { immediate: true })
fetchCategories()

// ==================== 交互 ====================
/** 金额与折扣的格式化在 utils/product.js 中统一维护 */

function selectCategory(categoryId) {
  // 切换分类时回到第一页，否则可能停在不存在的页上
  router.push({ path: '/products', query: buildQuery({ categoryId, page: 1 }) })
}

function changePage(page) {
  router.push({ path: '/products', query: buildQuery({ page }) })
}

function buildQuery({ categoryId, page }) {
  const query = { ...route.query }
  if (categoryId === undefined) {
    // 未显式指定则沿用当前值
  } else if (categoryId === null) {
    delete query.categoryId
  } else {
    query.categoryId = String(categoryId)
  }

  if (page === 1) {
    delete query.page
  } else {
    query.page = String(page)
  }
  return query
}

function clearFilter() {
  router.push({ path: '/products', query: {} })
}

/** 只撤掉关键词，保留分类筛选 */
function clearKeyword() {
  const query = { ...route.query }
  delete query.keyword
  delete query.page
  router.push({ path: '/products', query })
}

/** 只撤掉分类筛选，保留关键词 */
function clearCategory() {
  const query = { ...route.query }
  delete query.categoryId
  delete query.page
  router.push({ path: '/products', query })
}

</script>

<style scoped>
.home {
  padding-top: var(--mp-space-xl);
  padding-bottom: var(--mp-space-2xl);
}

/* ==================== 页头 ==================== */
.home__head {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: var(--mp-space-md);
}

.home__eyebrow {
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.14em;
  color: var(--mp-primary);
}

.home__title {
  margin-top: var(--mp-space-2xs);
  font-size: 30px;
}

.home__count {
  font-size: 13px;
  color: var(--mp-ink-secondary);
}

/* ==================== 当前筛选条件 ==================== */
.filters {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: var(--mp-space-xs);
  margin-top: var(--mp-space-md);
  padding: var(--mp-space-sm) var(--mp-space-md);
  border-radius: var(--mp-radius);
  background: var(--mp-surface-subdued);
  font-size: 13px;
}

.filters__label {
  color: var(--mp-ink-secondary);
}

.filters__chip {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  height: 26px;
  padding: 0 var(--mp-space-sm);
  border: none;
  border-radius: var(--mp-radius-full);
  background: var(--mp-surface);
  color: var(--mp-ink);
  font-family: var(--mp-font-sans);
  font-size: 12px;
  cursor: pointer;
  transition: background-color 0.2s, color 0.2s;
}

.filters__chip:hover {
  background: var(--mp-primary);
  color: #fff;
}

.filters__reset {
  margin-left: auto;
  border: none;
  background: transparent;
  color: var(--mp-primary);
  font-family: var(--mp-font-sans);
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
}

.filters__reset:hover {
  text-decoration: underline;
}

/* ==================== 分类筛选 ==================== */
.chips {
  display: flex;
  flex-wrap: wrap;
  gap: var(--mp-space-xs);
  margin-top: var(--mp-space-lg);
}

.chip {
  height: 36px;
  padding: 0 var(--mp-space-lg);
  border: none;
  border-radius: var(--mp-radius-full);
  background: var(--mp-surface-subdued);
  color: var(--mp-ink-secondary);
  font-family: var(--mp-font-sans);
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: background-color 0.2s, color 0.2s;
}

.chip:hover {
  color: var(--mp-ink);
}

.chip--active {
  background: var(--mp-primary);
  color: #fff;
}

.chip--active:hover {
  color: #fff;
}

/* ==================== 商品网格 ==================== */
.home__body {
  margin-top: var(--mp-space-lg);
  min-height: 200px;
}

.grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--mp-space-md);
  /* 让所有行等高，卡片高度不随各商品简介长短参差，按钮位置才能跨行对齐 */
  grid-auto-rows: 1fr;
}

.product {
  overflow: hidden;
  display: flex;
  flex-direction: column;
  transition: box-shadow 0.2s, transform 0.2s;
}

.product:hover {
  box-shadow: var(--mp-shadow-overlay);
  transform: translateY(-2px);
}

/* 全站封面统一 1:1，见 README「封面比例」一节 */
.product__media {
  position: relative;
  display: block;
  aspect-ratio: 1 / 1;
  background: var(--mp-surface-subdued);
}

.product__media img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.product__soldout {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.72);
  color: var(--mp-ink-secondary);
  font-size: 14px;
  font-weight: 600;
  letter-spacing: 0.1em;
}

.product__body {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: var(--mp-space-md);
}

.product__name {
  font-size: 16px;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.product__desc {
  margin-top: var(--mp-space-2xs);
  font-size: 13px;
  line-height: 1.5;
  color: var(--mp-ink-secondary);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  /* 简介按「最多两行」设计：只写了一行的商品也占满两行高度，
     否则下方价格与按钮会整体上移，与同排其他卡片错开 */
  min-height: 3em;
}

.product__meta {
  /* 把价格与按钮这一组推到卡片底部，使同排卡片的按钮严格对齐 */
  margin-top: auto;
  padding-top: var(--mp-space-sm);
  /* 价格靠左、销量靠右；剩余空间由价格后的自适应间距吸收 */
  display: flex;
  align-items: baseline;
  gap: var(--mp-space-xs);
  /* 卡片较窄时允许换行，避免原价与省额被挤压变形 */
  flex-wrap: wrap;
}

/* 划线原价与"省¥N"标签作为一组，始终连在一起 */
.product__original-group {
  display: inline-flex;
  align-items: center;
  gap: var(--mp-space-2xs);
}

.product__saving {
  align-self: center;
}

.product__price {
  font-size: 20px;
}

.product__sales {
  margin-left: auto;
  font-size: 12px;
  color: var(--mp-ink-muted);
}

/* ==================== 空态与分页 ==================== */
.empty {
  padding: var(--mp-space-2xl) 0;
  text-align: center;
}

.empty__title {
  font-size: 20px;
}

.empty__hint {
  margin: var(--mp-space-xs) 0 var(--mp-space-md);
  font-size: 13px;
  color: var(--mp-ink-secondary);
}

.home__pager {
  margin-top: var(--mp-space-xl);
  display: flex;
  justify-content: center;
}

/* ==================== 响应式 ==================== */
@media (max-width: 1100px) {
  .grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 820px) {
  .grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
