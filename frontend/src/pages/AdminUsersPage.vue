<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import AdminTemplate from '@/components/templates/AdminTemplate.vue'
import StatusFilterTabs from '@/components/molecules/StatusFilterTabs.vue'
import UserListTable from '@/components/organisms/UserListTable.vue'
import type { UserListResponse, UserStatus } from '@/types/auth'
import { adminApi } from '@/utils/api'

const users = ref<UserListResponse[]>([])
const loading = ref(false)
const activeFilter = ref<UserStatus | null>(null)

const counts = computed(() => ({
  all: users.value.length,
  PENDING: users.value.filter((u) => u.status === 'PENDING').length,
  ACTIVE: users.value.filter((u) => u.status === 'ACTIVE').length,
  INACTIVE: users.value.filter((u) => u.status === 'INACTIVE').length,
}))

const filteredUsers = computed(() => {
  if (!activeFilter.value) return users.value
  return users.value.filter((u) => u.status === activeFilter.value)
})

async function fetchUsers() {
  loading.value = true
  try {
    const { data } = await adminApi.getUsers()
    users.value = data
  } finally {
    loading.value = false
  }
}

onMounted(fetchUsers)
</script>

<template>
  <AdminTemplate>
    <div class="space-y-6">
      <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3">
        <div>
          <h2 class="text-lg sm:text-xl font-bold text-gray-800">회원 관리</h2>
          <p class="mt-1 text-sm text-gray-500">회원 목록을 확인하고 승인/비활성화할 수 있습니다</p>
        </div>
        <div class="flex gap-2">
          <router-link
            to="/admin/users"
            class="rounded-lg bg-amber-600 px-3 py-1.5 text-sm font-medium text-white"
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
            class="rounded-lg border border-gray-200 px-3 py-1.5 text-sm text-gray-600 hover:bg-gray-50 transition-colors"
          >
            통계
          </router-link>
        </div>
      </div>

      <StatusFilterTabs v-model="activeFilter" :counts="counts" />

      <UserListTable :users="filteredUsers" :loading="loading" @refresh="fetchUsers" />
    </div>
  </AdminTemplate>
</template>
