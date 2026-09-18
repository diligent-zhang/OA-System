<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { flowDetail, flowApprove, flowReject, flowTransfer, flowWithdraw, flowAddComment, contactsQuery } from '@/api'
import { auth } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const instanceId = route.params.id

const loading = ref(false)
const detail = ref(null)
const opinion = ref('')
const handleLoading = ref(false)

const showTransfer = ref(false)
const transferUserId = ref('')
const transferComment = ref('')
const transferOptions = ref([])

const isApproving = computed(() => detail.value?.status === 'APPROVING')
const canHandle = computed(() => !!detail.value?.myTodoTaskId)

const STATUS_LABEL = { APPROVING: '审批中', APPROVED: '已通过', REJECTED: '已驳回', WITHDRAWN: '已撤回' }
const ACTION_LABEL = { SUBMIT: '提交申请', APPROVE: '同意', REJECT: '驳回', TRANSFER: '转办', WITHDRAW: '撤回', COMMENT: '补充意见' }

async function load() {
  loading.value = true
  try {
    const res = await flowDetail(instanceId)
    detail.value = res.data
  } finally {
    loading.value = false
  }
}

async function loadContacts() {
  try {
    const res = await contactsQuery('')
    transferOptions.value = (res.data || [])
      .filter((c) => c.userId)
      .map((c) => ({ value: c.userId, label: `${c.name}（${c.empNo}）` }))
  } catch (_) {
    transferOptions.value = []
  }
}

function fmtTime(v) {
  return v ? String(v).replace('T', ' ') : '—'
}

function displayField(f, v) {
  if (v === undefined || v === null || v === '') return '—'
  if (f.fieldType === 'ATTACHMENT') return '附件已上传'
  if (Array.isArray(v)) return v.join('、')
  return String(v)
}

async function doApprove() {
  handleLoading.value = true
  try {
    await flowApprove(detail.value.myTodoTaskId, { comment: opinion.value })
    ElMessage.success('已同意')
    opinion.value = ''
    load()
  } finally {
    handleLoading.value = false
  }
}

async function doReject() {
  await ElMessageBox.confirm('驳回后流程将直接结束，确认驳回？', '驳回', { type: 'warning' })
  handleLoading.value = true
  try {
    await flowReject(detail.value.myTodoTaskId, { comment: opinion.value })
    ElMessage.success('已驳回')
    opinion.value = ''
    load()
  } finally {
    handleLoading.value = false
  }
}

async function doTransfer() {
  if (!transferUserId.value) {
    ElMessage.warning('请选择转办对象')
    return
  }
  await flowTransfer(detail.value.myTodoTaskId, { toUserId: transferUserId.value, comment: transferComment.value })
  ElMessage.success('已转办')
  showTransfer.value = false
  transferComment.value = ''
  load()
}

async function doWithdraw() {
  await ElMessageBox.confirm('确认撤回该审批？', '撤回', { type: 'warning' })
  await flowWithdraw(instanceId)
  ElMessage.success('已撤回')
  load()
}

async function doComment() {
  if (!opinion.value) {
    ElMessage.warning('请输入意见内容')
    return
  }
  await flowAddComment(instanceId, { content: opinion.value })
  ElMessage.success('意见已添加')
  opinion.value = ''
  load()
}

onMounted(() => {
  load()
  loadContacts()
})
</script>

