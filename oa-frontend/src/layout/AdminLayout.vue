<script setup>
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { auth } from '@/stores/auth'
import { logout } from '@/api'

const route = useRoute()
const router = useRouter()
const collapsed = ref(false)

/* 按角色过滤菜单：管理员看全部管理模块，员工仅个人相关 */
const menuGroups = computed(() => {
  const isAdmin = auth.isAdmin
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
        { path: '/admin/depts', title: '部门管理', icon: 'OfficeBuilding' }
      ]
    })
    groups.push({
      title: '内容管理',
      items: [{ path: '/admin/notices', title: '公告管理', icon: 'ChatDotSquare' }]
    })
  } else {
    groups.push({
      title: '我的办公',
      items: [
        { path: '/employee/workbench', title: '个人工作台', icon: 'HomeFilled' },
        { path: '/employee/notices', title: '公告中心', icon: 'Bell' }
      ]
    })
  }
  groups.push({
    title: '个人中心',
    items: [
      { path: '/profile', title: '个人信息', icon: 'User' },
      { path: '/password', title: '修改密码', icon: 'Lock' }
    ]
  })
  return groups
})

const breadcrumb = computed(() => route.meta.breadcrumb || ['首页'])
const activeMenu = computed(() => route.path)

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
    <!-- 侧栏 -->
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

    <!-- 右侧主体 -->
    <div class="main-wrap">
      <header class="topbar">
        <el-icon class="collapse-btn" @click="collapsed = !collapsed" role="button" tabindex="0" aria-label="折叠菜单">
          <component :is="collapsed ? 'Expand' : 'Fold'" />
        </el-icon>
        <el-breadcrumb separator="/">
          <el-breadcrumb-item v-for="(b, i) in breadcrumb" :key="i">{{ b }}</el-breadcrumb-item>
        </el-breadcrumb>

        <div class="topbar-right">
          <el-tag :type="auth.isAdmin ? 'primary' : 'success'" effect="plain" size="small">
            {{ auth.isAdmin ? '管理员' : '员工' }}
          </el-tag>
          <el-dropdown @command="handleCommand">
            <div class="user-chip">
              <el-avatar :size="30" class="avatar">{{ (auth.user?.name || 'U').charAt(0) }}</el-avatar>
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
.layout {
  height: 100%;
  display: flex;
}

.sidebar {
  width: 220px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  transition: width 0.2s ease;
}

.sidebar.collapsed {
  width: 64px;
}

.side-brand {
  height: 56px;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
}

.side-title {
  color: #fff;
  font-size: 17px;
  font-weight: 600;
  letter-spacing: 0.12em;
  white-space: nowrap;
}

.side-scroll {
  flex: 1;
}

.group-label {
  margin: 18px 20px 6px;
  font-size: 11px;
  font-family: var(--oa-mono);
  letter-spacing: 0.18em;
  color: #5f7893;
  white-space: nowrap;
}

.main-wrap {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.topbar {
  height: 56px;
  background: #fff;
  border-bottom: 1px solid var(--oa-line);
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 0 20px;
}

.collapse-btn {
  font-size: 18px;
  cursor: pointer;
  color: var(--oa-ink-light);
}

.topbar-right {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 14px;
}

.user-chip {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
}

.user-chip:hover {
  background: var(--oa-surface);
}

.avatar {
  background: var(--oa-primary);
}

.uname {
  font-size: 14px;
  color: var(--oa-ink);
}

.content {
  flex: 1;
  padding: 20px;
  overflow: auto;
}
</style>
