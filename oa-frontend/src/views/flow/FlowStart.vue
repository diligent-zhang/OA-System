<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { flowEnabledDefs, flowDefDetail, flowSubmit, fileUpload } from '@/api'

const route = useRoute()
const router = useRouter()

const defs = ref([])
const loading = ref(false)
const def = ref(null)
const formRef = ref()
const form = ref({})
const rules = ref({})
const submitting = ref(false)

const FIELD_TYPES = {
  TEXT: 'input',
  NUMBER: 'number',
  DATE: 'date',
  SELECT: 'select',
  MULTI_SELECT: 'multi',
  ATTACHMENT: 'attachment'
}

const fields = computed(() => (def.value ? def.value.fields || [] : []))

function parseOptions(field) {
  if (!field.options) return []
  try {
    return JSON.parse(field.options)
  } catch (_) {
    return []
  }
}

function buildRules() {
  const r = {}
  fields.value.forEach((f) => {
    if (f.required === 1) {
      r[f.fieldKey] = [
        { required: true, message: `请填写「${f.label}」`, trigger: ['blur', 'change'] }
      ]
    }
  })
  rules.value = r
}

async function loadForm(defId) {
  loading.value = true
  try {
    const res = await flowDefDetail(defId)
    def.value = res.data
    form.value = {}
    buildRules()
  } finally {
    loading.value = false
  }
}

async function onUpload(field) {
  const input = document.createElement('input')
  input.type = 'file'
  input.onchange = async () => {
    const file = input.files[0]
    if (!file) return
    const fd = new FormData()
    fd.append('file', file)
    // 先以临时 bizId 上传，提交审批后由后端回填到实例
    const res = await fileUpload(fd, 'flow', '0')
    form.value[field.fieldKey] = res.data.fileId
    ElMessage.success('附件已上传')
  }
  input.click()
}

async function submit() {
  try {
    await formRef.value.validate()
  } catch (_) {
    return
  }
  submitting.value = true
  try {
    await flowSubmit({ defId: def.value.defId, title: buildTitle(), formData: form.value })
    ElMessage.success('审批已提交')
    router.push('/flow/mine')
  } finally {
    submitting.value = false
  }
}

function buildTitle() {
  const leaveType = form.value.leave_type || ''
  const reason = (form.value.reason || '').toString().slice(0, 20)
  return leaveType && reason ? `${leaveType}-${reason}` : `${def.value.defName}申请`
}

onMounted(async () => {
  if (route.params.defId) {
    await loadForm(route.params.defId)
  } else {
    loading.value = true
    try {
      const res = await flowEnabledDefs()
      defs.value = res.data || []
    } finally {
      loading.value = false
    }
  }
})
</script>

<template>
  <div v-loading="loading" class="oa-page">
    <template v-if="!def">
      <h3 class="oa-page-title">发起审批</h3>
      <div class="oa-page-card">
        <el-empty v-if="!defs.length" description="暂无可用的审批流程，请联系管理员配置" />
        <el-row :gutter="16">
          <el-col v-for="d in defs" :key="d.defId" :md="8" :sm="12" :xs="24">
            <div class="def-card" role="button" tabindex="0" @click="loadForm(d.defId)" @keyup.enter="loadForm(d.defId)">
              <div class="def-icon"><el-icon><Document /></el-icon></div>
              <div class="def-name">{{ d.defName }}</div>
              <div class="def-meta">{{ d.category }}</div>
            </div>
          </el-col>
        </el-row>
      </div>
    </template>

    <template v-else>
      <h3 class="oa-page-title">填写申请 · {{ def.defName }}</h3>
      <div class="oa-page-card" style="max-width: 720px">
        <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
          <el-form-item v-for="f in fields" :key="f.fieldKey" :label="f.label" :prop="f.fieldKey" :required="f.required === 1">
            <!-- 文本 -->
            <el-input v-if="FIELD_TYPES[f.fieldType] === 'input'" v-model="form[f.fieldKey]" :placeholder="`请输入${f.label}`" maxlength="200" />
            <!-- 数字 -->
            <el-input-number v-else-if="FIELD_TYPES[f.fieldType] === 'number'" v-model="form[f.fieldKey]" :min="0" :precision="f.fieldKey === 'hours' ? 1 : 0" />
            <!-- 日期 -->
            <el-date-picker v-else-if="FIELD_TYPES[f.fieldType] === 'date'" v-model="form[f.fieldKey]" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" />
            <!-- 单选 -->
            <el-radio-group v-else-if="FIELD_TYPES[f.fieldType] === 'select'" v-model="form[f.fieldKey]">
              <el-radio v-for="opt in parseOptions(f)" :key="opt" :value="opt">{{ opt }}</el-radio>
            </el-radio-group>
            <!-- 多选 -->
            <el-checkbox-group v-else-if="FIELD_TYPES[f.fieldType] === 'multi'" v-model="form[f.fieldKey]">
              <el-checkbox v-for="opt in parseOptions(f)" :key="opt" :value="opt">{{ opt }}</el-checkbox>
            </el-checkbox-group>
            <!-- 附件 -->
            <div v-else-if="FIELD_TYPES[f.fieldType] === 'attachment'" class="att-row">
              <el-button size="small" @click="onUpload(f)">上传附件</el-button>
              <span v-if="form[f.fieldKey]" class="att-done">已上传 √</span>
            </div>
          </el-form-item>
        </el-form>
        <div class="form-actions">
          <el-button @click="router.push('/flow/start')">返回</el-button>
          <el-button type="primary" :loading="submitting" @click="submit">提交审批</el-button>
        </div>
      </div>
    </template>
  </div>
</template>

<style scoped>
.def-card {
  background: #fff;
  border: 1px solid var(--oa-line);
  border-radius: 6px;
  padding: 18px;
  text-align: center;
  cursor: pointer;
  margin-bottom: 14px;
  transition: border-color 0.15s, transform 0.15s;
}
.def-card:hover { border-color: var(--oa-primary); transform: translateY(-2px); }
.def-icon { font-size: 26px; color: var(--oa-primary); }
.def-name { font-weight: 600; margin-top: 8px; color: var(--oa-navy); }
.def-meta { font-size: 12px; color: var(--oa-ink-light); margin-top: 2px; font-family: var(--oa-mono); }
.att-row { display: flex; align-items: center; gap: 10px; }
.att-done { color: #047857; font-size: 13px; }
.form-actions { text-align: center; }
</style>