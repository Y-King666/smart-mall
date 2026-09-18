<!--
  用户端整体布局：顶部导航 + 内容区 + 页脚

  导航项只保留实际存在的功能入口（商品、我的订单），
  不放置没有对应页面的装饰性链接。
-->
<template>
  <div class="app">
    <!-- ==================== 顶部导航 ==================== -->
    <header class="nav">
      <div class="nav__inner mp-container">
        <router-link to="/products" class="nav__brand">
          <img src="/logo.png" alt="严选商城" class="nav__logo" />
          <span class="mp-serif nav__wordmark">严选商城</span>
        </router-link>

        <nav class="nav__links">
          <router-link
            v-for="item in navItems"
            :key="item.path"
            :to="item.path"
            class="nav__link"
            :class="{ 'nav__link--active': isActive(item) }"
          >
            {{ item.label }}
          </router-link>
        </nav>

        <!-- 智能客服：进入 AI 客服整页（悬浮窗在整页上会自动隐藏） -->
        <router-link to="/ai" class="nav__ai" title="智能客服">
          <el-icon :size="15"><MagicStick /></el-icon>
          智能客服
        </router-link>

        <form
          class="nav__search"
          :class="{ 'nav__search--filled': searchInput }"
          @submit.prevent="handleSearch"
        >
          <el-icon class="nav__search-icon"><Search /></el-icon>
          <input
            v-model="searchInput"
            class="nav__search-input"
            placeholder="搜索甄选生活良品…"
            @input="handleInput"
          />
          <button
            v-if="searchInput"
            class="nav__search-clear"
            type="button"
            aria-label="清空搜索"
            @click="handleClear"
          >
            <el-icon :size="14"><Close /></el-icon>
          </button>
        </form>

        <button class="nav__cart" type="button" title="购物车" @click="goCart">
          <el-icon :size="20"><ShoppingCart /></el-icon>
          <span v-if="cartStore.count > 0" class="nav__cart-badge">{{ cartStore.count }}</span>
        </button>

        <div class="nav__user">
          <template v-if="userStore.isLoggedIn">
            <el-dropdown trigger="click" @command="handleUserCommand">
              <span class="nav__user-trigger">
                <span class="nav__avatar">{{ userStore.displayName.charAt(0) }}</span>
                <span class="nav__user-name">{{ userStore.displayName }}</span>
                <el-icon :size="12"><ArrowDown /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="orders">我的订单</el-dropdown-item>
                  <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <router-link to="/login" class="nav__auth-link">登录</router-link>
            <router-link to="/register" class="mp-btn nav__register">注册</router-link>
          </template>
        </div>
      </div>
    </header>

    <!-- ==================== 内容区 ==================== -->
    <main class="app__main">
      <router-view />
    </main>

    <!--
      页脚
      AI 客服页是整屏对话布局（内容恰好占满一屏），
      页脚存在会让页面多出可滚动的高度、把固定不动的输入框也带着滚，因此该页隐去页脚。
    -->
    <footer v-if="!isFullScreenPage" class="foot">
      <div class="foot__inner mp-container">
        <div>
          <p class="mp-serif foot__brand">严选商城</p>
          <p class="foot__tagline">以东方极简美学，臻选日常良品</p>
        </div>
        <p class="foot__copy">© {{ year }} 严选商城</p>
      </div>
    </footer>

    <!-- ==================== AI 客服悬浮窗（各页面通用） ==================== -->
    <AiAssistantWidget />
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'
import { useCartStore } from '@/store/cart'
import AiAssistantWidget from '@/components/AiAssistantWidget.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const cartStore = useCartStore()

const year = new Date().getFullYear()
const searchInput = ref('')

/** 输入停顿多久后才真正去查，避免每敲一个字符就发一次请求 */
const SEARCH_DEBOUNCE = 350

let searchTimer = null

/** 导航项：与路由表保持一致，只放真实存在的页面 */
const navItems = [
  { label: '商品', path: '/products' },
  { label: '我的订单', path: '/orders' }
]

/** 进入某个栏目下的子页面（如商品详情）时，该栏目也算选中 */
function isActive(item) {
  return route.path.startsWith(item.path)
}

/** 整屏页面：这些页面自己占满一屏并负责内部滚动，不需要页脚 */
const isFullScreenPage = computed(() => route.path === '/ai')

