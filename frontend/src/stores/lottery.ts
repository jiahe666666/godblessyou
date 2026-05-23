import { defineStore } from 'pinia'
import { createPrizeApi, deletePrizeApi, fetchAdminPrizesApi, fetchDashboardApi, fetchStockAuditsApi, togglePrizeApi, updatePrizeApi } from '../api/admin'
import { drawApi, fetchHistoryApi, fetchPrizePoolApi } from '../api/lottery'
import type { Dashboard, DrawResult, HistoryItem, Prize, PrizeCreatePayload, PrizeStockAudit } from '../types'

const seedPrizes = (): Prize[] => [
  {
    id: 1,
    name: '尊享兑换码',
    description: '用于激活年度会员权益，系统抽中后直接显示兑换码。',
    prizeType: 'code',
    stockTotal: 12,
    stockLeft: 12,
    probability: 0.12,
    enabled: true,
  },
  {
    id: 2,
    name: '限量海报图',
    description: '图片类奖品，适合展示中奖预览。',
    prizeType: 'image',
    stockTotal: 18,
    stockLeft: 18,
    probability: 0.2,
    enabled: true,
  },
  {
    id: 3,
    name: '积分礼包',
    description: '高命中率奖品，用于体验抽奖发放流程。',
    prizeType: 'code',
    stockTotal: 66,
    stockLeft: 66,
    probability: 0.68,
    enabled: true,
  },
]

const seedHistory = (): HistoryItem[] => [
  {
    prizeItemId: 1001,
    prizeName: '新人体验码',
    prizeType: 'code',
    content: 'WELCOME-2026',
    createdAt: '2026-05-20 09:30:18',
  },
  {
    prizeItemId: 1002,
    prizeName: '品牌海报图',
    prizeType: 'image',
    content: '/src/assets/hero.png',
    createdAt: '2026-05-20 09:42:06',
  },
]

const formatNow = () => new Date().toLocaleString('zh-CN', { hour12: false }).replaceAll('/', '-')

