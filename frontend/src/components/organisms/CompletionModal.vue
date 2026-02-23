<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useTypingStore } from '@/stores/typing'
import { storeToRefs } from 'pinia'
import BadgeDisplay from '@/components/atoms/BadgeDisplay.vue'

const router = useRouter()
const typingStore = useTypingStore()
const { bookName, chapter, readCount } = storeToRefs(typingStore)

const newReadCount = computed(() => readCount.value + 1)

async function handleGoHome() {
  await typingStore.completeChapter()
  router.push('/')
}

async function handleNextChapter() {
  const nextCh = chapter.value + 1
  const book = bookName.value
  await typingStore.completeChapter()
  await typingStore.fetchChapter(book, nextCh)
  router.push(`/typing/${book}/${nextCh}`)
}
</script>

<template>
  <div class="fixed inset-0 bg-black/40 flex items-center justify-center z-50 backdrop-blur-sm animate-fadeIn">
    <div class="bg-white rounded-2xl shadow-2xl p-8 max-w-md w-full mx-4 text-center animate-scaleIn">
      <BadgeDisplay :read-count="newReadCount" />

      <h2 class="text-xl font-bold text-gray-800 mt-4 mb-2">
        {{ bookName }} {{ chapter }}장 완료!
      </h2>
      <p class="text-gray-500 mb-6">
        {{ newReadCount }}회독을 완료하셨습니다
      </p>

      <div class="flex gap-3 justify-center">
        <button
          @click="handleGoHome"
          class="px-5 py-2.5 bg-gray-100 text-gray-700 rounded-lg hover:bg-gray-200 transition-colors text-sm"
        >
          홈으로
        </button>
        <button
          @click="handleNextChapter"
          class="px-5 py-2.5 bg-gradient-to-r from-amber-600 to-amber-700 text-white rounded-lg hover:from-amber-700 hover:to-amber-800 transition-all text-sm shadow-md hover:shadow-lg"
        >
          다음 장으로
        </button>
      </div>
    </div>
  </div>
</template>
