<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { flowMine, flowWithdraw } from '@/api'

const router = useRouter()
const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = ref({ pageNum: 1, pageSize: 10, status: '' })

const STATUS_LABEL = {
  APPROVING: '审批中',
  APPROVED: '已通过',
  REJECTED: '已驳回',
  WITHDRAWN: '已撤回'
}

async function load() {
  loading.value = true
  try {
    const res = await flowMine(query.value)
    list.value = res.data.list || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

async function withdraw(row) {
  await ElMessageBox.confirm(`确认撤回「${row.title}」？`, '撤回审批', { type: 'warning' })
  await flowWithdraw(row.instanceId)
  ElMessage.success('已撤回')
  load()
}

onMounted(load)
</script>

<template>
  <div class="oa-page">
    <h3 class="oa-page-title">我发起的</h3>
    <div class="oa-page-card">
      <div class="toolbar">
        <el-radio-group v-model="query.status" @change="query.pageNum = 1; load()">
          <el-radio-button value="">全部</el-radio-button>
          <el-radio-button value="APPROVING">审批中</el-radio-button>
          <el-radio-button value="APPROVED">已通过</el-radio-button>
          <el-radio-button value="REJECTED">已驳回</el-radio-button>
          <el-radio-button value="WITHDRAWN">已撤回</el-radio-button>
        </el-radio-group>
      </div>

      <el-table v-loading="loading" :data="list" border stripe>
        <el-table-column prop="title" label="标题" min-width="200" />
        <el-table-column prop="defName" label="流程" width="140" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'APPROVED' ? 'success' : row.status === 'REJECTED' ? 'danger' : row.status === 'WITHDRAWN' ? 'info' : 'warning'" size="small">
              {{ STATUS_LABEL[row.status] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="发起时间" width="170" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="router.push(`/flow/detail/${row.instanceId}`)">详情</el-button>
            <el-button v-if="row.status === 'APPROVING'" link type="danger" @click="withdraw(row)">撤回</el-button>
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
.toolbar { margin-bottom: 14px; }
</style>