<script setup>
import { onMounted, ref, onBeforeUnmount } from 'vue'
import * as echarts from 'echarts'
import { dashStats, dashDeptEmp, dashLatestNotices } from '@/api'
import { auth } from '@/stores/auth'

const stats = ref({ empTotal: 0, onJobTotal: 0, deptTotal: 0, noticeTotal: 0 })
const notices = ref([])
const loading = ref(true)
const chartRef = ref()
let chart = null

const CARDS = [
  { key: 'empTotal', label: '员工总数', icon: 'UserFilled', tone: 'primary' },
  { key: 'onJobTotal', label: '在职人数', icon: 'Sunny', tone: 'success' },
  { key: 'deptTotal', label: '部门总数', icon: 'OfficeBuilding', tone: 'navy' },
  { key: 'noticeTotal', label: '公告总数', icon: 'ChatDotSquare', tone: 'signal' }
]

const TYPE_LABEL = { 1: '通知', 2: '公告', 3: '制度' }

async function load() {
  loading.value = true
  try {
    const [s, d, n] = await Promise.all([dashStats(), dashDeptEmp(), dashLatestNotices()])
    stats.value = s.data
    notices.value = n.data

    chart = echarts.init(chartRef.value)
    chart.setOption({
      color: ['#1A56A8', '#38BDF8', '#0E2238', '#4D7EBE', '#8CAAD6', '#34D399', '#F59E0B'],
      tooltip: { trigger: 'item', formatter: '{b}：{c} 人（{d}%）' },
      legend: { bottom: 0, icon: 'circle', itemWidth: 8, textStyle: { color: '#5F6F81' } },
      series: [{
        type: 'pie',
        radius: ['42%', '68%'],
        center: ['50%', '44%'],
        itemStyle: { borderColor: '#fff', borderWidth: 2 },
        label: { show: false },
        emphasis: { scaleSize: 6 },
        data: d.data.map((x) => ({ name: x.name, value: x.value }))
      }]
    })
  } finally {
    loading.value = false
  }
}

function onResize() { chart?.resize() }

onMounted(() => {
  load()
  window.addEventListener('resize', onResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', onResize)
  chart?.dispose()
})
</script>

<template>
  <div v-loading="loading" class="dashboard">
    <div class="welcome">
      <div>
        <h2>管理员工作台</h2>
        <p class="eyebrow">SYSTEM OVERVIEW · {{ auth.user?.name }}</p>
      </div>
      <div class="topo-badge" aria-hidden="true">
        <svg viewBox="0 0 60 40" width="60" height="40">
          <line x1="30" y1="20" x2="8" y2="8" stroke="#2B4A6F" stroke-width="1.5"/>
          <line x1="30" y1="20" x2="8" y2="32" stroke="#2B4A6F" stroke-width="1.5"/>
          <line x1="30" y1="20" x2="52" y2="20" stroke="#2B4A6F" stroke-width="1.5"/>
          <circle cx="30" cy="20" r="4" fill="#1A56A8" stroke="#38BDF8"/>
          <circle cx="8" cy="8" r="3" fill="#16324F" stroke="#38BDF8"/>
          <circle cx="8" cy="32" r="3" fill="#16324F" stroke="#38BDF8"/>
          <circle cx="52" cy="20" r="3" fill="#16324F" stroke="#38BDF8"/>
        </svg>
      </div>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="16" class="stats-row">
      <el-col v-for="c in CARDS" :key="c.key" :xs="12" :sm="12" :md="6">
        <div class="stat-card" :class="c.tone">
          <el-icon class="stat-icon"><component :is="c.icon" /></el-icon>
          <div>
            <div class="stat-num oa-mono">{{ stats[c.key] }}</div>
            <div class="stat-label">{{ c.label }}</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="16">
      <!-- 部门人员占比 -->
      <el-col :md="14" :sm="24">
        <div class="oa-page-card panel">
          <h3 class="oa-page-title">部门人员占比（按一级部门归并，在职）</h3>
          <div ref="chartRef" class="chart"></div>
        </div>
      </el-col>

      <!-- 最新公告 -->
      <el-col :md="10" :sm="24">
        <div class="oa-page-card panel">
          <h3 class="oa-page-title">最新发布公告</h3>
          <ul class="notice-roll">
            <li v-for="n in notices" :key="n.noticeId">
              <span class="tag" :class="'t' + n.type">{{ TYPE_LABEL[n.type] }}</span>
              <span class="title" :title="n.title">{{ n.title }}</span>
              <span class="time oa-mono">{{ n.publishTime?.slice(5, 16) }}</span>
            </li>
            <li v-if="!notices.length" class="empty">暂无已发布公告</li>
          </ul>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.welcome {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 18px;
}

.welcome h2 {
  margin: 0;
  font-size: 20px;
  color: var(--oa-navy);
}

.eyebrow {
  margin: 4px 0 0;
  font-family: var(--oa-mono);
  font-size: 11px;
  letter-spacing: 0.2em;
  color: var(--oa-ink-light);
}

.stats-row { margin-bottom: 16px; }

.stat-card {
  background: #fff;
  border: 1px solid var(--oa-line);
  border-radius: 6px;
  padding: 18px 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  position: relative;
  overflow: hidden;
}

.stat-card::before {
  content: '';
  position: absolute;
  top: 0; left: 0; bottom: 0;
  width: 3px;
}

.stat-card.primary::before { background: var(--oa-primary); }
.stat-card.success::before { background: #34d399; }
.stat-card.navy::before { background: var(--oa-navy); }
.stat-card.signal::before { background: var(--oa-signal); }

.stat-icon {
  font-size: 26px;
  color: var(--oa-primary);
  background: var(--el-color-primary-light-9);
  padding: 12px;
  border-radius: 6px;
}

.stat-num {
  font-size: 26px;
  font-weight: 700;
  color: var(--oa-navy);
  line-height: 1.1;
}

.stat-label {
  font-size: 13px;
  color: var(--oa-ink-light);
  margin-top: 2px;
}

.panel { margin-bottom: 16px; }

.chart { height: 320px; }

.notice-roll {
  list-style: none;
  margin: 0;
  padding: 0;
}

.notice-roll li {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 11px 0;
  border-bottom: 1px dashed var(--oa-line);
}

.notice-roll li:last-child { border-bottom: none; }

.notice-roll .tag {
  flex-shrink: 0;
  font-size: 11px;
  padding: 1px 7px;
  border-radius: 3px;
  background: var(--el-color-primary-light-9);
  color: var(--oa-primary);
}

.notice-roll .tag.t3 { background: #fef3c7; color: #b45309; }
.notice-roll .tag.t2 { background: #d1fae5; color: #047857; }

.notice-roll .title {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 13.5px;
}

.notice-roll .time {
  flex-shrink: 0;
  font-size: 12px;
  color: var(--oa-ink-light);
}

.notice-roll .empty {
  color: var(--oa-ink-light);
  font-size: 13px;
  justify-content: center;
}
</style>
