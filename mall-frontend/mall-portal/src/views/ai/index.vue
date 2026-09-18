<!--
  AI 客服整页

  版式取自用户端设计稿的智能客服页，按约定的「标准」档实现：
  左侧历史咨询记录（新建会话 / 切换会话 / 续聊），右侧对话区。
-->
<template>
  <div class="ai mp-container">
    <div class="ai__layout">
      <!-- ==================== 会话侧栏 ==================== -->
      <aside class="sessions">
        <button class="mp-btn sessions__new" type="button" @click="startNewSession">
          新建对话
        </button>

        <div class="sessions__head">
          <span class="sessions__label">历史咨询记录</span>
          <button
            v-if="sessions.length"
            class="sessions__clear"
            type="button"
            @click="handleClearAll"
          >
            清空全部
          </button>
        </div>

        <p v-if="sessions.length === 0" class="sessions__empty">
          还没有咨询记录，在右侧提问后会自动保存
        </p>

        <ul v-else class="sessions__list">
          <li v-for="session in sessions" :key="session.sessionId" class="sessions__item">
            <button
              class="session"
              :class="{ 'session--active': session.sessionId === activeSessionId }"
              type="button"
              @click="selectSession(session.sessionId)"
            >
              <span class="mp-serif session__title">{{ session.title }}</span>
              <span class="session__meta">
                {{ session.lastTime }} · {{ session.messageCount }} 条
              </span>
            </button>

            <!--
              删除按钮与切换按钮是并列的两个按钮，不能嵌套
              （button 里套 button 是非法结构，浏览器会拆散）
            -->
            <button
              class="session__remove"
              type="button"
              :aria-label="`删除会话「${session.title}」`"
              title="删除会话"
              @click="handleDelete(session)"
            >
              <el-icon :size="13"><Delete /></el-icon>
            </button>
          </li>
        </ul>
      </aside>

      <!-- ==================== 对话区 ==================== -->
      <section class="ai__chat">
        <header class="ai__head">
          <h1 class="mp-serif ai__title">智能客服</h1>
          <p class="ai__subtitle">
            可以查询商品的价格与库存；每轮对话都会保存在左侧的历史记录中
          </p>
        </header>

        <div class="ai__panel">
          <AiChatPanel :session-id="activeSessionId" @replied="loadSessions" />
        </div>
      </section>

      <!-- 右栏：推荐商品，把对话区右侧的空白用起来 -->
      <ProductSuggest class="ai__suggest" title="智能客服推荐" />
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { clearSessions, createSessionId, deleteSession, getSessions } from '@/api/ai'
import AiChatPanel from '@/components/AiChatPanel.vue'
import ProductSuggest from '@/components/ProductSuggest.vue'

const sessions = ref([])
// 打开页面即进入一个新会话；提问后它会出现在左侧列表里
const activeSessionId = ref(createSessionId())

async function loadSessions() {
  try {
    sessions.value = await getSessions()
  } catch (e) {
    sessions.value = []
  }
}

/** 新建对话：换一个会话标识，面板会清空并显示欢迎语 */
function startNewSession() {
  activeSessionId.value = createSessionId()
}

/** 切换到历史会话 */
function selectSession(sessionId) {
  activeSessionId.value = sessionId
}

/** 清空全部历史会话 */
async function handleClearAll() {
  const totalMessages = sessions.value.reduce((sum, item) => sum + (item.messageCount || 0), 0)
  try {
    await ElMessageBox.confirm(
      `确定清空全部 ${sessions.value.length} 个会话吗？共 ${totalMessages} 条对话记录将被永久删除，无法恢复。`,
      '清空全部历史',
      { confirmButtonText: '全部清空', cancelButtonText: '取消', type: 'warning' }
    )
  } catch (e) {
    return   // 用户取消
  }

  try {
    await clearSessions()
    ElMessage.success('历史记录已清空')
    // 正在聊的会话也一并被清掉了，另起一个新的
    activeSessionId.value = createSessionId()
    await loadSessions()
  } catch (e) {
    // 错误已由请求拦截器提示
  }
}

/** 删除会话（连同其全部消息） */
async function handleDelete(session) {
  try {
    await ElMessageBox.confirm(
      `确定删除会话「${session.title}」吗？该会话的 ${session.messageCount} 条对话记录将被清除。`,
      '删除会话',
      { confirmButtonText: '删除', cancelButtonText: '取消', type: 'warning' }
    )
  } catch (e) {
    return   // 用户取消
  }

  try {
    await deleteSession(session.sessionId)
    ElMessage.success('会话已删除')
    // 删掉的正是当前正在聊的会话时，另起一个新会话，
    // 否则接着聊会往已被删除的会话里写，历史又冒出来
    if (session.sessionId === activeSessionId.value) {
      activeSessionId.value = createSessionId()
    }
    await loadSessions()
  } catch (e) {
    // 错误已由请求拦截器提示
  }
}

onMounted(loadSessions)
</script>

<style scoped>
/*
  对话页按「整屏」布局：
  顶栏以下的视窗高度全部交给内容区，对话面板因此始终占据视窗中央的那块区域，
  不会像固定高度那样在下方留出空白。页脚会被推到折叠线以下（对话页本就该是全屏的）。

  高度链路上每一层都要 min-height: 0，否则 flex 子项无法收缩，内部滚动区也就滚不起来。
*/
.ai {
  /*
    高度必须同时有上下限，缺一不可：
    - 只写 min-height 不行：那只是下限，消息一多整个面板就跟着长高，
      输入框被顶到屏幕外、消息区也不再有滚动条（回到底部按钮因此永不出现）
    - 只写固定高度也不行：视窗过矮时会把输入框挤出屏幕且滚不到
    所以取「顶栏以下的视窗高度」与 520px 中的较大值：
    正常视窗下内容锁在一屏内、内部滚动；视窗过矮时退回 520px，页面整体滚动。

    69px = 顶栏内容区 68px（--mp-header-height）+ 1px 下边框，见 layout 的 .nav / .nav__inner
  */
  height: max(520px, calc(100vh - 69px));
  display: flex;
  flex-direction: column;
  padding-top: var(--mp-space-lg);
  padding-bottom: var(--mp-space-lg);
}

