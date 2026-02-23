<script setup lang="ts">
import { computed, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useTypingStore } from '@/stores/typing'
import { storeToRefs } from 'pinia'
import VerseDisplay from '@/components/organisms/VerseDisplay.vue'
import TypingInput from '@/components/organisms/TypingInput.vue'
import VerseProgress from '@/components/molecules/VerseProgress.vue'
import CompletionModal from '@/components/organisms/CompletionModal.vue'

const route = useRoute()
const typingStore = useTypingStore()
const { bookName, chapter, verses, currentVerseIndex, loading, chapterCompleted } =
  storeToRefs(typingStore)

const routeBook = computed(() => route.params.book as string)
const routeChapter = computed(() => Number(route.params.chapter))

const currentVerse = computed(() => verses.value[currentVerseIndex.value])

watch(
  [routeBook, routeChapter],
  ([book, ch]) => {
    if (book && ch) {
      typingStore.fetchChapter(book, ch)
    }
  },
  { immediate: true },
)
</script>

<template>
  <div v-if="loading" class="flex-1 flex items-center justify-center">
    <div
      class="animate-spin rounded-full h-8 w-8 border-2 border-amber-700 border-t-transparent"
    />
  </div>

  <div
    v-else-if="!bookName || !chapter || verses.length === 0"
    class="flex-1 flex items-center justify-center text-gray-400"
  >
    사이드바에서 책과 장을 선택해주세요
  </div>

  <div v-else class="max-w-3xl mx-auto w-full">
    <div class="mb-6 flex items-baseline justify-between">
      <h2 class="text-xl font-semibold text-gray-800">
        {{ bookName }} {{ chapter }}장
      </h2>
      <VerseProgress />
    </div>

    <div class="bg-white rounded-2xl shadow-sm border border-gray-100 p-8 md:p-10">
      <VerseDisplay
        v-if="currentVerse"
        :verse="currentVerse.verse"
        :content="currentVerse.content"
      />
      <TypingInput />
    </div>

    <CompletionModal v-if="chapterCompleted" />
  </div>
</template>
