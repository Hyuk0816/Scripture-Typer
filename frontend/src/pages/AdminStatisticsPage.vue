<script setup lang="ts">
import { ref, onMounted } from 'vue'
import AdminTemplate from '@/components/templates/AdminTemplate.vue'
import DateRangePicker from '@/components/molecules/DateRangePicker.vue'
import MonthlySummaryCards from '@/components/organisms/MonthlySummaryCards.vue'
import LoginDailyChart from '@/components/organisms/LoginDailyChart.vue'
import ProgressDailyTable from '@/components/organisms/ProgressDailyTable.vue'
import ChatDailyChart from '@/components/organisms/ChatDailyChart.vue'
import Spinner from '@/components/atoms/Spinner.vue'
import { useAdminStatsStore } from '@/stores/adminStats'

const store = useAdminStatsStore()

const today = new Date()
const firstDay = new Date(today.getFullYear(), today.getMonth(), 1)

function formatDate(d: Date): string {
  return d.toISOString().slice(0, 10)
}

const startDate = ref(formatDate(firstDay))
const endDate = ref(formatDate(today))

async function fetchData() {
  const d = new Date(startDate.value)
  await store.fetchAll(startDate.value, endDate.value, d.getFullYear(), d.getMonth() + 1)
}

onMounted(fetchData)
</script>

<template>
  <AdminTemplate>
    <div class="space-y-6 sm:space-y-8">
      <!-- Header -->
      <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3">
        <h2 class="text-lg sm:text-xl font-bold text-gray-800">통계 대시보드</h2>
        <div class="flex gap-2">
          <router-link
            to="/admin/users"
            class="rounded-lg border border-gray-200 px-3 py-1.5 text-sm text-gray-600 hover:bg-gray-50 transition-colors"
          >
            회원 관리
          </router-link>
          <router-link
            to="/admin/affiliations"
            class="rounded-lg border border-gray-200 px-3 py-1.5 text-sm text-gray-600 hover:bg-gray-50 transition-colors"
          >
            소속 관리
          </router-link>
          <router-link
            to="/admin/stats"
            class="rounded-lg bg-amber-600 px-3 py-1.5 text-sm font-medium text-white"
          >
            통계
          </router-link>
        </div>
      </div>

      <!-- Date Range Picker -->
      <DateRangePicker
        v-model:start-date="startDate"
        v-model:end-date="endDate"
        @search="fetchData"
      />

      <!-- Loading -->
      <div v-if="store.loading" class="flex justify-center py-12">
        <Spinner size="lg" />
      </div>

      <template v-else>
        <!-- Monthly Summary -->
        <section>
          <h3 class="text-base sm:text-lg font-semibold text-gray-700 mb-3">월별 요약</h3>
          <MonthlySummaryCards :data="store.monthlyStats" />
        </section>

        <!-- Login Daily -->
        <section class="rounded-xl bg-white border border-gray-100 p-4 sm:p-6 shadow-sm">
          <h3 class="text-base sm:text-lg font-semibold text-gray-700 mb-4">일별 접속 현황</h3>
          <LoginDailyChart :data="store.loginStats" />
        </section>

        <!-- Progress Daily -->
        <section class="rounded-xl bg-white border border-gray-100 p-4 sm:p-6 shadow-sm">
          <h3 class="text-base sm:text-lg font-semibold text-gray-700 mb-4">
            일별 통독/필사 현황
          </h3>
          <ProgressDailyTable :data="store.progressStats" />
        </section>

        <!-- Chat Daily -->
        <section class="rounded-xl bg-white border border-gray-100 p-4 sm:p-6 shadow-sm">
          <h3 class="text-base sm:text-lg font-semibold text-gray-700 mb-4">
            일별 AI 질문 현황
          </h3>
          <ChatDailyChart :data="store.chatStats" />
        </section>
      </template>
    </div>
  </AdminTemplate>
</template>
