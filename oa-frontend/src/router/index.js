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
      /* ===== 员工端（管理员亦可见常用办公） ===== */
      {
        path: 'employee/workbench',
        name: 'Workbench',
        component: () => import('@/views/employee/Workbench.vue'),
        meta: { title: '个人工作台', icon: 'HomeFilled', breadcrumb: ['首页', '个人工作台'] }
      },
      {
        path: 'employee/notices',
        name: 'NoticeCenter',
        component: () => import('@/views/employee/NoticeCenter.vue'),
        meta: { title: '公告中心', icon: 'Bell', breadcrumb: ['办公服务', '公告中心'] }
      },
      {
        path: 'messages',
        name: 'MessageCenter',
        component: () => import('@/views/message/MessageCenter.vue'),
        meta: { title: '消息中心', icon: 'Message', breadcrumb: ['办公服务', '消息中心'] }
      },
      {
        path: 'contacts',
        name: 'Contacts',
        component: () => import('@/views/contacts/Contacts.vue'),
        meta: { title: '企业通讯录', icon: 'PhoneFilled', breadcrumb: ['组织管理', '企业通讯录'] }
      },
      /* ===== 审批 ===== */
      {
        path: 'flow/start',
        name: 'FlowStart',
        component: () => import('@/views/flow/FlowStart.vue'),
        meta: { title: '发起审批', icon: 'DocumentAdd', breadcrumb: ['流程审批', '发起审批'] }
      },
      {
        path: 'flow/start/:defId',
        name: 'FlowStartForm',
        component: () => import('@/views/flow/FlowStart.vue'),
        meta: { title: '填写申请', breadcrumb: ['流程审批', '发起审批'] }
      },
      {
        path: 'flow/mine',
        name: 'FlowMine',
        component: () => import('@/views/flow/FlowMine.vue'),
        meta: { title: '我发起的', icon: 'EditPen', breadcrumb: ['流程审批', '我发起的'] }
      },
      {
        path: 'flow/todo',
        name: 'FlowTodo',
        component: () => import('@/views/flow/FlowTodo.vue'),
        meta: { title: '待我审批', icon: 'AlarmClock', breadcrumb: ['流程审批', '待我审批'] }
      },
      {
        path: 'flow/done',
        name: 'FlowDone',
        component: () => import('@/views/flow/FlowDone.vue'),
        meta: { title: '已审批', icon: 'Finished', breadcrumb: ['流程审批', '已审批'] }
      },
      {
        path: 'flow/detail/:id',
        name: 'FlowDetail',
        component: () => import('@/views/flow/FlowDetail.vue'),
        meta: { title: '审批详情', breadcrumb: ['流程审批', '审批详情'] }
      },
      {
        path: 'admin/flow/forms',
        name: 'FlowFormDesign',
        component: () => import('@/views/flow/FormDesign.vue'),
        meta: { title: '表单设计', icon: 'Grid', role: 'ADMIN', breadcrumb: ['审批配置', '表单设计'] }
      },
      {
        path: 'admin/flow/defs',
        name: 'FlowDefConfig',
        component: () => import('@/views/flow/FlowConfig.vue'),
        meta: { title: '流程配置', icon: 'Share', role: 'ADMIN', breadcrumb: ['审批配置', '流程配置'] }
      },
      {
        path: 'admin/flow/archive',
        name: 'FlowArchive',
        component: () => import('@/views/flow/FlowArchive.vue'),
        meta: { title: '审批归档', icon: 'Files', role: 'ADMIN', breadcrumb: ['流程审批', '审批归档'] }
      },
      /* ===== 考勤 ===== */
      {
        path: 'att/punch',
        name: 'PunchCard',
        component: () => import('@/views/attendance/PunchCard.vue'),
        meta: { title: '考勤打卡', icon: 'Location', breadcrumb: ['考勤管理', '考勤打卡'] }
      },
      {
        path: 'att/mine',
        name: 'MyAttendance',
        component: () => import('@/views/attendance/MyAttendance.vue'),
        meta: { title: '我的考勤', icon: 'Calendar', breadcrumb: ['考勤管理', '我的考勤'] }
      },
      {
        path: 'admin/att/rules',
        name: 'AttRules',
        component: () => import('@/views/attendance/AttRules.vue'),
        meta: { title: '考勤规则', icon: 'Timer', role: 'ADMIN', breadcrumb: ['考勤管理', '考勤规则'] }
      },
      {
        path: 'admin/att/report',
        name: 'AttReport',
        component: () => import('@/views/attendance/AttReport.vue'),
        meta: { title: '考勤报表', icon: 'DataAnalysis', breadcrumb: ['考勤管理', '考勤报表'] }
      },
      /* ===== 文档中心 ===== */
      {
        path: 'doc',
        name: 'DocCenter',
        component: () => import('@/views/doc/DocCenter.vue'),
        meta: { title: '文档中心', icon: 'FolderOpened', breadcrumb: ['办公室务', '文档中心'] }
      },
      /* ===== 系统管理（ADMIN） ===== */
      {
        path: 'system/op-logs',
        name: 'OpLogList',
        component: () => import('@/views/system/OpLog.vue'),
        meta: { title: '操作日志', icon: 'Document', role: 'ADMIN', breadcrumb: ['系统管理', '操作日志'] }
      },
      {
        path: 'system/config',
        name: 'SysConfig',
        component: () => import('@/views/system/SysConfig.vue'),
        meta: { title: '系统设置', icon: 'Setting', role: 'ADMIN', breadcrumb: ['系统管理', '系统设置'] }
      },
      {
        path: 'system/roles',
        name: 'RoleManage',
        component: () => import('@/views/system/RoleManage.vue'),
        meta: { title: '角色权限', icon: 'Lock', role: 'ADMIN', breadcrumb: ['系统管理', '角色权限'] }
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
      },
      {
        path: 'profile/op-logs',
        name: 'MyOperations',
        component: () => import('@/views/profile/MyOperations.vue'),
        meta: { title: '我的操作记录', icon: 'List', breadcrumb: ['个人中心', '我的操作记录'] }
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