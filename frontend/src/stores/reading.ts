import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import type { VerseData } from '@/types/bible'
import type { ReadingProgressResponse } from '@/types/progress'
import { bibleApi, progressApi } from '@/utils/api'

export type PerPage = 1 | 5 | 10

export const useReadingStore = defineStore('reading', () => {
  const verses = ref<VerseData[]>([])
  const bookName = ref('')
  const chapter = ref(0)
  const loading = ref(false)
  const error = ref<string | null>(null)
  const saving = ref(false)
  const completing = ref(false)

  // Progress
  const lastReadVerse = ref(0)
  const readCount = ref(0)

  // Pagination (desktop)
  const perPage = ref<PerPage>(5)
  const currentPage = ref(1)

  const totalPages = computed(() =>
    Math.max(1, Math.ceil(verses.value.length / perPage.value))
  )

  const pagedVerses = computed(() => {
    const start = (currentPage.value - 1) * perPage.value
    return verses.value.slice(start, start + perPage.value)
  })

  const isLastPage = computed(() => currentPage.value >= totalPages.value)

  async function fetchChapter(book: string, ch: number) {
    loading.value = true
    error.value = null
    bookName.value = book
    chapter.value = ch
    currentPage.value = 1

    try {
      const [chapterRes, progressRes] = await Promise.all([
        bibleApi.getChapter(book, ch),
        progressApi.getReadingProgress(book, ch).catch(() => null),
      ])

      verses.value = chapterRes.data.verses
      if (progressRes) {
        lastReadVerse.value = progressRes.data.lastReadVerse
        readCount.value = progressRes.data.readCount
      } else {
        lastReadVerse.value = 0
        readCount.value = 0
      }
    } catch (err) {
      error.value = (err as Error).message
    } finally {
      loading.value = false
    }

    await saveCurrentPageProgress()
  }

  async function saveCurrentPageProgress() {
    if (verses.value.length === 0) return
    const lastVerseOnPage = pagedVerses.value[pagedVerses.value.length - 1]
    if (lastVerseOnPage && lastVerseOnPage.verse > lastReadVerse.value) {
      await saveProgress(lastVerseOnPage.verse)
    }
  }

  async function saveProgress(verseNumber: number) {
    if (saving.value) return
    saving.value = true
    try {
      await progressApi.saveReadingProgress({
        bookName: bookName.value,
        chapter: chapter.value,
        lastReadVerse: verseNumber,
      })
      lastReadVerse.value = verseNumber
    } catch {
      // silent fail - progress save is non-critical
    } finally {
      saving.value = false
    }
  }

  async function completeChapter() {
    if (completing.value) return
    completing.value = true
    try {
      await progressApi.completeReading({
        bookName: bookName.value,
        chapter: chapter.value,
      })
      readCount.value += 1
      lastReadVerse.value = verses.value.length
    } catch (err) {
      error.value = (err as Error).message
    } finally {
      completing.value = false
    }
  }

  function setPerPage(value: PerPage) {
    perPage.value = value
    currentPage.value = 1
    saveCurrentPageProgress()
  }

  function nextPage() {
    if (currentPage.value < totalPages.value) {
      currentPage.value++
      saveCurrentPageProgress()
    }
  }

  function prevPage() {
    if (currentPage.value > 1) {
      currentPage.value--
      saveCurrentPageProgress()
    }
  }

  function goToPage(page: number) {
    if (page >= 1 && page <= totalPages.value) {
      currentPage.value = page
      saveCurrentPageProgress()
    }
  }

  return {
    verses,
    bookName,
    chapter,
    loading,
    error,
    saving,
    completing,
    lastReadVerse,
    readCount,
    perPage,
    currentPage,
    totalPages,
    pagedVerses,
    isLastPage,
    fetchChapter,
    saveProgress,
    completeChapter,
    setPerPage,
    nextPage,
    prevPage,
    goToPage,
  }
})