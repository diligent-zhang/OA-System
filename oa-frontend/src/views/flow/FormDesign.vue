<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { flowAdminForms, flowAdminFormSave, flowAdminFormUpdate, flowAdminFormDelete } from '@/api'

const loading = ref(false)
const forms = ref([])

const dialogVisible = ref(false)
const editingId = ref(null)
const formMeta = ref({ formName: '', remark: '' })
const fields = ref([])

const FIELD_TYPES = [
  { key: 'TEXT', label: '文本' },
  { key: 'NUMBER', label: '数字' },
  { key: 'DATE', label: '日期' },
  { key: 'DATETIME', label: '日期时间' },
  { key: 'DURATION', label: '时长(自动)' },
  { key: 'SELECT', label: '单选' },
  { key: 'MULTI_SELECT', label: '多选' },
  { key: 'ATTACHMENT', label: '附件' }
]

const FIELD_LABEL = Object.fromEntries(FIELD_TYPES.map((t) => [t.key, t.label]))

async function load() {
  loading.value = true
  try {
    const res = await flowAdminForms()
    forms.value = res.data || []
  } finally {
    loading.value = false
  }
}

function defaultKey(type) {
  const map = { TEXT: 'text', NUMBER: 'num', DATE: 'date', DATETIME: 'datetime', DURATION: 'duration', SELECT: 'select', MULTI_SELECT: 'multiSel', ATTACHMENT: 'attach' }
  return `${map[type]}_${Date.now().toString(36)}`
}

function addField(type) {
  fields.value.push({
    fieldKey: defaultKey(type),
    label: FIELD_LABEL[type],
    fieldType: type,
    options: [],
    durFrom: '',
    durTo: '',
    required: false
  })
}

function removeField(idx) {
  fields.value.splice(idx, 1)
}

function moveField(idx, dir) {
  const target = idx + dir
  if (target < 0 || target >= fields.value.length) return
  const arr = fields.value
  ;[arr[idx], arr[target]] = [arr[target], arr[idx]]
}

function reset() {
  editingId.value = null
  formMeta.value = { formName: '', remark: '' }
  fields.value = []
  dialogVisible.value = true
}

function editForm(form) {
  editingId.value = form.formId
  formMeta.value = { formName: form.formName, remark: form.remark || '' }
  fields.value = (form.fields || []).map((f) => ({
    fieldKey: f.fieldKey,
    label: f.label,
    fieldType: f.fieldType,
    options: parseOptions(f.options),
    durFrom: parseDuration(f.options).from || '',
    durTo: parseDuration(f.options).to || '',
    required: f.required === 1
  }))
  dialogVisible.value = true
}

function parseOptions(options) {
  if (!options) return []
  try {
    const arr = JSON.parse(options)
    return Array.isArray(arr) ? arr : []
  } catch (_) {
    return []
  }
}

function parseDuration(options) {
  if (!options) return {}
  try {
    const o = JSON.parse(options)
    return o && typeof o === 'object' && !Array.isArray(o) ? o : {}
  } catch (_) {
    return {}
  }
}

/* 可作为时长来源的字段：DATE / DATETIME，排除自身 */
function sourceFields(current) {
  return fields.value.filter(
    (f) => ['DATE', 'DATETIME'].includes(f.fieldType) && f.fieldKey !== current.fieldKey
  )
}

async function save() {
  if (!formMeta.value.formName) {
    ElMessage.warning('请填写表单名称')
    return
  }
  if (!fields.value.length) {
    ElMessage.warning('请至少添加一个字段')
    return
  }
  const payload = {
    formName: formMeta.value.formName,
    remark: formMeta.value.remark,
    fields: fields.value.map((f, i) => ({
      fieldKey: f.fieldKey,
      label: f.label,
      fieldType: f.fieldType,
      required: !!f.required,
      options: ['SELECT', 'MULTI_SELECT'].includes(f.fieldType)
        ? f.options || []
        : f.fieldType === 'DURATION' && f.durFrom && f.durTo
          ? { from: f.durFrom, to: f.durTo }
          : undefined
    }))
  }
  if (editingId.value) {
    await flowAdminFormUpdate(editingId.value, payload)
    ElMessage.success('表单已更新')
  } else {
    await flowAdminFormSave(payload)
    ElMessage.success('表单已创建')
  }
  dialogVisible.value = false
  load()
}

async function removeForm(form) {
  await ElMessageBox.confirm(`确认删除表单「${form.formName}」？`, '删除', { type: 'warning' })
  await flowAdminFormDelete(form.formId)
  ElMessage.success('已删除')
  load()
}

onMounted(load)
</script>

