<script setup>
import { computed, nextTick, onMounted, onUnmounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { chatOpen, chatMessages, chatMarkRead, chatSend, contactsQuery } from '@/api'
import { chatState, connectChat, refreshConversations, subscribeConversation } from '@/stores/chat'
import { auth } from '@/stores/auth'
import UserAvatar from '@/components/UserAvatar.vue'

const route = useRoute()
const router = useRouter()

const active = ref(null)
const messages = ref([])
const input = ref('')
const sending = ref(false)
const loadingMsg = ref(false)
const scrollRef = ref()

const pickerVisible = ref(false)
const pickerLoading = ref(false)
const contacts = ref([])
const selected = ref([])

const conversations = computed(() => chatState.conversations)

function fmtTime(v) {
  return v ? String(v).slice(5, 16) : ''
}

async function loadConversations() {
  await refreshConversations()
}

async function openConversation(conv) {
  active.value = conv
  router.replace(`/chat/${conv.conversationId}`)
  await loadMessages()
  try {
    await chatMarkRead(conv.conversationId)
    loadConversations()
  } catch (_) {
    /* 已读失败不影响聊天 */
  }
}

async function loadMessages() {
  if (!active.value) return
  loadingMsg.value = true
  try {
    const res = await chatMessages(active.value.conversationId, { pageNum: 1, pageSize: 50 })
    messages.value = res.data || []
    scrollBottom()
  } finally {
    loadingMsg.value = false
  }
}

function appendMessage(m) {
  if (!m || messages.value.some((x) => x.msgId === m.msgId)) return
  messages.value.push(m)
  scrollBottom()
}

function scrollBottom() {
  nextTick(() => {
    const el = scrollRef.value
    if (el) el.scrollTop = el.scrollHeight
  })
}

async function send() {
  const content = input.value.trim()
  if (!content) return
  if (!active.value) {
    ElMessage.warning('请先选择会话')
    return
  }
  sending.value = true
  try {
    const res = await chatSend({ conversationId: active.value.conversationId, content })
    appendMessage(res.data)
    input.value = ''
    loadConversations()
  } finally {
    sending.value = false
  }
}

/* 切换会话时订阅其实时消息频道 */
let unsubConv = null
watch(active, (conv) => {
  if (unsubConv) {
    unsubConv()
    unsubConv = null
  }
  if (!conv) return
  unsubConv = subscribeConversation(conv.conversationId, (msg) => {
    appendMessage(msg)
    if (active.value && msg.senderUserId !== auth.user?.userId) {
      chatMarkRead(active.value.conversationId).then(loadConversations).catch(() => {})
    }
  })
})

async function openPicker() {
  pickerVisible.value = true
  selected.value = []
  if (!contacts.value.length) await loadContacts()
}

async function loadContacts() {
  pickerLoading.value = true
  try {
    const res = await contactsQuery({})
    contacts.value = (res.data || []).filter((c) => c.userId && c.userId !== auth.user?.userId)
  } finally {
    pickerLoading.value = false
  }
}

async function startChat() {
  if (!selected.value.length) {
    ElMessage.warning('请选择至少一位成员')
    return
  }
  let type = 'SINGLE'
  let groupName
  if (selected.value.length > 1) {
    type = 'GROUP'
    try {
      const { value } = await ElMessageBox.prompt('请输入群聊名称', '发起群聊', {
        inputValue: '工作群聊',
        inputValidator: (v) => (v && v.trim() ? true : '请输入群名称')
      })
      groupName = value.trim()
    } catch (_) {
      return
    }
  }
  const res = await chatOpen({ type, memberUserIds: selected.value, groupName })
  pickerVisible.value = false
  await loadConversations()
  const conv = conversations.value.find((c) => c.conversationId === res.data)
  openConversation(conv || { conversationId: res.data, type, displayName: '会话' })
}

onMounted(async () => {
  connectChat()
  await loadConversations()
  const target = Number(route.params.id)
  if (target) {
    const conv = conversations.value.find((c) => c.conversationId === target)
    if (conv) openConversation(conv)
  }
})

onUnmounted(() => {
  if (unsubConv) unsubConv()
})
</script>

<template>
  <div class="chat-page">
    <div class="chat-shell">
      <!-- 左：会话列表 -->
      <aside class="conv-pane">
        <div class="conv-head">
          <span class="conv-title">会话
            <el-tag v-if="!chatState.connected" size="small" type="info" effect="plain">连接中…</el-tag>
          </span>
          <el-button size="small" type="primary" @click="openPicker">发起会话</el-button>
        </div>
        <el-scrollbar class="conv-scroll">
          <el-empty v-if="!conversations.length" description="暂无会话" :image-size="60" />
          <div
            v-for="c in conversations"
            :key="c.conversationId"
            class="conv-item"
            :class="{ active: active && active.conversationId === c.conversationId }"
            role="button"
            tabindex="0"
            @click="openConversation(c)"
            @keyup.enter="openConversation(c)"
          >
            <UserAvatar :user-id="c.peerUserId" :name="c.displayName" :has-avatar="true" :size="38" />
            <div class="conv-main">
              <div class="conv-top">
                <span class="conv-name">{{ c.displayName }}</span>
                <span class="conv-time">{{ fmtTime(c.lastTime) }}</span>
              </div>
              <div class="conv-bottom">
                <span class="conv-last">{{ c.lastContent || '暂无消息' }}</span>
                <el-badge v-if="c.unreadCount" :value="c.unreadCount" :max="99" />
              </div>
            </div>
          </div>
        </el-scrollbar>
      </aside>

      <!-- 右：消息面板 -->
      <section class="msg-pane">
        <template v-if="active">
          <div class="msg-head">
            <UserAvatar :user-id="active.peerUserId" :name="active.displayName" :has-avatar="true" :size="30" />
            <span class="msg-head-title">{{ active.displayName }}</span>
          </div>
          <div ref="scrollRef" v-loading="loadingMsg" class="msg-scroll">
            <div
              v-for="m in messages"
              :key="m.msgId"
              class="msg-row"
              :class="{ mine: m.senderUserId === auth.user?.userId }"
            >
              <UserAvatar
                v-if="m.senderUserId !== auth.user?.userId"
                :user-id="m.senderUserId"
                :name="m.senderName"
                :has-avatar="true"
                :size="32"
              />
              <div class="bubble-wrap">
                <div v-if="active.type === 'GROUP' && m.senderUserId !== auth.user?.userId" class="bubble-sender">
                  {{ m.senderName }}
                </div>
                <div class="bubble">{{ m.content }}</div>
                <div class="bubble-time">{{ fmtTime(m.createTime) }}</div>
              </div>
              <UserAvatar
                v-if="m.senderUserId === auth.user?.userId"
                :user-id="m.senderUserId"
                :name="m.senderName"
                :has-avatar="true"
                :size="32"
              />
            </div>
          </div>
          <div class="msg-input">
            <el-input
              v-model="input"
              type="textarea"
              :rows="2"
              resize="none"
              maxlength="2000"
              placeholder="输入消息，Enter 发送，Shift+Enter 换行"
              @keydown.enter.exact.prevent="send"
            />
            <el-button type="primary" :loading="sending" @click="send">发送</el-button>
          </div>
        </template>
        <el-empty v-else description="选择左侧会话开始聊天" />
      </section>
    </div>

    <!-- 成员选择 -->
    <el-dialog v-model="pickerVisible" title="选择会话成员" width="460px">
      <div v-loading="pickerLoading" class="pick-list">
        <el-checkbox-group v-model="selected">
          <el-checkbox v-for="c in contacts" :key="c.userId" :value="c.userId" class="pick-item">
            <UserAvatar :user-id="c.userId" :name="c.name" :has-avatar="true" :size="26" class="pick-avatar" />
            {{ c.name }}
            <span class="pick-dept">{{ c.deptName || '' }}</span>
          </el-checkbox>
        </el-checkbox-group>
      </div>
      <template #footer>
        <span class="pick-tip">选 1 人单聊，选多人发起群聊</span>
        <el-button @click="pickerVisible = false">取消</el-button>
        <el-button type="primary" @click="startChat">开始</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.chat-page { height: calc(100vh - 96px); }
.chat-shell { display: flex; height: 100%; border: 1px solid var(--oa-line); border-radius: 6px; overflow: hidden; background: #fff; }

.conv-pane { width: 280px; flex-shrink: 0; border-right: 1px solid var(--oa-line); display: flex; flex-direction: column; }
.conv-head { height: 52px; display: flex; align-items: center; justify-content: space-between; padding: 0 14px; border-bottom: 1px solid var(--oa-line); }
.conv-title { font-weight: 600; color: var(--oa-navy); display: flex; align-items: center; gap: 8px; }
.conv-scroll { flex: 1; }
.conv-item { display: flex; gap: 10px; padding: 10px 14px; cursor: pointer; border-bottom: 1px solid var(--oa-surface); }
.conv-item:hover { background: var(--oa-surface); }
.conv-item.active { background: var(--el-color-primary-light-9); }
.conv-main { flex: 1; min-width: 0; }
.conv-top { display: flex; justify-content: space-between; align-items: center; }
.conv-name { font-size: 14px; color: var(--oa-navy); font-weight: 500; }
.conv-time { font-size: 11px; color: var(--oa-ink-light); font-family: var(--oa-mono); }
.conv-bottom { display: flex; justify-content: space-between; align-items: center; margin-top: 3px; }
.conv-last { font-size: 12px; color: var(--oa-ink-light); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }

.msg-pane { flex: 1; min-width: 0; display: flex; flex-direction: column; }
.msg-head { height: 52px; display: flex; align-items: center; gap: 10px; padding: 0 16px; border-bottom: 1px solid var(--oa-line); }
.msg-head-title { font-weight: 600; color: var(--oa-navy); }
.msg-scroll { flex: 1; overflow: auto; padding: 16px; background: var(--oa-surface); }
.msg-row { display: flex; gap: 10px; margin-bottom: 14px; }
.msg-row.mine { flex-direction: row-reverse; }
.bubble-wrap { max-width: 60%; }
.bubble-sender { font-size: 12px; color: var(--oa-ink-light); margin-bottom: 3px; }
.bubble { background: #fff; border: 1px solid var(--oa-line); border-radius: 8px; padding: 8px 12px; font-size: 14px; color: var(--oa-ink); word-break: break-word; white-space: pre-wrap; }
.msg-row.mine .bubble { background: var(--oa-primary); color: #fff; border-color: var(--oa-primary); }
.bubble-time { font-size: 11px; color: var(--oa-ink-light); margin-top: 3px; font-family: var(--oa-mono); }
.msg-row.mine .bubble-time { text-align: right; }
.msg-input { display: flex; gap: 10px; align-items: flex-end; padding: 12px 16px; border-top: 1px solid var(--oa-line); }
.msg-input :deep(.el-textarea) { flex: 1; }

.pick-list { max-height: 340px; overflow: auto; }
.pick-item { display: flex; align-items: center; width: 100%; margin: 0; padding: 6px 4px; }
.pick-avatar { margin-right: 8px; }
.pick-dept { color: var(--oa-ink-light); font-size: 12px; margin-left: 6px; }
.pick-tip { float: left; color: var(--oa-ink-light); font-size: 12px; line-height: 32px; }
</style>
