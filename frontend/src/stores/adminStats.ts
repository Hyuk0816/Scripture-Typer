import { ref } from 'vue'
import { defineStore } from 'pinia'
import { adminStatsApi } from '@/utils/api'
import type {
  DailyLoginStatResponse,
  DailyProgressStatResponse,
  DailyChatStatResponse,
  MonthlyStatResponse,
} from '@/types/admin-stats'

export const useAdminStatsStore = defineStore('adminStats', () => {
  const loginStats = ref<DailyLoginStatResponse[]>([])
  const progressStats = ref<DailyProgressStatResponse[]>([])
  const chatStats = ref<DailyChatStatResponse[]>([])
  const monthlyStats = ref<MonthlyStatResponse | null>(null)
  const loading = ref(false)

  async function fetchAll(startDate: string, endDate: string, year: number, month: number) {
    loading.value = true
    try {
      const [loginRes, progressRes, chatRes, monthlyRes] = await Promise.all([
        adminStatsApi.getDailyLoginStats(startDate, endDate),
        adminStatsApi.getDailyProgressStats(startDate, endDate),
        adminStatsApi.getDailyChatStats(startDate, endDate),
        adminStatsApi.getMonthlyStats(year, month),
      ])
      loginStats.value = loginRes.data
      progressStats.value = progressRes.data
      chatStats.value = chatRes.data
      monthlyStats.value = monthlyRes.data
    } finally {
      loading.value = false
    }
  }

  async function fetchDaily(startDate: string, endDate: string) {
    loading.value = true
    try {
      const [loginRes, progressRes, chatRes] = await Promise.all([
        adminStatsApi.getDailyLoginStats(startDate, endDate),
        adminStatsApi.getDailyProgressStats(startDate, endDate),
        adminStatsApi.getDailyChatStats(startDate, endDate),
      ])
      loginStats.value = loginRes.data
      progressStats.value = progressRes.data
      chatStats.value = chatRes.data
    } finally {
      loading.value = false
    }
  }

  async function fetchMonthly(year: number, month: number) {
    const { data } = await adminStatsApi.getMonthlyStats(year, month)
    monthlyStats.value = data
  }

  return {
    loginStats,
    progressStats,
    chatStats,
    monthlyStats,
    loading,
    fetchAll,
    fetchDaily,
    fetchMonthly,
  }
})
