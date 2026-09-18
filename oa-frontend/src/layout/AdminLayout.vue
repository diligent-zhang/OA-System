<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { auth } from '@/stores/auth'
import { logout, messageUnreadCount } from '@/api'

const route = useRoute()
const router = useRouter()
const collapsed = ref(false)
const unread = ref(0)
let timer = null

/* 按角色过滤菜单 */
const menuGroups = computed(() => {
  const isAdmin = auth.isAdmin
  const isDeptAdmin = auth.role === 'DEPT_ADMIN'
  const groups = []

  if (isAdmin) {
    groups.push({
      title: '系统概览',
      items: [{ path: '/admin/dashboard', title: '系统仪表盘', icon: 'Odometer' }]
    })
    groups.push({
      title: '组织管理',
      items: [
        { path: '/admin/employees', title: '员工信息管理', icon: 'UserFilled' },
        { path: '/admin/depts', title: '部门管理', icon: 'OfficeBuilding' },
        { path: '/contacts', title: '企业通讯录', icon: 'PhoneFilled' }
      ]
    })
    groups.push({
      title: '流程审批',
      items: [
        { path: '/flow/start', title: '发起审批', icon: 'DocumentAdd' },
        { path: '/flow/mine', title: '我发起的', icon: 'EditPen' },
        { path: '/flow/todo', title: '待我审批', icon: 'AlarmClock' },
        { path: '/flow/done', title: '已审批', icon: 'Finished' },
        { path: '/admin/flow/archive', title: '审批归档', icon: 'Files' },
        { path: '/admin/flow/forms', title: '表单设计', icon: 'Grid' },
        { path: '/admin/flow/defs', title: '流程配置', icon: 'Share' }
      ]
    })
    groups.push({
      title: '考勤管理',
      items: [
        { path: '/att/punch', title: '考勤打卡', icon: 'Location' },
        { path: '/att/mine', title: '我的考勤', icon: 'Calendar' },
        { path: '/admin/att/rules', title: '考勤规则', icon: 'Timer' },
        { path: '/admin/att/report', title: '考勤报表', icon: 'DataAnalysis' }
      ]
    })
    groups.push({
      title: '文档中心',
      items: [{ path: '/doc', title: '文档中心', icon: 'FolderOpened' }]
    })
    groups.push({
      title: '内容管理',
      items: [
        { path: '/admin/notices', title: '公告管理', icon: 'ChatDotSquare' },
        { path: '/employee/notices', title: '公告中心', icon: 'Bell' },
        { path: '/messages', title: '消息中心', icon: 'Message' }
      ]
    })
    groups.push({
      title: '系统管理',
      items: [
        { path: '/system/roles', title: '角色权限', icon: 'Lock' },
        { path: '/system/op-logs', title: '操作日志', icon: 'Document' },
        { path: '/system/config', title: '系统设置', icon: 'Setting' }
      ]
    })
  } else if (isDeptAdmin) {
    groups.push({
      title: '我的办公',
      items: [
        { path: '/employee/workbench', title: '个人工作台', icon: 'HomeFilled' },
        { path: '/flow/start', title: '发起审批', icon: 'DocumentAdd' },
        { path: '/flow/mine', title: '我发起的', icon: 'EditPen' },
        { path: '/flow/todo', title: '待我审批', icon: 'AlarmClock' },
        { path: '/flow/done', title: '已审批', icon: 'Finished' }
      ]
    })
    groups.push({
      title: '部门管理',
      items: [
        { path: '/contacts', title: '企业通讯录', icon: 'PhoneFilled' },
        { path: '/admin/att/report', title: '本部门考勤报表', icon: 'DataAnalysis' }
      ]
    })
  } else {
    groups.push({
      title: '我的办公',
      items: [
        { path: '/employee/workbench', title: '个人工作台', icon: 'HomeFilled' },
        { path: '/flow/start', title: '发起审批', icon: 'DocumentAdd' },
        { path: '/flow/mine', title: '我发起的', icon: 'EditPen' },
        { path: '/flow/todo', title: '待我审批', icon: 'AlarmClock' },
        { path: '/flow/done', title: '已审批', icon: 'Finished' }
      ]
    })
  }

  if (!isAdmin) {
    groups.push({
      title: '考勤管理',
      items: [
        { path: '/att/punch', title: '考勤打卡', icon: 'Location' },
        { path: '/att/mine', title: '我的考勤', icon: 'Calendar' }
      ]
    })
  }

  groups.push({
    title: '办公服务',
    items: [
      { path: '/doc', title: '文档中心', icon: 'FolderOpened' },
      { path: '/employee/notices', title: '公告中心', icon: 'Bell' },
      { path: '/messages', title: '消息中心', icon: 'Message' }
    ]
  })

  groups.push({
    title: '个人中心',
    items: [
      { path: '/profile', title: '个人信息', icon: 'User' },
      { path: '/profile/op-logs', title: '我的操作记录', icon: 'List' },
      { path: '/password', title: '修改密码', icon: 'Lock' }
    ]
  })
  return groups
})

const breadcrumb = computed(() => route.meta.breadcrumb || ['首页'])
const activeMenu = computed(() => {
  if (route.path.startsWith('/flow/start')) return '/flow/start'
  return route.path
})

async function refreshUnread() {
  try {
    const res = await messageUnreadCount()
    unread.value = res.data || 0
  } catch (_) {
    /* 静默失败 */
  }
}

onMounted(() => {
  refreshUnread()
  if (!timer) {
    timer = setInterval(refreshUnread, 60000)
  }
})

onUnmounted(() => {
  if (timer) {
    clearInterval(timer)
    timer = null
  }
})

