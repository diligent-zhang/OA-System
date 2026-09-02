<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { register, deptTree, usernameExists } from '@/api'

const router = useRouter()
const formRef = ref()
const loading = ref(false)
const depts = ref([])

const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  name: '',
  gender: 1,
  phone: '',
  email: '',
  deptId: null,
  hireDate: ''
})

/* 用户名唯一性异步校验（与 required/pattern 规则配合使用） */
const validateUsername = async (rule, value, callback) => {
  if (!value) return callback()
  if (!/^[a-zA-Z][a-zA-Z0-9_]{3,19}$/.test(value)) return callback()
  const res = await usernameExists(value)
  res.data ? callback(new Error('用户名已存在，请更换')) : callback()
}

const validateConfirm = (rule, value, callback) => {
  if (!value) return callback()
  value !== form.password ? callback(new Error('两次输入的密码不一致')) : callback()
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { pattern: /^[a-zA-Z][a-zA-Z0-9_]{3,19}$/, message: '字母开头，4~20 位，仅含字母、数字、下划线', trigger: 'blur' },
    { validator: validateUsername, trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { pattern: /^(?=.*[A-Za-z])(?=.*\d)\S{6,20}$/, message: '长度 6~20 位，须包含字母和数字', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { validator: validateConfirm, trigger: 'blur' }
  ],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }],
  phone: [{ required: true, pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }],
  email: [{ required: true, type: 'email', message: '邮箱格式不正确', trigger: 'blur' }]
}

deptTree().then((res) => (depts.value = res.data))

async function handleRegister() {
  await formRef.value.validate()
  loading.value = true
  try {
    const { confirmPassword, ...data } = form
    await register(data)
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="register-page">
    <div class="register-card oa-page-card">
      <div class="head">
        <h2>员工注册</h2>
        <p class="sub">注册后默认获得普通员工权限，可浏览公告与维护个人信息</p>
      </div>

      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px" hide-required-asterisk>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="用户名" prop="username">
              <el-input v-model="form.username" placeholder="字母开头，4~20 位" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="姓名" prop="name">
              <el-input v-model="form.name" placeholder="真实姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="密码" prop="password">
              <el-input v-model="form.password" type="password" show-password placeholder="含字母和数字" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input v-model="form.confirmPassword" type="password" show-password placeholder="再次输入密码" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="性别" prop="gender">
              <el-radio-group v-model="form.gender">
                <el-radio :value="1">男</el-radio>
                <el-radio :value="0">女</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="form.phone" maxlength="11" placeholder="11 位手机号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="form.email" placeholder="name@company.com" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所属部门">
              <el-tree-select
                v-model="form.deptId"
                :data="depts"
                :props="{ label: 'deptName', children: 'children' }"
                node-key="deptId"
                check-strictly
                placeholder="选填，入职后再分配"
                clearable
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="入职日期">
              <el-date-picker v-model="form.hireDate" type="date" value-format="YYYY-MM-DD" placeholder="选填，默认今天" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <div class="actions">
          <el-button @click="router.push('/login')">返回登录</el-button>
          <el-button type="primary" :loading="loading" @click="handleRegister">注 册</el-button>
        </div>
      </el-form>
    </div>
  </div>
</template>

<style scoped>
.register-page {
  min-height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background:
    radial-gradient(ellipse 60% 40% at 85% -5%, rgba(26, 86, 168, 0.08), transparent),
    var(--oa-surface);
  padding: 40px 16px;
}

.register-card {
  width: 720px;
  max-width: 100%;
}

.head h2 {
  margin: 0 0 6px;
  color: var(--oa-navy);
  font-size: 22px;
}

.head .sub {
  margin: 0 0 24px;
  color: var(--oa-ink-light);
  font-size: 13px;
}

.actions {
  display: flex;
  justify-content: flex-end;
  gap: 4px;
  margin-top: 8px;
}
</style>
