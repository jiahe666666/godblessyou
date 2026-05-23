import http from './http'
import type { ApiResponse, DrawResult, HistoryItem, Prize } from '../types'

export const fetchPrizePoolApi = () => http.get<ApiResponse<Prize[]>>('/lottery/prizes')
export const drawApi = () => http.post<ApiResponse<DrawResult>>('/lottery/draw')
export const fetchHistoryApi = () => http.get<ApiResponse<HistoryItem[]>>('/lottery/history')
