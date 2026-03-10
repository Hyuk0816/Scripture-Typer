<script setup lang="ts">
import type { AffiliationRankingResponse } from '@/types/progress'

defineProps<{
  data: AffiliationRankingResponse | null
}>()

const medals = ['🥇', '🥈', '🥉']
</script>

<template>
  <div class="bg-white rounded-2xl shadow-sm border border-gray-100 p-5">
    <h3 class="text-base font-bold text-gray-800 mb-1">내 소속 랭킹</h3>

    <div v-if="!data" class="text-center py-8 text-gray-400">소속이 지정되지 않았습니다</div>
    <template v-else>
      <p class="text-xs text-gray-500 mb-4">{{ data.affiliationName }}</p>

      <div v-if="data.myRank > 0" class="bg-amber-50 rounded-xl p-3 mb-4">
        <p class="text-sm text-amber-800">
          내 순위: <span class="font-bold">{{ data.myRank }}위</span>
          ({{ data.myCompletedChapters }}장 완료)
        </p>
      </div>

      <div v-if="data.rankings.length === 0" class="text-center py-4 text-gray-400 text-sm">
        아직 랭킹 데이터가 없습니다
      </div>

      <div v-else class="divide-y divide-gray-50">
        <div
          v-for="entry in data.rankings"
          :key="entry.userId"
          class="flex items-center justify-between py-2.5"
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
    </template>
  </div>
</template>