// 地址栏 → 输入框：路由变化（后退、页面上清除筛选等）时回读，
// 否则输入框会残留已经不生效的旧关键词，与列表状态对不上
watch(
  () => route.query.keyword,
  (keyword) => {
    const next = keyword || ''
    if (next !== searchInput.value) {
      searchInput.value = next
    }
  },
  { immediate: true }
)

/** 输入框 → 地址栏：跟随输入实时筛选 */
function handleInput() {
  clearTimeout(searchTimer)

  // 清空时立即撤回，不等防抖——否则输入框已经空了、列表却还在筛
  if (!searchInput.value.trim()) {
    applyKeyword('')
    return
  }

  searchTimer = setTimeout(() => applyKeyword(searchInput.value.trim()), SEARCH_DEBOUNCE)
}

/** 回车：跳过防抖，立即生效 */
function handleSearch() {
  clearTimeout(searchTimer)
  applyKeyword(searchInput.value.trim())
}

/** 清空搜索（× 按钮） */
function handleClear() {
  clearTimeout(searchTimer)
  searchInput.value = ''
  applyKeyword('')
}

/**
 * 把关键词写入地址栏
 *
 * 地址栏是筛选条件的唯一来源，写入后商品页的 watch 会重新拉取列表。
 */
function applyKeyword(keyword) {
  if (keyword === (route.query.keyword || '')) {
    return   // 关键词没变，不必导航
  }

  // 从其他页面发起搜索时，不要把那个页面的查询参数一起带过来
  const query = route.path === '/products' ? { ...route.query } : {}

  if (keyword) {
    query.keyword = keyword
  } else {
    delete query.keyword
  }
  delete query.page   // 换了关键词就回到第一页

  // 用 replace：实时搜索触发频繁，不该在浏览器历史里留下每个中间状态
  router.replace({ path: '/products', query })
}

onUnmounted(() => {
  clearTimeout(searchTimer)
})

function goCart() {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push({ path: '/login', query: { redirect: '/cart' } })
    return
  }
  router.push('/cart')
}

function handleUserCommand(command) {
  if (command === 'orders') {
    router.push('/orders')
  } else if (command === 'logout') {
    userStore.logout()
    cartStore.clear()
    ElMessage.success('已退出登录')
    router.push('/products')
  }
}

// 顶栏可能在任何页面被看到，这里统一刷新一次购物车角标
onMounted(() => {
  cartStore.refresh()
})
</script>

<style scoped>
.app {
  min-height: 100%;
  display: flex;
  flex-direction: column;
}

.app__main {
  flex: 1;
}

/* ==================== 顶部导航 ==================== */
.nav {
  position: sticky;
  top: 0;
  z-index: 100;
  background: var(--mp-surface);
  border-bottom: 1px solid var(--mp-separator);
}

.nav__inner {
  height: var(--mp-header-height);
  display: flex;
  align-items: center;
  gap: var(--mp-space-lg);
}

.nav__brand {
  display: flex;
  align-items: center;
  gap: var(--mp-space-xs);
  flex-shrink: 0;
  /* 与右侧导航项拉开距离，站名不会贴着第一个导航项 */
  margin-right: var(--mp-space-lg);
}

.nav__logo {
  width: 36px;
  height: 36px;
  object-fit: contain;
  border-radius: var(--mp-radius-sm);
}

.nav__wordmark {
  font-size: 19px;
  letter-spacing: 0.02em;
}

.nav__links {
  display: flex;
  align-items: center;
  gap: var(--mp-space-lg);
  flex-shrink: 0;
}

.nav__link {
  position: relative;
  padding: 6px 0;
  font-size: 14px;
  color: var(--mp-ink-secondary);
  transition: color 0.2s;
}

.nav__link:hover {
  color: var(--mp-ink);
}

.nav__link--active {
  color: var(--mp-ink);
  font-weight: 600;
}

/* 命中态下方的小红点，取自设计规范的导航激活样式 */
.nav__link--active::after {
  content: '';
  position: absolute;
  left: 50%;
  bottom: -4px;
  width: 4px;
  height: 4px;
  margin-left: -2px;
  border-radius: var(--mp-radius-full);
  background: var(--mp-primary);
}

/* 智能客服入口：用一枚低调的胶囊与普通导航项区分开，
   更醒目的 AI 渐变留给右下角的悬浮球 */
.nav__ai {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  height: 32px;
  padding: 0 var(--mp-space-sm);
  border: 1px solid var(--mp-outline);
  border-radius: var(--mp-radius-full);
  color: var(--mp-ink);
  font-size: 13px;
  flex-shrink: 0;
  transition: background-color 0.2s, border-color 0.2s;
}

