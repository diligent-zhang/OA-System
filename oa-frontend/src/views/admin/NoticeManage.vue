<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { noticeAdminPage, noticeAdd, noticeUpdate, noticeDelete, noticeToggle } from '@/api'

const TYPE_LABEL = { 1: '通知', 2: '公告', 3: '制度' }
const TYPE_TAG = { 1: 'primary', 2: 'success', 3: 'warning' }

const query = reactive({ title: '', type: null, timeRange: null, pageNum: 1, pageSize: 10 })
const loading = ref(false)
const tableData = ref([])
const total = ref(0)

async function load() {
  loading.value = true
  try {
    const params = { ...query }
    if (params.timeRange?.length === 2) {
      params.beginTime = params.timeRange[0]
      params.endTime = params.timeRange[1]
    }
    delete params.timeRange
    const res = await noticeAdminPage(params)
    tableData.value = res.data.list
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

onMounted(load)

/* ===== 发布/编辑 ===== */
const dialog = reactive({ visible: false, isEdit: false, id: null })
const formRef = ref()
const form = reactive({ title: '', content: '', type: 1, topFlag: 0, publish: true })
const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入正文', trigger: 'blur' }],
  type: [{ required: true, message: '请选择公告类型', trigger: 'change' }]
}

function openAdd() {
  Object.assign(form, { title: '', content: '', type: 1, topFlag: 0, publish: true })
  dialog.isEdit = false
  dialog.visible = true
}

function openEdit(row) {
  Object.assign(form, { title: row.title, content: row.content, type: row.type, topFlag: row.topFlag, publish: row.status === 1 })
  dialog.isEdit = true
  dialog.id = row.noticeId
  dialog.visible = true
}

async function submit() {
  await formRef.value.validate()
  if (dialog.isEdit) {
    await noticeUpdate(dialog.id, { ...form, publish: undefined })
    ElMessage.success('公告已更新')
  } else {
    await noticeAdd(form)
    ElMessage.success(form.publish ? '公告发布成功' : '已保存为下架状态')
  }
  dialog.visible = false
  load()
}

/* ===== 预览 ===== */
const preview = reactive({ visible: false, data: {} })

function openPreview(row) {
  preview.data = row
  preview.visible = true
}

async function handleToggle(row) {
  await noticeToggle(row.noticeId)
  ElMessage.success(row.status === 1 ? '公告已下架' : '公告已重新发布')
  load()
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确认删除公告「${row.title}」？此操作不可恢复。`, '删除确认',
    { type: 'error', confirmButtonText: '确认删除', cancelButtonText: '取消' })
  await noticeDelete(row.noticeId)
  ElMessage.success('公告已删除')
  load()
}
</script>

<template>
  <div>
    <div class="oa-page-card">
      <el-form inline @submit.prevent>
        <el-form-item label="标题">
          <el-input v-model="query.title" placeholder="模糊搜索" clearable style="width: 180px" @keyup.enter="query.pageNum = 1; load()" />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="query.type" placeholder="全部" clearable style="width: 110px">
            <el-option label="通知" :value="1" />
            <el-option label="公告" :value="2" />
            <el-option label="制度" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="发布时间">
          <el-date-picker v-model="query.timeRange" type="daterange" value-format="YYYY-MM-DD"
            start-placeholder="开始" end-placeholder="结束" style="width: 240px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="'Search'" @click="query.pageNum = 1; load()">查询</el-button>
          <el-button :icon="'Refresh'" @click="Object.assign(query, { title: '', type: null, timeRange: null, pageNum: 1 }); load()">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="oa-page-card list-card">
      <div class="toolbar">
        <h3 class="oa-page-title" style="margin: 0">公告管理</h3>
        <el-button type="primary" :icon="'Plus'" @click="openAdd">发布公告</el-button>
      </div>

      <el-table v-loading="loading" :data="tableData" stripe border>
        <el-table-column type="expand">
          <template #default="{ row }">
            <pre class="notice-body">{{ row.content }}</pre>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="220" show-overflow-tooltip>
          <template #default="{ row }">
            <el-tag v-if="row.topFlag === 1" type="danger" size="small" effect="dark" class="top-tag">顶</el-tag>
            {{ row.title }}
          </template>
        </el-table-column>
        <el-table-column label="类型" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="TYPE_TAG[row.type]" size="small" effect="plain">{{ TYPE_LABEL[row.type] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="publisherName" label="发布人" width="110" />
        <el-table-column prop="publishTime" label="发布时间" width="170" sortable />
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">{{ row.status === 1 ? '已发布' : '已下架' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openPreview(row)">预览</el-button>
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link :type="row.status === 1 ? 'warning' : 'success'" @click="handleToggle(row)">
              {{ row.status === 1 ? '下架' : '发布' }}
            </el-button>
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

    <!-- 发布/编辑弹窗 -->
    <el-dialog v-model="dialog.visible" :title="dialog.isEdit ? '编辑公告' : '发布公告'" width="640px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" maxlength="100" show-word-limit placeholder="请输入公告标题" />
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-radio-group v-model="form.type">
            <el-radio :value="1">通知</el-radio>
            <el-radio :value="2">公告</el-radio>
            <el-radio :value="3">制度</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="正文" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="8" placeholder="请输入公告正文" />
        </el-form-item>
        <el-form-item label="置顶">
          <el-switch v-model="form.topFlag" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item v-if="!dialog.isEdit" label="立即发布">
          <el-switch v-model="form.publish" />
          <span class="publish-tip">{{ form.publish ? '保存后员工即可查看' : '保存为下架状态，稍后手动发布' }}</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog.visible = false">取消</el-button>
        <el-button type="primary" @click="submit">保存</el-button>
      </template>
    </el-dialog>

    <!-- 详情预览 -->
    <el-dialog v-model="preview.visible" title="公告预览" width="640px">
      <h3 class="preview-title">{{ preview.data.title }}</h3>
      <div class="preview-meta">
        <el-tag :type="TYPE_TAG[preview.data.type]" size="small" effect="plain">{{ TYPE_LABEL[preview.data.type] }}</el-tag>
        <span>{{ preview.data.publisherName }} · {{ preview.data.publishTime }}</span>
      </div>
      <el-divider />
      <pre class="notice-body">{{ preview.data.content }}</pre>
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
.notice-body {
  margin: 8px 20px;
  white-space: pre-wrap;
  word-break: break-word;
  font-family: inherit;
  font-size: 13.5px;
  line-height: 1.8;
  color: var(--oa-ink);
}
.top-tag { margin-right: 6px; }
.publish-tip {
  margin-left: 12px;
  font-size: 12px;
  color: var(--oa-ink-light);
}
.preview-title {
  margin: 0 0 10px;
  color: var(--oa-navy);
}
.preview-meta {
  display: flex;
  align-items: center;
  gap: 10px;
  color: var(--oa-ink-light);
  font-size: 13px;
}
</style>
