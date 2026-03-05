<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import PageNavigator from '@/components/molecules/PageNavigator.vue'
import type { DailyProgressStatResponse } from '@/types/admin-stats'

const PER_PAGE = 5

const props = defineProps<{
  data: DailyProgressStatResponse[]
}>()

interface FlatRow {
  date: string
  showDate: boolean
  userId: number
  userName: string
  readingCount: number
  typingCount: number
}

const flatRows = computed<FlatRow[]>(() =>
  props.data.flatMap((row) =>
    row.users.map((u, i) => ({
      date: row.date,
      showDate: i === 0,
      userId: u.userId,
      userName: u.userName,
      readingCount: u.readingCount,
      typingCount: u.typingCount,
    }))
  )
)

const currentPage = ref(1)
const totalPages = computed(() => Math.max(1, Math.ceil(flatRows.value.length / PER_PAGE)))
const pagedRows = computed(() => {
  const start = (currentPage.value - 1) * PER_PAGE
  return flatRows.value.slice(start, start + PER_PAGE)
})

watch(() => props.data, () => { currentPage.value = 1 })
</script>

<template>
  <div>
    <div v-if="data.length > 0" class="overflow-x-auto">
      <table class="w-full text-sm">
        <thead>
          <tr class="border-b border-gray-200 text-left text-gray-500">
            <th class="py-2 pr-4 font-medium">날짜</th>
            <th class="py-2 pr-4 font-medium">이름</th>
            <th class="py-2 pr-4 font-medium text-center">통독</th>
            <th class="py-2 font-medium text-center">필사</th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="row in pagedRows"
            :key="`${row.date}-${row.userId}`"
            class="border-b border-gray-50"
          >
            <td class="py-2 pr-4 text-gray-700">
              {{ row.showDate ? row.date : '' }}
            </td>
            <td class="py-2 pr-4 text-gray-700">{{ row.userName }}</td>
            <td class="py-2 pr-4 text-center font-semibold text-blue-600">
              {{ row.readingCount }}
            </td>
            <td class="py-2 text-center font-semibold text-green-600">
              {{ row.typingCount }}
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
    <p v-else class="text-sm text-gray-400 py-8 text-center">데이터가 없습니다.</p>
  </div>
</template>
