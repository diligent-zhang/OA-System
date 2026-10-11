import { reactive } from 'vue'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client/dist/sockjs.min.js'
import { BASE_URL } from '@/api/request'
import { auth } from '@/stores/auth'
import { chatConversations } from '@/api'

/* 即时通讯：STOMP 连接 + 会话列表共享状态 */
export const chatState = reactive({
  connected: false,
  conversations: []
})

let client = null
const messageHandlers = new Set()
const connectedHandlers = new Set()

function emitConnected() {
  connectedHandlers.forEach((h) => {
    try {
      h()
    } catch (_) {
      /* 忽略单个订阅异常 */
    }
  })
}

function dispatchMessage(msg) {
  messageHandlers.forEach((h) => {
    try {
      h(msg)
    } catch (_) {
      /* 忽略单个处理异常 */
    }
  })
}

export function connectChat() {
  if (client) return
  client = new Client({
    // SockJS 走 http(s)，内部升级为 ws；token 经 CONNECT 帧头携带
    webSocketFactory: () => new SockJS(`${BASE_URL.replace(/\/api$/, '')}/ws`),
    connectHeaders: { Authorization: `Bearer ${auth.token}` },
    reconnectDelay: 5000,
    beforeConnect: () => {
      // Token 可能已续期：每次连接前刷新
      if (client) client.connectHeaders = { Authorization: `Bearer ${auth.token}` }
    },
    onConnect: () => {
      chatState.connected = true
      client.subscribe('/user/queue/chat', (frame) => {
        let msg
        try {
          msg = JSON.parse(frame.body)
        } catch (_) {
          return
        }
        dispatchMessage(msg)
        refreshConversations()
      })
      emitConnected()
    },
    onDisconnect: () => {
      chatState.connected = false
    },
    onStompError: () => {
      chatState.connected = false
    },
    onWebSocketClose: () => {
      chatState.connected = false
    }
  })
  client.activate()
}

export function disconnectChat() {
  if (client) {
    try {
      client.deactivate()
    } catch (_) {
      /* 忽略 */
    }
    client = null
  }
  chatState.connected = false
  chatState.conversations = []
}

export async function refreshConversations() {
  try {
    const res = await chatConversations()
    chatState.conversations = res.data || []
  } catch (_) {
    /* 静默失败 */
  }
}

export function onChatMessage(cb) {
  messageHandlers.add(cb)
  return () => messageHandlers.delete(cb)
}

/** 注册「已连接」回调；连接建立或重连后触发，用于（重新）订阅会话频道 */
export function onChatConnected(cb) {
  connectedHandlers.add(cb)
  if (chatState.connected) {
    try {
      cb()
    } catch (_) {
      /* 忽略 */
    }
  }
  return () => connectedHandlers.delete(cb)
}

/** 订阅指定会话的实时消息，返回取消订阅函数 */
export function subscribeConversation(conversationId, cb) {
  let sub = null
  const start = () => {
    if (!client || !chatState.connected) return
    sub = client.subscribe(`/topic/chat/${conversationId}`, (frame) => {
      let msg
      try {
        msg = JSON.parse(frame.body)
      } catch (_) {
        return
      }
      cb(msg)
    })
  }
  const off = onChatConnected(() => {
    if (sub) {
      try {
        sub.unsubscribe()
      } catch (_) {
        /* 忽略 */
      }
      sub = null
    }
    start()
  })
  return () => {
    off()
    if (sub) {
      try {
        sub.unsubscribe()
      } catch (_) {
        /* 忽略 */
      }
      sub = null
    }
  }
}
