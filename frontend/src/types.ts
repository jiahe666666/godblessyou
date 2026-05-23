export type PrizeType = 'code' | 'image'

export interface ApiResponse<T> {
  code: number
  message: string
  data: T
  timestamp: number
}

export interface Tokens {
  accessToken: string
  refreshToken: string
  username: string
  verified: boolean
  admin: boolean
}

export interface Prize {
  id: number
  name: string
  description: string
  prizeType: PrizeType
  stockTotal: number
  stockLeft: number
  probability: number
  enabled: boolean
}

export interface DrawResult {
  prizeId: number
  prizeName: string
  prizeType: PrizeType
  content: string
  stockLeft: number
}

export interface HistoryItem {
  prizeItemId: number
  prizeName: string
  prizeType: PrizeType
  content: string
  createdAt: string
}

export interface Dashboard {
  prizeCount: number
  enabledPrizeCount: number
  stockLeftTotal: number
  winnerCount: number
}

export interface MessagePayload {
  message: string
}

export interface PrizeCreatePayload {
  name: string
  description: string
  prizeType: PrizeType
  probability: number
  contents: string
  enabled?: boolean
}

export interface PrizeUpdatePayload {
  name: string
  description: string
  prizeType: PrizeType
  probability: number
  enabled?: boolean
}

export interface PrizeStockAudit {
  id: number
  prizeId: number
  prizeName: string
  operatorUserId: number | null
  changeType: string
  delta: number
  stockBefore: number
  stockAfter: number
  remark: string
  createdAt: string
}
