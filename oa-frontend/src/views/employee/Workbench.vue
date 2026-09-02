<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { myProfile, noticePublishedPage } from '@/api'
import { auth } from '@/stores/auth'

const router = useRouter()
const profile = ref({})
const notices = ref([])
const loading = ref(true)

const TYPE_LABEL = { 1: '通知', 2: '公告', 3: '制度' }

onMounted(async () => {
  try {
    const [p, n] = await Promise.all([myProfile(), noticePublishedPage({ pageNum: 1, pageSize: 5 })])
    profile.value = p.data
    notices.value = n.data.list
  } finally {
    loading.value = false
  }
})

const shortcuts = [
  { title: '公告中心', desc: '浏览公司通知与制度', icon: 'Bell', path: '/employee/notices' },
  { title: '我的信息', desc: '查看与维护个人资料', icon: 'User', path: '/profile' },
  { title: '修改密码', desc: '定期更换更安全', icon: 'Lock', path: '/password' }
]
</script>

<template>
  <div v-loading="loading">
    <!-- 欢迎横幅 -->
    <div class="hero">
      <div>
        <h2>{{ profile.name || auth.user?.name }}，欢迎回来</h2>
        <p class="eyebrow">EMPLOYEE WORKBENCH · {{ auth.user?.username }}</p>
        <div class="hero-meta">
          <span>工号 <b class="oa-mono">{{ profile.empNo || '—' }}</b></span>
          <el-divider direction="vertical" />
          <span>{{ profile.deptName || '未分配部门' }}</span>
          <el-divider direction="vertical" />
          <span>入职 {{ profile.hireDate || '—' }}</span>
        </div>
      </div>
      <svg class="hero-topo" viewBox="0 0 60 40" width="72" height="48" aria-hidden="true">
        <line x1="30" y1="20" x2="8" y2="8" stroke="#4D7EBE" stroke-width="1.5"/>
        <line x1="30" y1="20" x2="8" y2="32" stroke="#4D7EBE" stroke-width="1.5"/>
        <line x1="30" y1="20" x2="52" y2="20" stroke="#4D7EBE" stroke-width="1.5"/>
        <circle cx="30" cy="20" r="4" fill="#38BDF8"/>
        <circle cx="8" cy="8" r="3" fill="#fff"/>
        <circle cx="8" cy="32" r="3" fill="#fff"/>
        <circle cx="52" cy="20" r="3" fill="#fff"/>
      </svg>
    </div>

    <el-row :gutter="16">
      <!-- 快捷入口 -->
      <el-col :md="14" :sm="24">
        <h3 class="oa-page-title">常用功能</h3>
        <el-row :gutter="12">
          <el-col v-for="s in shortcuts" :key="s.path" :span="8">
            <div class="shortcut" role="button" tabindex="0" @click="router.push(s.path)" @keyup.enter="router.push(s.path)">
              <el-icon><component :is="s.icon" /></el-icon>
              <div class="s-title">{{ s.title }}</div>
              <div class="s-desc">{{ s.desc }}</div>
            </div>
          </el-col>
        </el-row>

        <h3 class="oa-page-title" style="margin-top: 22px">我的档案</h3>
        <div class="oa-page-card">
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="姓名">{{ profile.name || '—' }}</el-descriptions-item>
            <el-descriptions-item label="性别">{{ profile.gender === 0 ? '女' : '男' }}</el-descriptions-item>
            <el-descriptions-item label="手机号"><span class="oa-mono">{{ profile.phone || '—' }}</span></el-descriptions-item>
            <el-descriptions-item label="邮箱">{{ profile.email || '—' }}</el-descriptions-item>
            <el-descriptions-item label="所属部门">{{ profile.deptName || '—' }}</el-descriptions-item>
            <el-descriptions-item label="入职日期">{{ profile.hireDate || '—' }}</el-descriptions-item>
          </el-descriptions>
        </div>
      </el-col>

      <!-- 最新公告 -->
      <el-col :md="10" :sm="24">
        <h3 class="oa-page-title">最新公告</h3>
        <div class="oa-page-card">
          <ul class="notice-list">
            <li v-for="n in notices" :key="n.noticeId" @click="router.push({ path: '/employee/notices', query: { id: n.noticeId } })">
              <span class="tag" :class="'t' + n.type">{{ TYPE_LABEL[n.type] }}</span>
              <span class="t">{{ n.title }}</span>
            </li>
            <li v-if="!notices.length" class="empty">暂无公告</li>
          </ul>
          <el-button link type="primary" class="more" @click="router.push('/employee/notices')">进入公告中心 →</el-button>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.hero {
  background: linear-gradient(105deg, var(--oa-navy) 0%, #16324f 62%, #1a4a7e 100%);
  color: #fff;
  border-radius: 6px;
  padding: 22px 26px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.hero h2 { margin: 0; font-size: 19px; font-weight: 600; }

.eyebrow {
  margin: 5px 0 0;
  font-family: var(--oa-mono);
  font-size: 11px;
  letter-spacing: 0.2em;
  color: #7d95af;
}

.hero-meta {
  margin-top: 12px;
  font-size: 13px;
  color: #c6d4e2;
}

.hero-meta b { color: var(--oa-signal); font-weight: 600; }

.shortcut {
  background: #fff;
  border: 1px solid var(--oa-line);
  border-radius: 6px;
  padding: 16px;
  cursor: pointer;
  transition: border-color 0.15s ease, transform 0.15s ease;
  margin-bottom: 12px;
}

.shortcut:hover, .shortcut:focus-visible {
  border-color: var(--oa-primary);
  transform: translateY(-2px);
}

.shortcut .el-icon { font-size: 20px; color: var(--oa-primary); }
.s-title { font-weight: 600; margin-top: 8px; color: var(--oa-navy); }
.s-desc { font-size: 12px; color: var(--oa-ink-light); margin-top: 2px; }

.notice-list {
  list-style: none;
  margin: 0;
  padding: 0;
}

.notice-list li {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 11px 0;
  border-bottom: 1px dashed var(--oa-line);
  cursor: pointer;
}

.notice-list li:hover .t { color: var(--oa-primary); }

.notice-list .tag {
  flex-shrink: 0;
  font-size: 11px;
  padding: 1px 7px;
  border-radius: 3px;
  background: var(--el-color-primary-light-9);
  color: var(--oa-primary);
}
.notice-list .tag.t2 { background: #d1fae5; color: #047857; }
.notice-list .tag.t3 { background: #fef3c7; color: #b45309; }

.notice-list .t {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 13.5px;
}

.notice-list .empty { color: var(--oa-ink-light); font-size: 13px; cursor: default; }

.more { margin-top: 10px; }

@media (max-width: 768px) {
  .hero-topo { display: none; }
}
</style>
