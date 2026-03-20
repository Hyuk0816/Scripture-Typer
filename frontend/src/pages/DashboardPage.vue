<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useProgressStore } from '@/stores/progress'
import { bibleApi } from '@/utils/api'
import Spinner from '@/components/atoms/Spinner.vue'

const router = useRouter()
const progressStore = useProgressStore()

const dailyVerse = ref<{ bookName: string; chapter: number; verse: number; content: string } | null>(null)

function todayString(): string {
  const d = new Date()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${d.getFullYear()}.${m}.${day}`
}

onMounted(async () => {
  progressStore.fetchLatest()
  try {
    const { data } = await bibleApi.getDailyVerse()
    dailyVerse.value = data
  } catch {
    // silent
  }
})

function navigateTyping() {
  const p = progressStore.latestTyping
  if (p) router.push(`/typing/${p.bookName}/${p.chapter}`)
}

function navigateReading() {
  const p = progressStore.latestReading
  if (p) router.push(`/reading/${p.bookName}/${p.chapter}`)
}
</script>

<template>
  <div class="max-w-2xl mx-auto">
    <!-- Header -->
    <div class="text-center mb-10 mt-8">
      <h1 class="text-3xl font-bold text-gray-800 mb-3">Scripture Typer</h1>
      <p class="text-gray-500">성경을 타이핑하며 말씀을 묵상하세요</p>
    </div>

    <!-- 오늘의 말씀 -->
    <div v-if="dailyVerse" class="bg-white rounded-xl shadow-sm border border-gray-100 p-6 mb-4">
      <div class="flex items-center justify-between mb-3">
        <h2 class="text-lg font-semibold text-gray-700">오늘의 말씀</h2>
        <span class="text-xs text-gray-400">{{ todayString() }}</span>
      </div>
      <p class="text-sm text-amber-700 font-medium mb-2">
        {{ dailyVerse.bookName }} {{ dailyVerse.chapter }}장 {{ dailyVerse.verse }}절
      </p>
      <p class="text-gray-700 leading-relaxed text-[15px]">
        {{ dailyVerse.content }}
      </p>
      <div class="flex gap-2 mt-4 justify-end">
        <button
          @click="router.push(`/reading/${dailyVerse.bookName}/${dailyVerse.chapter}`)"
          class="px-4 py-2 text-xs font-medium text-green-700 bg-green-50 rounded-lg hover:bg-green-100 transition-colors"
        >
          통독하러 가기
        </button>
        <button
          @click="router.push(`/typing/${dailyVerse.bookName}/${dailyVerse.chapter}`)"
          class="px-4 py-2 text-xs font-medium text-amber-700 bg-amber-50 rounded-lg hover:bg-amber-100 transition-colors"
        >
          필사하러 가기
        </button>
      </div>
    </div>

    <!-- Loading -->
    <div v-if="progressStore.loadingLatest" class="flex justify-center py-12">
      <Spinner />
    </div>

    <template v-else>
      <!-- 최근 필사 카드 -->
      <div v-if="progressStore.latestTyping" class="bg-white rounded-xl shadow-sm border border-gray-100 p-8 mb-4">
        <div class="flex items-start gap-4">
          <div class="text-4xl">✍️</div>
          <div class="flex-1">
            <h2 class="text-lg font-semibold text-gray-700 mb-1">최근 필사</h2>
            <p class="text-gray-600">
              {{ progressStore.latestTyping.bookName }} {{ progressStore.latestTyping.chapter }}장
              <span class="text-gray-400">
                ({{ progressStore.latestTyping.lastTypedVerse }}/{{ progressStore.latestTyping.totalVerses }}절)
              </span>
            </p>

            <div class="mt-3 bg-gray-100 rounded-full h-2 overflow-hidden">
              <div
                class="bg-amber-600 h-full rounded-full transition-all duration-500"
                :style="{ width: `${Math.round((progressStore.latestTyping.lastTypedVerse / progressStore.latestTyping.totalVerses) * 100)}%` }"
              />
            </div>

            <button
              @click="navigateTyping"
              class="mt-4 px-6 py-2.5 bg-gradient-to-r from-amber-600 to-amber-700 text-white rounded-lg hover:from-amber-700 hover:to-amber-800 transition-all text-sm font-medium shadow-md hover:shadow-lg"
            >
              이어서 필사하기
            </button>
          </div>
        </div>
      </div>

      <!-- 최근 통독 카드 -->
      <div v-if="progressStore.latestReading" class="bg-white rounded-xl shadow-sm border border-gray-100 p-8 mb-4">
        <div class="flex items-start gap-4">
          <div class="text-4xl">📖</div>
          <div class="flex-1">
            <h2 class="text-lg font-semibold text-gray-700 mb-1">최근 통독</h2>
            <p class="text-gray-600">
              {{ progressStore.latestReading.bookName }} {{ progressStore.latestReading.chapter }}장
              <span v-if="progressStore.latestReading.readCount > 0" class="text-green-600 text-sm ml-1">
                {{ progressStore.latestReading.readCount }}회독
              </span>
            </p>

            <button
              @click="navigateReading"
              class="mt-4 px-6 py-2.5 bg-gradient-to-r from-green-600 to-green-700 text-white rounded-lg hover:from-green-700 hover:to-green-800 transition-all text-sm font-medium shadow-md hover:shadow-lg"
            >
              이어서 통독하기
            </button>
          </div>
        </div>
      </div>

      <!-- 빈 상태 -->
      <div v-if="!progressStore.latestTyping && !progressStore.latestReading" class="bg-white rounded-xl shadow-sm border border-gray-100 p-8 text-center">
        <div class="text-4xl mb-4">📖</div>
        <h2 class="text-lg font-semibold text-gray-700 mb-2">처음 오셨군요!</h2>
        <p class="text-gray-500 text-sm">사이드바에서 원하는 책과 장을 선택하여 시작하세요.</p>
      </div>
    </template>
  </div>
</template>
