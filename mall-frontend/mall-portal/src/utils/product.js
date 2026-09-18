/**
 * 商品展示相关的公共计算
 *
 * 金额格式与折扣信息在商品页卡片、推荐轮播、商品详情都要用到，
 * 集中在这里维护，避免每个组件各写一份、口径跑偏。
 */

/** 金额统一保留两位小数展示 */
export function formatAmount(value) {
  return Number(value ?? 0).toFixed(2)
}

/** 原价高于现价才算有折扣可展示 */
export function hasDiscount(product) {
  return Number(product?.originalPrice) > Number(product?.price)
}

/**
 * 立省的金额，取整到元
 *
 * 促销文案里角分没有意义，取整更好读
 */
export function savingOf(product) {
  return Math.round(Number(product.originalPrice) - Number(product.price))
}
