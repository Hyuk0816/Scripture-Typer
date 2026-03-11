<script setup lang="ts">
import { onMounted, watch } from 'vue'
import { useRankingStore } from '@/stores/ranking'
import RankingModeTab from '@/components/molecules/RankingModeTab.vue'
import OverallRanking from '@/components/organisms/OverallRanking.vue'
import MonthlyRanking from '@/components/organisms/MonthlyRanking.vue'
import AffiliationRanking from '@/components/organisms/AffiliationRanking.vue'
import SarangbangRanking from '@/components/organisms/SarangbangRanking.vue'
import Spinner from '@/components/atoms/Spinner.vue'

const rankingStore = useRankingStore()

onMounted(() => {
  rankingStore.fetchAll()
})

watch(() => rankingStore.mode, () => {
  rankingStore.fetchAll()
})

function onPeriodUpdate(period: { year: number; month: number }) {
  rankingStore.setMonthlyPeriod(period.year, period.month)
}
</script>

<template>
  <div class="max-w-2xl mx-auto px-4 py-6 space-y-6">
    <div class="flex items-center justify-between">
      <h1 class="text-xl font-bold text-gray-800">랭킹</h1>
    </div>

    <RankingModeTab
      :model-value="rankingStore.mode"
      @update:model-value="rankingStore.setMode"
    />

    <div v-if="rankingStore.loading" class="flex justify-center py-12">
      <Spinner />
    </div>

    <template v-else>
      <OverallRanking
        :rankings="rankingStore.overallRanking"
        :loading="false"
      />

      <MonthlyRanking
        :rankings="rankingStore.monthlyRanking"
        :year="rankingStore.selectedYear"
        :month="rankingStore.selectedMonth"
        @update:period="onPeriodUpdate"
      />

      <AffiliationRanking
        :data="rankingStore.affiliationRanking"
      />

      <SarangbangRanking
        v-if="rankingStore.isSarangbang"
        :rankings="rankingStore.sarangbangRanking"
      />
    </template>
  </div>
</template>