.ai__layout {
  flex: 1;
  /* 视窗过矮时保底，此时页面恢复为整体滚动，而不是把对话区挤没 */
  min-height: 520px;
  display: grid;
  /* 会话栏 / 对话区 / 猜你喜欢，两侧都是固定宽，中间吃掉剩余空间 */
  grid-template-columns: 280px minmax(0, 1fr) 260px;
  gap: var(--mp-space-xl);
}

/* 推荐列表自己滚，不会把这一行撑高 */
.ai__suggest {
  min-height: 0;
}

/* ==================== 会话侧栏 ==================== */
.sessions {
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.sessions__new {
  width: 100%;
  height: 42px;
  flex-shrink: 0;
}

.sessions__head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: var(--mp-space-sm);
  margin: var(--mp-space-lg) 0 var(--mp-space-xs);
}

.sessions__label {
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.14em;
  color: var(--mp-ink-secondary);
}

/* 清空全部：弱化处理，避免与「新建对话」抢注意力 */
.sessions__clear {
  border: none;
  background: transparent;
  color: var(--mp-ink-muted);
  font-family: var(--mp-font-sans);
  font-size: 12px;
  cursor: pointer;
  transition: color 0.2s;
}

.sessions__clear:hover {
  color: var(--mp-primary);
  text-decoration: underline;
}

.sessions__empty {
  padding: var(--mp-space-md) 0;
  font-size: 13px;
  line-height: 1.6;
  color: var(--mp-ink-muted);
}

/* 会话列表自己滚动，会话再多也不会把整页撑高 */
.sessions__list {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  margin: 0;
  padding: 0;
  list-style: none;
}

.sessions__item {
  position: relative;
}

.session {
  width: 100%;
  /* 右侧留出删除按钮的位置，避免标题跑到按钮下面 */
  padding: var(--mp-space-sm) 36px var(--mp-space-sm) var(--mp-space-md);
  border: none;
  border-radius: var(--mp-radius);
  background: transparent;
  text-align: left;
  cursor: pointer;
  transition: background-color 0.2s;
}

/* 删除按钮平时隐去，悬停该项或键盘聚焦时出现 */
.session__remove {
  position: absolute;
  top: 10px;
  right: 8px;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  border-radius: var(--mp-radius-sm);
  background: transparent;
  color: var(--mp-ink-muted);
  cursor: pointer;
  opacity: 0;
  transition: opacity 0.2s, background-color 0.2s, color 0.2s;
}

.sessions__item:hover .session__remove,
.session__remove:focus-visible {
  opacity: 1;
}

.session__remove:hover {
  background: var(--mp-surface-subdued);
  color: var(--mp-primary);
}

.session:hover {
  background: var(--mp-surface-subdued);
}

.session--active {
  background: var(--mp-surface);
  box-shadow: var(--mp-shadow-card);
}

.session__title {
  display: block;
  font-size: 14px;
  line-height: 1.5;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.session--active .session__title {
  color: var(--mp-primary);
}

.session__meta {
  display: block;
  margin-top: 2px;
  font-size: 12px;
  color: var(--mp-ink-muted);
}

/* ==================== 对话区 ==================== */
/*
  整栏（标题 + 对话面板）铺在同一张白色卡片上，与页面底色 --mp-canvas 区分开，
  对话栏因此从页面里独立出来，标题也属于这一栏而不是浮在页面上。
  下侧不留内边距，让对话面板直接贴到卡片边缘。
*/
.ai__chat {
  display: flex;
  flex-direction: column;
  min-width: 0;
  min-height: 0;
  padding: var(--mp-space-lg) var(--mp-space-lg) 0;
  border-radius: var(--mp-radius-lg);
  overflow: hidden;
  background: var(--mp-surface);
  box-shadow: var(--mp-shadow-card);
}

.ai__title {
  font-size: 30px;
}

.ai__subtitle {
  margin-top: var(--mp-space-2xs);
  font-size: 13px;
  color: var(--mp-ink-secondary);
}

/*
  对话面板吃掉剩余高度，内部消息区自行滚动。
  底色改用分组容器色而非白色：白底上白色气泡几乎看不出边界。
*/
.ai__panel {
  flex: 1;
  min-height: 0;
  margin-top: var(--mp-space-md);
  border-radius: var(--mp-radius) var(--mp-radius) 0 0;
  overflow: hidden;
  background: var(--mp-surface-subdued);
}

/* ==================== 窄屏 ==================== */
/* 宽度不够时先收起右栏，保证对话区不至于被挤得太窄 */
@media (max-width: 1100px) {
  .ai__layout {
    grid-template-columns: 280px minmax(0, 1fr);
  }

  .ai__suggest {
    display: none;
  }
}

@media (max-width: 900px) {
  .ai__layout {
    grid-template-columns: 1fr;
    /* 单列时两栏是上下排布，各自给一个可用高度 */
    grid-template-rows: auto minmax(0, 1fr);
  }

  /* 会话列表在小屏上不再无限拉长，避免把对话区挤到屏幕外 */
  .sessions__list {
    max-height: 200px;
  }
}
</style>
