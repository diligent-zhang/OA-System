<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { attAdminRules, attAdminRuleSave, attAdminRuleUpdate, attAdminRuleDelete, attAdminRuleToggle } from '@/api'

const loading = ref(false)
const rules = ref([])
const dialogVisible = ref(false)
const editingId = ref(null)
const form = ref({ ruleName: '', workStart: '09:00', workEnd: '18:00', radiusM: 200, locationCheck: 0, latitude: null, longitude: null, restDays: ['6', '7'] })

const WEEK = [
  { key: '0', label: '周日' },
  { key: '1', label: '周一' },
  { key: '2', label: '周二' },
  { key: '3', label: '周三' },
  { key: '4', label: '周四' },
  { key: '5', label: '周五' },
  { key: '6', label: '周六' }
]

async function load() {
  loading.value = true
  try {
    const res = await attAdminRules()
    rules.value = res.data || []
  } finally {
    loading.value = false
  }
}

function reset() {
  editingId.value = null
  form.value = { ruleName: '', workStart: '09:00', workEnd: '18:00', radiusM: 200, locationCheck: 0, latitude: null, longitude: null, restDays: ['6', '7'] }
  dialogVisible.value = true
}

function editRule(row) {
  editingId.value = row.ruleId
  let restDays = []
  try {
    restDays = JSON.parse(row.restDays || '[]')
  } catch (_) {
    restDays = []
  }
  form.value = {
    ruleName: row.ruleName,
    workStart: row.workStart,
    workEnd: row.workEnd,
    radiusM: row.radiusM,
    locationCheck: row.locationCheck,
    latitude: row.latitude,
    longitude: row.longitude,
    restDays
  }
  dialogVisible.value = true
}

async function save() {
  if (!form.value.ruleName) {
    ElMessage.warning('请填写规则名称')
    return
  }
  if (editingId.value) {
    await attAdminRuleUpdate(editingId.value, form.value)
    ElMessage.success('规则已更新')
  } else {
    await attAdminRuleSave(form.value)
    ElMessage.success('规则已保存并生效，旧的生效规则将停用')
  }
  dialogVisible.value = false
  load()
}

async function removeRule(row) {
  await ElMessageBox.confirm('确认删除该规则？', '删除', { type: 'warning' })
  await attAdminRuleDelete(row.ruleId)
  ElMessage.success('已删除')
  load()
}

async function toggle(row) {
  await attAdminRuleToggle(row.ruleId, row.enabled === 1 ? 0 : 1)
  ElMessage.success(row.enabled === 1 ? '已停用' : '已启用')
  load()
}

onMounted(load)
</script>

<template>
  <div class="oa-page">
    <h3 class="oa-page-title">考勤规则配置</h3>
    <div class="oa-page-card">
      <div style="margin-bottom: 14px; text-align: right">
        <el-button type="primary" @click="reset">新建规则</el-button>
      </div>
      <el-table :data="rules" border stripe>
        <el-table-column prop="ruleName" label="规则名称" min-width="130" />
        <el-table-column label="上下班时间" width="150">
          <template #default="{ row }"><span class="oa-mono">{{ row.workStart }} ~ {{ row.workEnd }}</span></template>
        </el-table-column>
        <el-table-column label="定位校验" width="100">
          <template #default="{ row }">{{ row.locationCheck === 1 ? '开启' : '关闭' }}</template>
        </el-table-column>
        <el-table-column prop="radiusM" label="打卡半径(米)" width="120" />
        <el-table-column label="休息日" min-width="130">
          <template #default="{ row }">
            <span>{{ row.restDays || JSON.stringify(['6','7']) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.enabled === 1 ? 'success' : 'info'" size="small">{{ row.enabled === 1 ? '生效' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="editRule(row)">编辑</el-button>
            <el-button link type="warning" @click="toggle(row)">{{ row.enabled === 1 ? '停用' : '启用' }}</el-button>
            <el-button link type="danger" @click="removeRule(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑规则' : '新建规则'" width="520px">
      <el-form label-width="110px">
        <el-form-item label="规则名称"><el-input v-model="form.ruleName" /></el-form-item>
        <el-form-item label="上班时间"><el-time-picker v-model="form.workStart" format="HH:mm" value-format="HH:mm" style="width: 100%" /></el-form-item>
        <el-form-item label="下班时间"><el-time-picker v-model="form.workEnd" format="HH:mm" value-format="HH:mm" style="width: 100%" /></el-form-item>
        <el-form-item label="休息日">
          <el-checkbox-group v-model="form.restDays">
            <el-checkbox v-for="w in WEEK" :key="w.key" :value="w.key">{{ w.label }}</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="定位校验">
          <el-switch v-model="form.locationCheck" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <template v-if="form.locationCheck === 1">
          <el-form-item label="经度"><el-input-number v-model="form.longitude" :precision="6" style="width: 100%" /></el-form-item>
          <el-form-item label="纬度"><el-input-number v-model="form.latitude" :precision="6" style="width: 100%" /></el-form-item>
          <el-form-item label="打卡半径(米)"><el-input-number v-model="form.radiusM" :min="50" :step="50" style="width: 100%" /></el-form-item>
        </template>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>