<!--
  AI 客服悬浮窗

  有整个门户的页面都能看到：右下角一个悬浮球，点开是一个小对话窗。
  对话区直接复用整页用的 AiChatPanel，只是切成紧凑模式，
  因此两处的对话体验、流式渲染、错误处理完全一致。

  进入 AI 客服整页时自动隐藏，避免与页面本身重复。

  入口用设计规范为 AI 角色单独定义的渐变与光晕（--mp-ai-gradient / --mp-ai-halo）。
-->
<template>
  <div v-if="visible" class="assistant">
    <!-- ==================== 展开的对话窗 ==================== -->
    <transition name="pop">
      <section v-if="open" class="widget">
        <header class="widget__head">
          <div class="widget__heading">
            <span class="widget__badge">AI</span>
            <div>
              <p class="mp-serif widget__title">智能客服</p>
              <p class="widget__sub">商品、下单、售后都可以问我</p>
            </div>
          </div>
          <div class="widget__tools">
            <button
              v-if="userStore.isLoggedIn"
              class="widget__tool"
              type="button"
              @click="startNewSession"
            >
              新对话
            </button>
            <button class="widget__tool" type="button" aria-label="收起" @click="open = false">✕</button>
          </div>
        </header>

        <div class="widget__body">
          <!--
            未登录时不挂载对话面板：面板一挂载就会去拉历史会话，
            接口返回 401 后请求拦截器会把用户甩到登录页，而他只是想看一眼客服。
          -->
          <div v-if="!userStore.isLoggedIn" class="guest">
            <p class="mp-serif guest__title">登录后即可咨询</p>
            <!-- 标题栏已经说了顾问能答什么，这里只补充「登录后多出什么」 -->
            <p class="guest__hint">登录后还能查询你的订单与购物车</p>
            <router-link :to="loginTarget" class="mp-btn guest__login">去登录</router-link>
          </div>

          <AiChatPanel v-else :session-id="sessionId" compact />
        </div>
      </section>
    </transition>

    <!-- ==================== 悬浮球 ==================== -->
    <button
      class="ball"
      type="button"
      :aria-label="open ? '收起智能客服' : '打开智能客服'"
      @click="open = !open"
    >
      <el-icon :size="open ? 18 : 22">
        <Close v-if="open" />
        <ChatDotRound v-else />
      </el-icon>
    </button>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRoute } from 'vue-router'
import { createSessionId } from '@/api/ai'
import AiChatPanel from '@/components/AiChatPanel.vue'
import { useUserStore } from '@/store/user'

const route = useRoute()
const userStore = useUserStore()

/**
 * 悬浮窗的会话标识存在 sessionStorage 里
 *
 * 存内存的话，刷新页面就换了新会话、聊天记录也就没了；
 * 存 sessionStorage 则是「刷新不丢、标签页关掉才重置」，
 * 正好对应「把一次完整的浏览视为一个会话」。
 */
const SESSION_KEY = 'portal_ai_widget_session'

function ensureSessionId() {
  let id = sessionStorage.getItem(SESSION_KEY)
  if (!id) {
    id = createSessionId()
    sessionStorage.setItem(SESSION_KEY, id)
  }
  return id
}

const open = ref(false)
const sessionId = ref(ensureSessionId())

/** AI 客服整页上不再显示悬浮窗 */
const visible = computed(() => route.path !== '/ai')

/** 登录后回到当前页，用户可以直接再点开悬浮窗继续 */
const loginTarget = computed(() => ({ path: '/login', query: { redirect: route.fullPath } }))

function startNewSession() {
  sessionId.value = createSessionId()
  sessionStorage.setItem(SESSION_KEY, sessionId.value)
}
</script>

<style scoped>
/* ==================== 悬浮球 ==================== */
.ball {
  position: fixed;
  right: 24px;
  bottom: 24px;
  z-index: 200;
  width: 54px;
  height: 54px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  border-radius: var(--mp-radius-full);
  background: var(--mp-ai-gradient);
  color: #fff;
  box-shadow: var(--mp-ai-halo);
  cursor: pointer;
  transition: transform 0.2s;
}

.ball:hover {
  transform: scale(1.06);
}

/* ==================== 对话窗 ==================== */
.widget {
  position: fixed;
  right: 24px;
  bottom: 92px;
  z-index: 200;
  width: 380px;
  height: 520px;
  display: flex;
  flex-direction: column;
  border-radius: var(--mp-radius-xl);
  overflow: hidden;
  background: var(--mp-canvas);
  box-shadow: var(--mp-shadow-overlay);
  /*
    面板内任何位置（含标题栏、输入区）的滚动都不再传给页面。
    悬浮窗是浮在长页面上的小窗，滚动"漏"到背后会让页面莫名跟着跑。
  */
  overscroll-behavior: contain;
}

.widget__head {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--mp-space-sm);
  padding: var(--mp-space-md) var(--mp-space-md) var(--mp-space-sm);
  background: var(--mp-surface);
  border-bottom: 1px solid var(--mp-separator);
}

.widget__heading {
  display: flex;
  align-items: center;
  gap: var(--mp-space-xs);
  min-width: 0;
}

.widget__badge {
  flex-shrink: 0;
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--mp-radius-full);
  background: var(--mp-ai-gradient);
  color: #fff;
  font-size: 11px;
  font-weight: 600;
}

.widget__title {
  font-size: 15px;
  line-height: 1.3;
}

.widget__sub {
  font-size: 11px;
  color: var(--mp-ink-muted);
}

.widget__tools {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  gap: var(--mp-space-2xs);
}

.widget__tool {
  height: 26px;
  padding: 0 var(--mp-space-xs);
  border: none;
  border-radius: var(--mp-radius-sm);
  background: transparent;
  color: var(--mp-ink-secondary);
  font-family: var(--mp-font-sans);
  font-size: 12px;
  cursor: pointer;
  transition: background-color 0.2s, color 0.2s;
}

.widget__tool:hover {
  background: var(--mp-surface-subdued);
  color: var(--mp-ink);
}

.widget__body {
  flex: 1;
  min-height: 0;
}

/* ==================== 未登录态 ==================== */
/* 内容在面板里居中，与对话区占位一致 */
.guest {
  height: 100%;
  padding: var(--mp-space-lg);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
}

.guest__title {
  font-size: 17px;
}

.guest__hint {
  margin-top: var(--mp-space-2xs);
  font-size: 13px;
  color: var(--mp-ink-secondary);
}

.guest__login {
  margin-top: var(--mp-space-lg);
}

/* ==================== 展开动画 ==================== */
.pop-enter-active,
.pop-leave-active {
  transition: opacity 0.18s ease, transform 0.18s ease;
}

.pop-enter-from,
.pop-leave-to {
  opacity: 0;
  transform: translateY(8px);
}

/* ==================== 窄屏：占满可用宽度 ==================== */
@media (max-width: 520px) {
  .widget {
    right: 12px;
    left: 12px;
    bottom: 84px;
    width: auto;
    height: 70vh;
  }

  .ball {
    right: 16px;
    bottom: 16px;
  }
}
</style>