<template>
  <div v-loading="loading" class="oa-page">
    <template v-if="detail">
      <div class="head-row">
        <h3 class="oa-page-title">
          {{ detail.title }}
          <el-tag :type="detail.status === 'APPROVED' ? 'success' : detail.status === 'REJECTED' ? 'danger' : detail.status === 'WITHDRAWN' ? 'info' : 'warning'" size="small">
            {{ STATUS_LABEL[detail.status] }}
          </el-tag>
        </h3>
        <el-button @click="router.back()">返回</el-button>
      </div>

      <el-row :gutter="16">
        <el-col :md="14" :sm="24">
          <div class="oa-page-card">
            <h4 class="card-title">申请信息</h4>
            <el-descriptions :column="1" border size="small">
              <el-descriptions-item label="流程模板">{{ detail.defName }}</el-descriptions-item>
              <el-descriptions-item label="发起人">{{ detail.applicantName }}</el-descriptions-item>
              <el-descriptions-item label="发起时间">{{ fmtTime(detail.createTime) }}</el-descriptions-item>
              <el-descriptions-item label="完成时间">{{ fmtTime(detail.finishTime) }}</el-descriptions-item>
            </el-descriptions>

            <el-divider content-position="left">申请表单</el-divider>
            <el-descriptions :column="1" border size="small">
              <el-descriptions-item v-for="f in detail.fields" :key="f.fieldKey" :label="f.label">
                <template v-if="f.fieldType === 'ATTACHMENT'">
                  <a v-for="att in detail.attachments" :key="att.fileId" :href="att.downloadUrl" target="_blank"
                     style="color: var(--oa-primary); margin-right: 10px">
                    {{ att.originalName }}
                  </a>
                  <span v-if="!detail.attachments.length">—</span>
                </template>
                <span v-else>{{ displayField(f, detail.formData[f.fieldKey]) }}</span>
              </el-descriptions-item>
            </el-descriptions>

            <el-divider content-position="left">审批链路</el-divider>
            <el-timeline>
              <el-timeline-item v-for="c in detail.comments" :key="c.commentId" :timestamp="fmtTime(c.createTime)" placement="top">
                <div class="log-line">
                  <el-tag size="small" effect="plain">{{ ACTION_LABEL[c.action] || c.action }}</el-tag>
                  <span class="log-user">{{ c.username }}</span>
                  <span v-if="c.content" class="log-msg">{{ c.content }}</span>
                </div>
              </el-timeline-item>
            </el-timeline>
          </div>
        </el-col>

        <el-col :md="10" :sm="24">
          <div class="oa-page-card">
            <h4 class="card-title">节点进度</h4>
            <el-steps direction="vertical" :active="detail.tasks.filter((t) => t.status !== 'PENDING').length">
              <el-step v-for="t in detail.tasks" :key="t.taskId" :title="t.nodeName"
                       :description="`${t.assigneeName} · ${t.status === 'PENDING' ? '待处理' : fmtTime(t.handleTime)}`" />
            </el-steps>
          </div>

          <!-- 我的待办处理 -->
          <div v-if="canHandle && isApproving" class="oa-page-card">
            <h4 class="card-title">我的处理</h4>
            <el-input v-model="opinion" type="textarea" :rows="3" placeholder="审批意见（选填）" />
            <div class="action-row">
              <el-button type="success" :loading="handleLoading" @click="doApprove">同意</el-button>
              <el-button type="danger" :loading="handleLoading" @click="doReject">驳回</el-button>
              <el-button @click="showTransfer = true">转办</el-button>
            </div>
          </div>

          <!-- 参与人补充意见 / 发起人撤回 -->
          <div v-else-if="isApproving" class="oa-page-card">
            <h4 class="card-title">意见与操作</h4>
            <el-input v-model="opinion" type="textarea" :rows="2" placeholder="仅记录意见，不改变状态" />
            <div class="action-row">
              <el-button @click="doComment">添加意见</el-button>
              <el-button v-if="detail.applicantUserId === auth.user?.userId" type="warning" plain @click="doWithdraw">撤回审批</el-button>
            </div>
          </div>
        </el-col>
      </el-row>

      <el-dialog v-model="showTransfer" title="转办审批" width="420px">
        <el-select v-model="transferUserId" placeholder="选择转办对象" filterable style="width: 100%">
          <el-option v-for="opt in transferOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
        </el-select>
        <el-input v-model="transferComment" type="textarea" :rows="2" placeholder="转办说明（选填）" style="margin-top: 10px" />
        <template #footer>
          <el-button @click="showTransfer = false">取消</el-button>
          <el-button type="primary" @click="doTransfer">确认转办</el-button>
        </template>
      </el-dialog>
    </template>
  </div>
</template>

<style scoped>
.head-row { display: flex; align-items: center; justify-content: space-between; }
.card-title { margin: 0 0 12px; font-size: 14px; color: var(--oa-navy); }
.log-line { display: flex; align-items: center; gap: 8px; }
.log-user { color: var(--oa-ink-light); font-size: 12px; }
.log-msg { color: var(--oa-ink); font-size: 13px; }
.action-row { display: flex; gap: 10px; margin-top: 12px; flex-wrap: wrap; }
</style>