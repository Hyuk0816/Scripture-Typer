import { ref } from 'vue'
import { defineStore } from 'pinia'
import type { VerseData } from '@/types/bible'
import { bibleApi, progressApi } from '@/utils/api'

export const useTypingStore = defineStore('typing', () => {
  const bookName = ref('')
  const chapter = ref(0)
  const verses = ref<VerseData[]>([])
  const currentVerseIndex = ref(0)
  const typedText = ref('')
  const isComposing = ref(false)
  const lastTypedVerse = ref(0)
  const readCount = ref(0)
  const totalVerses = ref(0)
  const loading = ref(false)
  const chapterCompleted = ref(false)

  async function fetchChapter(book: string, ch: number) {
    loading.value = true
    chapterCompleted.value = false
    bookName.value = book
    chapter.value = ch

    try {
      const [chapterRes, progressRes] = await Promise.all([
        bibleApi.getChapter(book, ch),
        progressApi.getTypingProgress(book, ch).catch(() => null),
      ])

      verses.value = chapterRes.data.verses
      totalVerses.value = chapterRes.data.verses.length

      if (progressRes) {
        lastTypedVerse.value = progressRes.data.lastTypedVerse
        readCount.value = progressRes.data.readCount
        // Resume from lastTypedVerse
        const startIndex = Math.min(lastTypedVerse.value, verses.value.length - 1)
        currentVerseIndex.value = lastTypedVerse.value > 0 ? startIndex : 0
      } else {
        lastTypedVerse.value = 0
        readCount.value = 0
        currentVerseIndex.value = 0
      }
      typedText.value = ''
    } catch {
      // silent fail
    } finally {
      loading.value = false
    }
  }

  async function saveProgress(verse: number) {
    try {
      await progressApi.saveTypingProgress({
        bookName: bookName.value,
        chapter: chapter.value,
        lastTypedVerse: verse,
      })
      lastTypedVerse.value = verse
    } catch {
      // non-critical
    }
  }

  function completeVerse() {
    const current = verses.value[currentVerseIndex.value]
    if (!current) return

    if (currentVerseIndex.value < verses.value.length - 1) {
      lastTypedVerse.value = current.verse
      currentVerseIndex.value++
      typedText.value = ''
    } else {
      lastTypedVerse.value = current.verse
      chapterCompleted.value = true
    }
  }

  async function completeChapter() {
    try {
      await progressApi.completeTyping({
        bookName: bookName.value,
        chapter: chapter.value,
      })
      readCount.value++
    } catch {
      // silent fail
    }
  }

  function setTypedText(text: string) {
    typedText.value = text
  }

  function setIsComposing(composing: boolean) {
    isComposing.value = composing
  }

  function reset() {
    bookName.value = ''
    chapter.value = 0
    verses.value = []
    currentVerseIndex.value = 0
    typedText.value = ''
    isComposing.value = false
    lastTypedVerse.value = 0
    readCount.value = 0
    totalVerses.value = 0
    loading.value = false
    chapterCompleted.value = false
  }

  return {
    bookName,
    chapter,
    verses,
    currentVerseIndex,
    typedText,
    isComposing,
    lastTypedVerse,
    readCount,
    totalVerses,
    loading,
    chapterCompleted,
    fetchChapter,
    saveProgress,
    completeVerse,
    completeChapter,
    setTypedText,
    setIsComposing,
    reset,
  }
})
