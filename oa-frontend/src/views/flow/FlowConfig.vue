<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { flowAdminDefs, flowAdminForms, flowAdminFormSave, flowAdminFormUpdate, flowAdminDefSave, flowAdminDefUpdate, flowAdminDefDelete, flowAdminDefToggle } from '@/api'

const loading = ref(false)
const defs = ref([])
const forms = ref([])

const dialogVisible = ref(false)
const editingId = ref(null)
const defMeta = ref({ formId: '', defName: '', category: 'custom' })
const nodes = ref([])

const APPROVER_TYPES = [
  { key: 'DEPT_LEADER', label: '发起人部门负责人' },
  { key: 'ROLE', label: '指定角色' },
  { key: 'SPECIFIC_USER', label: '指定用户' },
  { key: 'OWN_DEPT', label: '发起人部门全体' }
]
const APPROVER_LABEL = Object.fromEntries(APPROVER_TYPES.map((t) => [t.key, t.label]))
const BRANCH_OPS = [
  { key: '>', label: '大于' },
  { key: '>=', label: '大于等于' },
  { key: '<', label: '小于' },
  { key: '==', label: '等于' },
  { key: 'contains', label: '包含' }
]

async function load() {
  loading.value = true
  try {
    const [d, f] = await Promise.all([flowAdminDefs(), flowAdminForms()])
    defs.value = d.data || []
    forms.value = f.data || []
  } finally {
    loading.value = false
  }
}

function fieldOptions() {
  const form = forms.value.find((x) => x.formId === defMeta.value.formId)
  return (form?.fields || []).filter((x) => ['NUMBER', 'SELECT', 'MULTI_SELECT'].includes(x.fieldType))
}

function reset() {
  editingId.value = null
  defMeta.value = { formId: '', defName: '', category: 'custom' }
  nodes.value = [{ nodeName: '部门负责人审批', approverType: 'DEPT_LEADER', approverIds: '', branchField: '', branchOp: '>', branchValue: '', toIdx: -1, nextIdx: -1 }]
  dialogVisible.value = true
}

function addNode() {
  nodes.value.push({ nodeName: `审批节点 ${nodes.value.length}`, approverType: 'DEPT_LEADER', approverIds: '', branchField: '', branchOp: '>', branchValue: '', toIdx: -1, nextIdx: -1 })
}

function removeNode(idx) {
  if (nodes.value.length <= 1) {
    ElMessage.warning('至少保留一个审批节点')
    return
  }
  nodes.value.splice(idx, 1)
}

function editDef(def) {
  editingId.value = def.defId
  defMeta.value = { formId: def.formId, defName: def.defName, category: def.category || 'custom' }
  nodes.value = ((def.nodes || []).filter((n) => n.nodeType === 'APPROVAL')).map((n) => ({
    nodeName: n.nodeName,
    approverType: n.approverType,
    approverIds: n.approverIds || '',
    branchField: n.branchField || '',
    branchOp: n.branchOp || '>',
    branchValue: n.branchValue || '',
    toIdx: nodeIdxOf(n.branchToNodeId, def.nodes),
    nextIdx: nodeIdxOf(n.nextNodeId, def.nodes)
  }))
  dialogVisible.value = true
}

function nodeIdxOf(nodeId, nodesList) {
  if (!nodeId) return -1
  const approvals = (nodesList || []).filter((n) => n.nodeType === 'APPROVAL')
  const idx = approvals.findIndex((n) => n.nodeId === nodeId)
  return idx
}

async function save() {
  if (!defMeta.value.defName || !defMeta.value.formId) {
    ElMessage.warning('请填写流程名称并选择表单')
    return
  }
  const payload = {
    formId: defMeta.value.formId,
    defName: defMeta.value.defName,
    category: defMeta.value.category,
    nodes: nodes.value.map((n) => ({
      nodeName: n.nodeName,
      nodeType: 'APPROVAL',
      approverType: n.approverType,
      approverIds: n.approverType === 'ROLE' || n.approverType === 'SPECIFIC_USER' ? n.approverIds || '' : undefined,
      branchField: n.branchField || '',
      branchOp: n.branchOp || '',
      branchValue: n.branchValue || '',
      branchToIdx: n.branchField ? n.toIdx : -1,
      nextIdx: n.nextIdx
    }))
  }
  if (editingId.value) {
    await flowAdminDefUpdate(editingId.value, payload)
    ElMessage.success('流程已更新')
  } else {
    await flowAdminDefSave(payload)
    ElMessage.success('流程已创建')
  }
  dialogVisible.value = false
  load()
}

async function removeDef(def) {
  await ElMessageBox.confirm(`确认删除流程「${def.defName}」？`, '删除', { type: 'warning' })
  await flowAdminDefDelete(def.defId)
  ElMessage.success('已删除')
  load()
}

