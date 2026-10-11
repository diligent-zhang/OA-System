import { get, post, put, del } from './request'
import service from './request'

/* 即时通讯接口 */
export * from './chat'

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
export const noticeDeleteAttachment = (noticeId, attachmentId) => del(`/admin/notices/${noticeId}/attachments/${attachmentId}`)
export const noticeReadStats = (id) => get(`/admin/notices/${id}/read-stats`)
export const noticePublishedPage = (params) => get('/notices', params)
export const noticeDetail = (id) => get(`/notices/${id}`)

/* ===== 个人中心 ===== */
export const myProfile = () => get('/user/profile')
export const updateProfile = (data) => put('/user/profile', data)
export const updatePassword = (data) => put('/user/password', data)
export const avatarUpload = (formData) => service.post('/user/avatar', formData)
export const myOpLogs = (params) => get('/user/op-logs', params)

/* ===== 仪表盘 ===== */
export const dashStats = () => get('/admin/dashboard/stats')
export const dashDeptEmp = () => get('/admin/dashboard/dept-emp')
export const dashLatestNotices = () => get('/admin/dashboard/latest-notices')

/* ===== 门户与消息 ===== */
export const portalSummary = () => get('/portal/summary')
export const messagePage = (params) => get('/messages', params)
export const messageRead = (id) => put(`/messages/${id}/read`)
export const messageReadAll = () => put('/messages/read-all')
export const messageUnreadCount = () => get('/messages/unread-count')

/* ===== 附件 ===== */
export const fileUpload = (formData, bizType, bizId) =>
  service.post('/file/upload', formData, { params: { bizType, bizId } })

/* ===== 流量审批：员工端 ===== */
export const flowEnabledDefs = () => get('/flow/defs')
export const flowDefDetail = (id) => get(`/flow/defs/${id}`)
export const flowSubmit = (data) => post('/flow/instances', data)
export const flowMine = (params) => get('/flow/instances/mine', params)
export const flowTodo = (params) => get('/flow/instances/my-todo', params)
export const flowDone = (params) => get('/flow/instances/done', params)
export const flowDetail = (id) => get(`/flow/instances/${id}`)
export const flowWithdraw = (id) => put(`/flow/instances/${id}/withdraw`)
export const flowApprove = (taskId, data) => put(`/flow/tasks/${taskId}/approve`, data)
export const flowReject = (taskId, data) => put(`/flow/tasks/${taskId}/reject`, data)
export const flowTransfer = (taskId, data) => put(`/flow/tasks/${taskId}/transfer`, data)
export const flowAddComment = (instanceId, data) => post(`/flow/instances/${instanceId}/comment`, data)

/* ===== 流量审批：管理端（表单设计与流程配置） ===== */
export const flowAdminForms = () => get('/admin/flow/forms')
export const flowAdminFormSave = (data) => post('/admin/flow/forms', data)
export const flowAdminFormUpdate = (id, data) => put(`/admin/flow/forms/${id}`, data)
export const flowAdminFormDelete = (id) => del(`/admin/flow/forms/${id}`)
export const flowAdminDefs = () => get('/admin/flow/defs')
export const flowAdminDefDetail = (id) => get(`/admin/flow/defs/${id}`)
export const flowAdminDefSave = (data) => post('/admin/flow/defs', data)
export const flowAdminDefUpdate = (id, data) => put(`/admin/flow/defs/${id}`, data)
export const flowAdminDefDelete = (id) => del(`/admin/flow/defs/${id}`)
export const flowAdminDefToggle = (id) => put(`/admin/flow/defs/${id}/toggle`)
export const flowAdminArchive = (params) => get('/admin/flow/instances', params)

