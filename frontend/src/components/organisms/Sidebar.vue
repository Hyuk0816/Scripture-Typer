<script setup lang="ts">
import { useUiStore } from '@/stores/ui'
import { useAuthStore } from '@/stores/auth'
import { useRouter } from 'vue-router'
import TestamentTabs from '@/components/molecules/TestamentTabs.vue'
import BookList from './BookList.vue'

const uiStore = useUiStore()
const authStore = useAuthStore()
const router = useRouter()

function navigateAndClose(path: string) {
  router.push(path)
  uiStore.toggleSidebar()
}

async function handleLogout() {
  await authStore.logout()
  router.push('/login')
}
</script>

<template>
  <!-- Mobile backdrop -->
  <div
    v-if="uiStore.sidebarOpen"
    class="fixed inset-0 bg-black/30 z-40 md:hidden"
    @click="uiStore.toggleSidebar()"
  />

  <aside
    :class="[
      uiStore.sidebarOpen ? 'w-64' : 'w-0',
      'bg-white border-r border-gray-200 transition-all duration-300 overflow-hidden flex flex-col shrink-0',
      'md:relative fixed inset-y-0 left-0 z-50 md:z-auto shadow-lg md:shadow-none'
    ]"
  >
    <!-- Mobile nav links -->
    <nav class="lg:hidden border-b border-gray-100 p-3 space-y-1">
      <button
        @click="navigateAndClose('/mypage')"
        class="w-full text-left text-sm text-gray-600 hover:text-amber-700 hover:bg-amber-50 px-3 py-2 rounded-lg transition-colors"
      >
        마이페이지
      </button>
      <button
        @click="navigateAndClose('/board')"
        class="w-full text-left text-sm text-gray-600 hover:text-amber-700 hover:bg-amber-50 px-3 py-2 rounded-lg transition-colors"
      >
        게시판
      </button>
      <button
        v-if="authStore.isAdmin"
        @click="navigateAndClose('/admin/users')"
        class="w-full text-left text-sm text-gray-600 hover:text-amber-700 hover:bg-amber-50 px-3 py-2 rounded-lg transition-colors"
      >
        관리자
      </button>
      <button
        @click="handleLogout"
        class="w-full text-left text-sm text-gray-400 hover:text-red-600 hover:bg-red-50 px-3 py-2 rounded-lg transition-colors"
      >
        로그아웃
      </button>
    </nav>

    <div class="p-4 border-b border-gray-100">
      <h2 class="text-sm font-semibold text-gray-500 uppercase tracking-wider">성경 목차</h2>
    </div>
    <TestamentTabs />
    <BookList />
  </aside>
</template>