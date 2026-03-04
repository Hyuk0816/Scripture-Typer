<script setup lang="ts">
import type { DailyProgressStatResponse } from '@/types/admin-stats'

defineProps<{
  data: DailyProgressStatResponse[]
}>()
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
              <td class="py-2 pr-4 text-center font-semibold text-blue-600">
                {{ u.readingCount }}
              </td>
              <td class="py-2 text-center font-semibold text-green-600">
                {{ u.typingCount }}
              </td>
            </tr>
          </template>
        </tbody>
      </table>
    </div>
    <p v-else class="text-sm text-gray-400 py-8 text-center">데이터가 없습니다.</p>
  </div>
</template>
