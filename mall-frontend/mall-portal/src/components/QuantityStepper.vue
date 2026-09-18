<!--
  数量步进器

  商品详情与购物车都用它，样式与边界逻辑只维护这一份。
  采用 v-model 写法：父组件持有真实数量（购物车的数量以服务端为准），
  组件只负责在边界内产生新值。
-->
<template>
  <div class="stepper">
    <button
      class="stepper__btn"
      type="button"
      aria-label="减少"
      :disabled="modelValue <= min"
      @click="step(-1)"
    >
      −
    </button>
    <span class="stepper__value">{{ modelValue }}</span>
    <button
      class="stepper__btn"
      type="button"
      aria-label="增加"
      :disabled="modelValue >= max"
      @click="step(1)"
    >
      +
    </button>
  </div>
</template>

<script setup>
const props = defineProps({
  /** 当前数量 */
  modelValue: { type: Number, required: true },
  /** 下限，默认 1 */
  min: { type: Number, default: 1 },
  /** 上限，商品场景传入库存 */
  max: { type: Number, default: Number.MAX_SAFE_INTEGER }
})

const emit = defineEmits(['update:modelValue'])

function step(delta) {
  const next = props.modelValue + delta
  if (next < props.min || next > props.max) {
    return
  }
  emit('update:modelValue', next)
}
</script>

<style scoped>
.stepper {
  display: inline-flex;
  align-items: center;
  border: 1px solid var(--mp-outline);
  border-radius: var(--mp-radius);
  overflow: hidden;
  width: fit-content;
}

.stepper__btn {
  width: 32px;
  height: 32px;
  border: none;
  background: var(--mp-surface);
  color: var(--mp-ink);
  font-size: 15px;
  line-height: 1;
  cursor: pointer;
  transition: background-color 0.2s;
}

.stepper__btn:hover:not(:disabled) {
  background: var(--mp-surface-subdued);
}

.stepper__btn:disabled {
  color: var(--mp-ink-muted);
  cursor: not-allowed;
}

.stepper__value {
  min-width: 42px;
  text-align: center;
  font-size: 14px;
  font-weight: 600;
}
</style>
