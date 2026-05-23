<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from './stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const activePath = computed(() => route.path)

const handleLogout = async () => {
  await authStore.logout()
  router.push('/auth')
}
</script>

<template>
  <div class="app-shell">
    <aside class="side-nav">
      <div>
        <p class="brand-kicker">LUCKY DRAW</p>
        <h1>抽奖系统</h1>
        <p class="brand-copy">Vue 3 + Pinia + Router + Element Plus</p>
      </div>

      <el-menu :default-active="activePath" class="nav-menu" router>
        <el-menu-item index="/lottery">抽奖大厅</el-menu-item>
        <el-menu-item index="/profile">个人中心</el-menu-item>
        <el-menu-item index="/admin">管理后台</el-menu-item>
        <el-menu-item index="/auth">登录注册</el-menu-item>
      </el-menu>

      <div class="side-footer">
        <el-tag type="info">账号：demo / demo123</el-tag>
        <el-tag type="danger">管理员：admin / admin123</el-tag>
      </div>
    </aside>

    <section class="main-panel">
      <header class="main-header">
        <div>
          <h2>{{ route.meta.title ?? '抽奖系统' }}</h2>
          <p>{{ authStore.isLoggedIn ? `当前用户：${authStore.username}` : '当前尚未登录' }}</p>
        </div>

        <div class="header-actions">
          <el-tag :type="authStore.verified ? 'success' : 'warning'">
            {{ authStore.verified ? '邮箱已验证' : '邮箱未验证' }}
          </el-tag>
          <el-button v-if="authStore.isLoggedIn" type="primary" plain @click="handleLogout">退出登录</el-button>
          <el-button v-else type="primary" @click="router.push('/auth')">立即登录</el-button>
        </div>
      </header>

      <router-view />
    </section>
  </div>
</template>
