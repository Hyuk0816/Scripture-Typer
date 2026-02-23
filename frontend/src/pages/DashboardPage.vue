<script setup lang="ts">
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useProgressStore } from '@/stores/progress'
import Spinner from '@/components/atoms/Spinner.vue'

const router = useRouter()
const progressStore = useProgressStore()

onMounted(() => {
  progressStore.fetchLatest()
  progressStore.fetchTopRanking(3)
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

      <!-- 필사 랭킹 Top 3 -->
      <div v-if="progressStore.topRanking.length > 0" class="bg-white rounded-xl shadow-sm border border-gray-100 p-6 mt-6">
        <h2 class="text-lg font-semibold text-gray-700 mb-4">필사 랭킹</h2>
        <div class="space-y-3">
          <div
            v-for="entry in progressStore.topRanking"
            :key="entry.userId"
            class="flex items-center gap-3 px-3 py-2 rounded-lg"
            :class="{
              'bg-amber-50': entry.rank === 1,
              'bg-gray-50': entry.rank !== 1,
            }"
          >
            <span class="text-2xl w-8 text-center">
              {{ entry.rank === 1 ? '🥇' : entry.rank === 2 ? '🥈' : '🥉' }}
            </span>
            <span class="flex-1 font-medium text-gray-800">{{ entry.name }}</span>
            <span class="text-sm text-gray-500">{{ entry.completedChapters }}장 완료</span>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>
