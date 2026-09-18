<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { flowAdminArchive } from '@/api'

const router = useRouter()
const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = ref({ pageNum: 1, pageSize: 10, status: '', keyword: '' })

const STATUS_LABEL = { APPROVING: '审批中', APPROVED: '已通过', REJECTED: '已驳回', WITHDRAWN: '已撤回' }

async function load() {
  loading.value = true
  try {
    const res = await flowAdminArchive(query.value)
    list.value = res.data.list || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

function search() {
  query.value.pageNum = 1
  load()
}

onMounted(load)
</script>

<template>
  <div class="oa-page">
    <h3 class="oa-page-title">审批归档（永久保存）</h3>
    <div class="oa-page-card">
      <div class="toolbar">
        <el-input v-model="query.keyword" placeholder="搜索标题 / 发起人" clearable style="width: 240px" @keyup.enter="search" @clear="search">
          <template #suffix><el-icon @click="search"><Search /></el-icon></template>
        </el-input>
        <el-select v-model="query.status" placeholder="状态" clearable style="width: 150px" @change="search">
          <el-option label="审批中" value="APPROVING" />
          <el-option label="已通过" value="APPROVED" />
          <el-option label="已驳回" value="REJECTED" />
          <el-option label="已撤回" value="WITHDRAWN" />
        </el-select>
        <el-button type="primary" @click="search">查询</el-button>
      </div>

      <el-table v-loading="loading" :data="list" border stripe>
        <el-table-column prop="title" label="标题" min-width="200" />
        <el-table-column prop="defName" label="流程" width="130" />
        <el-table-column prop="applicantName" label="发起人" width="100" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'APPROVED' ? 'success' : row.status === 'REJECTED' ? 'danger' : 'warning'" size="small">
              {{ STATUS_LABEL[row.status] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="发起时间" width="170" />
        <el-table-column prop="finishTime" label="结束时间" width="170" />
        <el-table-column label="操作" width="90" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="router.push(`/flow/detail/${row.instanceId}`)">查看</el-button>
          </template>
        </el-table-column>
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
.toolbar { display: flex; gap: 10px; margin-bottom: 14px; }
</style>