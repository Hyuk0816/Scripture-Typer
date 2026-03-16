<script setup lang="ts">
import { onMounted } from 'vue'
import { useChatStore } from '@/stores/chat'

const chatStore = useChatStore()

onMounted(() => {
  chatStore.fetchSessions()
})

function formatDate(dateStr: string): string {
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))

  if (days === 0) {
    return date.toLocaleTimeString('ko-KR', { hour: '2-digit', minute: '2-digit' })
  }
  if (days === 1) return '어제'
  if (days < 7) return `${days}일 전`
  return date.toLocaleDateString('ko-KR', { month: 'short', day: 'numeric' })
}

function handleSessionClick(id: number) {
  if (chatStore.deleteMode) {
    chatStore.toggleSelectSession(id)
  } else {
    chatStore.selectSession(id)
  }
}

function handleDelete() {
  if (!confirm(`${chatStore.selectedCount}개의 대화를 삭제하시겠습니까?`)) return
  chatStore.deleteSelectedSessions()
}
</script>

<template>
  <div class="flex-1 overflow-y-auto">
    <div class="px-4 py-3">
      <button
        v-if="!chatStore.deleteMode"
        @click="chatStore.startNewSession()"
        class="w-full py-2.5 rounded-lg bg-amber-500 text-white text-sm font-medium hover:bg-amber-600 transition-colors"
      >
        + 새 대화
      </button>
      <!-- Delete mode header -->
      <div v-else class="flex items-center justify-between">
        <button
          @click="chatStore.selectAllSessions()"
          class="text-sm text-gray-600 hover:text-gray-800 transition-colors"
        >
          {{ chatStore.selectedSessions.size === chatStore.sessions.length ? '전체 해제' : '전체 선택' }}
        </button>
        <button
          @click="chatStore.toggleDeleteMode()"
          class="text-sm text-gray-500 hover:text-gray-700 transition-colors"
        >
          취소
        </button>
      </div>
    </div>

    <div v-if="chatStore.sessions.length === 0" class="text-center text-gray-400 text-sm mt-8">
      <p>이전 대화가 없습니다.</p>
    </div>

    <div class="px-2">
      <div
        v-for="session in chatStore.sessions"
        :key="session.id"
        class="group flex items-center gap-2 px-3 py-3 mx-1 mb-1 rounded-lg hover:bg-gray-100 cursor-pointer transition-colors"
        @click="handleSessionClick(session.id)"
      >
        <!-- Checkbox in delete mode -->
        <input
          v-if="chatStore.deleteMode"
          type="checkbox"
          :checked="chatStore.selectedSessions.has(session.id)"
          class="w-5 h-5 rounded border-gray-300 text-red-500 focus:ring-red-500 shrink-0 cursor-pointer"
          @click.stop="chatStore.toggleSelectSession(session.id)"
        />
        <div class="flex-1 min-w-0">
          <p class="text-sm font-medium text-gray-800 truncate">
            {{ session.title }}
          </p>
          <div class="flex items-center gap-2 mt-0.5">
            <span v-if="session.bookName" class="text-xs text-amber-600">
              {{ session.bookName }}{{ session.chapter ? ` ${session.chapter}장` : '' }}
            </span>
            <span class="text-xs text-gray-400">
              {{ formatDate(session.updatedAt) }}
            </span>
            <span class="text-xs text-gray-400">
              · {{ session.messageCount }}개
            </span>
          </div>
        </div>
        <button
          v-if="!chatStore.deleteMode"
          @click.stop="chatStore.deleteSession(session.id)"
          class="p-1 rounded opacity-0 group-hover:opacity-100 hover:bg-gray-200 text-gray-400 hover:text-red-500 transition-all"
          aria-label="삭제"
        >
          <svg class="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
            <path stroke-linecap="round" stroke-linejoin="round" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
          </svg>
        </button>
      </div>
    </div>

    <!-- Bottom delete bar -->
    <div
      v-if="chatStore.deleteMode && chatStore.sessions.length > 0"
      class="sticky bottom-0 px-4 py-3 bg-white border-t border-gray-100 pb-[env(safe-area-inset-bottom,12px)]"
    >
      <div class="flex gap-2">
        <button
          @click="chatStore.toggleDeleteMode()"
          class="flex-1 py-2.5 rounded-lg border border-gray-200 text-gray-600 text-sm font-medium hover:bg-gray-50 transition-colors"
        >
          취소
        </button>
        <button
          @click="handleDelete"
          :disabled="chatStore.selectedCount === 0"
          class="flex-1 py-2.5 rounded-lg bg-red-500 text-white text-sm font-medium hover:bg-red-600 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
        >
          {{ chatStore.selectedCount }}개 삭제
        </button>
      </div>
    </div>
  </div>
</template>
