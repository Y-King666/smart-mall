<!--
  AI 客服对话面板

  AI 客服整页与右下角悬浮窗共用这一个组件，只通过 compact 属性切换尺寸密度。
  会话由外部传入（页面侧是用户选中的会话，悬浮窗侧是它自己新建的会话），
  面板只负责「展示历史 + 发送 + 流式接收」这三件事。

  流式接收期间会往最后一条 AI 气泡里不断追加内容，
  因此这里对消息数组的写入一律通过数组下标访问，保证走的是响应式代理。
-->
<template>
  <div class="chat" :class="{ 'chat--compact': compact }">
    <div class="chat__body">
      <div ref="scrollRef" class="chat__messages" @scroll="handleScroll">
        <!-- 空态：欢迎语 + 快捷问题 -->
        <div v-if="messages.length === 0" class="welcome">
          <p class="mp-serif welcome__title">您好！我是严选商城智能客服，有什么我可以帮您的？</p>
          <div class="welcome__quick">
            <button
              v-for="question in QUICK_QUESTIONS"
              :key="question"
              class="quick"
              type="button"
              :disabled="streaming"
              @click="ask(question)"
            >
              {{ question }}
            </button>
          </div>
        </div>

        <!-- 消息列表 -->
        <div v-for="(message, index) in messages" :key="index" class="msg" :class="`msg--${message.role}`">
          <span v-if="message.role === 'assistant'" class="msg__avatar">AI</span>
          <div class="msg__bubble">
            <!--
              首个字还没到达时先显示三点动画，否则会出现一个空气泡里只有光标在闪的画面
              高度与一行正文一致，文字到达时气泡不会忽大忽小
            -->
            <span v-if="thinking && index === messages.length - 1" class="typing">
              <span></span><span></span><span></span>
            </span>
            <template v-else>{{ renderText(message.content) }}</template>
          </div>
        </div>
      </div>

      <!-- 回到底部：上滑看不到最新消息时出现，点击平滑滚到底 -->
      <transition name="fade">
        <button
          v-if="showToBottom"
          class="chat__to-bottom"
          type="button"
          aria-label="回到最新消息"
          title="回到最新消息"
          @click="jumpToBottom"
        >
          <el-icon :size="16"><ArrowDown /></el-icon>
        </button>
      </transition>
    </div>

    <!-- 输入区 -->
    <form class="chat__composer" @submit.prevent="send">
      <textarea
        ref="inputRef"
        v-model="draft"
        class="chat__input"
        rows="1"
        :placeholder="placeholder"
        :disabled="streaming"
        @keydown.enter.exact.prevent="send"
      />
      <button class="mp-btn chat__send" type="submit" :disabled="!canSend">
        {{ streaming ? '回复中…' : '发送' }}
      </button>
    </form>
  </div>
</template>

