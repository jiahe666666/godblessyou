import http from './http'
import type { ApiResponse, Dashboard, Prize, PrizeCreatePayload, PrizeStockAudit, MessagePayload } from '../types'

export const fetchDashboardApi = () => http.get<ApiResponse<Dashboard>>('/admin/dashboard')
export const fetchAdminPrizesApi = () => http.get<ApiResponse<Prize[]>>('/admin/prizes')
export const createPrizeApi = (payload: PrizeCreatePayload) => http.post<ApiResponse<Prize>>('/admin/prizes', payload)
export const updatePrizeApi = (id: number, payload: PrizeCreatePayload) => http.put<ApiResponse<Prize>>(`/admin/prizes/${id}`, payload)
export const togglePrizeApi = (id: number) => http.put<ApiResponse<Prize>>(`/admin/prizes/${id}/toggle`)
export const deletePrizeApi = (id: number) => http.delete<ApiResponse<MessagePayload>>(`/admin/prizes/${id}`)
export const fetchStockAuditsApi = () => http.get<ApiResponse<PrizeStockAudit[]>>('/admin/stock-audits')
