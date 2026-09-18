<script setup>
import { onMounted, ref } from 'vue'
import { adminOpLogs } from '@/api'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = ref({ pageNum: 1, pageSize: 10, opType: '' })

async function load() {
  loading.value = true
  try {
    const res = await adminOpLogs(query.value)
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
    <h3 class="oa-page-title">操作日志（登录 / 文件 / 流程 / 配置等）</h3>
    <div class="oa-page-card">
      <div style="margin-bottom: 14px">
        <el-select v-model="query.opType" placeholder="类型" clearable style="width: 160px" @change="query.pageNum = 1; load()">
          <el-option label="登录" value="LOGIN" />
          <el-option label="审批" value="FLOW" />
          <el-option label="文件" value="FILE" />
          <el-option label="文档" value="DOC" />
          <el-option label="公告" value="NOTICE" />
          <el-option label="考勤" value="ATT" />
          <el-option label="员工" value="EMP" />
          <el-option label="配置" value="CONFIG" />
          <el-option label="角色" value="AUTH" />
        </el-select>
      </div>
      <el-table v-loading="loading" :data="list" border stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="username" label="操作人" width="100" />
        <el-table-column prop="opType" label="类型" width="100">
          <template #default="{ row }"><span class="oa-mono">{{ row.opType }}</span></template>
        </el-table-column>
        <el-table-column prop="detail" label="操作内容" min-width="200" />
        <el-table-column prop="bizId" label="业务ID" width="110" />
        <el-table-column prop="ip" label="IP" width="130" />
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