async function toggle(def) {
  await flowAdminDefToggle(def.defId)
  ElMessage.success(def.status === 1 ? '已停用' : '已启用')
  load()
}

onMounted(load)
</script>

<template>
  <div class="oa-page">
    <h3 class="oa-page-title">流程配置</h3>
    <div class="oa-page-card">
      <div style="margin-bottom: 14px; text-align: right">
        <el-button type="primary" @click="reset">新建流程</el-button>
      </div>
      <el-table :data="defs" border stripe>
        <el-table-column prop="defName" label="流程名称" min-width="150" />
        <el-table-column prop="formName" label="绑定表单" width="140" />
        <el-table-column prop="category" label="类别" width="110">
          <template #default="{ row }"><span class="oa-mono">{{ row.category }}</span></template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="内置" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.builtIn === 1" size="small" type="info">内置</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="170" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" :disabled="row.builtIn === 1" @click="editDef(row)">编辑</el-button>
            <el-button link type="warning" @click="toggle(row)">{{ row.status === 1 ? '停用' : '启用' }}</el-button>
            <el-button link type="danger" :disabled="row.builtIn === 1" @click="removeDef(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑流程' : '新建流程'" width="880px">
      <el-form label-width="90px" inline>
        <el-form-item label="流程名称"><el-input v-model="defMeta.defName" style="width: 200px" /></el-form-item>
        <el-form-item label="绑定表单">
          <el-select v-model="defMeta.formId" style="width: 200px" filterable>
            <el-option v-for="f in forms" :key="f.formId" :label="f.formName" :value="f.formId" />
          </el-select>
        </el-form-item>
        <el-form-item label="类别">
          <el-select v-model="defMeta.category" style="width: 150px">
            <el-option label="自定义" value="custom" />
            <el-option label="请假" value="leave" />
            <el-option label="加班" value="overtime" />
            <el-option label="出差" value="travel" />
            <el-option label="物品领用" value="requisition" />
          </el-select>
        </el-form-item>
      </el-form>

      <div class="node-head">审批节点（按顺序串行；支持一个分支条件）</div>
      <div v-for="(n, idx) in nodes" :key="idx" class="node-card">
        <div class="node-row">
          <span class="node-idx">{{ idx + 1 }}</span>
          <el-input v-model="n.nodeName" placeholder="节点名称" style="width: 180px" />
          <el-select v-model="n.approverType" style="width: 200px">
            <el-option v-for="t in APPROVER_TYPES" :key="t.key" :label="t.label" :value="t.key" />
          </el-select>
          <el-input v-if="n.approverType === 'ROLE'" v-model="n.approverIds" placeholder="角色值，如 ADMIN" style="width: 160px" />
          <el-input v-if="n.approverType === 'SPECIFIC_USER'" v-model="n.approverIds" placeholder="用户ID，逗号分隔" style="width: 160px" />
          <el-select v-model="n.nextIdx" style="width: 150px" placeholder="下一节点">
            <el-option label="结束" :value="-1" />
            <el-option v-for="(x, xi) in nodes" :key="'n' + xi" :label="`节点 ${xi + 1}`" :value="xi" />
          </el-select>
          <el-button link type="danger" @click="removeNode(idx)">删除</el-button>
        </div>
        <div class="node-row branch-row">
          <span class="branch-tag">分支条件</span>
          <el-select v-model="n.branchField" style="width: 160px" clearable placeholder="选择字段">
            <el-option v-for="fld in fieldOptions()" :key="fld.fieldKey" :label="fld.label" :value="fld.fieldKey" />
          </el-select>
          <el-select v-if="n.branchField" v-model="n.branchOp" style="width: 110px">
            <el-option v-for="o in BRANCH_OPS" :key="o.key" :label="o.label" :value="o.key" />
          </el-select>
          <el-input v-if="n.branchField" v-model="n.branchValue" placeholder="比较值" style="width: 120px" />
          <span v-if="n.branchField">满足时流转至</span>
          <el-select v-if="n.branchField" v-model="n.toIdx" style="width: 150px">
            <el-option label="结束" :value="-1" />
            <el-option v-for="(x, xi) in nodes" :key="'b' + xi" :label="`节点 ${xi + 1}`" :value="xi" />
          </el-select>
        </div>
      </div>
      <div style="margin-top: 10px">
        <el-button @click="addNode">+ 添加节点</el-button>
      </div>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.node-head { color: var(--oa-ink-light); font-size: 12px; margin-bottom: 8px; }
.node-card { border: 1px solid var(--oa-line); border-radius: 6px; padding: 10px; margin-bottom: 10px; background: var(--oa-surface); }
.node-row { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
.branch-row { margin-top: 8px; }
.node-idx { width: 22px; height: 22px; border-radius: 50%; background: var(--oa-primary); color: #fff; font-size: 12px; text-align: center; line-height: 22px; }
.branch-tag { color: var(--oa-ink-light); font-size: 12px; }
</style>