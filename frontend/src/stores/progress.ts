import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import { progressApi, rankingApi } from '@/utils/api'
import type {
  ReadingProgressResponse,
  TypingProgressResponse,
  RankingEntryResponse,
} from '@/types/progress'

export const useProgressStore = defineStore('progress', () => {
  // --- State ---
  const latestTyping = ref<TypingProgressResponse | null>(null)
  const latestReading = ref<ReadingProgressResponse | null>(null)
  const allReading = ref<ReadingProgressResponse[]>([])
  const allTyping = ref<TypingProgressResponse[]>([])
  const topRanking = ref<RankingEntryResponse[]>([])
  const loadingLatest = ref(false)
  const loadingAll = ref(false)
  const loadingRanking = ref(false)

  // --- Actions ---

  async function fetchLatest() {
    loadingLatest.value = true
    try {
      const results = await Promise.allSettled([
        progressApi.getLatestTypingProgress(),
        progressApi.getLatestReadingProgress(),
      ])

      if (results[0].status === 'fulfilled') {
        latestTyping.value = results[0].value.data
      }
      if (results[1].status === 'fulfilled') {
        latestReading.value = results[1].value.data
      }
    } finally {
      loadingLatest.value = false
    }
  }

  async function fetchAll() {
    loadingAll.value = true
    try {
      const [readingRes, typingRes] = await Promise.all([
        progressApi.getAllReadingProgress(),
        progressApi.getAllTypingProgress(),
      ])
      allReading.value = readingRes.data
      allTyping.value = typingRes.data
    } finally {
      loadingAll.value = false
    }
  }

  async function fetchTopRanking(limit = 3) {
    loadingRanking.value = true
    try {
      const { data } = await rankingApi.getTypingRanking(limit)
      topRanking.value = data
    } catch (e) {
      console.error('fetchTopRanking failed:', e)
    } finally {
      loadingRanking.value = false
    }
  }

  // --- Computed stats ---

  const readingStats = computed(() => {
    let inProgress = 0
    let completed = 0
    let totalReadCount = 0

    for (const p of allReading.value) {
      if (p.readCount >= 1) {
        completed++
        totalReadCount += p.readCount
      } else if (p.lastReadVerse > 0) {
        inProgress++
      }
    }

    return { inProgress, completed, totalReadCount }
  })

  const typingStats = computed(() => {
    let inProgress = 0
    let completed = 0
    let totalReadCount = 0

    for (const p of allTyping.value) {
      if (p.readCount >= 1) {
        completed++
        totalReadCount += p.readCount
      } else if (p.lastTypedVerse > 0) {
        inProgress++
      }
    }

    return { inProgress, completed, totalReadCount }
  })

  return {
    latestTyping,
    latestReading,
    allReading,
    allTyping,
    topRanking,
    loadingLatest,
    loadingAll,
    loadingRanking,
    fetchLatest,
    fetchAll,
    fetchTopRanking,
    readingStats,
    typingStats,
  }
})
