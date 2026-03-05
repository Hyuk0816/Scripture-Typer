<script setup lang="ts">
import { ref, computed } from 'vue'
import type { UserLoginDetail } from '@/types/admin-stats'

const props = defineProps<{
  date: string
  users: UserLoginDetail[]
}>()

const emit = defineEmits<{
  close: []
}>()

const search = ref('')

const filteredUsers = computed(() => {
  if (!search.value) return props.users
  const keyword = search.value.toLowerCase()
  return props.users.filter((u) => u.userName.toLowerCase().includes(keyword))
})

function handleBackdrop(e: MouseEvent) {
  if (e.target === e.currentTarget) emit('close')
}
</script>

<template>
  <Teleport to="body">
    <div
      class="fixed inset-0 z-50 flex items-center justify-center bg-black/40"
      @click="handleBackdrop"
    >
      <div class="bg-white rounded-2xl shadow-xl w-[90vw] max-w-md mx-4 max-h-[80vh] flex flex-col">
        <!-- Header -->
        <div class="flex items-center justify-between px-5 py-4 border-b border-gray-100">
          <h3 class="font-semibold text-gray-800">{{ date }} 접속 유저</h3>
          <button
            @click="emit('close')"
            class="p-1 rounded-lg hover:bg-gray-100 transition-colors"
            aria-label="닫기"
          >
            <svg class="w-5 h-5 text-gray-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>

        <!-- Search -->
        <div class="px-5 py-3">
          <input
            v-model="search"
            type="text"
            placeholder="이름 검색..."
            class="w-full px-3 py-2 text-sm border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-amber-500/40 focus:border-amber-500"
          />
        </div>

        <!-- User List -->
        <div class="flex-1 overflow-y-auto px-5 pb-4 min-h-0">
          <div v-if="filteredUsers.length === 0" class="text-center text-gray-400 text-sm py-6">
            검색 결과가 없습니다
          </div>
          <div v-else class="divide-y divide-gray-50">
            <div
              v-for="user in filteredUsers"
              :key="user.userId"
              class="flex items-center justify-between py-2.5"
            >
              <span class="text-sm text-gray-700">{{ user.userName }}</span>
              <span class="text-sm font-semibold text-amber-700">{{ user.loginCount }}회</span>
            </div>
          </div>
        </div>

        <!-- Footer -->
        <div class="px-5 py-3 border-t border-gray-100 text-xs text-gray-400 text-center">
          총 {{ filteredUsers.length }}명{{ search ? ` (전체 ${users.length}명)` : '' }}
        </div>
      </div>
    </div>
  </Teleport>
</template>
