<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { messagePage, messageRead, messageReadAll } from '@/api'
import { messageState } from '@/stores/message'

const router = useRouter()
const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = ref({ pageNum: 1, pageSize: 10, msgType: '', isRead: '' })

const TYPE_LABEL = { 1: '审批提醒', 2: '系统通知', 3: '公告提醒' }
const TYPE_TYPE = { 1: 'warning', 2: 'info', 3: 'success' }

async function load() {
  loading.value = true
  try {
    const res = await messagePage(query.value)
    list.value = res.data.list || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

async function read(row) {
  if (!row.isRead) {
    await messageRead(row.msgId)
    row.isRead = 1
    messageState.refresh()
  }
  if (row.bizLink) {
    router.push(row.bizLink)
  }
}

async function readAll() {
  await messageReadAll()
  messageState.setUnread(0)
  ElMessage.success('已全部标为已读')
  load()
}

onMounted(() => {
  load()
  messageState.refresh()
})
</script>

<template>
  <div class="oa-page">
    <h3 class="oa-page-title">消息中心</h3>
    <div class="oa-page-card">
      <div style="display: flex; align-items: center; gap: 12px; margin-bottom: 14px">
        <el-radio-group v-model="query.msgType" @change="query.pageNum = 1; load()">
          <el-radio-button value="">全部</el-radio-button>
          <el-radio-button value="1">审批提醒</el-radio-button>
          <el-radio-button value="2">系统通知</el-radio-button>
          <el-radio-button value="3">公告提醒</el-radio-button>
        </el-radio-group>
        <el-radio-group v-model="query.isRead" @change="query.pageNum = 1; load()">
          <el-radio-button value="">全部</el-radio-button>
          <el-radio-button value="0">未读</el-radio-button>
          <el-radio-button value="1">已读</el-radio-button>
        </el-radio-group>
        <span style="flex: 1"></span>
        <el-button @click="readAll">全部已读</el-button>
      </div>

      <el-table v-loading="loading" :data="list" border stripe>
        <el-table-column width="60">
          <template #default="{ row }">
            <span v-if="!row.isRead" class="dot"></span>
          </template>
        </el-table-column>
        <el-table-column label="类型" width="110">
          <template #default="{ row }">
            <el-tag :type="TYPE_TYPE[row.msgType] || 'info'" size="small" effect="plain">{{ TYPE_LABEL[row.msgType] || row.msgType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="220">
          <template #default="{ row }">
            <span class="msg-title" :class="{ mute: row.isRead }" role="button" tabindex="0" @click="read(row)" @keyup.enter="read(row)">
              {{ row.title }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="content" label="内容" min-width="280">
          <template #default="{ row }"><span class="msg-content">{{ row.content }}</span></template>
        </el-table-column>
        <el-table-column prop="createTime" label="时间" width="170" />
      </el-table>

      <el-pagination
        v-model:current-page="query.pageNum"
        v-model:page-size="query.pageSize"
        :total="total"
        layout="total, prev, pager, next"
        style="margin-top: 14px; justify-content: flex-end"
        @current-change="load"
      />
    </div>
  </div>
</template>

<style scoped>
.dot { display: inline-block; width: 8px; height: 8px; border-radius: 50%; background: #ef4444; }
.msg-title { cursor: pointer; font-weight: 500; }
.msg-title.mute { color: var(--oa-ink-light); font-weight: 400; }
.msg-content { color: var(--oa-ink-light); font-size: 13px; }
</style>