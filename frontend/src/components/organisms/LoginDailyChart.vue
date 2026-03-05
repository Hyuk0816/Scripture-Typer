<script setup lang="ts">
import { computed, ref, watch } from 'vue'
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
import PageNavigator from '@/components/molecules/PageNavigator.vue'
import LoginUserModal from '@/components/molecules/LoginUserModal.vue'
import type { DailyLoginStatResponse, UserLoginDetail } from '@/types/admin-stats'

ChartJS.register(CategoryScale, LinearScale, BarElement, Title, Tooltip, Legend)

const PER_PAGE = 5

const props = defineProps<{
  data: DailyLoginStatResponse[]
}>()

const currentPage = ref(1)
const totalPages = computed(() => Math.max(1, Math.ceil(props.data.length / PER_PAGE)))
const pagedData = computed(() => {
  const start = (currentPage.value - 1) * PER_PAGE
  return props.data.slice(start, start + PER_PAGE)
})

watch(() => props.data, () => { currentPage.value = 1 })

const MAX_VISIBLE = 3
const modalDate = ref('')
const modalUsers = ref<UserLoginDetail[]>([])
const showModal = ref(false)

function openModal(date: string, users: UserLoginDetail[]) {
  modalDate.value = date
  modalUsers.value = users
  showModal.value = true
}

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
          <tr v-for="row in pagedData" :key="row.date" class="border-b border-gray-50">
            <td class="py-2 pr-4 text-gray-700">{{ row.date }}</td>
            <td class="py-2 pr-4 font-semibold text-amber-700">{{ row.totalLogins }}</td>
            <td class="py-2 text-gray-600">
              <span v-for="(u, i) in row.users.slice(0, MAX_VISIBLE)" :key="u.userId">
                {{ u.userName }}({{ u.loginCount }}){{ i < Math.min(row.users.length, MAX_VISIBLE) - 1 ? ', ' : '' }}
              </span>
              <button
                v-if="row.users.length > MAX_VISIBLE"
                class="ml-1 text-xs text-amber-700 hover:text-amber-900 font-medium"
                @click="openModal(row.date, row.users)"
              >
                ... 외 {{ row.users.length - MAX_VISIBLE }}명
              </button>
            </td>
          </tr>
        </tbody>
      </table>
      <PageNavigator
        v-if="totalPages > 1"
        :current-page="currentPage"
        :total-pages="totalPages"
        class="py-4"
        @prev="currentPage--"
        @next="currentPage++"
        @go-to="(p) => currentPage = p"
      />
    </div>

    <LoginUserModal
      v-if="showModal"
      :date="modalDate"
      :users="modalUsers"
      @close="showModal = false"
    />
  </div>
</template>
