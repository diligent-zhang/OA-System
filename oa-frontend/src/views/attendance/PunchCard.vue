<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { attActiveRule, attPunch } from '@/api'

const rule = ref(null)
const loading = ref(false)
const punching = ref(false)
const today = ref('')
const location = ref({ latitude: null, longitude: null, address: '未定位' })

function now() {
  const d = new Date()
  today.value = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
  return `${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

async function load() {
  loading.value = true
  try {
    const res = await attActiveRule()
    rule.value = res.data
    today.value = new Date().toISOString().slice(0, 10)
  } finally {
    loading.value = false
  }
}

function useLocation() {
  if (!navigator.geolocation) {
    ElMessage.warning('浏览器不支持定位，可手工填写经纬度')
    return
  }
  navigator.geolocation.getCurrentPosition(
    (pos) => {
      location.value = {
        latitude: pos.coords.latitude,
        longitude: pos.coords.longitude,
        address: `${pos.coords.latitude.toFixed(5)}, ${pos.coords.longitude.toFixed(5)}`
      }
      ElMessage.success('定位成功')
    },
    () => ElMessage.warning('定位失败，可手工填写经纬度')
  )
}

async function punch(type) {
  punching.value = true
  try {
    const res = await attPunch({
      type,
      latitude: location.value.latitude,
      longitude: location.value.longitude,
      address: location.value.address
    })
    ElMessage.success(`${type === 'IN' ? '上班' : '下班'}打卡成功`)
  } finally {
    punching.value = false
  }
}

onMounted(() => {
  load()
  now()
})
</script>

<template>
  <div v-loading="loading" class="oa-page">
    <h3 class="oa-page-title">考勤打卡</h3>
    <div class="oa-page-card punch-wrap">
      <div class="time-big oa-mono">{{ now() }}</div>
      <div class="date-line">{{ today }}</div>

      <template v-if="rule">
        <el-descriptions :column="2" border size="small" style="margin: 18px 0">
          <el-descriptions-item label="上班时间"><span class="oa-mono">{{ rule.workStart }}</span></el-descriptions-item>
          <el-descriptions-item label="下班时间"><span class="oa-mono">{{ rule.workEnd }}</span></el-descriptions-item>
          <el-descriptions-item label="定位校验">{{ rule.locationCheck === 1 ? '开启' : '关闭' }}</el-descriptions-item>
          <el-descriptions-item label="休息日">{{ rule.restDays || '周末' }}</el-descriptions-item>
        </el-descriptions>
      </template>
      <el-empty v-else description="管理员尚未配置考勤规则" />

      <div class="loc-row">
        <span class="loc-text">{{ location.address }}</span>
        <el-button size="small" @click="useLocation">获取定位</el-button>
      </div>

      <div class="punch-btns">
        <el-button type="primary" :disabled="!rule" :loading="punching" @click="punch('IN')">上班打卡</el-button>
        <el-button type="success" :disabled="!rule" :loading="punching" @click="punch('OUT')">下班打卡</el-button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.punch-wrap { text-align: center; padding: 40px 20px; }
.time-big { font-size: 54px; color: var(--oa-navy); letter-spacing: 2px; }
.date-line { color: var(--oa-ink-light); font-size: 14px; }
.loc-row { display: flex; align-items: center; justify-content: center; gap: 10px; margin: 18px 0 24px; }
.loc-text { font-size: 13px; color: var(--oa-ink-light); }
.punch-btns { display: flex; gap: 20px; justify-content: center; }
.punch-btns .el-button { min-width: 140px; height: 44px; font-size: 16px; }
</style>