async function handleCommand(cmd) {
  if (cmd === 'logout') {
    await ElMessageBox.confirm('退出后将清除本地登录状态，确认退出登录？', '退出登录', {
      confirmButtonText: '退出',
      cancelButtonText: '取消',
      type: 'warning'
    })
    logout().catch(() => {})
    auth.clear()
    ElMessage.success('已退出登录')
    router.push('/login')
  } else if (cmd === 'profile') {
    router.push('/profile')
  } else if (cmd === 'password') {
    router.push('/password')
  }
}
</script>

<template>
  <div class="layout">
    <aside class="sidebar oa-sidebar" :class="{ collapsed }">
      <div class="side-brand">
        <svg viewBox="0 0 40 40" width="26" height="26" aria-hidden="true">
          <rect x="3" y="3" width="14" height="14" rx="2" fill="none" stroke="#38BDF8" stroke-width="2.5"/>
          <rect x="23" y="3" width="14" height="14" rx="2" fill="none" stroke="#9FB3C8" stroke-width="2.5"/>
          <rect x="3" y="23" width="14" height="14" rx="2" fill="none" stroke="#9FB3C8" stroke-width="2.5"/>
          <rect x="23" y="23" width="14" height="14" rx="2" fill="#1A56A8" stroke="#38BDF8" stroke-width="2.5"/>
        </svg>
        <span v-show="!collapsed" class="side-title">OA 办公管理系统</span>
      </div>

      <el-scrollbar class="side-scroll">
        <el-menu :default-active="activeMenu" :collapse="collapsed" :collapse-transition="false" router>
          <template v-for="group in menuGroups" :key="group.title">
            <p v-show="!collapsed" class="group-label">{{ group.title }}</p>
            <el-menu-item v-for="item in group.items" :key="item.path" :index="item.path">
              <el-icon><component :is="item.icon" /></el-icon>
              <template #title>{{ item.title }}</template>
            </el-menu-item>
          </template>
        </el-menu>
      </el-scrollbar>
    </aside>

    <div class="main-wrap">
      <header class="topbar">
        <el-icon class="collapse-btn" @click="collapsed = !collapsed" role="button" tabindex="0" aria-label="折叠菜单">
          <component :is="collapsed ? 'Expand' : 'Fold'" />
        </el-icon>
        <el-breadcrumb separator="/">
          <el-breadcrumb-item v-for="(b, i) in breadcrumb" :key="i">{{ b }}</el-breadcrumb-item>
        </el-breadcrumb>

        <div class="topbar-right">
          <el-tag :type="auth.role === 'ADMIN' ? 'primary' : auth.role === 'DEPT_ADMIN' ? 'warning' : 'success'" effect="plain" size="small">
            {{ auth.role === 'ADMIN' ? '超管' : auth.role === 'DEPT_ADMIN' ? '部门管理员' : '员工' }}
          </el-tag>
          <el-badge :value="unread" :max="99" :hidden="unread === 0" class="msg-badge">
            <el-icon class="top-icon" role="button" tabindex="0" @click="router.push('/messages')">
              <Message />
            </el-icon>
          </el-badge>
          <el-dropdown @command="handleCommand">
            <div class="user-chip">
              <el-avatar :size="30" class="avatar">
                <img v-if="auth.user?.avatarUrl" :src="`http://localhost:8080/api/file/avatar/${auth.user?.userId}`" alt="avatar" />
                <span v-else>{{ (auth.user?.name || 'U').charAt(0) }}</span>
              </el-avatar>
              <span class="uname">{{ auth.user?.name || auth.user?.username }}</span>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile"><el-icon><User /></el-icon>个人中心</el-dropdown-item>
                <el-dropdown-item command="password"><el-icon><Lock /></el-icon>修改密码</el-dropdown-item>
                <el-dropdown-item command="logout" divided><el-icon><SwitchButton /></el-icon>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <main class="content">
        <router-view v-slot="{ Component }">
          <transition name="oa-fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </main>
    </div>
  </div>
</template>

<style scoped>
.layout { height: 100%; display: flex; }
.sidebar { width: 220px; flex-shrink: 0; display: flex; flex-direction: column; transition: width 0.2s ease; }
.sidebar.collapsed { width: 64px; }
.side-brand { height: 56px; display: flex; align-items: center; gap: 10px; padding: 0 16px; border-bottom: 1px solid rgba(255,255,255,0.06); }
.side-title { color: #fff; font-size: 17px; font-weight: 600; letter-spacing: 0.12em; white-space: nowrap; }
.side-scroll { flex: 1; }
.group-label { margin: 18px 20px 6px; font-size: 11px; font-family: var(--oa-mono); letter-spacing: 0.18em; color: #5f7893; white-space: nowrap; }
.main-wrap { flex: 1; min-width: 0; display: flex; flex-direction: column; }
.topbar { height: 56px; background: #fff; border-bottom: 1px solid var(--oa-line); display: flex; align-items: center; gap: 16px; padding: 0 20px; }
.collapse-btn { font-size: 18px; cursor: pointer; color: var(--oa-ink-light); }
.topbar-right { margin-left: auto; display: flex; align-items: center; gap: 14px; }
.top-icon { font-size: 19px; cursor: pointer; color: var(--oa-ink-light); }
.top-icon:hover { color: var(--oa-primary); }
.user-chip { display: flex; align-items: center; gap: 8px; cursor: pointer; padding: 4px 8px; border-radius: 4px; }
.user-chip:hover { background: var(--oa-surface); }
.avatar { background: var(--oa-primary); }
.avatar img { width: 100%; height: 100%; object-fit: cover; }
.uname { font-size: 14px; color: var(--oa-ink); }
.content { flex: 1; padding: 20px; overflow: auto; }
</style>