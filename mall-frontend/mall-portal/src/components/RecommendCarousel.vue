<!--
  热销推荐 · 层叠式大卡片轮播（商品页开屏）

  取热销榜前 10 件，每次露出 3 张：
  - 正中是当前商品，尺寸最大、层级最高（z-index 30），完整可见
  - 左侧是循环里的上一件、右侧是下一件，缩小后退并向两侧让开，
    只露出朝外的一半；二者层级都在正中之下（z-index 20），被正中卡片压住一部分
  - 其余商品都叠在正中卡片背后（z-index 10、完全透明），轮到时才依次转到前排
  依次轮转，每一件都会占据正中主视图。两侧卡片点一下即可转到正中。

  卡片为「1:1 封面 + 下方信息条」，与商品网格的卡片语言保持一致。
  注意商品名在封面图里已经印了一次，因此信息条不再重复大字号标题以外的冗余元素。

  数据来源：商品列表接口按销量倒序（sort=sales）取前 10 件，
  排序在数据库里完成，因此取到的就是全站销量最高的几件。
-->
<template>
  <section v-if="items.length >= 3" class="rec">
    <header class="rec__head">
      <div>
        <p class="rec__eyebrow">HOT PICKS</p>
        <h2 class="mp-serif rec__title">热销甄选</h2>
      </div>
    </header>

    <div class="rec__viewport" @mouseenter="paused = true" @mouseleave="paused = false">
      <div class="rec__stage">
        <div
          v-for="(item, index) in items"
          :key="item.id"
          class="rec__card"
          :class="`rec__card--${slotOf(index)}`"
        >
          <!--
            用 router-link 的 custom 模式：它不再自行挂载点击处理，
            跳转与否完全由 handleCardClick 决定——正中才跳详情，两侧只转到正中。
            直接把 @click 挂在普通 router-link 上不可靠，RouterLink 自己的
            点击处理会先完成导航，preventDefault 来不及生效。
          -->
          <router-link :to="`/products/${item.id}`" custom v-slot="{ navigate }">
            <div class="rec__face" @click="handleCardClick($event, index, navigate)">
              <div class="rec__media">
                <img :src="item.coverImage" :alt="item.name" />
              </div>
              <div class="rec__body">
                <p class="mp-serif rec__name">{{ item.name }}</p>
                <p class="rec__desc">{{ item.description }}</p>
                <div class="rec__meta">
                  <span class="mp-price rec__price">
                    <span class="mp-price__symbol">¥</span>{{ formatAmount(item.price) }}
                  </span>
                  <!-- 与商品页商品卡保持一致：划线原价 + 金色"省¥N" -->
                  <span v-if="hasDiscount(item)" class="rec__original-group">
                    <span class="mp-price-original rec__original">
                      ¥{{ formatAmount(item.originalPrice) }}
                    </span>
                    <span class="mp-tag rec__saving">省¥{{ savingOf(item) }}</span>
                  </span>
                  <span class="rec__sales">已售 {{ item.salesCount }} 件</span>
                </div>
              </div>
            </div>
          </router-link>
        </div>
      </div>

      <!-- 轮播箭头：固定在推荐区域左右两侧、垂直居中 -->
      <button class="rec__arrow rec__arrow--prev" type="button" aria-label="上一件" @click="prev">
        <el-icon><ArrowLeft /></el-icon>
      </button>
      <button class="rec__arrow rec__arrow--next" type="button" aria-label="下一件" @click="next">
        <el-icon><ArrowRight /></el-icon>
      </button>
    </div>
  </section>
</template>

<script setup>
import { onMounted, onUnmounted, ref } from 'vue'
import { getProducts } from '@/api/product'
import { formatAmount, hasDiscount, savingOf } from '@/utils/product'

/** 轮播的商品数量：取热销榜前 10 件，依次轮转占据正中 */
const TOP_COUNT = 10

/** 自动轮转间隔（毫秒） */
const AUTO_PLAY_INTERVAL = 3500

const items = ref([])
const activeIndex = ref(0)
const paused = ref(false)

let timer = null

/**
 * 判断某个商品当前处在哪个位置
 *
 * 以循环相对位置（circular difference）计算它相对当前商品的位置：
 * 0 = 正中，1 = 右侧，n-1 = 左侧，其余（n 为 4 时即 2）藏在正中背后
 */
function slotOf(index) {
  const count = items.value.length
  if (!count) {
    return 'back'
  }
  const diff = (index - activeIndex.value + count) % count
  if (diff === 0) return 'center'
  if (diff === 1) return 'right'
  if (diff === count - 1) return 'left'
  return 'back'
}

async function load() {
  try {
    // 热销榜交给后端整体排序后取前几条。
    // 不能取「最新若干条」再在前端排序：商品一多，真正的高销量商品就进不了这若干条。
    const data = await getProducts({ page: 1, size: TOP_COUNT, sort: 'sales' })
    items.value = data.records || []
  } catch (e) {
    items.value = []
  }
}

function setActive(index) {
  activeIndex.value = index
}

/** 只有正中的卡片进入商品详情；两侧的卡片点到就把它转到正中 */
function handleCardClick(event, index, navigate) {
  if (slotOf(index) === 'center') {
    navigate(event)
    return
  }
  setActive(index)
}

function next() {
  if (items.value.length) {
    activeIndex.value = (activeIndex.value + 1) % items.value.length
  }
}

function prev() {
  if (items.value.length) {
    activeIndex.value = (activeIndex.value - 1 + items.value.length) % items.value.length
  }
}

function autoAdvance() {
  if (!paused.value) {
    next()
  }
}

onMounted(async () => {
  await load()
  timer = setInterval(autoAdvance, AUTO_PLAY_INTERVAL)
})

