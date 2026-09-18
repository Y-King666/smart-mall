<!--
  猜你喜欢

  两处共用：AI 客服整页的右栏、商品详情页的右栏。

  取法：传了 categoryId 就先取同分类的在售商品，不够的再用热销榜补齐——
  既保证相关性，又不会因为某个分类商品太少而只列出一两件。
  传 excludeId 可排除当前商品自身。

  列表自身可滚动，放进定高的栏（AI 整页那一栏）时不会把整行撑高。
-->
<template>
  <aside v-if="items.length" class="suggest">
    <h2 class="mp-serif suggest__title">{{ title }}</h2>
    <div class="suggest__list" :class="{ 'suggest__list--grid': isGrid }" :style="gridStyle">
      <router-link
        v-for="item in items"
        :key="item.id"
        :to="`/products/${item.id}`"
        class="suggest__item"
        :class="{ 'suggest__item--card': isGrid }"
      >
        <img :src="item.coverImage" :alt="item.name" class="suggest__thumb" />
        <div class="suggest__info">
          <p class="suggest__name">{{ item.name }}</p>
          <span class="mp-price suggest__price">
            <span class="mp-price__symbol">¥</span>{{ formatAmount(item.price) }}
          </span>
          <span class="suggest__sales">已售 {{ item.salesCount }} 件</span>
        </div>
      </router-link>
    </div>
  </aside>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { getProducts } from '@/api/product'
import { formatAmount } from '@/utils/product'

const props = defineProps({
  /** 区块标题：商品详情页是「猜你喜欢」，AI 客服页是「智能客服推荐」 */
  title: { type: String, default: '猜你喜欢' },
  /**
   * 版式：list=一行一件（窄栏，如 AI 客服页右栏）；
   * grid=多列卡片（放在宽栏里，避免一长条竖向排布）
   */
  variant: { type: String, default: 'list' },
  /** 仅 grid 版式生效：排几列 */
  columns: { type: Number, default: 2 },
  /** 优先推荐的分类，不传则只按热销榜推荐 */
  categoryId: { type: Number, default: null },
  /** 需要排除的商品（详情页传当前商品，免得推荐到自己） */
  excludeId: { type: Number, default: null },
  /**
   * 最多推荐几件
   *
   * 6 是照 AI 页右栏的可用高度定的（实测列表可视 ~744px、每项 ~106px，最多放 7 件），
   * 取 6 件正好填满并留一点余量。
   */
  limit: { type: Number, default: 6 }
})

const items = ref([])

const isGrid = computed(() => props.variant === 'grid')

const gridStyle = computed(() =>
  isGrid.value ? { gridTemplateColumns: `repeat(${props.columns}, minmax(0, 1fr))` } : null
)

async function load() {
  const picked = []
  const seen = new Set()
  if (props.excludeId) {
    seen.add(props.excludeId)
  }

  /** 依次收下没出现过的商品，收满为止 */
  const take = (list) => {
    for (const product of list) {
      if (picked.length >= props.limit) {
        return
      }
      if (seen.has(product.id)) {
        continue
      }
      seen.add(product.id)
      picked.push(product)
    }
  }

  try {
    if (props.categoryId) {
      const data = await getProducts({ categoryId: props.categoryId, page: 1, size: props.limit + 1 })
      take(data.records || [])
    }
    if (picked.length < props.limit) {
      const hot = await getProducts({ page: 1, size: props.limit + 2, sort: 'sales' })
      take(hot.records || [])
    }
  } catch (e) {
    // 推荐拿不到不影响主内容，留空即可
  }

  items.value = picked
}

// 详情页在同类商品之间跳转时组件会复用，靠这两个参数变化重新取数
watch(() => [props.categoryId, props.excludeId], load, { immediate: true })
</script>

<style scoped>
.suggest {
  display: flex;
  flex-direction: column;
  /* 放进定高栏时可收缩，让下面的列表自己去滚 */
  min-height: 0;
}

.suggest__title {
  flex-shrink: 0;
  font-size: 17px;
}

.suggest__list {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  margin-top: var(--mp-space-sm);
}

/* 多列卡片版式：整块不再拉成一条竖线，也就无需自身滚动 */
.suggest__list--grid {
  flex: none;
  display: grid;
  gap: var(--mp-space-md);
  overflow: visible;
}

.suggest__item {
  display: flex;
  gap: var(--mp-space-sm);
  padding: var(--mp-space-xs);
  border-radius: var(--mp-radius);
  transition: background-color 0.2s;
}

.suggest__item:hover {
  background: var(--mp-surface-subdued);
}

/* 卡片式：图片在上、文字在下，与首页商品卡的语言一致 */
.suggest__item--card {
  display: block;
  padding: 0;
}

.suggest__item--card:hover {
  background: transparent;
}

.suggest__item--card:hover .suggest__name {
  color: var(--mp-primary);
}

.suggest__thumb {
  flex-shrink: 0;
  width: 72px;
  height: 72px;
  border-radius: var(--mp-radius-sm);
  object-fit: cover;
  background: var(--mp-surface-subdued);
}

/*
  卡片式的图用 4:3，比首页商品卡的 4:5 扁一些：
  推荐是次要内容，图矮一点整块才不会又变成一长条。
*/
.suggest__item--card .suggest__thumb {
  width: 100%;
  height: auto;
  aspect-ratio: 1 / 1;
}

.suggest__info {
  min-width: 0;
}

.suggest__item--card .suggest__info {
  margin-top: var(--mp-space-xs);
}

.suggest__item--card .suggest__name {
  font-size: 14px;
}

.suggest__name {
  font-size: 13px;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.suggest__price {
  display: block;
  margin-top: var(--mp-space-2xs);
  font-size: 15px;
}

.suggest__sales {
  display: block;
  margin-top: 2px;
  font-size: 12px;
  color: var(--mp-ink-muted);
}
</style>
