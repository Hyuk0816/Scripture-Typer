<script setup lang="ts">
import { computed } from 'vue'
import { useUiStore } from '@/stores/ui'
import { useAuthStore } from '@/stores/auth'
import { useRoute, useRouter } from 'vue-router'
import ModeTabs from '@/components/molecules/ModeTabs.vue'

const uiStore = useUiStore()
const authStore = useAuthStore()
const route = useRoute()
const router = useRouter()

const bookName = computed(() => route.params.book as string)
const chapter = computed(() => route.params.chapter as string)
const isReadingOrTyping = computed(() =>
  route.path.startsWith('/reading') || route.path.startsWith('/typing')
)

async function handleLogout() {
  await authStore.logout()
  router.push('/login')
}
</script>

<template>
  <header class="h-14 border-b border-gray-200 bg-white/90 backdrop-blur-md flex items-center px-4 gap-4 sticky top-0 z-30">
    <button
      @click="uiStore.toggleSidebar()"
      class="p-2 hover:bg-gray-100 rounded-lg transition-colors"
      aria-label="사이드바 열기/닫기"
    >
      <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16" />
      </svg>
    </button>

    <router-link to="/" class="text-lg font-semibold text-gray-800 hover:text-amber-700 transition-colors">
      Scripture Typer
    </router-link>

    <span v-if="isReadingOrTyping && bookName && chapter" class="text-sm text-gray-500 ml-2">
      {{ bookName }} {{ chapter }}장
    </span>

    <ModeTabs />

    <div class="flex-1" />

    <router-link
      v-if="authStore.isAdmin"
      to="/admin/users"
      class="text-sm text-gray-500 hover:text-gray-700 hover:bg-gray-100 px-3 py-1.5 rounded-lg transition-colors"
    >
      관리자
    </router-link>

    <button
      @click="handleLogout"
      class="text-sm text-gray-500 hover:text-gray-700 hover:bg-gray-100 px-3 py-1.5 rounded-lg transition-colors"
    >
      로그아웃
    </button>
  </header>
</template>