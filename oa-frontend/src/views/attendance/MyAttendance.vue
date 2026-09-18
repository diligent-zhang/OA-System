<script setup>
import { onMounted, ref } from 'vue'
import { attMyRecords } from '@/api'

const loading = ref(false)
const list = ref([])
const month = ref(new Date().toISOString().slice(0, 7))

const STATUS_LABEL = {
  NORMAL: '正常',
  LATE: '迟到',
  EARLY_LEAVE: '早退',
  MISSING_IN: '缺上班卡',
  MISSING_OUT: '缺下班卡',
  LEAVE: '请假',
  OVERTIME: '加班',
  ABSENT: '旷工'
}
const STATUS_TYPE = {
  NORMAL: 'success',
  LATE: 'warning',
  EARLY_LEAVE: 'warning',
  MISSING_IN: 'danger',
  MISSING_OUT: 'danger',
  LEAVE: 'info',
  OVERTIME: 'primary',
  ABSENT: 'danger'
}

const days = ref([])

async function load() {
  loading.value = true
  try {
    const res = await attMyRecords({ month: month.value })
    list.value = res.data || []
    // 构建月度日历网格
    const [y, m] = month.value.split('-').map(Number)
    const first = new Date(y, m - 1, 1)
    const count = new Date(y, m, 0).getDate()
    days.value = []
    for (let i = 1; i <= count; i++) {
      const dateStr = `${month.value}-${String(i).padStart(2, '0')}`
      const rec = list.value.find((r) => r.recordDate === dateStr)
      days.value.push({ date: i, dateStr, rec })
    }
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<template>
  <div class="oa-page">
    <h3 class="oa-page-title">我的考勤</h3>
    <div class="oa-page-card">
      <div style="display: flex; align-items: center; gap: 12px; margin-bottom: 14px">
        <el-date-picker v-model="month" type="month" value-format="YYYY-MM" @change="load" />
        <span style="font-size: 13px; color: var(--oa-ink-light)">按月查看每日打卡明细</span>
      </div>

      <div v-loading="loading" class="cal-grid">
        <div v-for="d in days" :key="d.dateStr" class="cal-cell" :class="{ empty: !d.rec }">
          <div class="cal-date">{{ d.date }}</div>
          <template v-if="d.rec">
            <el-tag :type="STATUS_TYPE[d.rec.status] || 'info'" size="small">{{ STATUS_LABEL[d.rec.status] || d.rec.status }}</el-tag>
            <div class="cal-time oa-mono">
              {{ d.rec.clockIn ? d.rec.clockIn.slice(11, 16) : '--' }}
              <span class="sep">/</span>
              {{ d.rec.clockOut ? d.rec.clockOut.slice(11, 16) : '--' }}
            </div>
          </template>
          <template v-else>
            <div class="cal-dash">-</div>
          </template>
        </div>
      </div>

      <el-table :data="list" border stripe style="margin-top: 18px">
        <el-table-column prop="recordDate" label="日期" width="130" />
        <el-table-column prop="clockIn" label="上班打卡" min-width="150">
          <template #default="{ row }">{{ row.clockIn ? row.clockIn.slice(11, 16) : '—' }}</template>
        </el-table-column>
        <el-table-column prop="clockOut" label="下班打卡" min-width="150">
          <template #default="{ row }">{{ row.clockOut ? row.clockOut.slice(11, 16) : '—' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="STATUS_TYPE[row.status] || 'info'" size="small">{{ STATUS_LABEL[row.status] || row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="source" label="来源" width="110">
          <template #default="{ row }">{{ row.source === 'FLOW_LINK' ? '审批联动' : '手动打卡' }}</template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="160" />
      </el-table>
    </div>
  </div>
</template>

<style scoped>
.cal-grid { display: grid; grid-template-columns: repeat(7, 1fr); gap: 8px; }
.cal-cell { border: 1px solid var(--oa-line); border-radius: 6px; padding: 10px; min-height: 76px; text-align: center; }
.cal-cell.empty { background: var(--oa-surface); }
.cal-date { font-weight: 600; color: var(--oa-navy); margin-bottom: 6px; }
.cal-time { font-size: 12px; color: var(--oa-ink-light); margin-top: 6px; }
.cal-time .sep { color: #c4cfdd; }
.cal-dash { color: #c4cfdd; }
</style>