/* ===== 考勤 ===== */
export const attActiveRule = () => get('/att/rule/active')
export const attPunch = (data) => post('/att/punch', data)
export const attMyRecords = (params) => get('/att/my-records', params)
export const attAdminRules = () => get('/admin/att/rules')
export const attAdminRuleSave = (data) => post('/admin/att/rules', data)
export const attAdminRuleUpdate = (id, data) => put(`/admin/att/rules/${id}`, data)
export const attAdminRuleDelete = (id) => del(`/admin/att/rules/${id}`)
export const attAdminRuleToggle = (id, enabled) => put(`/admin/att/rules/${id}/toggle`, null, { enabled })
export const attAdminReport = (params) => get('/admin/att/report-month', params)

/* ===== 通讯录 ===== */
export const contactsTree = () => get('/contacts/tree')
export const contactsQuery = (params) => get('/contacts/query', params)

/* ===== 文档中心 ===== */
export const docFolderTree = () => get('/doc/folders/tree')
export const docFolderCreate = (parentId, name) => post(`/doc/folders?parentId=${parentId}&name=${encodeURIComponent(name)}`)
export const docFolderRename = (id, name) => put(`/doc/folders/${id}/rename?name=${encodeURIComponent(name)}`)
export const docFolderDelete = (id) => del(`/doc/folders/${id}`)
export const docPerms = (id) => get(`/doc/folders/${id}/perms`)
export const docPermAdd = (id, data) => post(`/doc/folders/${id}/perms`, data)
export const docPermRemove = (id, data) => service.delete(`/doc/folders/${id}/perms`, { data })
export const docFiles = (folderId) => get(`/doc/folders/${folderId}/files`)
export const docFileSearch = (params) => get('/doc/files/search', params)
export const docFileUpload = (folderId, formData, overwrite) =>
  service.post(`/doc/folders/${folderId}/files/upload`, formData, { params: { overwrite } })
export const docFileOverwriteUpload = (fileId, formData) => service.post(`/doc/files/${fileId}/version`, formData)
export const docFileRename = (fileId, name) => put(`/doc/files/${fileId}/rename?name=${encodeURIComponent(name)}`)
export const docFileDelete = (fileId) => del(`/doc/files/${fileId}`)
export const docFileVersions = (fileId) => get(`/doc/files/${fileId}/versions`)
export const docFileDownload = (fileId) => service.get(`/doc/files/${fileId}/download`, { responseType: 'blob' })
export const docVersionDownload = (versionId) => service.get(`/doc/versions/${versionId}/download`, { responseType: 'blob' })

/* ===== 系统基础管理（ADMIN） ===== */
export const adminOpLogs = (params) => get('/admin/op-logs', params)
export const adminSysConfig = () => get('/admin/sys-config')
export const adminUpdateConfig = (data) => put('/admin/sys-config', data)
export const adminSetRole = (userId, role) => put(`/admin/users/${userId}/role`, { role })

/* ===== 部门管理员 ===== */
export const deptAdminEmployees = (params) => get('/dept-admin/employees', params)
export const deptAdminEmpAdd = (data) => post('/dept-admin/employees', data)
export const deptAdminEmpUpdate = (id, data) => put(`/dept-admin/employees/${id}`, data)
export const deptAdminEmpDelete = (id) => del(`/dept-admin/employees/${id}`)
export const deptAdminEmpResetPwd = (id) => put(`/dept-admin/employees/${id}/password/reset`)
export const deptAdminEmpStatus = (id, status) => put(`/dept-admin/employees/${id}/status`, null, { status })
export const deptAdminAttReport = (params) => get('/dept-admin/attendance/report-month', params)

/* ===== 报表导出（blob 下载） ===== */
export const attReportExport = (month) =>
  service.get('/admin/att/report-month/export', { params: { month }, responseType: 'blob' })
export const deptAttReportExport = (month) =>
  service.get('/dept-admin/attendance/report-month/export', { params: { month }, responseType: 'blob' })

/** 通用 blob 下载：data 为 blob，filename 为文件名 */
export function downloadBlob(data, filename = 'download') {
  const url = window.URL.createObjectURL(new Blob([data]))
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  window.URL.revokeObjectURL(url)
}