onUnmounted(() => {
  clearInterval(timer)
})
</script>

<style scoped>
.rec {
  /*
    卡片宽度随窗口收放；信息条高度固定，便于推算舞台总高。
    封面改成 1:1 后同样宽度会比原来高，这里把上限收小一点，
    免得开屏把下面的商品网格挤得太靠下。
  */
  --cover-w: clamp(220px, 30vw, 400px);
  --cover-body-h: 150px;
  padding: var(--mp-space-xl) 0 var(--mp-space-lg);
  border-bottom: 1px solid var(--mp-separator);
}

.rec__head {
  margin-bottom: var(--mp-space-lg);
}

.rec__eyebrow {
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.14em;
  color: var(--mp-primary);
}

.rec__title {
  margin-top: var(--mp-space-2xs);
  font-size: 24px;
}

/* 推荐区域的定位容器：卡片舞台由它包裹，箭头相对它左右贴边摆放 */
.rec__viewport {
  position: relative;
}

/* 箭头固定在推荐区域左右两侧、垂直居中；两侧卡片之外留有空档，不会压住正中的卡片 */
.rec__arrow {
  position: absolute;
  top: 50%;
  z-index: 40;
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid var(--mp-outline);
  border-radius: var(--mp-radius-full);
  background: var(--mp-surface);
  color: var(--mp-ink);
  box-shadow: var(--mp-shadow-card);
  cursor: pointer;
  transform: translateY(-50%);
  transition: background-color 0.2s, border-color 0.2s, color 0.2s;
}

.rec__arrow--prev {
  left: 0;
}

.rec__arrow--next {
  right: 0;
}

.rec__arrow:hover {
  background: var(--mp-primary);
  border-color: var(--mp-primary);
  color: #fff;
}

/* ==================== 层叠舞台 ==================== */
.rec__stage {
  position: relative;
  /* 1:1 图片 + 固定高度的信息条 */
  height: calc(var(--cover-w) + var(--cover-body-h));
  perspective: 1200px;   /* 两侧卡片的旋转由此产生景深，形成圆筒层叠感 */
}

.rec__card {
  position: absolute;
  top: 0;
  left: 50%;
  width: var(--cover-w);
  margin-left: calc(var(--cover-w) / -2);
  border-radius: var(--mp-radius-lg);
  overflow: hidden;
  background: var(--mp-surface);
  box-shadow: var(--mp-shadow-overlay);
  transition: transform 0.6s cubic-bezier(0.4, 0, 0.2, 1), opacity 0.6s;
}

/* 两侧卡片压一层很淡的暖白，强化「在正中卡片之下」的层次。
   过重的薄纱会把两侧压成幽灵卡，反而削弱层次，这里保持克制 */
.rec__card--left::after,
.rec__card--right::after {
  content: '';
  position: absolute;
  inset: 0;
  background: rgba(250, 247, 242, 0.26);
  pointer-events: none;
}

.rec__face {
  display: block;
  width: 100%;
  padding: 0;
  border: none;
  background: transparent;
  cursor: pointer;
  text-align: left;
  font: inherit;
  color: inherit;
}

.rec__media {
  aspect-ratio: 1 / 1;
  background: var(--mp-surface-subdued);
  overflow: hidden;
}

.rec__media img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.rec__body {
  height: var(--cover-body-h);
  padding: var(--mp-space-md) var(--mp-space-lg);
  display: flex;
  flex-direction: column;
}

.rec__name {
  font-size: 20px;
  line-height: 1.4;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.rec__desc {
  margin-top: var(--mp-space-2xs);
  font-size: 13px;
  line-height: 1.5;
  color: var(--mp-ink-secondary);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.rec__meta {
  margin-top: auto;
  display: flex;
  align-items: baseline;
  gap: var(--mp-space-sm);
  /* 卡片收窄时允许换行，避免价格与省额被挤压 */
  flex-wrap: wrap;
}

.rec__price {
  font-size: 24px;
}

/* 划线原价与"省¥N"作为一组，始终连在一起 */
.rec__original-group {
  display: inline-flex;
  align-items: center;
  gap: var(--mp-space-2xs);
}

.rec__original {
  font-size: 14px;
}

.rec__saving {
  align-self: center;
}

.rec__sales {
  margin-left: auto;
  font-size: 12px;
  color: var(--mp-ink-muted);
}

/* ==================== 三种位置 ==================== */
/* 正中：完整可见、层级最高、不旋转 */
.rec__card--center {
  z-index: 30;
  transform: translateX(0) scale(1);
}

/* 两侧：缩小后退并向两侧让开，只露出朝外的一半，被正中卡片压住一部分
   旋转方向决定整体是凸起还是内凹：
   - 内缘（靠正中那侧）朝前、外缘朝后 → 内缘被透视放大、外缘收窄，形成圆筒凸起
   - 反过来则会外缘朝前、内缘朝后，中间凹下去
   因此左侧用负角、右侧用正角（rotateY 为正时卡片右缘后退）。 */
.rec__card--left {
  z-index: 20;
  transform: translateX(-56%) scale(0.86) rotateY(-20deg);
}

.rec__card--right {
  z-index: 20;
  transform: translateX(56%) scale(0.86) rotateY(20deg);
}

/* 藏在正中背后 */
.rec__card--back {
  z-index: 10;
  opacity: 0;
  transform: translateX(0) scale(0.72);
  pointer-events: none;
}

/* ==================== 窄屏 ==================== */
@media (max-width: 720px) {
  .rec__title {
    font-size: 20px;
  }

  /* 窄屏下两侧卡片露出的部分太少，反而显乱，直接隐去 */
  .rec__card--left,
  .rec__card--right {
    opacity: 0;
  }
}
</style>
