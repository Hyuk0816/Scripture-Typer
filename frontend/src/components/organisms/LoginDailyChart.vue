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
import type { DailyLoginStatResponse } from '@/types/admin-stats'

ChartJS.register(CategoryScale, LinearScale, BarElement, Title, Tooltip, Legend)

const props = defineProps<{
  data: DailyLoginStatResponse[]
}>()

const chartData = computed(() => ({
  labels: props.data.map((d) => d.date),
  datasets: [
    {
      label: '일별 접속 수',
      data: props.data.map((d) => d.totalLogins),
      backgroundColor: 'rgba(217, 119, 6, 0.6)',
      borderColor: 'rgb(217, 119, 6)',
      borderWidth: 1,
      borderRadius: 4,
    },
  ],
}))

const chartOptions = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: { display: false },
  },
  scales: {
    y: {
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
            <th class="py-2 pr-4 font-medium">총 접속</th>
            <th class="py-2 font-medium">접속 유저</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="row in data" :key="row.date" class="border-b border-gray-50">
            <td class="py-2 pr-4 text-gray-700">{{ row.date }}</td>
            <td class="py-2 pr-4 font-semibold text-amber-700">{{ row.totalLogins }}</td>
            <td class="py-2 text-gray-600">
              <span v-for="(u, i) in row.users" :key="u.userId">
                {{ u.userName }}({{ u.loginCount }}){{ i < row.users.length - 1 ? ', ' : '' }}
              </span>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>
