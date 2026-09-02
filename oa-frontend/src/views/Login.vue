<script setup>
import { reactive, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '@/api'
import { auth } from '@/stores/auth'

const router = useRouter()
const route = useRoute()
const formRef = ref()
const loading = ref(false)

const form = reactive({ username: '', password: '' })

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function handleLogin() {
  await formRef.value.validate()
  loading.value = true
  try {
    const res = await login(form)
    auth.setToken(res.data.token)
    auth.setUser(res.data)
    ElMessage.success(`欢迎回来，${res.data.name}`)
    const redirect = route.query.redirect
    if (redirect) {
      router.push(redirect)
    } else {
      router.push(res.data.role === 'ADMIN' ? '/admin/dashboard' : '/employee/workbench')
    }
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-page">
    <!-- 左：品牌面板 —— 数据中心网络拓扑 -->
    <section class="brand-panel">
      <div class="brand-top">
        <div class="logo-mark">
          <svg viewBox="0 0 40 40" width="38" height="38" aria-hidden="true">
            <rect x="3" y="3" width="14" height="14" rx="2" fill="none" stroke="#38BDF8" stroke-width="2"/>
            <rect x="23" y="3" width="14" height="14" rx="2" fill="none" stroke="#9FB3C8" stroke-width="2"/>
            <rect x="3" y="23" width="14" height="14" rx="2" fill="none" stroke="#9FB3C8" stroke-width="2"/>
            <rect x="23" y="23" width="14" height="14" rx="2" fill="#1A56A8" stroke="#38BDF8" stroke-width="2"/>
          </svg>
        </div>
        <div class="brand-name">
          <h1>OA 办公管理系统</h1>
          <p class="eyebrow">DATA CENTER · ENTERPRISE OFFICE SUITE</p>
        </div>
      </div>

      <!-- 拓扑示意：核心交换 + 服务器机柜，数据包沿链路流动 -->
      <div class="topology" role="img" aria-label="数据中心网络拓扑示意图">
        <svg viewBox="0 0 520 320" preserveAspectRatio="xMidYMid meet">
          <!-- 链路 -->
          <g class="links" stroke="#2B4A6F" stroke-width="1.5" fill="none">
            <path d="M260 168 L100 68" />
            <path d="M260 168 L260 52" />
            <path d="M260 168 L420 68" />
            <path d="M260 168 L100 268" />
            <path d="M260 168 L260 288" />
            <path d="M260 168 L420 268" />
          </g>

          <!-- 边缘节点：服务器机柜 -->
          <g class="node n1"><rect x="76" y="48" width="48" height="34" rx="3"/><text x="100" y="38">WEB-01</text></g>
          <g class="node n2"><rect x="236" y="22" width="48" height="34" rx="3"/><text x="260" y="14">APP-01</text></g>
          <g class="node n3"><rect x="396" y="48" width="48" height="34" rx="3"/><text x="420" y="38">DB-01</text></g>
          <g class="node n4"><rect x="76" y="248" width="48" height="34" rx="3"/><text x="100" y="300">VPN</text></g>
          <g class="node n5"><rect x="236" y="278" width="48" height="34" rx="3" transform="translate(0,-26)"/><text x="260" y="296">VLAN</text></g>
          <g class="node n6"><rect x="396" y="248" width="48" height="34" rx="3"/><text x="420" y="300">FW</text></g>

          <!-- 核心交换机 -->
          <g class="core">
            <rect x="218" y="146" width="84" height="44" rx="4"/>
            <text x="260" y="173">CORE-SW</text>
          </g>

          <!-- 数据包光点：CSS 沿直线位移，错峰流动 -->
          <circle class="packet p1" r="3.5" cx="260" cy="168"/>
          <circle class="packet p2" r="3.5" cx="260" cy="168"/>
          <circle class="packet p3" r="3.5" cx="260" cy="168"/>
          <circle class="packet p4" r="3.5" cx="100" cy="68"/>
          <circle class="packet p5" r="3.5" cx="420" cy="268"/>
        </svg>
      </div>

      <ul class="net-status">
        <li><span class="dot"></span>服务器 VLAN 已划分</li>
        <li><span class="dot"></span>VPN 远程访问正常</li>
        <li><span class="dot"></span>防火墙端口策略生效</li>
      </ul>
    </section>

    <!-- 右：登录表单 -->
    <section class="form-panel">
      <div class="form-card">
        <h2>账号登录</h2>
        <p class="sub">请使用员工账号或管理员账号登录</p>

        <el-form ref="formRef" :model="form" :rules="rules" size="large" @keyup.enter="handleLogin">
          <el-form-item prop="username">
            <el-input v-model="form.username" placeholder="用户名" clearable :prefix-icon="'User'" autocomplete="username" />
          </el-form-item>
          <el-form-item prop="password">
            <el-input v-model="form.password" type="password" placeholder="密码" show-password :prefix-icon="'Lock'" autocomplete="current-password" />
          </el-form-item>
          <el-button class="submit-btn" type="primary" :loading="loading" @click="handleLogin">
            登 录
          </el-button>
        </el-form>

        <div class="form-foot">
          <span>还没有账号？<router-link to="/register">员工注册</router-link></span>
        </div>

        <el-alert class="demo-tip" type="info" :closable="false">
          <template #title>
            测试账号：管理员 <b class="oa-mono">admin / 123456</b>　员工 <b class="oa-mono">lina / 123456</b>
          </template>
        </el-alert>
      </div>
    </section>
  </div>
</template>

<style scoped>
.login-page {
  height: 100%;
  display: flex;
  background: var(--oa-surface);
}

/* ---------- 左品牌面板 ---------- */
.brand-panel {
  flex: 1.15;
  background:
    radial-gradient(ellipse 70% 55% at 22% 18%, rgba(56, 189, 248, 0.09), transparent),
    var(--oa-navy);
  color: #fff;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 56px 64px;
  gap: 40px;
}

.brand-top {
  display: flex;
  align-items: center;
  gap: 16px;
}

.brand-name h1 {
  margin: 0;
  font-size: 26px;
  font-weight: 600;
  letter-spacing: 0.06em;
}

.eyebrow {
  margin: 6px 0 0;
  font-family: var(--oa-mono);
  font-size: 11px;
  letter-spacing: 0.22em;
  color: #7d95af;
}

.topology {
  max-width: 560px;
}

.topology svg {
  width: 100%;
  height: auto;
  overflow: visible;
}

.node rect {
  fill: #16324f;
  stroke: #2f567f;
  stroke-width: 1.5;
}

.node text {
  fill: #9fb3c8;
  font-size: 11px;
  font-family: var(--oa-mono);
  text-anchor: middle;
  letter-spacing: 0.08em;
}

.core rect {
  fill: #1a56a8;
  stroke: var(--oa-signal);
  stroke-width: 1.5;
}

.core text {
  fill: #fff;
  font-size: 12px;
  font-family: var(--oa-mono);
  text-anchor: middle;
  letter-spacing: 0.1em;
}

.packet {
  fill: var(--oa-signal);
  filter: drop-shadow(0 0 4px rgba(56, 189, 248, 0.9));
}

/* 数据包沿六条直线链路错峰流动（起点见 cx/cy） */
.p1 { animation: flowToWeb 3.2s linear infinite; }
.p2 { animation: flowToApp 2.6s linear infinite 0.8s; }
.p3 { animation: flowToDb 3.6s linear infinite 1.4s; }
.p4 { animation: flowToCoreFromWeb 3s linear infinite 0.4s; }
.p5 { animation: flowToCoreFromFw 3.4s linear infinite 2s; }

@keyframes flowToWeb        { from { transform: translate(0, 0); opacity: 0; } 12% { opacity: 1; } to { transform: translate(-160px, -100px); opacity: 0; } }
@keyframes flowToApp        { from { transform: translate(0, 0); opacity: 0; } 15% { opacity: 1; } to { transform: translate(0, -116px); opacity: 0; } }
@keyframes flowToDb         { from { transform: translate(0, 0); opacity: 0; } 12% { opacity: 1; } to { transform: translate(160px, -100px); opacity: 0; } }
@keyframes flowToCoreFromWeb{ from { transform: translate(0, 0); opacity: 0; } 12% { opacity: 1; } to { transform: translate(160px, 100px); opacity: 0; } }
@keyframes flowToCoreFromFw { from { transform: translate(0, 0); opacity: 0; } 12% { opacity: 1; } to { transform: translate(-160px, -100px); opacity: 0; } }

.net-status {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  gap: 28px;
  flex-wrap: wrap;
  color: #9fb3c8;
  font-size: 13px;
}

.net-status li {
  display: flex;
  align-items: center;
  gap: 8px;
}

.net-status .dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #34d399;
  box-shadow: 0 0 6px rgba(52, 211, 153, 0.8);
}

/* ---------- 右表单面板 ---------- */
.form-panel {
  flex: 0 0 480px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff;
  border-left: 1px solid var(--oa-line);
}

.form-card {
  width: 340px;
}

.form-card h2 {
  margin: 0 0 6px;
  font-size: 22px;
  color: var(--oa-navy);
}

.form-card .sub {
  margin: 0 0 28px;
  color: var(--oa-ink-light);
  font-size: 13px;
}

.submit-btn {
  width: 100%;
  letter-spacing: 0.5em;
  margin-top: 4px;
}

.form-foot {
  margin-top: 18px;
  text-align: center;
  font-size: 13px;
  color: var(--oa-ink-light);
}

.form-foot a {
  color: var(--oa-primary);
  text-decoration: none;
  font-weight: 600;
}

.demo-tip {
  margin-top: 28px;
}

@media (max-width: 992px) {
  .brand-panel { display: none; }
  .form-panel { flex: 1; border-left: none; }
}
</style>