.nav__ai .el-icon {
  color: var(--mp-primary);
}

.nav__ai:hover {
  background: var(--mp-surface-subdued);
  border-color: var(--mp-primary);
}

.nav__search {
  position: relative;
  flex: 1;
  max-width: 320px;
  display: flex;
  align-items: center;
}

.nav__search-icon {
  position: absolute;
  left: 12px;
  color: var(--mp-ink-secondary);
}

/* 搜索框按规范：浅底、无硬边框，聚焦时转白底并加一道主色描边 */
.nav__search-input {
  width: 100%;
  height: 38px;
  padding: 0 var(--mp-space-md) 0 36px;
  border: 1px solid transparent;
  border-radius: var(--mp-radius-full);
  background: var(--mp-surface-subdued);
  color: var(--mp-ink);
  font-family: var(--mp-font-sans);
  font-size: 13px;
  outline: none;
  transition: background-color 0.2s, border-color 0.2s;
}

.nav__search-input::placeholder {
  color: var(--mp-ink-muted);
}

.nav__search-input:focus {
  background: var(--mp-surface);
  border-color: var(--mp-primary);
}

/* 有输入内容时给右侧清除按钮让出位置 */
.nav__search--filled .nav__search-input {
  padding-right: 34px;
}

.nav__search-clear {
  position: absolute;
  right: 8px;
  top: 50%;
  width: 22px;
  height: 22px;
  transform: translateY(-50%);
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  border-radius: var(--mp-radius-full);
  background: transparent;
  color: var(--mp-ink-secondary);
  cursor: pointer;
  transition: background-color 0.2s, color 0.2s;
}

.nav__search-clear:hover {
  background: var(--mp-surface-subdued);
  color: var(--mp-ink);
}

.nav__cart {
  position: relative;
  margin-left: auto;
  width: 38px;
  height: 38px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  border-radius: var(--mp-radius-full);
  background: transparent;
  color: var(--mp-ink);
  cursor: pointer;
  transition: background-color 0.2s;
}

.nav__cart:hover {
  background: var(--mp-surface-subdued);
}

/* 角标用第三色（珊瑚陶），与规范里「通知角标」的角色一致 */
.nav__cart-badge {
  position: absolute;
  top: 1px;
  right: 1px;
  min-width: 17px;
  height: 17px;
  padding: 0 4px;
  border-radius: var(--mp-radius-full);
  background: var(--mp-tertiary);
  color: #fff;
  font-size: 11px;
  font-weight: 600;
  line-height: 17px;
  text-align: center;
}

.nav__user {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  gap: var(--mp-space-sm);
}

.nav__user-trigger {
  display: flex;
  align-items: center;
  gap: var(--mp-space-xs);
  color: var(--mp-ink);
  cursor: pointer;
  outline: none;
}

.nav__avatar {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--mp-radius-full);
  background: var(--mp-primary);
  color: #fff;
  font-size: 14px;
  font-weight: 600;
}

.nav__user-name {
  font-size: 14px;
  max-width: 96px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.nav__auth-link {
  font-size: 14px;
  color: var(--mp-ink-secondary);
}

.nav__auth-link:hover {
  color: var(--mp-ink);
}

.nav__register {
  height: 34px;
  padding: 0 var(--mp-space-md);
  font-size: 13px;
}

/* ==================== 页脚 ==================== */
.foot {
  margin-top: var(--mp-space-2xl);
  border-top: 1px solid var(--mp-separator);
  background: var(--mp-surface);
}

.foot__inner {
  padding-top: var(--mp-space-xl);
  padding-bottom: var(--mp-space-xl);
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: var(--mp-space-md);
}

.foot__brand {
  font-size: 17px;
}

.foot__tagline {
  margin-top: var(--mp-space-2xs);
  font-size: 13px;
  color: var(--mp-ink-secondary);
}

.foot__copy {
  font-size: 12px;
  color: var(--mp-ink-muted);
}

/* 窄屏：搜索框收起，导航改为换行排列 */
@media (max-width: 900px) {
  .nav__inner {
    height: auto;
    min-height: var(--mp-header-height);
    flex-wrap: wrap;
    padding-top: var(--mp-space-sm);
    padding-bottom: var(--mp-space-sm);
    gap: var(--mp-space-md);
  }

  .nav__search {
    order: 3;
    max-width: none;
    width: 100%;
  }

  .foot__inner {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
