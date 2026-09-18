/**
 * 订单状态相关的公共映射
 *
 * 订单列表与订单详情都要展示状态标签、判断能做哪些操作，
 * 集中在这里维护，避免两处口径不一致。
 */

/** 支付状态 → 展示文案与标签样式（与后端 pay_status 的取值对应） */
const PAY_STATUS = {
  0: { text: '待支付', type: 'warning' },
  1: { text: '已支付', type: 'success' },
  2: { text: '已取消', type: 'info' }
}

/** 状态文案 */
export function payStatusText(status) {
  return PAY_STATUS[status]?.text || '未知状态'
}

/** 状态标签样式（Element Plus 的 el-tag type） */
export function payStatusType(status) {
  return PAY_STATUS[status]?.type || 'info'
}

/** 待支付订单可以支付 */
export function canPay(order) {
  return order?.payStatus === 0
}

/**
 * 能否取消
 *
 * 与本项目后端的口径一致：待支付与已支付订单都可以取消，
 * 已支付的取消时后端会恢复库存并扣回销量
 */
export function canCancel(order) {
  return order?.payStatus === 0 || order?.payStatus === 1
}
