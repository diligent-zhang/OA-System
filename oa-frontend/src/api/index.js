import { get, post, put, del } from './request'

/* ===== 认证 ===== */
export const login = (data) => post('/auth/login', data)
export const register = (data) => post('/auth/register', data)
export const usernameExists = (username) => get('/auth/username-exists', { username })
export const logout = () => post('/auth/logout')

/* ===== 员工管理（ADMIN） ===== */
export const employeePage = (params) => get('/admin/employees', params)
export const employeeAdd = (data) => post('/admin/employees', data)
export const employeeUpdate = (id, data) => put(`/admin/employees/${id}`, data)
export const employeeDelete = (id) => del(`/admin/employees/${id}`)
export const employeeDeleteBatch = (ids) => del('/admin/employees/batch', ids)
export const employeeResetPwd = (id) => put(`/admin/employees/${id}/password/reset`)
export const employeeAccountStatus = (id, status) => put(`/admin/employees/${id}/status`, null, { status })

/* ===== 部门 ===== */
export const deptPage = (params) => get('/admin/depts', params)
export const deptTree = () => get('/depts/tree')
export const deptAdd = (data) => post('/admin/depts', data)
export const deptUpdate = (id, data) => put(`/admin/depts/${id}`, data)
export const deptDelete = (id) => del(`/admin/depts/${id}`)

/* ===== 公告 ===== */
export const noticeAdminPage = (params) => get('/admin/notices', params)
export const noticeAdd = (data) => post('/admin/notices', data)
export const noticeUpdate = (id, data) => put(`/admin/notices/${id}`, data)
export const noticeDelete = (id) => del(`/admin/notices/${id}`)
export const noticeToggle = (id) => put(`/admin/notices/${id}/toggle`)
export const noticePublishedPage = (params) => get('/notices', params)
export const noticeDetail = (id) => get(`/notices/${id}`)

/* ===== 个人中心 ===== */
export const myProfile = () => get('/user/profile')
export const updateProfile = (data) => put('/user/profile', data)
export const updatePassword = (data) => put('/user/password', data)

/* ===== 仪表盘 ===== */
export const dashStats = () => get('/admin/dashboard/stats')
export const dashDeptEmp = () => get('/admin/dashboard/dept-emp')
export const dashLatestNotices = () => get('/admin/dashboard/latest-notices')
