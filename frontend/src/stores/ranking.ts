import { ref } from 'vue'
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
  const loading = ref(false)

  async function fetchOverallRanking(limit = 20) {
    loading.value = true
    try {
      const { data } = await rankingApi.getRanking(mode.value, limit)
      overallRanking.value = data
    } catch (e) {
      console.error('fetchOverallRanking failed:', e)
    } finally {
      loading.value = false
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

  async function fetchAll() {
    loading.value = true
    try {
      await Promise.allSettled([
        fetchOverallRanking(),
        fetchAffiliationRanking(),
        fetchSarangbangRanking(),
      ])
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
    loading,
    fetchOverallRanking,
    fetchAffiliationRanking,
    fetchSarangbangRanking,
    fetchAll,
    setMode,
  }
})
