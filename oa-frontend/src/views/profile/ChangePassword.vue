<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { updatePassword } from '@/api'
import { auth } from '@/stores/auth'

const router = useRouter()
const formRef = ref()
const saving = ref(false)

const form = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })

const validateConfirm = (rule, value, callback) => {
  if (!value) return callback(new Error('请再次输入新密码'))
  value !== form.newPassword ? callback(new Error('两次输入的新密码不一致')) : callback()
}

const rules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { pattern: /^(?=.*[A-Za-z])(?=.*\d)\S{6,20}$/, message: '长度 6~20 位，须包含字母和数字', trigger: 'blur' }
  ],
  confirmPassword: [{ validator: validateConfirm, trigger: 'blur' }]
}

async function submit() {
  await formRef.value.validate()
  saving.value = true
  try {
    await updatePassword({ oldPassword: form.oldPassword, newPassword: form.newPassword })
    ElMessage.success('密码修改成功，请重新登录')
    auth.clear()
    router.push('/login')
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <div class="oa-page-card pwd-card">
    <h3 class="oa-page-title">修改登录密码</h3>
    <p class="sub">为保障账号安全，修改成功后需要重新登录</p>

    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
      <el-form-item label="原密码" prop="oldPassword">
        <el-input v-model="form.oldPassword" type="password" show-password autocomplete="current-password" />
      </el-form-item>
      <el-form-item label="新密码" prop="newPassword">
        <el-input v-model="form.newPassword" type="password" show-password autocomplete="new-password" placeholder="6~20 位，包含字母和数字" />
      </el-form-item>
      <el-form-item label="确认新密码" prop="confirmPassword">
        <el-input v-model="form.confirmPassword" type="password" show-password autocomplete="new-password" @keyup.enter="submit" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="saving" @click="submit">确认修改</el-button>
        <el-button @click="formRef.resetFields()">重置</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<style scoped>
.pwd-card { max-width: 520px; }
.sub {
  margin: -8px 0 22px;
  color: var(--oa-ink-light);
  font-size: 13px;
}
</style>
