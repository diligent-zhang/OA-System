<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { employeePage, adminSetRole } from '@/api'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = ref({ pageNum: 1, pageSize: 10, name: '' })

const ROLES = { ADMIN: '超级管理员', DEPT_ADMIN: '部门管理员', EMPLOYEE: '普通员工' }

async function load() {
  loading.value = true
  try {
    const res = await employeePage(query.value)
    list.value = res.data.list || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

async function setRole(row, target) {
  if (row.role === target) return
  await ElMessageBox.confirm(
    `将「${row.name}」的角色调整为「${ROLES[target]}」？`,
    '角色调整',
    { type: 'warning' }
  )
  await adminSetRole(row.userId, target)
  ElMessage.success('角色已更新，重新登录后生效')
  load()
}

onMounted(load)
</script>

<template>
  <div class="oa-page">
    <h3 class="oa-page-title">角色权限管理（超级管理员 / 部门管理员 / 普通员工）</h3>
    <div class="oa-page-card">
      <div style="display: flex; gap: 10px; margin-bottom: 14px">
        <el-input v-model="query.name" placeholder="按姓名搜索" clearable style="width: 220px" @keyup.enter="query.pageNum = 1; load()" @clear="load()">
          <template #suffix><el-icon @click="load"><Search /></el-icon></template>
        </el-input>
        <el-button type="primary" @click="query.pageNum = 1; load()">查询</el-button>
      </div>

      <el-table v-loading="loading" :data="list" border stripe>
        <el-table-column prop="name" label="姓名" width="110" />
        <el-table-column prop="username" label="登录账号" width="130">
          <template #default="{ row }"><span class="oa-mono">{{ row.username }}</span></template>
        </el-table-column>
        <el-table-column prop="deptName" label="部门" min-width="130" />
        <el-table-column label="当前角色" width="120">
          <template #default="{ row }">
            <el-tag :type="row.role === 'ADMIN' ? 'danger' : row.role === 'DEPT_ADMIN' ? 'warning' : 'success'" size="small">
              {{ ROLES[row.role] || row.role }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="调整角色" width="200">
          <template #default="{ row }">
            <template v-if="row.role !== 'ADMIN'">
              <el-button v-if="row.role !== 'DEPT_ADMIN'" link type="warning" @click="setRole(row, 'DEPT_ADMIN')">晋升部门管理员</el-button>
              <el-button v-else link type="info" @click="setRole(row, 'EMPLOYEE')">收回管理员</el-button>
            </template>
            <span v-else style="color: var(--oa-ink-light); font-size: 12px">超级管理员固定</span>
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