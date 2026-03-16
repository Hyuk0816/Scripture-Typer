<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { onMounted } from 'vue'
import StatusBadge from '@/components/molecules/StatusBadge.vue'
import Spinner from '@/components/atoms/Spinner.vue'
import PageNavigator from '@/components/molecules/PageNavigator.vue'
import type { UserListResponse } from '@/types/auth'
import type { AffiliationResponse } from '@/types/affiliation'
import { adminApi, affiliationApi } from '@/utils/api'

const PER_PAGE = 5

const props = defineProps<{
  users: UserListResponse[]
  loading: boolean
}>()

const currentPage = ref(1)
const totalPages = computed(() => Math.max(1, Math.ceil(props.users.length / PER_PAGE)))
const pagedUsers = computed(() => {
  const start = (currentPage.value - 1) * PER_PAGE
  return props.users.slice(start, start + PER_PAGE)
})

watch(() => props.users, () => { currentPage.value = 1 })

const emit = defineEmits<{
  refresh: []
}>()

const actionLoading = ref<number | null>(null)
const affiliations = ref<AffiliationResponse[]>([])

onMounted(async () => {
  try {
    const { data } = await affiliationApi.getAll()
    affiliations.value = data
  } catch {
    // silent
  }
})

async function handleRoleChange(userId: number, role: string) {
  actionLoading.value = userId
  try {
    await adminApi.updateUserRole(userId, role)
    emit('refresh')
  } finally {
    actionLoading.value = null
  }
}

async function handleAffiliationChange(userId: number, affiliationId: number | null) {
  actionLoading.value = userId
  try {
    await adminApi.updateUserAffiliation(userId, affiliationId)
    emit('refresh')
  } finally {
    actionLoading.value = null
  }
}

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

    <!-- Content -->
    <template v-else>
      <!-- Mobile Card Layout -->
      <div class="md:hidden divide-y divide-gray-50">
        <div
          v-for="user in pagedUsers"
          :key="user.id"
          class="p-4 space-y-2"
        >
          <div class="flex items-center justify-between">
            <div class="flex items-center gap-2 flex-wrap">
              <span class="font-medium text-gray-800">{{ user.name }}</span>
              <select
                :value="user.role"
                class="text-xs border border-gray-200 rounded-lg px-2 py-1"
                @change="handleRoleChange(user.id, ($event.target as HTMLSelectElement).value)"
              >
                <option value="USER">일반</option>
                <option value="PASTOR">교역자</option>
                <option value="MOKJANG">목장</option>
                <option value="ADMIN">관리자</option>
              </select>
              <StatusBadge type="status" :value="user.status" />
            </div>
            <div class="flex gap-2 shrink-0 ml-2">
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
          </div>
          <div class="text-xs text-gray-500 flex flex-wrap gap-x-3 gap-y-1">
            <span>{{ user.email }}</span>
            <span>{{ user.ttorae }}</span>
            <span>{{ user.affiliationName || '미지정' }}</span>
            <span>{{ formatDate(user.createdAt) }}</span>
          </div>
          <select
            :value="user.affiliationId"
            class="mt-1 w-full text-xs border border-gray-200 rounded-lg px-2 py-1"
            @change="handleAffiliationChange(user.id, ($event.target as HTMLSelectElement).value ? Number(($event.target as HTMLSelectElement).value) : null)"
          >
            <option :value="null">소속 미지정</option>
            <option v-for="aff in affiliations" :key="aff.id" :value="aff.id">
              {{ aff.displayName }}
            </option>
          </select>
        </div>
      </div>

      <!-- Desktop Table -->
      <table class="hidden md:table w-full">
      <thead>
        <tr class="border-b border-gray-100 text-left text-sm text-gray-500">
          <th class="px-6 py-4 font-medium">이름</th>
          <th class="px-6 py-4 font-medium">이메일</th>
          <th class="px-6 py-4 font-medium">또래</th>
          <th class="px-6 py-4 font-medium">역할</th>
          <th class="px-6 py-4 font-medium">상태</th>
          <th class="px-6 py-4 font-medium">소속</th>
          <th class="px-6 py-4 font-medium">가입일</th>
          <th class="px-6 py-4 font-medium">관리</th>
        </tr>
      </thead>
      <tbody>
        <tr
          v-for="user in pagedUsers"
          :key="user.id"
          class="border-b border-gray-50 hover:bg-gray-50/50 transition-colors"
        >
          <td class="px-6 py-4 text-sm font-medium text-gray-800">{{ user.name }}</td>
          <td class="px-6 py-4 text-sm text-gray-600">{{ user.email }}</td>
          <td class="px-6 py-4 text-sm text-gray-600">{{ user.ttorae }}</td>
          <td class="px-6 py-4">
            <select
              :value="user.role"
              class="text-xs border border-gray-200 rounded-lg px-2 py-1 w-24"
              @change="handleRoleChange(user.id, ($event.target as HTMLSelectElement).value)"
            >
              <option value="USER">일반</option>
              <option value="PASTOR">교역자</option>
              <option value="MOKJANG">목장</option>
              <option value="ADMIN">관리자</option>
            </select>
          </td>
          <td class="px-6 py-4">
            <StatusBadge type="status" :value="user.status" />
          </td>
          <td class="px-6 py-4">
            <select
              :value="user.affiliationId"
              class="text-xs border border-gray-200 rounded-lg px-2 py-1 w-32"
              @change="handleAffiliationChange(user.id, ($event.target as HTMLSelectElement).value ? Number(($event.target as HTMLSelectElement).value) : null)"
            >
              <option :value="null">미지정</option>
              <option v-for="aff in affiliations" :key="aff.id" :value="aff.id">
                {{ aff.displayName }}
              </option>
            </select>
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

      <!-- Pagination -->
      <PageNavigator
        v-if="totalPages > 1"
        :current-page="currentPage"
        :total-pages="totalPages"
        class="py-4"
        @prev="currentPage--"
        @next="currentPage++"
        @go-to="(p) => currentPage = p"
      />
    </template>
  </div>
</template>
