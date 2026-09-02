<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  employeePage, employeeAdd, employeeUpdate, employeeDelete, employeeDeleteBatch,
  employeeResetPwd, employeeAccountStatus, deptTree
} from '@/api'

/* ===== 查询 ===== */
const query = reactive({ name: '', empNo: '', deptId: null, status: null, pageNum: 1, pageSize: 10 })
const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const selection = ref([])
const depts = ref([])

async function load() {
  loading.value = true
  try {
    const res = await employeePage({ ...query })
    tableData.value = res.data.list
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

function search() {
  query.pageNum = 1
  load()
}

function resetQuery() {
  Object.assign(query, { name: '', empNo: '', deptId: null, status: null, pageNum: 1 })
  load()
}

onMounted(async () => {
  const res = await deptTree()
  depts.value = res.data
  load()
})

/* ===== 新增/编辑弹窗 ===== */
const dialog = reactive({ visible: false, isEdit: false, id: null })
const formRef = ref()
const form = reactive({
  username: '', name: '', gender: 1, phone: '', email: '',
  deptId: null, hireDate: '', status: 1
})

const rules = {
  username: [
    { required: true, message: '请设置登录账号', trigger: 'blur' },
    { pattern: /^[a-zA-Z][a-zA-Z0-9_]{3,19}$/, message: '字母开头，4~20 位，仅含字母数字下划线', trigger: 'blur' }
  ],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }],
  phone: [{ required: true, pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }],
  email: [{ type: 'email', message: '邮箱格式不正确', trigger: 'blur' }],
  deptId: [{ required: true, message: '请选择所属部门', trigger: 'change' }],
  hireDate: [{ required: true, message: '请选择入职日期', trigger: 'change' }],
  status: [{ required: true, message: '请选择在职状态', trigger: 'change' }]
}

function openAdd() {
  Object.assign(form, { username: '', name: '', gender: 1, phone: '', email: '', deptId: null, hireDate: '', status: 1 })
  dialog.isEdit = false
  dialog.id = null
  dialog.visible = true
}

function openEdit(row) {
  Object.assign(form, {
    username: row.username, name: row.name, gender: row.gender, phone: row.phone,
    email: row.email, deptId: row.deptId, hireDate: row.hireDate, status: row.status
  })
  dialog.isEdit = true
  dialog.id = row.empId
  dialog.visible = true
}

async function submit() {
  await formRef.value.validate()
  if (dialog.isEdit) {
    await employeeUpdate(dialog.id, form)
    ElMessage.success('员工信息已更新')
  } else {
    await employeeAdd(form)
    ElMessage.success('员工新增成功，初始密码为 123456')
  }
  dialog.visible = false
  load()
}

/* ===== 高危操作 ===== */
async function handleDelete(row) {
  await ElMessageBox.confirm(
    `确认删除员工「${row.name}（${row.empNo}）」？其登录账号将一并删除，此操作不可恢复。`,
    '删除确认', { type: 'error', confirmButtonText: '确认删除', cancelButtonText: '取消' }
  )
  await employeeDelete(row.empId)
  ElMessage.success('员工及其账号已删除')
  load()
}

async function handleBatchDelete() {
  if (!selection.value.length) {
    return ElMessage.warning('请先勾选要删除的员工')
  }
  await ElMessageBox.confirm(
    `确认删除选中的 ${selection.value.length} 名员工？其登录账号将一并删除，此操作不可恢复。`,
    '批量删除确认', { type: 'error', confirmButtonText: '确认删除', cancelButtonText: '取消' }
  )
  await employeeDeleteBatch(selection.value.map((r) => r.empId))
  ElMessage.success('批量删除成功')
  load()
}

async function handleResetPwd(row) {
  await ElMessageBox.confirm(`确认将「${row.name}」的登录密码重置为 123456？`, '重置密码', { type: 'warning' })
  await employeeResetPwd(row.empId)
  ElMessage.success('密码已重置为 123456')
}

async function handleToggleAccount(row) {
  const disable = row.accountStatus === 1
  await ElMessageBox.confirm(
    disable ? `禁用后「${row.name}」将无法登录系统，确认禁用？` : `确认启用「${row.name}」的登录账号？`,
    disable ? '禁用账号' : '启用账号', { type: 'warning' }
  )
  await employeeAccountStatus(row.empId, disable ? 0 : 1)
  ElMessage.success(disable ? '账号已禁用' : '账号已启用')
  load()
}
</script>

