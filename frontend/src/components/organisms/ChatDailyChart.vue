<script setup lang="ts">
import { computed } from 'vue'
import { Bar } from 'vue-chartjs'
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  BarElement,
  Title,
  Tooltip,
  Legend,
} from 'chart.js'
import type { DailyChatStatResponse } from '@/types/admin-stats'

ChartJS.register(CategoryScale, LinearScale, BarElement, Title, Tooltip, Legend)

const props = defineProps<{
  data: DailyChatStatResponse[]
}>()

const COLORS = [
  'rgba(59, 130, 246, 0.7)',
  'rgba(16, 185, 129, 0.7)',
  'rgba(245, 158, 11, 0.7)',
  'rgba(239, 68, 68, 0.7)',
  'rgba(139, 92, 246, 0.7)',
  'rgba(236, 72, 153, 0.7)',
  'rgba(20, 184, 166, 0.7)',
  'rgba(249, 115, 22, 0.7)',
]

const chartData = computed(() => {
  const labels = props.data.map((d) => d.date)

  // Collect all unique users
  const userMap = new Map<number, string>()
  for (const day of props.data) {
    for (const u of day.users) {
      userMap.set(u.userId, u.userName)
    }
  }

  const users = Array.from(userMap.entries())
  const datasets = users.map(([userId, userName], idx) => ({
    label: userName,
    data: props.data.map(
      (d) => d.users.find((u) => u.userId === userId)?.questionCount ?? 0,
    ),
    backgroundColor: COLORS[idx % COLORS.length],
    borderRadius: 4,
  }))

  return { labels, datasets }
})

const chartOptions = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: { position: 'bottom' as const },
  },
  scales: {
    x: { stacked: true },
    y: {
      stacked: true,
      beginAtZero: true,
      ticks: { stepSize: 1 },
    },
  },
}
</script>

<template>
  <div class="space-y-4">
    <div v-if="data.length > 0" class="h-64">
      <Bar :data="chartData" :options="chartOptions" />
    </div>
    <p v-else class="text-sm text-gray-400 py-8 text-center">데이터가 없습니다.</p>

    <!-- Detail table -->
    <div v-if="data.length > 0" class="overflow-x-auto">
      <table class="w-full text-sm">
        <thead>
          <tr class="border-b border-gray-200 text-left text-gray-500">
            <th class="py-2 pr-4 font-medium">날짜</th>
            <th class="py-2 pr-4 font-medium">이름</th>
            <th class="py-2 font-medium text-center">AI 질문 수</th>
          </tr>
        </thead>
        <tbody>
          <template v-for="row in data" :key="row.date">
            <tr
              v-for="(u, i) in row.users"
              :key="`${row.date}-${u.userId}`"
              class="border-b border-gray-50"
            >
              <td class="py-2 pr-4 text-gray-700">
                {{ i === 0 ? row.date : '' }}
              </td>
              <td class="py-2 pr-4 text-gray-700">{{ u.userName }}</td>
              <td class="py-2 text-center font-semibold text-violet-600">
                {{ u.questionCount }}
              </td>
            </tr>
          </template>
        </tbody>
      </table>
    </div>
  </div>
</template>
