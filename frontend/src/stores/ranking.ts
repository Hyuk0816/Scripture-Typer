import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import { rankingApi } from '@/utils/api'
import type {
  RankingEntryResponse,
  AffiliationRankingResponse,
  GroupRankingResponse,
  RankingMode,
} from '@/types/progress'

export const useRankingStore = defineStore('ranking', () => {
  const mode = ref<RankingMode>('TYPING')
  const overallRanking = ref<RankingEntryResponse[]>([])
  const affiliationRanking = ref<AffiliationRankingResponse | null>(null)
  const sarangbangRanking = ref<GroupRankingResponse[]>([])
  const monthlyRanking = ref<RankingEntryResponse[]>([])
  const loading = ref(false)

  const now = new Date()
  const selectedYear = ref(now.getFullYear())
  const selectedMonth = ref(now.getMonth() + 1)

  const isSarangbang = computed(() => affiliationRanking.value?.mainAffiliation === 'SARANGBANG')

  async function fetchOverallRanking(limit = 20) {
    try {
      const { data } = await rankingApi.getRanking(mode.value, limit)
      overallRanking.value = data
    } catch (e) {
      console.error('fetchOverallRanking failed:', e)
    }
  }

  async function fetchAffiliationRanking(limit = 20) {
    try {
      const { data } = await rankingApi.getMyAffiliationRanking(mode.value, limit)
      affiliationRanking.value = data
    } catch (e) {
      console.error('fetchAffiliationRanking failed:', e)
    }
  }

  async function fetchSarangbangRanking() {
    try {
      const { data } = await rankingApi.getSarangbangRanking(mode.value)
      sarangbangRanking.value = data
    } catch (e) {
      console.error('fetchSarangbangRanking failed:', e)
    }
  }

  async function fetchMonthlyRanking(limit = 20) {
    try {
      const { data } = await rankingApi.getMonthlyRanking(mode.value, selectedYear.value, selectedMonth.value, limit)
      monthlyRanking.value = data
    } catch (e) {
      console.error('fetchMonthlyRanking failed:', e)
    }
  }

  function setMonthlyPeriod(year: number, month: number) {
    selectedYear.value = year
    selectedMonth.value = month
    fetchMonthlyRanking()
  }

  async function fetchAll() {
    loading.value = true
    try {
      await Promise.allSettled([
        fetchOverallRanking(),
        fetchAffiliationRanking(),
        fetchMonthlyRanking(),
      ])
      if (affiliationRanking.value?.mainAffiliation === 'SARANGBANG') {
        await fetchSarangbangRanking()
      }
    } finally {
      loading.value = false
    }
  }

  function setMode(newMode: RankingMode) {
    mode.value = newMode
  }

  return {
    mode,
    overallRanking,
    affiliationRanking,
    sarangbangRanking,
    monthlyRanking,
    selectedYear,
    selectedMonth,
    isSarangbang,
    loading,
    fetchOverallRanking,
    fetchAffiliationRanking,
    fetchSarangbangRanking,
    fetchMonthlyRanking,
    fetchAll,
    setMode,
    setMonthlyPeriod,
  }
})
