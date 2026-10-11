<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { contactsTree, contactsQuery, chatOpen } from '@/api'

const router = useRouter()
const loading = ref(false)
const tree = ref([])
const book = ref([])
const keyword = ref('')
const selectedRows = ref([])

function onSelectionChange(rows) {
  selectedRows.value = rows
}

/* 发起会话：1 人单聊，多人群聊 */
async function startChat(userIds, groupName) {
  const ids = (userIds || []).filter((id) => id != null)
  if (!ids.length) {
    ElMessage.warning('该成员未关联登录账号，无法发起会话')
    return
  }
  const res = await chatOpen({ type: ids.length > 1 ? 'GROUP' : 'SINGLE', memberUserIds: ids, groupName })
  router.push(`/chat/${res.data}`)
}

function chatWith(row) {
  startChat([row.userId])
}

async function chatSelected() {
  const ids = selectedRows.value.map((r) => r.userId).filter((id) => id != null)
  if (!ids.length) {
    ElMessage.warning('请先勾选成员')
    return
  }
  if (ids.length === 1) {
    startChat(ids)
    return
  }
  try {
    const { value } = await ElMessageBox.prompt('请输入群聊名称', '发起群聊', { inputValue: '工作群聊' })
    startChat(ids, value)
  } catch (_) {
    /* 取消 */
  }
}

async function load() {
  loading.value = true
  try {
    const res = await contactsTree()
    tree.value = res.data || []
    book.value = flatten(tree.value)
  } finally {
    loading.value = false
  }
}

function flatten(nodes) {
  let out = []
  nodes.forEach((n) => {
    ;(n.employees || []).forEach((e) => out.push({ ...e, folder: n.deptName }))
    out = out.concat(flatten(n.children || []))
  })
  return out
}

async function search() {
  if (!keyword.value) {
    load()
    return
  }
  loading.value = true
  try {
    const res = await contactsQuery({ keyword: keyword.value })
    book.value = res.data || []
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<template>
  <div class="oa-page">
    <h3 class="oa-page-title">企业通讯录</h3>
    <el-row :gutter="16">
      <el-col :md="8" :sm="24">
        <div class="oa-page-card">
          <h4 class="card-title">组织架构</h4>
          <el-tree :data="tree" default-expand-all :props="{ label: 'deptName', children: 'children' }">
            <template #default="{ node, data }">
              <span class="dept-node">
                <el-icon><OfficeBuilding /></el-icon>
                {{ node.label }}
                <span class="dept-count">{{ (data.employees || []).length }}</span>
              </span>
            </template>
          </el-tree>
        </div>
      </el-col>
      <el-col :md="16" :sm="24">
        <div class="oa-page-card">
          <div style="display: flex; margin-bottom: 14px">
            <el-input v-model="keyword" placeholder="姓名 / 工号 / 岗位 / 手机号 / 部门" clearable style="max-width: 300px" @keyup.enter="search" @clear="load">
              <template #suffix><el-icon @click="search"><Search /></el-icon></template>
            </el-input>
            <el-button style="margin-left: 10px" @click="search">搜索</el-button>
            <el-button type="primary" :disabled="!selectedRows.length" style="margin-left: 10px" @click="chatSelected">
              发起会话{{ selectedRows.length > 1 ? '（群聊）' : '' }}
            </el-button>
          </div>
          <el-table v-loading="loading" :data="book" border stripe @selection-change="onSelectionChange">
            <el-table-column type="selection" width="46" :selectable="(row) => !!row.userId" />
            <el-table-column prop="name" label="姓名" width="100">
              <template #default="{ row }">
                <span
                  class="name-link"
                  :class="{ 'name-link-off': !row.userId }"
                  role="button"
                  tabindex="0"
                  @click="row.userId && chatWith(row)"
                  @keyup.enter="row.userId && chatWith(row)"
                >{{ row.name }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="position" label="岗位" width="140">
              <template #default="{ row }">{{ row.position || '—' }}</template>
            </el-table-column>
            <el-table-column prop="empNo" label="工号" width="130">
              <template #default="{ row }"><span class="oa-mono">{{ row.empNo }}</span></template>
            </el-table-column>
            <el-table-column prop="folder" label="部门" min-width="130">
              <template #default="{ row }">{{ row.folder || row.deptName || '—' }}</template>
            </el-table-column>
            <el-table-column prop="phone" label="手机号" width="140">
              <template #default="{ row }"><span class="oa-mono">{{ row.phone }}</span></template>
            </el-table-column>
            <el-table-column prop="email" label="邮箱" min-width="170" />
            <el-table-column label="操作" width="96" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" :disabled="!row.userId" @click="chatWith(row)">发消息</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.card-title { margin: 0 0 12px; font-size: 14px; color: var(--oa-navy); }
.dept-node { display: flex; align-items: center; gap: 6px; }
.dept-count { background: var(--el-color-primary-light-9); color: var(--oa-primary); border-radius: 8px; padding: 0 7px; font-size: 12px; }
.name-link { color: var(--oa-primary); cursor: pointer; font-weight: 500; }
.name-link:hover { text-decoration: underline; }
.name-link-off { color: var(--oa-ink-light); cursor: default; }
.name-link-off:hover { text-decoration: none; }
</style>