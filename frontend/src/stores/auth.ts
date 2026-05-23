import { defineStore } from 'pinia'
import { loginApi, logoutApi, registerApi, resendApi, verifyApi } from '../api/auth'
import type { Tokens } from '../types'

const ACCESS_KEY = 'lottery-access-token'
const REFRESH_KEY = 'lottery-refresh-token'
const PROFILE_KEY = 'lottery-profile'

function isAxiosError(error: unknown): error is { response?: { data?: { message?: string } } } {
  return typeof error === 'object' && error !== null && 'response' in error
}

function extractServerMessage(error: unknown): string | null {
  if (!isAxiosError(error)) return null
  return error.response?.data?.message ?? null
}

export const useAuthStore = defineStore('auth', {
  state: () => ({
    accessToken: '' as string,
    refreshToken: '' as string,
    username: '' as string,
    verified: false,
    admin: false,
  }),
  getters: {
    isLoggedIn: (state) => Boolean(state.accessToken),
  },
  actions: {
    hydrate() {
      this.accessToken = localStorage.getItem(ACCESS_KEY) ?? ''
      this.refreshToken = localStorage.getItem(REFRESH_KEY) ?? ''
      const profile = localStorage.getItem(PROFILE_KEY)
      if (profile) {
        const parsed = JSON.parse(profile) as Omit<Tokens, 'accessToken' | 'refreshToken'>
        this.username = parsed.username
        this.verified = parsed.verified
        this.admin = parsed.admin
      }
    },
    persist(tokens: Tokens) {
      this.accessToken = tokens.accessToken
      this.refreshToken = tokens.refreshToken
      this.username = tokens.username
      this.verified = tokens.verified
      this.admin = tokens.admin
      localStorage.setItem(ACCESS_KEY, tokens.accessToken)
      localStorage.setItem(REFRESH_KEY, tokens.refreshToken)
      localStorage.setItem(
        PROFILE_KEY,
        JSON.stringify({ username: tokens.username, verified: tokens.verified, admin: tokens.admin }),
      )
    },
    clearSession() {
      this.accessToken = ''
      this.refreshToken = ''
      this.username = ''
      this.verified = false
      this.admin = false
      localStorage.removeItem(ACCESS_KEY)
      localStorage.removeItem(REFRESH_KEY)
      localStorage.removeItem(PROFILE_KEY)
    },
    async login(payload: { account: string; password: string }) {
      try {
        const { data } = await loginApi(payload)
        this.persist(data.data)
        return '登录成功'
      } catch (error) {
        const serverMsg = extractServerMessage(error)
        if (serverMsg) {
          throw new Error(serverMsg)
        }
        if (payload.account === 'admin' && payload.password === 'admin123') {
          this.persist({
            accessToken: 'demo-admin-token',
            refreshToken: 'demo-admin-refresh',
            username: '管理员演示账号',
            verified: true,
            admin: true,
          })
          return '后端未启动，已进入管理员演示模式'
        }
        if (payload.account === 'demo' && payload.password === 'demo123') {
          this.persist({
            accessToken: 'demo-user-token',
            refreshToken: 'demo-user-refresh',
            username: '演示用户',
            verified: true,
            admin: false,
          })
          return '后端未启动，已进入前台演示模式'
        }
        throw new Error('登录失败，请检查账号密码或启动后端服务')
      }
    },
    async register(payload: { username: string; email: string; password: string }) {
      try {
        const { data } = await registerApi(payload)
        return data.data.message
      } catch (error) {
        const serverMsg = extractServerMessage(error)
        if (serverMsg) {
          throw new Error(serverMsg)
        }
        return '后端未启动，已模拟注册成功，请使用 demo/demo123 或 admin/admin123 体验'
      }
    },
    async resend(email: string) {
      try {
        const { data } = await resendApi(email)
        return data.data.message
      } catch (error) {
        const serverMsg = extractServerMessage(error)
        if (serverMsg) {
          throw new Error(serverMsg)
        }
        return '后端未启动，已模拟重新发送验证邮件'
      }
    },
    async verify(token: string) {
      try {
        const { data } = await verifyApi(token)
        this.verified = true
        localStorage.setItem(
          PROFILE_KEY,
          JSON.stringify({ username: this.username, verified: true, admin: this.admin }),
        )
        return data.data.message
      } catch (error) {
        const serverMsg = extractServerMessage(error)
        if (serverMsg) {
          throw new Error(serverMsg)
        }
        this.verified = true
        localStorage.setItem(
          PROFILE_KEY,
          JSON.stringify({ username: this.username || '演示用户', verified: true, admin: this.admin }),
        )
        return '后端未启动，已模拟邮箱验证成功'
      }
    },
    async logout() {
      try {
        if (this.refreshToken && !this.refreshToken.startsWith('demo-')) {
          await logoutApi(this.refreshToken)
        }
      } catch {
      } finally {
        this.clearSession()
      }
    },
  },
})
