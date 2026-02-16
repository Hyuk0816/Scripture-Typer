import { ref } from 'vue'
import { defineStore } from 'pinia'
import type { BookSummary } from '@/types/bible'
import { bibleApi } from '@/utils/api'

export const useBibleStore = defineStore('bible', () => {
  const oldTestament = ref<BookSummary[]>([])
  const newTestament = ref<BookSummary[]>([])
  const loading = ref(false)
  const error = ref<string | null>(null)

  async function fetchBooks() {
    loading.value = true
    error.value = null
    try {
      const { data } = await bibleApi.getBooks()
      oldTestament.value = data.oldTestament
      newTestament.value = data.newTestament
    } catch (err) {
      error.value = (err as Error).message
    } finally {
      loading.value = false
    }
  }

  return {
    oldTestament,
    newTestament,
    loading,
    error,
    fetchBooks,
  }
})