/**
 * AI 客服接口
 *
 * 对话是 SSE 流式返回，无法用 axios 的响应拦截器一次性解包，
 * 因此这里用原生 fetch + ReadableStream 手工解析事件流；
 * 会话列表与历史消息是普通 JSON 接口，仍走统一的 axios 封装。
 */
import request from '@/utils/request'
import { TOKEN_KEY } from '@/utils/request'

const BASE_URL = import.meta.env.VITE_API_BASE_URL || ''

/** 我的会话列表 */
export function getSessions() {
  return request({ url: '/api/portal/ai/sessions', method: 'get' })
}

/** 某个会话的历史消息 */
export function getSessionMessages(sessionId) {
  return request({
    url: `/api/portal/ai/sessions/${encodeURIComponent(sessionId)}/messages`,
    method: 'get'
  })
}

/** 删除一个会话（连同其全部消息） */
export function deleteSession(sessionId) {
  return request({
    url: `/api/portal/ai/sessions/${encodeURIComponent(sessionId)}`,
    method: 'delete'
  })
}

/** 清空当前用户的全部历史会话 */
export function clearSessions() {
  return request({ url: '/api/portal/ai/sessions', method: 'delete' })
}

/** 生成一个新的会话标识 */
export function createSessionId() {
  // crypto.randomUUID 在现代浏览器均可用；退化方案保证老环境也能用
  if (globalThis.crypto?.randomUUID) {
    return globalThis.crypto.randomUUID()
  }
  return `s-${Date.now()}-${Math.random().toString(16).slice(2, 10)}`
}

/**
 * 发起一次流式对话
 *
 * @param {Object}   options
 * @param {string}   options.sessionId 会话标识
 * @param {string}   options.message   提问内容
 * @param {Function} options.onContent 每收到一段内容时回调
 * @param {Function} options.onDone    正常结束时回调
 * @param {AbortSignal} options.signal 用于中断请求
 */
export async function streamChat({ sessionId, message, onContent, onDone, signal }) {
  const response = await fetch(`${BASE_URL}/api/portal/ai/chat`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${localStorage.getItem(TOKEN_KEY) || ''}`
    },
    body: JSON.stringify({ sessionId, message }),
    signal
  })

  if (!response.ok || !response.body) {
    throw new Error(response.status === 401 || response.status === 403 ? '登录已过期，请重新登录' : `AI 服务请求失败 (${response.status})`)
  }

  const reader = response.body.getReader()
  const decoder = new TextDecoder('utf-8')
  let buffer = ''

  while (true) {
    const { value, done } = await reader.read()
    if (done) {
      break
    }
    // 统一换行符，便于按空行切分事件
    buffer += decoder.decode(value, { stream: true }).replace(/\r\n/g, '\n')

    let boundary = buffer.indexOf('\n\n')
    while (boundary !== -1) {
      const rawEvent = buffer.slice(0, boundary)
      buffer = buffer.slice(boundary + 2)
      handleEvent(rawEvent, onContent, onDone)
      boundary = buffer.indexOf('\n\n')
    }
  }
}

/** 解析单个 SSE 事件并回调 */
function handleEvent(rawEvent, onContent, onDone) {
  const data = rawEvent
    .split('\n')
    .filter((line) => line.startsWith('data:'))
    .map((line) => line.slice(5).trim())
    .join('\n')

  if (!data) {
    return
  }

  try {
    const event = JSON.parse(data)
    if (event.type === 'content') {
      onContent?.(event.content)
    } else if (event.type === 'done') {
      onDone?.(event)
    }
  } catch (e) {
    // 极少数情况下会收到不完整片段，忽略即可，不影响后续事件
  }
}
