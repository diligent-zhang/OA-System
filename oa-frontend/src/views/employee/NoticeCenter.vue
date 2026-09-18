<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { noticePublishedPage, noticeDetail } from '@/api'

const TYPE_LABEL = { 1: '通知', 2: '公告', 3: '制度' }
const TYPE_TAG = { 1: 'primary', 2: 'success', 3: 'warning' }

const route = useRoute()
const query = reactive({ title: '', type: null, pageNum: 1, pageSize: 10 })
const loading = ref(false)
const list = ref([])
const total = ref(0)

async function load() {
  loading.value = true
  try {
    const res = await noticePublishedPage(query)
    list.value = res.data.list
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  await load()
  // 工作台快捷跳转直接打开详情
  if (route.query.id) openDetail({ noticeId: Number(route.query.id) })
})

const dialog = reactive({ visible: false, data: {}, loading: false })

async function openDetail(row) {
  dialog.visible = true
  dialog.loading = true
  try {
    const res = await noticeDetail(row.noticeId)
    dialog.data = res.data
  } finally {
    dialog.loading = false
  }
}

function onTypeChange() {
  query.pageNum = 1
  load()
}
</script>

<template>
  <div class="oa-page-card">
    <div class="head">
      <h3 class="oa-page-title" style="margin: 0">公告中心</h3>
      <div class="filters">
        <el-radio-group v-model="query.type" @change="onTypeChange">
          <el-radio-button :value="null">全部</el-radio-button>
          <el-radio-button :value="1">通知</el-radio-button>
          <el-radio-button :value="2">公告</el-radio-button>
          <el-radio-button :value="3">制度</el-radio-button>
        </el-radio-group>
        <el-input v-model="query.title" placeholder="搜索标题" clearable style="width: 200px"
          @keyup.enter="query.pageNum = 1; load()" @clear="query.pageNum = 1; load()">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-button type="primary" @click="query.pageNum = 1; load()">搜索</el-button>
      </div>
    </div>

    <ul v-loading="loading" class="notice-stream">
      <li v-for="n in list" :key="n.noticeId" @click="openDetail(n)">
        <div class="line1">
          <el-tag v-if="n.topFlag === 1" type="danger" size="small" effect="dark">置顶</el-tag>
          <el-tag :type="TYPE_TAG[n.type]" size="small" effect="plain">{{ TYPE_LABEL[n.type] }}</el-tag>
          <span class="title" :class="{ 'muted-title': n.readFlag }">{{ n.title }}</span>
          <span v-if="!n.readFlag" class="unread-dot" title="未读"></span>
          <span v-if="n.attachCount" class="attach-num"><el-icon><Paperclip /></el-icon>{{ n.attachCount }}</span>
        </div>
        <div class="meta">
          <span>{{ n.publisherName }}</span>
          <span class="oa-mono">{{ n.publishTime }}</span>
        </div>
      </li>
      <li v-if="!loading && !list.length" class="empty">
        暂无符合条件的公告，换个筛选条件试试
      </li>
    </ul>

    <div class="oa-pagination">
      <el-pagination
        v-model:current-page="query.pageNum" v-model:page-size="query.pageSize"
        :total="total" :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper" @change="load"
      />
    </div>

    <el-dialog v-model="dialog.visible" title="公告详情" width="640px">
      <div v-loading="dialog.loading">
        <h3 class="d-title">{{ dialog.data.title }}</h3>
        <div class="d-meta">
          <el-tag :type="TYPE_TAG[dialog.data.type]" size="small" effect="plain">{{ TYPE_LABEL[dialog.data.type] }}</el-tag>
          <span>{{ dialog.data.publisherName }} · {{ dialog.data.publishTime }}</span>
        </div>
        <el-divider />
        <pre class="d-body">{{ dialog.data.content }}</pre>
        <div v-if="dialog.data.attachments && dialog.data.attachments.length" class="d-attachments">
          <div class="d-att-title">附件（{{ dialog.data.attachments.length }}）</div>
          <a v-for="att in dialog.data.attachments" :key="att.fileId" :href="att.downloadUrl" target="_blank" class="d-att">
            <el-icon><Paperclip /></el-icon>{{ att.originalName }}
          </a>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<style scoped>
.head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 6px;
}

.filters {
  display: flex;
  align-items: center;
  gap: 12px;
}

.notice-stream {
  list-style: none;
  margin: 0;
  padding: 0;
  min-height: 200px;
}

.notice-stream li {
  padding: 16px 4px;
  border-bottom: 1px solid var(--oa-line);
  cursor: pointer;
  transition: background 0.12s ease;
}

.notice-stream li:hover { background: #f7fafd; }

.line1 {
  display: flex;
  align-items: center;
  gap: 8px;
}

.line1 .title {
  font-size: 15px;
  font-weight: 600;
  color: var(--oa-navy);
}

.line1 .title.muted-title { color: var(--oa-ink-light); font-weight: 400; }
.line1 .unread-dot { width: 8px; height: 8px; border-radius: 50%; background: #ef4444; flex-shrink: 0; }
.line1 .attach-num { display: inline-flex; align-items: center; gap: 3px; font-size: 12px; color: var(--oa-ink-light); }

.notice-stream li:hover .title:not(.muted-title) { color: var(--oa-primary); }

.meta {
  margin-top: 6px;
  display: flex;
  gap: 16px;
  font-size: 12.5px;
  color: var(--oa-ink-light);
}

.empty {
  text-align: center;
  color: var(--oa-ink-light);
  padding: 48px 0 !important;
  cursor: default !important;
}

.d-title { margin: 0 0 10px; color: var(--oa-navy); font-size: 18px; }
.d-meta {
  display: flex;
  align-items: center;
  gap: 10px;
  color: var(--oa-ink-light);
  font-size: 13px;
}
.d-body {
  white-space: pre-wrap;
  word-break: break-word;
  font-family: inherit;
  font-size: 14px;
  line-height: 1.9;
  margin: 0;
}
.d-attachments { margin-top: 16px; }
.d-att-title { font-size: 13px; color: var(--oa-ink-light); margin-bottom: 8px; }
.d-att {
  display: flex;
  align-items: center;
  gap: 5px;
  color: var(--oa-primary);
  font-size: 13px;
  padding: 6px 0;
}
</style>
