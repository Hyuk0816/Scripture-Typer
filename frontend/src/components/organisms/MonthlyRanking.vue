<script setup lang="ts">
import type { RankingEntryResponse } from '@/types/progress'

const props = defineProps<{
  rankings: RankingEntryResponse[]
  year: number
  month: number
}>()

const emit = defineEmits<{
  'update:period': [value: { year: number; month: number }]
}>()

const medals = ['🥇', '🥈', '🥉']

const now = new Date()
const currentYear = now.getFullYear()
const currentMonth = now.getMonth() + 1

const prevDate = new Date(currentYear, currentMonth - 2, 1)
const prevYear = prevDate.getFullYear()
const prevMonth = prevDate.getMonth() + 1

const periods = [
  { year: currentYear, month: currentMonth, label: `${currentMonth}월` },
  { year: prevYear, month: prevMonth, label: `${prevMonth}월` },
]

function isSelected(p: { year: number; month: number }) {
  return p.year === props.year && p.month === props.month
}
</script>

<template>
  <div class="bg-white rounded-2xl shadow-sm border border-gray-100 p-5">
    <h3 class="text-base font-bold text-gray-800 mb-4">월간 랭킹</h3>

    <div class="flex bg-gray-100 rounded-lg p-1 mb-4">
      <button
        v-for="p in periods"
        :key="`${p.year}-${p.month}`"
        class="flex-1 px-4 py-2 text-sm font-medium rounded-md transition-all"
        :class="isSelected(p)
          ? 'bg-white text-amber-700 shadow-sm'
          : 'text-gray-500 hover:text-gray-700'"
        @click="emit('update:period', { year: p.year, month: p.month })"
      >
        {{ p.label }}
      </button>
    </div>

    <div v-if="rankings.length === 0" class="text-center py-8 text-gray-400">아직 랭킹 데이터가 없습니다</div>

    <div v-else class="divide-y divide-gray-50">
      <div
        v-for="entry in rankings"
        :key="entry.userId"
        class="flex items-center justify-between py-3"
      >
        <div class="flex items-center gap-3">
          <span class="w-8 text-center text-lg">
            {{ entry.rank <= 3 ? medals[entry.rank - 1] : '' }}
            <span v-if="entry.rank > 3" class="text-sm text-gray-400">{{ entry.rank }}</span>
          </span>
          <span class="text-sm font-medium text-gray-800">{{ entry.name }}</span>
        </div>
        <span class="text-sm font-semibold text-amber-600">{{ entry.completedChapters }}장</span>
      </div>
    </div>
  </div>
</template>
