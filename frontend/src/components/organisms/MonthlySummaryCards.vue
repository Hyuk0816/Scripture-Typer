<script setup lang="ts">
import type { MonthlyStatResponse } from '@/types/admin-stats'

defineProps<{
  data: MonthlyStatResponse | null
}>()
</script>

<template>
  <div v-if="data" class="grid grid-cols-2 lg:grid-cols-4 gap-3 sm:gap-4">
    <!-- Active Users -->
    <div class="rounded-xl bg-white border border-gray-100 p-4 sm:p-5 shadow-sm">
      <p class="text-xs sm:text-sm text-gray-500">활동 유저</p>
      <p class="mt-1 text-2xl sm:text-3xl font-bold text-amber-700">
        {{ data.totalActiveUsers }}
      </p>
      <p class="mt-1 text-xs text-gray-400">{{ data.year }}년 {{ data.month }}월</p>
    </div>

    <!-- Reading -->
    <div class="rounded-xl bg-white border border-gray-100 p-4 sm:p-5 shadow-sm">
      <p class="text-xs sm:text-sm text-gray-500">통독 완료</p>
      <p class="mt-1 text-2xl sm:text-3xl font-bold text-blue-600">
        {{ data.totalReadingCompleted }}
      </p>
      <p class="mt-1 text-xs text-gray-400">총 읽기 횟수</p>
    </div>

    <!-- Typing -->
    <div class="rounded-xl bg-white border border-gray-100 p-4 sm:p-5 shadow-sm">
      <p class="text-xs sm:text-sm text-gray-500">필사 완료</p>
      <p class="mt-1 text-2xl sm:text-3xl font-bold text-green-600">
        {{ data.totalTypingCompleted }}
      </p>
      <p class="mt-1 text-xs text-gray-400">총 필사 횟수</p>
    </div>

    <!-- Chat Questions -->
    <div class="rounded-xl bg-white border border-gray-100 p-4 sm:p-5 shadow-sm">
      <p class="text-xs sm:text-sm text-gray-500">AI 질문</p>
      <p class="mt-1 text-2xl sm:text-3xl font-bold text-violet-600">
        {{ data.totalChatQuestions }}
      </p>
      <p class="mt-1 text-xs text-gray-400">총 질문 수</p>
    </div>
  </div>
  <p v-else class="text-sm text-gray-400 py-8 text-center">데이터가 없습니다.</p>
</template>
