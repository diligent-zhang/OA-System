<script setup>
import { onMounted, ref } from 'vue'
import { myOpLogs } from '@/api'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = ref({ pageNum: 1, pageSize: 10, opType: '' })

async function load() {
  loading.value = true
  try {
    const res = await myOpLogs(query.value)
    list.value = res.data.list || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<template>
  <div class="oa-page">
    <h3 class="oa-page-title">我的操作记录</h3>
    <div class="oa-page-card">
      <div style="margin-bottom: 14px">
        <el-select v-model="query.opType" placeholder="类型" clearable style="width: 160px" @change="query.pageNum = 1; load()">
          <el-option label="登录" value="LOGIN" />
          <el-option label="审批" value="FLOW" />
          <el-option label="文件" value="FILE" />
          <el-option label="考勤" value="ATT" />
          <el-option label="注册" value="AUTH" />
        </el-select>
      </div>
      <el-timeline>
        <el-timeline-item v-for="log in list" :key="log.id" :timestamp="log.createTime" placement="top">
          <span class="log-type oa-mono">{{ log.opType }}</span>
          <span class="log-detail">{{ log.detail }}</span>
          <span class="log-ip">{{ log.ip }}</span>
        </el-timeline-item>
      </el-timeline>
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
.log-type { color: var(--oa-primary); font-size: 12px; margin-right: 10px; }
.log-detail { color: var(--oa-ink); font-size: 13px; margin-right: 10px; }
.log-ip { color: var(--oa-ink-light); font-size: 12px; }
</style>