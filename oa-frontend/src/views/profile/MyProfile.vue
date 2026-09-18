<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { myProfile, updateProfile, avatarUpload } from '@/api'
import { auth } from '@/stores/auth'

const profile = ref({})
const loading = ref(true)
const saving = ref(false)
const formRef = ref()
const editing = ref(false)
const form = reactive({ phone: '', email: '' })
const avatarUploading = ref(false)
const avatarInputRef = ref()

async function onAvatarChange(event) {
  const file = event.target.files[0]
  if (!file) return
  if (!file.type.startsWith('image/')) {
    ElMessage.warning('请上传图片文件')
    return
  }
  avatarUploading.value = true
  try {
    const fd = new FormData()
    fd.append('file', file)
    const res = await avatarUpload(fd)
    profile.value.avatarUrl = res.data
    // 同步顶部菜单头像
    const user = auth.user || {}
    auth.setUser({ ...user, avatarUrl: res.data })
    ElMessage.success('头像已更新')
  } finally {
    avatarUploading.value = false
    if (avatarInputRef.value) avatarInputRef.value.value = ''
  }
}

const rules = {
  phone: [{ required: true, pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }],
  email: [{ required: true, type: 'email', message: '邮箱格式不正确', trigger: 'blur' }]
}

async function load() {
  loading.value = true
  try {
    const res = await myProfile()
    profile.value = res.data
    Object.assign(form, { phone: res.data.phone, email: res.data.email })
  } finally {
    loading.value = false
  }
}

onMounted(load)

async function save() {
  await formRef.value.validate()
  saving.value = true
  try {
    await updateProfile(form)
    ElMessage.success('资料已更新')
    editing.value = false
    load()
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <div v-loading="loading" class="profile-wrap">
    <!-- 身份卡 -->
    <div class="id-card">
      <div class="avatar-box">
        <el-avatar :size="56" class="avatar">
          <img v-if="profile.avatarUrl" :src="`http://localhost:8080/api/file/avatar/${profile.userId}`" alt="avatar" />
          <span v-else>{{ (profile.name || 'U').charAt(0) }}</span>
        </el-avatar>
        <el-button size="small" class="avatar-btn" :loading="avatarUploading" @click="avatarInputRef?.click()">更换头像</el-button>
        <input ref="avatarInputRef" type="file" accept="image/*" style="display: none" @change="onAvatarChange" />
      </div>
      <div class="id-name">{{ profile.name || auth.user?.username }}</div>
      <div class="id-no oa-mono">{{ profile.empNo || 'ADMIN' }}</div>
      <el-tag :type="profile.role === 'ADMIN' ? 'danger' : profile.role === 'DEPT_ADMIN' ? 'warning' : 'success'" size="small" effect="dark">
        {{ profile.role === 'ADMIN' ? '超级管理员' : profile.role === 'DEPT_ADMIN' ? '部门管理员' : '普通员工' }}
      </el-tag>
    </div>

    <!-- 档案 -->
    <div class="oa-page-card info-card">
      <div class="card-head">
        <h3 class="oa-page-title" style="margin: 0">个人档案</h3>
        <el-button v-if="!editing" type="primary" plain size="small" :icon="'EditPen'" :disabled="!profile.empId" @click="editing = true">
          修改资料
        </el-button>
        <el-button v-else size="small" @click="editing = false">取消</el-button>
      </div>

      <el-descriptions :column="2" border>
        <el-descriptions-item label="登录账号"><span class="oa-mono">{{ profile.username }}</span></el-descriptions-item>
        <el-descriptions-item label="姓名">{{ profile.name || '—' }}</el-descriptions-item>
        <el-descriptions-item label="性别">{{ profile.gender === 0 ? '女' : '男' }}</el-descriptions-item>
        <el-descriptions-item label="所属部门">{{ profile.deptName || '—' }}</el-descriptions-item>
        <el-descriptions-item label="入职日期">{{ profile.hireDate || '—' }}</el-descriptions-item>
        <el-descriptions-item label="注册时间">{{ profile.accountCreateTime || '—' }}</el-descriptions-item>
      </el-descriptions>

      <!-- 可编辑字段 -->
      <el-form v-if="editing" ref="formRef" :model="form" :rules="rules" label-width="80px" class="edit-form">
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" maxlength="11" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" />
        </el-form-item>
        <el-button type="primary" :loading="saving" @click="save">保存修改</el-button>
      </el-form>
      <el-descriptions v-else :column="2" border class="contact-desc">
        <el-descriptions-item label="手机号"><span class="oa-mono">{{ profile.phone || '—' }}</span></el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ profile.email || '—' }}</el-descriptions-item>
      </el-descriptions>

      <p v-if="!profile.empId" class="tip">管理员账号未关联员工档案，仅支持修改密码。</p>
    </div>
  </div>
</template>

<style scoped>
.profile-wrap {
  display: flex;
  gap: 16px;
  align-items: flex-start;
}

.id-card {
  width: 220px;
  flex-shrink: 0;
  background: linear-gradient(160deg, var(--oa-navy), #1a4a7e);
  border-radius: 6px;
  color: #fff;
  padding: 32px 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}

.avatar {
  background: var(--oa-signal);
  color: var(--oa-navy);
  font-size: 24px;
  font-weight: 700;
}
.avatar img { width: 100%; height: 100%; object-fit: cover; }
.avatar-box { text-align: center; position: relative; }
.avatar-btn { margin-top: 8px; font-size: 12px; }

.id-name { font-size: 17px; font-weight: 600; }

.id-no {
  font-size: 12px;
  color: var(--oa-signal);
  letter-spacing: 0.08em;
}

.info-card { flex: 1; min-width: 0; }

.card-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.edit-form {
  margin-top: 18px;
  max-width: 420px;
}

.contact-desc { margin-top: 16px; }

.tip {
  margin: 16px 0 0;
  font-size: 12.5px;
  color: var(--oa-ink-light);
}

@media (max-width: 992px) {
  .profile-wrap { flex-direction: column; }
  .id-card { width: 100%; flex-direction: row; padding: 16px 20px; }
}
</style>
