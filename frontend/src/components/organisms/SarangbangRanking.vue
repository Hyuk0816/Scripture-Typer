<script setup lang="ts">
import type { GroupRankingResponse } from '@/types/progress'

defineProps<{
  rankings: GroupRankingResponse[]
}>()

const medals = ['🥇', '🥈', '🥉']
</script>

<template>
  <div class="bg-white rounded-2xl shadow-sm border border-gray-100 p-5">
    <h3 class="text-base font-bold text-gray-800 mb-4">사랑방 간 랭킹</h3>

    <div v-if="rankings.length === 0" class="text-center py-8 text-gray-400">아직 랭킹 데이터가 없습니다</div>

    <div v-else class="divide-y divide-gray-50">
      <div
        v-for="entry in rankings"
        :key="entry.affiliationId"
        class="flex items-center justify-between py-3"
      >
        <div class="flex items-center gap-3">
          <span class="w-8 text-center text-lg">
            {{ entry.rank <= 3 ? medals[entry.rank - 1] : '' }}
            <span v-if="entry.rank > 3" class="text-sm text-gray-400">{{ entry.rank }}</span>
          </span>
          <div>
            <span class="text-sm font-medium text-gray-800">{{ entry.affiliationName }}</span>
            <span class="text-xs text-gray-400 ml-1">({{ entry.memberCount }}명)</span>
          </div>
        </div>
        <span class="text-sm font-semibold text-amber-600">{{ entry.totalCompletedChapters }}장</span>
      </div>
    </div>
  </div>
</template>