<template>
  <div>
    <!-- 查询区 -->
    <div class="oa-page-card">
      <el-form inline @submit.prevent>
        <el-form-item label="姓名">
          <el-input v-model="query.name" placeholder="模糊搜索" clearable style="width: 150px" @keyup.enter="search" />
        </el-form-item>
        <el-form-item label="工号">
          <el-input v-model="query.empNo" placeholder="模糊搜索" clearable style="width: 160px" @keyup.enter="search" />
        </el-form-item>
        <el-form-item label="部门">
          <el-tree-select
            v-model="query.deptId" :data="depts" node-key="deptId"
            :props="{ label: 'deptName', children: 'children' }"
            check-strictly clearable placeholder="全部部门" style="width: 180px"
          />
        </el-form-item>
        <el-form-item label="在职状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 110px">
            <el-option label="在职" :value="1" />
            <el-option label="离职" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="'Search'" @click="search">查询</el-button>
          <el-button :icon="'Refresh'" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 列表区 -->
    <div class="oa-page-card list-card">
      <div class="toolbar">
        <h3 class="oa-page-title" style="margin: 0">员工台账</h3>
        <div>
          <el-button type="primary" :icon="'Plus'" @click="openAdd">新增员工</el-button>
          <el-button type="danger" plain :icon="'Delete'" @click="handleBatchDelete">批量删除</el-button>
        </div>
      </div>

      <el-table
        v-loading="loading" :data="tableData" stripe border row-key="empId"
        @selection-change="selection = $event"
      >
        <el-table-column type="selection" width="45" />
        <el-table-column prop="empNo" label="工号" width="130" sortable>
          <template #default="{ row }"><span class="oa-mono">{{ row.empNo }}</span></template>
        </el-table-column>
        <el-table-column prop="name" label="姓名" width="100" />
        <el-table-column prop="gender" label="性别" width="60">
          <template #default="{ row }">{{ row.gender === 1 ? '男' : '女' }}</template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" width="130">
          <template #default="{ row }"><span class="oa-mono">{{ row.phone }}</span></template>
        </el-table-column>
        <el-table-column prop="email" label="邮箱" min-width="170" show-overflow-tooltip />
        <el-table-column prop="deptName" label="所属部门" width="130" />
        <el-table-column prop="hireDate" label="入职日期" width="115" sortable />
        <el-table-column label="在职状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">{{ row.status === 1 ? '在职' : '离职' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="账号状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.username" :type="row.accountStatus === 1 ? 'primary' : 'danger'" size="small" effect="plain">
              {{ row.accountStatus === 1 ? '正常' : '禁用' }}
            </el-tag>
            <span v-else class="oa-ink-light">无账号</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="225" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="warning" :disabled="!row.username" @click="handleResetPwd(row)">重置密码</el-button>
            <el-button link :type="row.accountStatus === 1 ? 'danger' : 'success'" :disabled="!row.username" @click="handleToggleAccount(row)">
              {{ row.accountStatus === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="oa-pagination">
        <el-pagination
          v-model:current-page="query.pageNum" v-model:page-size="query.pageSize"
          :total="total" :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @change="load"
        />
      </div>
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialog.visible" :title="dialog.isEdit ? '编辑员工信息' : '新增员工'" width="560px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-row :gutter="18">
          <el-col :span="12">
            <el-form-item label="登录账号" prop="username">
              <el-input v-model="form.username" :disabled="dialog.isEdit" placeholder="初始密码 123456" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="姓名" prop="name"><el-input v-model="form.name" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="性别" prop="gender">
              <el-radio-group v-model="form.gender">
                <el-radio :value="1">男</el-radio>
                <el-radio :value="0">女</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="手机号" prop="phone"><el-input v-model="form.phone" maxlength="11" /></el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="邮箱" prop="email"><el-input v-model="form.email" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所属部门" prop="deptId">
              <el-tree-select
                v-model="form.deptId" :data="depts" node-key="deptId"
                :props="{ label: 'deptName', children: 'children' }"
                check-strictly style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="入职日期" prop="hireDate">
              <el-date-picker v-model="form.hireDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="在职状态" prop="status">
              <el-radio-group v-model="form.status">
                <el-radio :value="1">在职</el-radio>
                <el-radio :value="0">离职</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialog.visible = false">取消</el-button>
        <el-button type="primary" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.list-card {
  margin-top: 16px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 14px;
}

.oa-ink-light {
  color: var(--oa-ink-light);
  font-size: 13px;
}
</style>
