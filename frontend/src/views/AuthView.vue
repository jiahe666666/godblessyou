<script setup lang="ts">
import { reactive, ref, watch } from "vue"
import { useRoute, useRouter } from "vue-router"
import { ElMessage } from "element-plus"
import { useAuthStore } from "../stores/auth"

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const activeTab = ref<"login" | "register" | "resend">("login")

const loginForm = reactive({ account: "demo", password: "demo123" })
const registerForm = reactive({ username: "", email: "", password: "" })
const resendForm = reactive({ email: "" })

watch(
  () => route.path,
  async () => {
    if (route.path === "/verify" && typeof route.query.token === "string") {
      const message = await authStore.verify(route.query.token)
      ElMessage.success(message)
      router.replace("/auth")
    }
  },
  { immediate: true },
)

const handleLogin = async () => {
  try {
    const message = await authStore.login(loginForm)
    ElMessage.success(message)
    router.push(authStore.admin ? "/admin" : "/lottery")
  } catch (error) {
    ElMessage.error((error as Error).message)
  }
}

const handleRegister = async () => {
  try {
    const message = await authStore.register(registerForm)
    ElMessage.success(message)
    activeTab.value = "login"
  } catch (error) {
    ElMessage.error((error as Error).message)
  }
}

const handleResend = async () => {
  try {
    const message = await authStore.resend(resendForm.email)
    ElMessage.success(message)
  } catch (error) {
    ElMessage.error((error as Error).message)
  }
}
</script>

<template>
  <div class="page-grid two-columns auth-grid">
    <el-card shadow="never" class="glass-card">
      <template #header>
        <div class="card-header">
          <div>
            <p class="section-kicker">账户入口</p>
            <h3>登录 / 注册 / 验证</h3>
          </div>
        </div>
      </template>

      <el-tabs v-model="activeTab">
        <el-tab-pane label="登录" name="login">
          <el-form label-position="top" :model="loginForm">
            <el-form-item label="账号">
              <el-input v-model="loginForm.account" placeholder="用户名或邮箱" />
            </el-form-item>
            <el-form-item label="密码">
              <el-input v-model="loginForm.password" type="password" show-password placeholder="请输入密码" />
            </el-form-item>
            <el-button type="primary" class="full-width" @click="handleLogin">登录</el-button>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="注册" name="register">
          <el-form label-position="top" :model="registerForm">
            <el-form-item label="用户名">
              <el-input v-model="registerForm.username" placeholder="3-20 位用户名" />
            </el-form-item>
            <el-form-item label="邮箱">
              <el-input v-model="registerForm.email" placeholder="请输入邮箱" />
            </el-form-item>
            <el-form-item label="密码">
              <el-input v-model="registerForm.password" type="password" show-password placeholder="至少 8 位，含大写、小写、数字、特殊字符中至少两种" />
            </el-form-item>
            <el-button type="primary" class="full-width" @click="handleRegister">注册并发送验证邮件</el-button>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="重发验证" name="resend">
          <el-form label-position="top" :model="resendForm">
            <el-form-item label="邮箱">
              <el-input v-model="resendForm.email" placeholder="请输入已注册邮箱" />
            </el-form-item>
            <el-button type="warning" class="full-width" @click="handleResend">重新发送</el-button>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <el-card shadow="never" class="glass-card">
      <template #header>
        <div class="card-header">
          <div>
            <p class="section-kicker">体验说明</p>
            <h3>开发要求映射</h3>
          </div>
        </div>
      </template>

      <div class="stack-list">
        <el-alert type="info" show-icon :closable="false" title="前台技术栈已切换为 Vue 3 + Router + Pinia + Axios + Element Plus" />
        <el-alert type="success" show-icon :closable="false" title="邮箱验证回调支持 /verify?token=xxx 路由" />
        <el-alert type="warning" show-icon :closable="false" title="后端未启动时，可使用 demo/demo123 或 admin/admin123 进入演示模式" />
        <el-descriptions :column="1" border>
          <el-descriptions-item label="普通用户">demo / demo123</el-descriptions-item>
          <el-descriptions-item label="管理员">admin / admin123</el-descriptions-item>
          <el-descriptions-item label="目标后端">Spring Boot 3.2 + JWT + Redis + MySQL</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-card>
  </div>
</template>
