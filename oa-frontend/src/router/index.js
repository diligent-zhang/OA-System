import { createRouter, createWebHistory } from 'vue-router'
import { auth } from '@/stores/auth'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录', public: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: { title: '注册', public: true }
  },
  {
    path: '/',
    component: () => import('@/layout/AdminLayout.vue'),
    redirect: () => (auth.isAdmin ? '/admin/dashboard' : '/employee/workbench'),
    children: [
      /* ===== 管理员端 ===== */
      {
        path: 'admin/dashboard',
        name: 'Dashboard',
        component: () => import('@/views/admin/Dashboard.vue'),
        meta: { title: '系统仪表盘', icon: 'Odometer', role: 'ADMIN', breadcrumb: ['首页', '系统仪表盘'] }
      },
      {
        path: 'admin/employees',
        name: 'EmployeeList',
        component: () => import('@/views/admin/EmployeeList.vue'),
        meta: { title: '员工信息管理', icon: 'UserFilled', role: 'ADMIN', breadcrumb: ['组织管理', '员工信息管理'] }
      },
      {
        path: 'admin/depts',
        name: 'DeptManage',
        component: () => import('@/views/admin/DeptManage.vue'),
        meta: { title: '部门管理', icon: 'OfficeBuilding', role: 'ADMIN', breadcrumb: ['组织管理', '部门管理'] }
      },
      {
        path: 'admin/notices',
        name: 'NoticeManage',
        component: () => import('@/views/admin/NoticeManage.vue'),
        meta: { title: '公告管理', icon: 'ChatDotSquare', role: 'ADMIN', breadcrumb: ['内容管理', '公告管理'] }
      },
      /* ===== 员工端 ===== */
      {
        path: 'employee/workbench',
        name: 'Workbench',
        component: () => import('@/views/employee/Workbench.vue'),
        meta: { title: '个人工作台', icon: 'HomeFilled', role: 'EMPLOYEE', breadcrumb: ['首页', '个人工作台'] }
      },
      {
        path: 'employee/notices',
        name: 'NoticeCenter',
        component: () => import('@/views/employee/NoticeCenter.vue'),
        meta: { title: '公告中心', icon: 'Bell', role: 'EMPLOYEE', breadcrumb: ['办公服务', '公告中心'] }
      },
      /* ===== 两端共用 ===== */
      {
        path: 'profile',
        name: 'MyProfile',
        component: () => import('@/views/profile/MyProfile.vue'),
        meta: { title: '个人中心', icon: 'User', breadcrumb: ['个人中心'] }
      },
      {
        path: 'password',
        name: 'ChangePassword',
        component: () => import('@/views/profile/ChangePassword.vue'),
        meta: { title: '修改密码', icon: 'Lock', breadcrumb: ['个人中心', '修改密码'] }
      }
    ]
  },
  { path: '/:pathMatch(.*)*', redirect: '/' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

/* 全局前置守卫：登录状态 + 角色权限 + 懒加载 */
router.beforeEach((to) => {
  document.title = to.meta.title ? `${to.meta.title} · OA 办公管理系统` : 'OA 办公管理系统'

  if (to.meta.public) {
    // 已登录禁止重复访问登录/注册页
    if (auth.isLogin && (to.path === '/login' || to.path === '/register')) {
      return auth.isAdmin ? '/admin/dashboard' : '/employee/workbench'
    }
    return true
  }

  if (!auth.isLogin) {
    return { path: '/login', query: to.fullPath !== '/' ? { redirect: to.fullPath } : {} }
  }

  if (to.meta.role && to.meta.role !== auth.role) {
    return auth.isAdmin ? '/admin/dashboard' : '/employee/workbench'
  }
  return true
})

export default router
