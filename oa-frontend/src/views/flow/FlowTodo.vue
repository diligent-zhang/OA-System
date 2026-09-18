<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { flowTodo } from '@/api'

const router = useRouter()
const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = ref({ pageNum: 1, pageSize: 10 })

async function load() {
  loading.value = true
  try {
    const res = await flowTodo(query.value)
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
    <h3 class="oa-page-title">待我审批 <span v-if="total" class="count-badge">{{ total }}</span></h3>
    <div class="oa-page-card">
      <el-table v-loading="loading" :data="list" border stripe>
        <el-table-column prop="title" label="标题" min-width="220" />
        <el-table-column prop="defName" label="流程" width="130" />
        <el-table-column prop="applicantName" label="发起人" width="100" />
        <el-table-column prop="nodeName" label="节点" width="150" />
        <el-table-column prop="createTime" label="发起时间" width="170" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="router.push(`/flow/detail/${row.instanceId}`)">处理</el-button>
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
.count-badge {
  display: inline-block;
  background: var(--oa-primary);
  color: #fff;
  border-radius: 10px;
  padding: 0 8px;
  font-size: 12px;
  vertical-align: middle;
}
</style>