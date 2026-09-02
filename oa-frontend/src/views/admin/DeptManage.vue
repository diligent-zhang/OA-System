<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { deptPage, deptAdd, deptUpdate, deptDelete } from '@/api'

/* 分页列表（关键词搜索） */
const query = reactive({ keyword: '', pageNum: 1, pageSize: 10 })
const loading = ref(false)
const tableData = ref([])
const total = ref(0)

async function load() {
  loading.value = true
  try {
    const res = await deptPage(query)
    tableData.value = res.data.list
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

onMounted(load)

/* 上级部门选项：一级部门 */
const parentOptions = ref([{ deptId: 0, deptName: '顶级部门' }])
async function loadParents() {
  const res = await deptPage({ keyword: '', pageNum: 1, pageSize: 200 })
  parentOptions.value = [
    { deptId: 0, deptName: '顶级部门' },
    ...res.data.list.filter((d) => d.parentId === 0).map((d) => ({ deptId: d.deptId, deptName: d.deptName }))
  ]
}

/* ===== 新增/编辑 ===== */
const dialog = reactive({ visible: false, isEdit: false, id: null })
const formRef = ref()
const form = reactive({ deptName: '', parentId: 0, sortNo: 0 })
const rules = {
  deptName: [{ required: true, message: '请输入部门名称', trigger: 'blur' }],
  parentId: [{ required: true, message: '请选择上级部门', trigger: 'change' }]
}

function openAdd(parentId = 0) {
  Object.assign(form, { deptName: '', parentId, sortNo: 0 })
  dialog.isEdit = false
  dialog.visible = true
  loadParents()
}

function openEdit(row) {
  Object.assign(form, { deptName: row.deptName, parentId: row.parentId, sortNo: row.sortNo })
  dialog.isEdit = true
  dialog.id = row.deptId
  dialog.visible = true
  loadParents()
}

async function submit() {
  await formRef.value.validate()
  if (dialog.isEdit) {
    await deptUpdate(dialog.id, form)
    ElMessage.success('部门已更新')
  } else {
    await deptAdd(form)
    ElMessage.success('部门新增成功')
  }
  dialog.visible = false
  load()
}

async function handleDelete(row) {
  await ElMessageBox.confirm(
    `确认删除部门「${row.deptName}」？存在在职员工或子部门时将无法删除。`,
    '删除确认', { type: 'warning', confirmButtonText: '确认删除', cancelButtonText: '取消' }
  )
  await deptDelete(row.deptId)
  ElMessage.success('部门已删除')
  load()
}
</script>

<template>
  <div>
    <div class="oa-page-card">
      <el-form inline @submit.prevent>
        <el-form-item label="部门名称">
          <el-input v-model="query.keyword" placeholder="模糊搜索" clearable style="width: 200px" @keyup.enter="query.pageNum = 1; load()" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="'Search'" @click="query.pageNum = 1; load()">查询</el-button>
          <el-button :icon="'Refresh'" @click="query.keyword = ''; query.pageNum = 1; load()">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="oa-page-card list-card">
      <div class="toolbar">
        <h3 class="oa-page-title" style="margin: 0">部门架构（支持两级）</h3>
        <el-button type="primary" :icon="'Plus'" @click="openAdd()">新增部门</el-button>
      </div>

      <el-table v-loading="loading" :data="tableData" stripe border row-key="deptId" default-expand-all
        :tree-props="{ children: 'children' }">
        <el-table-column prop="deptName" label="部门名称" min-width="180" />
        <el-table-column label="层级" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="row.parentId === 0 ? 'primary' : 'info'" size="small" effect="plain">
              {{ row.parentId === 0 ? '一级部门' : '二级部门' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="parentName" label="上级部门" width="150">
          <template #default="{ row }">{{ row.parentId === 0 ? '—' : row.parentName }}</template>
        </el-table-column>
        <el-table-column prop="empCount" label="员工数" width="90" align="center" sortable>
          <template #default="{ row }"><span class="oa-mono">{{ row.empCount }}</span></template>
        </el-table-column>
        <el-table-column prop="sortNo" label="排序号" width="90" align="center" />
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.parentId === 0" link type="success" @click="openAdd(row.deptId)">添加子部门</el-button>
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="oa-pagination">
        <el-pagination
          v-model:current-page="query.pageNum" v-model:page-size="query.pageSize"
          :total="total" :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper" @change="load"
        />
      </div>
    </div>

    <el-dialog v-model="dialog.visible" :title="dialog.isEdit ? '编辑部门' : '新增部门'" width="440px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="部门名称" prop="deptName">
          <el-input v-model="form.deptName" placeholder="如：后端开发组" />
        </el-form-item>
        <el-form-item label="上级部门" prop="parentId">
          <el-select v-model="form.parentId" style="width: 100%" :disabled="dialog.isEdit && form.parentId !== 0">
            <el-option v-for="p in parentOptions" :key="p.deptId" :label="p.deptName" :value="p.deptId" />
          </el-select>
        </el-form-item>
        <el-form-item label="排序号">
          <el-input-number v-model="form.sortNo" :min="0" :max="999" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog.visible = false">取消</el-button>
        <el-button type="primary" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.list-card { margin-top: 16px; }
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 14px;
}
</style>