<script setup>
import { computed, nextTick, onUnmounted, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { getSessionMessages, streamChat } from '@/api/ai'

const props = defineProps({
  /** 当前会话标识，由外部决定 */
  sessionId: { type: String, required: true },
  /** 紧凑模式：用于悬浮窗 */
  compact: { type: Boolean, default: false }
})

/** 一轮回复结束：外部据此刷新会话列表（标题与时间会随之更新） */
const emit = defineEmits(['replied'])

/**
 * 快捷问题：纯静态建议，点一下直接提问
 *
 * 都是 AI 依托 getMyOrders / getMyCart / getHotProducts / getProductInfo
 * 四个工具能实际答上来的服务类问题，不放它查不到数据的问法，
 * 否则点下去只会得到一句「无法查询」。
 */
const QUICK_QUESTIONS = [
  '查询我的历史订单',
  '查询我的购物车',
  '订单可以取消么？',
  '热销好物推荐'
]

const messages = ref([])
const draft = ref('')
const streaming = ref(false)
const thinking = ref(false)
const scrollRef = ref(null)
const inputRef = ref(null)

let controller = null

const canSend = computed(() => !streaming.value && draft.value.trim().length > 0)

/** 占位文案：窄窗里用短句，否则文字换行会让单行输入框冒出滚动条 */
const placeholder = computed(() =>
  props.compact ? '输入问题，回车发送' : '请输入您的问题，回车发送、Shift + 回车换行'
)

/** 是否停在消息区底部 */
const atBottom = ref(true)

/** 上滑离开底部、且已有消息时，露出「回到底部」按钮 */
const showToBottom = computed(() => messages.value.length > 0 && !atBottom.value)

/** 与底部的距离小于该值就算作「在底部」，避免像素级抖动让按钮忽隐忽现 */
const BOTTOM_THRESHOLD = 24

/** 滚动时更新「是否在底部」：既决定自动跟随，也决定按钮是否出现 */
function handleScroll() {
  const el = scrollRef.value
  if (!el) {
    return
  }
  atBottom.value = el.scrollHeight - el.scrollTop - el.clientHeight <= BOTTOM_THRESHOLD
}

/**
 * 滚到底部
 *
 * @param {boolean} smooth 平滑滚动。用户主动点击时用平滑，自动跟随时直接跳，
 *                         否则流式输出期间会一直重新触发动画
 */
function scrollToBottom(smooth = false) {
  nextTick(() => {
    const el = scrollRef.value
    if (!el) {
      return
    }
    if (smooth) {
      el.scrollTo({ top: el.scrollHeight, behavior: 'smooth' })
    } else {
      el.scrollTop = el.scrollHeight
    }
    atBottom.value = true
  })
}

/** 点击回到底部按钮 */
function jumpToBottom() {
  scrollToBottom(true)
}

/**
 * 轻量清洗模型输出
 *
 * 模型习惯按 Markdown 作答（** 加粗、* 列表、### 标题），而气泡按纯文本渲染，
 * 不清洗就会把标记符号直接展示给用户。
 * 这里只去掉最常见的几种标记；若日后要真正的富文本效果，应改为引入 Markdown 渲染库。
 */
function renderText(content) {
  return (content || '')
    .replace(/\*\*(.+?)\*\*/g, '$1')   // 加粗
    .replace(/^#{1,6}\s+/gm, '')       // 标题
    .replace(/^[*-]\s+/gm, '• ')       // 无序列表
}

/** 中断进行中的流式请求 */
function abortStream() {
  if (controller) {
    controller.abort()
    controller = null
  }
  streaming.value = false
  thinking.value = false
}

/** 切换会话时重新加载历史 */
async function loadHistory() {
  abortStream()
  messages.value = []
  if (!props.sessionId) {
    return
  }
  try {
    const history = await getSessionMessages(props.sessionId)
    messages.value = history.map((item) => ({ role: item.role, content: item.content }))
    scrollToBottom()
  } catch (e) {
    // 历史加载失败不阻塞新的对话
  }
}

function send() {
  const content = draft.value.trim()
  if (!content || streaming.value) {
    return
  }
  draft.value = ''
  ask(content)
}

/** 提问：先插入用户气泡与 AI 占位气泡，再流式填充 */
async function ask(content) {
  if (streaming.value) {
    return
  }

  messages.value.push({ role: 'user', content })
  messages.value.push({ role: 'assistant', content: '' })
  const replyIndex = messages.value.length - 1

  streaming.value = true
  thinking.value = true
  scrollToBottom()

  controller = new AbortController()
  try {
    await streamChat({
      sessionId: props.sessionId,
      message: content,
      signal: controller.signal,
      onContent: (chunk) => {
        thinking.value = false
        messages.value[replyIndex].content += chunk
        // 只在用户本就停在底部时自动跟随；他上滑回看历史时不要把他拽下来
        if (atBottom.value) {
          scrollToBottom()
        }
      }
    })
    if (messages.value[replyIndex].content) {
      emit('replied')
    }
  } catch (error) {
    if (error.name !== 'AbortError') {
      // 请求本身失败时，把原因写进气泡，避免留下一个空回复
      messages.value[replyIndex].content = messages.value[replyIndex].content || '抱歉，暂时无法获取回复，请稍后再试。'
      ElMessage.error(error.message || 'AI 服务暂时不可用')
    }
  } finally {
    streaming.value = false
    thinking.value = false
    controller = null
    scrollToBottom()
  }
}

watch(() => props.sessionId, loadHistory, { immediate: true })

onUnmounted(abortStream)

defineExpose({ focus: () => inputRef.value?.focus() })
</script>

<style scoped>
.chat {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
}

/* ==================== 消息区 ==================== */
/* 消息区的定位容器：回到底部按钮相对它定位，因此始终贴着输入框上沿 */
.chat__body {
  position: relative;
  flex: 1;
  min-height: 0;
  display: flex;
}

/* 只有这一层滚动：输入框固定不动，页面本身也不滚 */
.chat__messages {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  /* 滚到两端时不要把滚动传给页面：悬浮窗是小窗，背后页面被带着跑会很晕 */
  overscroll-behavior: contain;
  padding: var(--mp-space-lg);
  /* 列布局让欢迎语与消息自上而下依次排布 */
  display: flex;
  flex-direction: column;
}

/* ==================== 回到底部 ==================== */
.chat__to-bottom {
  position: absolute;
  left: 50%;
  bottom: 14px;
  z-index: 5;
  width: 34px;
  height: 34px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid var(--mp-outline);
  border-radius: var(--mp-radius-full);
  background: var(--mp-surface);
  color: var(--mp-ink);
  box-shadow: var(--mp-shadow-overlay);
  cursor: pointer;
  transform: translateX(-50%);
  transition: background-color 0.2s, border-color 0.2s, color 0.2s;
}

.chat__to-bottom:hover {
  background: var(--mp-primary);
  border-color: var(--mp-primary);
  color: #fff;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.18s;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* ==================== 空态 ==================== */
/*
  欢迎语与快捷问题贴着消息区顶部排布，与后续的 AI 气泡共用同一条左基线：
  提问后首条消息就出现在同一位置，内容不会整体跳一下。
*/
.welcome__title {
  font-size: 18px;
}

.welcome__quick {
  margin-top: var(--mp-space-lg);
  display: flex;
  flex-wrap: wrap;
  gap: var(--mp-space-xs);
}

.quick {
  height: 32px;
  padding: 0 var(--mp-space-md);
  border: 1px solid var(--mp-outline);
  border-radius: var(--mp-radius-full);
  background: var(--mp-surface);
  color: var(--mp-ink-secondary);
  font-family: var(--mp-font-sans);
  font-size: 13px;
  cursor: pointer;
  transition: border-color 0.2s, color 0.2s;
}

.quick:hover:not(:disabled) {
  border-color: var(--mp-primary);
  color: var(--mp-primary);
}

.quick:disabled {
  cursor: not-allowed;
  opacity: 0.6;
}

/* ==================== 消息气泡 ==================== */
.msg {
  display: flex;
  gap: var(--mp-space-xs);
  margin-bottom: var(--mp-space-md);
}

/* AI 在左、用户在右 */
.msg--user {
  flex-direction: row-reverse;
}

.msg__avatar {
  flex-shrink: 0;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--mp-radius-full);
  background: var(--mp-primary);
  color: #fff;
  font-size: 12px;
  font-weight: 600;
}

.msg__bubble {
  max-width: 78%;
  padding: var(--mp-space-sm) var(--mp-space-md);
  border-radius: var(--mp-radius-lg);
  background: var(--mp-surface);
  box-shadow: var(--mp-shadow-card);
  font-size: 14px;
  line-height: 1.7;
  color: var(--mp-ink);
  /* 模型可能返回换行，保留排版 */
  white-space: pre-wrap;
  word-break: break-word;
}

.msg--user .msg__bubble {
  background: var(--mp-primary);
  color: #fff;
  box-shadow: none;
}

/* 等待首个字到达时的三点动画 */
.typing {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  /* 与 .msg__bubble 的 line-height 对齐，气泡高度不跳动 */
  height: 1.7em;
}

.typing span {
  width: 6px;
  height: 6px;
  border-radius: var(--mp-radius-full);
  background: var(--mp-ink-muted);
  /* 三点错开相位，看起来像依次弹起 */
  animation: typing 1.2s ease-in-out infinite;
}

.typing span:nth-child(2) {
  animation-delay: 0.15s;
}

.typing span:nth-child(3) {
  animation-delay: 0.3s;
}

@keyframes typing {
  0%,
  60%,
  100% {
    opacity: 0.35;
    transform: translateY(0);
  }

  30% {
    opacity: 1;
    transform: translateY(-3px);
  }
}

/* ==================== 输入区 ==================== */
.chat__composer {
  flex-shrink: 0;
  display: flex;
  align-items: flex-end;
  gap: var(--mp-space-sm);
  padding: var(--mp-space-md) var(--mp-space-lg);
  border-top: 1px solid var(--mp-separator);
  background: var(--mp-surface);
}

.chat__input {
  flex: 1;
  max-height: 120px;
  padding: var(--mp-space-sm) var(--mp-space-md);
  border: 1px solid var(--mp-outline);
  border-radius: var(--mp-radius);
  background: var(--mp-surface-subdued);
  color: var(--mp-ink);
  font-family: var(--mp-font-sans);
  font-size: 14px;
  line-height: 1.6;
  resize: none;
  outline: none;
  transition: background-color 0.2s, border-color 0.2s;
}

.chat__input::placeholder {
  color: var(--mp-ink-muted);
}

.chat__input:focus {
  background: var(--mp-surface);
  border-color: var(--mp-primary);
}

.chat__send {
  height: 40px;
  flex-shrink: 0;
}

/* ==================== 紧凑模式（悬浮窗） ==================== */
.chat--compact .chat__messages {
  padding: var(--mp-space-md);
}

/* 窄窗里这行问候语会折成两行，字号调小一点 */
.chat--compact .welcome__title {
  font-size: 16px;
}

.chat--compact .welcome__quick {
  margin-top: var(--mp-space-md);
}

.chat--compact .msg__bubble {
  max-width: 86%;
  font-size: 13px;
}

.chat--compact .chat__composer {
  padding: var(--mp-space-sm) var(--mp-space-md);
}

.chat--compact .chat__input {
  font-size: 13px;
}
</style>
