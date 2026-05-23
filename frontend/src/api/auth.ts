import http from './http'
import type { ApiResponse, MessagePayload, Tokens } from '../types'

export const registerApi = (payload: { username: string; email: string; password: string }) =>
  http.post<ApiResponse<MessagePayload>>('/auth/register', payload)

export const loginApi = (payload: { account: string; password: string }) =>
  http.post<ApiResponse<Tokens>>('/auth/login', payload)

export const refreshApi = (refreshToken: string) =>
  http.post<ApiResponse<Tokens>>('/auth/refresh', { refreshToken })

export const logoutApi = (refreshToken: string) =>
  http.post<ApiResponse<MessagePayload>>('/auth/logout', { refreshToken })

export const resendApi = (email: string) =>
  http.post<ApiResponse<MessagePayload>>('/auth/resend', { email })

export const verifyApi = (token: string) =>
  http.get<ApiResponse<MessagePayload>>(`/auth/verify?token=${encodeURIComponent(token)}`)
