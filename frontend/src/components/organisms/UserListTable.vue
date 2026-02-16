<script setup lang="ts">
import { ref } from 'vue'
import StatusBadge from '@/components/molecules/StatusBadge.vue'
import Spinner from '@/components/atoms/Spinner.vue'
import type { UserListResponse } from '@/types/auth'
import { adminApi } from '@/utils/api'

defineProps<{
  users: UserListResponse[]
  loading: boolean
}>()

const emit = defineEmits<{
  refresh: []
}>()

const actionLoading = ref<number | null>(null)

async function handleActivate(userId: number) {
  actionLoading.value = userId
  try {
    await adminApi.activateUser(userId)
    emit('refresh')
  } finally {
    actionLoading.value = null
  }
}

async function handleDeactivate(userId: number) {
  actionLoading.value = userId
  try {
    await adminApi.deactivateUser(userId)
    emit('refresh')
  } finally {
    actionLoading.value = null
  }
}

function formatDate(dateStr: string): string {
  const date = new Date(dateStr)
  return date.toLocaleDateString('ko-KR', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  })
}
</script>

<template>
  <div class="bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden">
    <!-- Loading -->
    <div v-if="loading" class="flex justify-center py-12">
      <Spinner />
    </div>

    <!-- Empty -->
    <div v-else-if="users.length === 0" class="text-center py-12 text-gray-400">
      해당 조건의 사용자가 없습니다
    </div>

    <!-- Table -->
    <table v-else class="w-full">
      <thead>
        <tr class="border-b border-gray-100 text-left text-sm text-gray-500">
          <th class="px-6 py-4 font-medium">이름</th>
          <th class="px-6 py-4 font-medium">이메일</th>
          <th class="px-6 py-4 font-medium">또래</th>
          <th class="px-6 py-4 font-medium">역할</th>
          <th class="px-6 py-4 font-medium">상태</th>
          <th class="px-6 py-4 font-medium">가입일</th>
          <th class="px-6 py-4 font-medium">관리</th>
        </tr>
      </thead>
      <tbody>
        <tr
          v-for="user in users"
          :key="user.id"
          class="border-b border-gray-50 hover:bg-gray-50/50 transition-colors"
        >
          <td class="px-6 py-4 text-sm font-medium text-gray-800">{{ user.name }}</td>
          <td class="px-6 py-4 text-sm text-gray-600">{{ user.email }}</td>
          <td class="px-6 py-4 text-sm text-gray-600">{{ user.ttorae }}</td>
          <td class="px-6 py-4">
            <StatusBadge type="role" :value="user.role" />
          </td>
          <td class="px-6 py-4">
            <StatusBadge type="status" :value="user.status" />
          </td>
          <td class="px-6 py-4 text-sm text-gray-500">{{ formatDate(user.createdAt) }}</td>
          <td class="px-6 py-4">
            <div class="flex gap-2">
              <button
                v-if="user.status === 'PENDING' || user.status === 'INACTIVE'"
                :disabled="actionLoading === user.id"
                class="px-3 py-1.5 text-xs font-medium text-green-700 bg-green-50 rounded-lg hover:bg-green-100 transition-colors disabled:opacity-50"
                @click="handleActivate(user.id)"
              >
                승인
              </button>
              <button
                v-if="user.status === 'ACTIVE'"
                :disabled="actionLoading === user.id"
                class="px-3 py-1.5 text-xs font-medium text-red-700 bg-red-50 rounded-lg hover:bg-red-100 transition-colors disabled:opacity-50"
                @click="handleDeactivate(user.id)"
              >
                비활성화
              </button>
            </div>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>