<template>
  <div class="oa-page">
    <h3 class="oa-page-title">表单设计（点击字段类型添加，可上下排序）</h3>
    <div class="oa-page-card">
      <div class="toolbar">
        <template v-for="t in FIELD_TYPES" :key="t.key">
          <el-button @click="addField(t.key)">+ {{ t.label }}</el-button>
        </template>
        <span style="flex: 1"></span>
        <el-button type="primary" @click="reset">新建表单</el-button>
      </div>

      <el-table :data="forms" border stripe>
        <el-table-column prop="formName" label="表单名称" min-width="160" />
        <el-table-column prop="formKey" label="标识" width="160">
          <template #default="{ row }"><span class="oa-mono">{{ row.formKey }}</span></template>
        </el-table-column>
        <el-table-column label="字段数" width="90">
          <template #default="{ row }">{{ (row.fields || []).length }}</template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="160" />
        <el-table-column label="内置" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.builtIn === 1" size="small">内置</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" :disabled="row.builtIn === 1" @click="editForm(row)">编辑</el-button>
            <el-button link type="danger" :disabled="row.builtIn === 1" @click="removeForm(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑表单' : '新建表单'" width="760px">
      <el-form label-width="80px">
        <el-form-item label="表单名称"><el-input v-model="formMeta.formName" maxlength="60" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="formMeta.remark" maxlength="255" /></el-form-item>
      </el-form>

      <div class="palette-tip">从上方「+ 字段类型」添加字段，再编辑属性：</div>
      <el-table :data="fields" border size="small">
        <el-table-column label="排序" width="80">
          <template #default="{ $index }">
            <el-button link size="small" :disabled="$index === 0" @click="moveField($index, -1)">↑</el-button>
            <el-button link size="small" :disabled="$index === fields.length - 1" @click="moveField($index, 1)">↓</el-button>
          </template>
        </el-table-column>
        <el-table-column label="字段名称" width="200">
          <template #default="{ row }"><el-input v-model="row.label" size="small" /></template>
        </el-table-column>
        <el-table-column label="类型" width="110">
          <template #default="{ row }">{{ FIELD_LABEL[row.fieldType] }}</template>
        </el-table-column>
        <el-table-column label="字段键" width="160">
          <template #default="{ row }"><el-input v-model="row.fieldKey" size="small" :disabled="true" /></template>
        </el-table-column>
        <el-table-column label="必填" width="60">
          <template #default="{ row }"><el-checkbox v-model="row.required" /></template>
        </el-table-column>
        <el-table-column label="操作" width="70">
          <template #default="{ $index }">
            <el-button link type="danger" size="small" @click="removeField($index)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 选项编辑 -->
      <div v-if="fields.some((f) => ['SELECT', 'MULTI_SELECT'].includes(f.fieldType))" style="margin-top: 14px">
        <div v-for="(f, idx) in fields.filter((f) => ['SELECT', 'MULTI_SELECT'].includes(f.fieldType))" :key="f.fieldKey" class="opts-editor">
          <span class="opts-label">{{ f.label }} 候选值：</span>
          <el-select v-model="f.options" multiple filterable allow-create default-first-option placeholder="输入后回车添加" style="max-width: 420px">
            <el-option v-for="o in f.options" :key="o" :value="o" />
          </el-select>
        </div>
      </div>

      <!-- 时长来源编辑：时长字段由两个日期时间字段相减自动计算 -->
      <div v-if="fields.some((f) => f.fieldType === 'DURATION')" style="margin-top: 14px">
        <div v-for="f in fields.filter((f) => f.fieldType === 'DURATION')" :key="f.fieldKey" class="opts-editor">
          <span class="opts-label">{{ f.label }} 时长来源：</span>
          <el-select v-model="f.durFrom" placeholder="开始字段" style="width: 180px">
            <el-option v-for="o in sourceFields(f)" :key="o.fieldKey" :label="o.label" :value="o.fieldKey" />
          </el-select>
          <span class="opts-label">至</span>
          <el-select v-model="f.durTo" placeholder="结束字段" style="width: 180px">
            <el-option v-for="o in sourceFields(f)" :key="o.fieldKey" :label="o.label" :value="o.fieldKey" />
          </el-select>
        </div>
      </div>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.toolbar { display: flex; gap: 10px; margin-bottom: 14px; flex-wrap: wrap; }
.palette-tip { color: var(--oa-ink-light); font-size: 12px; margin-bottom: 8px; }
.opts-editor { display: flex; align-items: center; gap: 10px; margin-bottom: 8px; }
.opts-label { color: var(--oa-ink); font-size: 13px; }
</style>