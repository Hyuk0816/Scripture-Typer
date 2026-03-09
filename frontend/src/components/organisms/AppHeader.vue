<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useUiStore } from '@/stores/ui'
import { useAuthStore } from '@/stores/auth'
import { useGroupStore } from '@/stores/group'
import { useRoute, useRouter } from 'vue-router'
import ModeTabs from '@/components/molecules/ModeTabs.vue'

const uiStore = useUiStore()
const authStore = useAuthStore()
const groupStore = useGroupStore()
const route = useRoute()
const router = useRouter()

onMounted(() => {
  if (authStore.isAuthenticated) {
    groupStore.fetchPendingInviteCount()
  }
})

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

    <router-link to="/" class="text-lg font-semibold text-gray-800 hover:text-amber-700 transition-colors whitespace-nowrap">
      Scripture Typer
    </router-link>

    <span v-if="isReadingOrTyping && bookName && chapter" class="text-sm text-gray-500 ml-2 whitespace-nowrap hidden sm:inline">
      {{ bookName }} {{ chapter }}장
    </span>

    <ModeTabs />

    <div class="flex-1" />

    <!-- Desktop nav links -->
    <nav class="hidden lg:flex items-center gap-1">
      <router-link
        to="/mypage"
        class="text-sm text-gray-500 hover:text-gray-700 hover:bg-gray-100 px-3 py-1.5 rounded-lg transition-colors"
      >
        마이페이지
      </router-link>

      <router-link
        to="/group"
        class="relative text-sm text-gray-500 hover:text-gray-700 hover:bg-gray-100 px-3 py-1.5 rounded-lg transition-colors"
      >
        그룹
        <span
          v-if="groupStore.pendingInviteCount > 0"
          class="absolute -top-1 -right-1 min-w-[18px] h-[18px] flex items-center justify-center text-[10px] font-bold text-white bg-red-500 rounded-full px-1"
        >
          {{ groupStore.pendingInviteCount > 99 ? '99+' : groupStore.pendingInviteCount }}
        </span>
      </router-link>

      <router-link
        to="/ranking"
        class="text-sm text-gray-500 hover:text-gray-700 hover:bg-gray-100 px-3 py-1.5 rounded-lg transition-colors"
      >
        랭킹
      </router-link>

      <router-link
        to="/board"
        class="text-sm text-gray-500 hover:text-gray-700 hover:bg-gray-100 px-3 py-1.5 rounded-lg transition-colors"
      >
        게시판
      </router-link>

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
    </nav>
  </header>
</template>