export const useLotteryStore = defineStore('lottery', {
  state: () => ({
    prizes: [] as Prize[],
    history: [] as HistoryItem[],
    adminPrizes: [] as Prize[],
    stockAudits: [] as PrizeStockAudit[],
    dashboard: {
      prizeCount: 0,
      enabledPrizeCount: 0,
      stockLeftTotal: 0,
      winnerCount: 0,
    } as Dashboard,
    latestResult: null as DrawResult | null,
  }),
  actions: {
    ensureMockData() {
      if (!this.prizes.length) {
        this.prizes = seedPrizes()
      }
      if (!this.history.length) {
        this.history = seedHistory()
      }
      if (!this.adminPrizes.length) {
        this.adminPrizes = [...this.prizes]
      }
      this.dashboard = {
        prizeCount: this.adminPrizes.length,
        enabledPrizeCount: this.adminPrizes.filter((item) => item.enabled).length,
        stockLeftTotal: this.adminPrizes.reduce((sum, item) => sum + item.stockLeft, 0),
        winnerCount: this.history.length,
      }
    },
    async fetchPrizes() {
      try {
        const { data } = await fetchPrizePoolApi()
        this.prizes = data.data
      } catch {
        this.ensureMockData()
      }
    },
    async draw() {
      try {
        const { data } = await drawApi()
        this.latestResult = data.data
      } catch {
        this.ensureMockData()
        const available = this.prizes.filter((item) => item.enabled && item.stockLeft > 0)
        if (!available.length) {
          throw new Error('当前没有可抽奖品')
        }
        const total = available.reduce((sum, item) => sum + item.probability, 0)
        let cursor = Math.random() * total
        const picked = available.find((item) => {
          cursor -= item.probability
          return cursor <= 0
        }) ?? available[available.length - 1]
        picked.stockLeft -= 1
        const result: DrawResult = {
          prizeId: picked.id,
          prizeName: picked.name,
          prizeType: picked.prizeType,
          content: picked.prizeType === 'code' ? `DEMO-${Date.now()}` : '/src/assets/hero.png',
          stockLeft: picked.stockLeft,
        }
        this.latestResult = result
        this.history.unshift({
          prizeItemId: Date.now(),
          prizeName: result.prizeName,
          prizeType: result.prizeType,
          content: result.content,
          createdAt: formatNow(),
        })
      }
      this.syncAdminState()
      return this.latestResult
    },
    async fetchHistory() {
      try {
        const { data } = await fetchHistoryApi()
        this.history = data.data
      } catch {
        this.ensureMockData()
      }
    },
    async fetchDashboard() {
      try {
        const { data } = await fetchDashboardApi()
        this.dashboard = data.data
      } catch {
        this.ensureMockData()
      }
    },
    async fetchAdminPrizes() {
      try {
        const { data } = await fetchAdminPrizesApi()
        this.adminPrizes = data.data
      } catch {
        this.ensureMockData()
      }
      this.syncAdminState()
    },
    async fetchStockAudits() {
      try {
        const { data } = await fetchStockAuditsApi()
        this.stockAudits = data.data
      } catch {
        this.stockAudits = []
      }
    },
    async createPrize(payload: PrizeCreatePayload) {
      try {
        const { data } = await createPrizeApi(payload)
        this.adminPrizes.unshift(data.data)
      } catch {
        const lines = payload.contents.split('\n').map((item) => item.trim()).filter(Boolean)
        const prize: Prize = {
          id: Date.now(),
          name: payload.name,
          description: payload.description,
          prizeType: payload.prizeType,
          stockTotal: lines.length,
          stockLeft: lines.length,
          probability: payload.probability,
          enabled: payload.enabled ?? true,
        }
        this.adminPrizes.unshift(prize)
        this.prizes.unshift(prize)
      }
      this.syncAdminState()
    },
    async updatePrize(id: number, payload: PrizeCreatePayload) {
      try {
        const { data } = await updatePrizeApi(id, payload)
        const updateItem = (items: Prize[]) => {
          const idx = items.findIndex((item) => item.id === id)
          if (idx !== -1) {
            items[idx] = data.data
          }
        }
        updateItem(this.adminPrizes)
        updateItem(this.prizes)
      } catch {
        const updateItem = (items: Prize[]) => {
          const target = items.find((item) => item.id === id)
          if (target) {
            target.name = payload.name
            target.description = payload.description
            target.prizeType = payload.prizeType
            target.probability = payload.probability
            if (payload.enabled !== undefined) target.enabled = payload.enabled
          }
        }
        updateItem(this.adminPrizes)
        updateItem(this.prizes)
      }
      this.syncAdminState()
    },
    async togglePrize(id: number) {
      try {
        await togglePrizeApi(id)
      } finally {
        const updateItem = (items: Prize[]) => {
          const target = items.find((item) => item.id === id)
          if (target) {
            target.enabled = !target.enabled
          }
        }
        updateItem(this.adminPrizes)
        updateItem(this.prizes)
        this.syncAdminState()
      }
    },
    async deletePrize(id: number) {
      try {
        await deletePrizeApi(id)
      } catch {
        // Demo fallback
      }
      this.adminPrizes = this.adminPrizes.filter((item) => item.id !== id)
      this.prizes = this.prizes.filter((item) => item.id !== id)
      this.syncAdminState()
    },
    syncAdminState() {
      if (!this.adminPrizes.length) {
        this.adminPrizes = [...this.prizes]
      }
      this.dashboard = {
        prizeCount: this.adminPrizes.length,
        enabledPrizeCount: this.adminPrizes.filter((item) => item.enabled).length,
        stockLeftTotal: this.adminPrizes.reduce((sum, item) => sum + item.stockLeft, 0),
        winnerCount: this.history.length,
      }
    },
  },
})
