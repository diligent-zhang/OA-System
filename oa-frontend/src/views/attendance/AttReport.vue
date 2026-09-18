<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { attAdminReport, attReportExport, deptAdminAttReport, deptAttReportExport, downloadBlob } from '@/api'
import { auth } from '@/stores/auth'

const isDeptAdmin = auth.role === 'DEPT_ADMIN'

const loading = ref(false)
const rows = ref([])
const month = ref(new Date().toISOString().slice(0, 7))

async function load() {
  loading.value = true
  try {
    const api = isDeptAdmin ? deptAdminAttReport : attAdminReport
    // isDeptAdmin 会走 /dept-admin/...；超管走 /admin/...
    const res = await api({ month: month.value })
    rows.value = res.data || []
  } finally {
    loading.value = false
  }
}

async function exportCsv() {
  const res = isDeptAdmin ? await deptAttReportExport(month.value) : await attReportExport(month.value)
  downloadBlob(res, `考勤报表-${month.value}.csv`)
  ElMessage.success('报表已导出')
}

onMounted(load)
</script>

<template>
  <div class="oa-page">
    <h3 class="oa-page-title">{{ isDeptAdmin ? '本部门考勤报表' : '月度考勤报表' }}（Excel 兼容导出）</h3>
    <div class="oa-page-card">
      <div style="display: flex; align-items: center; gap: 12px; margin-bottom: 14px">
        <el-date-picker v-model="month" type="month" value-format="YYYY-MM" @change="load" />
        <el-button @click="load">查询</el-button>
        <el-button type="primary" @click="exportCsv">导出 Excel</el-button>
      </div>

      <el-table v-loading="loading" :data="rows" border stripe>
        <el-table-column prop="empNo" label="工号" width="130">
          <template #default="{ row }"><span class="oa-mono">{{ row.empNo }}</span></template>
        </el-table-column>
        <el-table-column prop="name" label="姓名" width="90" />
        <el-table-column prop="deptName" label="部门" width="130" />
        <el-table-column prop="shouldDays" label="应出勤" width="80" />
        <el-table-column prop="actualDays" label="实出勤" width="80" />
        <el-table-column prop="lateCount" label="迟到" width="70" />
        <el-table-column prop="earlyCount" label="早退" width="70" />
        <el-table-column prop="missingCount" label="缺卡" width="70" />
        <el-table-column prop="leaveDays" label="请假天" width="80" />
        <el-table-column prop="overtimeCount" label="加班次" width="80" />
        <el-table-column prop="overtimeHours" label="加班时长" width="90">
          <template #default="{ row }"><span class="oa-mono">{{ row.overtimeHours }}</span></template>
        </el-table-column>
        <el-table-column prop="absentDays" label="旷工" width="70">
          <template #default="{ row }">
            <span :style="{ color: row.absentDays > 0 ? '#dc2626' : 'inherit' }">{{ row.absentDays }}</span>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>