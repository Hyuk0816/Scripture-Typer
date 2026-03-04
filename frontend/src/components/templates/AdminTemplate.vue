<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

async function handleLogout() {
  await authStore.logout()
  router.push('/login')
}
</script>

<template>
  <div class="min-h-screen bg-[#faf9f6]">
    <!-- Header -->
    <header class="sticky top-0 z-10 bg-white/80 backdrop-blur-md border-b border-gray-100">
      <div class="max-w-6xl mx-auto px-4 sm:px-6 py-3 sm:py-4 flex items-center justify-between gap-3">
        <div class="flex items-center gap-2 sm:gap-4 min-w-0">
          <router-link to="/" class="text-lg sm:text-xl font-bold text-amber-700 whitespace-nowrap">
            Scripture Typer
          </router-link>
          <span class="text-xs sm:text-sm text-gray-400">관리자</span>
        </div>
        <div class="flex items-center gap-2 sm:gap-4 shrink-0">
          <span class="text-xs sm:text-sm text-gray-600 hidden sm:inline">{{ authStore.user?.email }}</span>
          <button
            class="text-xs sm:text-sm text-gray-500 hover:text-gray-700 transition-colors"
            @click="handleLogout"
          >
            로그아웃
          </button>
        </div>
      </div>
    </header>

    <!-- Content -->
    <main class="max-w-6xl mx-auto px-4 sm:px-6 py-6 sm:py-8">
      <slot />
    </main>
  </div>
</template>
