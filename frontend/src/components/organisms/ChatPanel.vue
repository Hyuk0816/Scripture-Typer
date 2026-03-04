<script setup lang="ts">
import { ref, watch, nextTick, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useChatStore } from '@/stores/chat'
import ChatMessageBubble from '@/components/molecules/ChatMessageBubble.vue'
import ChatInput from '@/components/molecules/ChatInput.vue'
import ChatSessionList from '@/components/organisms/ChatSessionList.vue'

const chatStore = useChatStore()
const route = useRoute()
const messagesEndRef = ref<HTMLDivElement | null>(null)

function scrollToBottom() {
  nextTick(() => {
    messagesEndRef.value?.scrollIntoView({ behavior: 'smooth' })
  })
}

watch(() => chatStore.messages, scrollToBottom, { deep: true })

onMounted(() => {
  chatStore.fetchUsage()
})

function handleSend(content: string) {
  const bookName = route.params.book as string | undefined
  const chapter = route.params.chapter ? Number(route.params.chapter) : undefined
  const context = bookName && chapter ? { bookName, chapter } : undefined
  chatStore.sendMessage(content, context)
}

const usageLimitExceeded = ref(false)

watch(
  () => chatStore.usage,
  (u) => {
    usageLimitExceeded.value = !u.unlimited && u.used >= u.limit
  },
  { immediate: true },
)
</script>

<template>
  <!-- Backdrop: fullscreen on all screens, normal only on desktop -->
  <div
    v-if="chatStore.isOpen"
    :class="[
      'fixed inset-0 z-40',
      chatStore.isFullscreen
        ? 'bg-black/40'
        : 'bg-black/30 hidden md:block',
    ]"
    @click="chatStore.toggleChat()"
  />

  <div
    v-if="chatStore.isOpen"
    :class="[
      'fixed z-50 flex flex-col bg-gray-50 shadow-2xl transition-all duration-200',
      chatStore.isFullscreen
        ? 'inset-0 md:inset-12 lg:inset-x-24 lg:inset-y-12 md:rounded-2xl border-t md:border border-gray-200'
        : 'bottom-0 left-0 right-0 h-[55vh] md:top-0 md:left-auto md:right-0 md:h-full md:w-96 rounded-t-2xl md:rounded-none border-t md:border-t-0 md:border-l border-gray-200',
    ]"
  >
    <!-- Header -->
    <div :class="[
      'flex items-center justify-between px-4 py-3 bg-white border-b border-gray-100',
      !chatStore.isFullscreen ? 'rounded-t-2xl md:rounded-none' : 'md:rounded-t-2xl',
    ]">
      <h2 class="text-base font-semibold text-gray-800">말씀 도우미</h2>
      <div class="flex items-center gap-1">
        <button
          @click="chatStore.clearMessages()"
          class="p-1.5 rounded-lg hover:bg-gray-100 text-gray-400 hover:text-gray-600 transition-colors"
          aria-label="대화 초기화"
          title="대화 초기화"
        >
          <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
            <path stroke-linecap="round" stroke-linejoin="round" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
          </svg>
        </button>
        <button
          @click="chatStore.toggleFullscreen()"
          class="p-1.5 rounded-lg hover:bg-gray-100 text-gray-400 hover:text-gray-600 transition-colors"
          :aria-label="chatStore.isFullscreen ? '축소' : '확장'"
          :title="chatStore.isFullscreen ? '축소' : '확장'"
        >
          <!-- arrows-pointing-in (exit fullscreen) -->
          <svg v-if="chatStore.isFullscreen" class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
            <path stroke-linecap="round" stroke-linejoin="round" d="M9 9V4.5M9 9H4.5M9 9 3.75 3.75M9 15v4.5M9 15H4.5M9 15l-5.25 5.25M15 9h4.5M15 9V4.5M15 9l5.25-5.25M15 15h4.5M15 15v4.5m0-4.5 5.25 5.25" />
          </svg>
          <!-- arrows-pointing-out (enter fullscreen) -->
          <svg v-else class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
            <path stroke-linecap="round" stroke-linejoin="round" d="M3.75 3.75v4.5m0-4.5h4.5m-4.5 0L9 9M3.75 20.25v-4.5m0 4.5h4.5m-4.5 0L9 15M20.25 3.75h-4.5m4.5 0v4.5m0-4.5L15 9m5.25 11.25h-4.5m4.5 0v-4.5m0 4.5L15 15" />
          </svg>
        </button>
        <button
          @click="chatStore.toggleChat()"
          class="p-1.5 rounded-lg hover:bg-gray-100 text-gray-400 hover:text-gray-600 transition-colors"
          aria-label="닫기"
        >
          <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
            <path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12" />
          </svg>
        </button>
      </div>
    </div>

    <!-- Tabs -->
    <div class="flex border-b border-gray-100 bg-white">
      <button
        @click="chatStore.setView('chat')"
        :class="[
          'flex-1 py-2 text-sm font-medium transition-colors',
          chatStore.view === 'chat'
            ? 'text-amber-600 border-b-2 border-amber-500'
            : 'text-gray-400 hover:text-gray-600',
        ]"
      >
        대화
      </button>
      <button
        @click="chatStore.setView('list')"
        :class="[
          'flex-1 py-2 text-sm font-medium transition-colors',
          chatStore.view === 'list'
            ? 'text-amber-600 border-b-2 border-amber-500'
            : 'text-gray-400 hover:text-gray-600',
        ]"
      >
        목록
      </button>
    </div>

    <!-- Usage bar -->
    <div
      v-if="!chatStore.usage.unlimited"
      class="px-4 py-2 bg-white border-b border-gray-100 flex items-center gap-2"
    >
      <div class="flex-1 h-1.5 bg-gray-200 rounded-full overflow-hidden">
        <div
          class="h-full rounded-full transition-all duration-300"
          :class="usageLimitExceeded ? 'bg-red-500' : 'bg-amber-500'"
          :style="{ width: Math.min((chatStore.usage.used / chatStore.usage.limit) * 100, 100) + '%' }"
        />
      </div>
      <span class="text-xs text-gray-500 whitespace-nowrap">
        {{ chatStore.usage.used }}/{{ chatStore.usage.limit }}
      </span>
    </div>

    <!-- Content -->
    <ChatSessionList v-if="chatStore.view === 'list'" />

    <template v-else>
      <!-- Messages -->
      <div class="flex-1 overflow-y-auto px-4 py-3">
        <div v-if="chatStore.messages.length === 0" class="text-center text-gray-400 text-sm mt-8">
          <p>성경 구절에 대해 질문해보세요.</p>
          <p class="mt-1 text-xs">현재 타이핑 중인 구절의 맥락을 자동으로 참고합니다.</p>
        </div>
        <ChatMessageBubble
          v-for="(msg, i) in chatStore.messages"
          :key="msg.id"
          :message="msg"
          :is-streaming="chatStore.isLoading && msg.role === 'assistant' && i === chatStore.messages.length - 1"
        />
        <div ref="messagesEndRef" />
      </div>

      <!-- Input -->
      <div v-if="usageLimitExceeded" class="px-4 py-3 border-t border-gray-100 bg-white">
        <p class="text-sm text-center text-red-500">오늘의 사용량을 초과했습니다.</p>
      </div>
      <ChatInput v-else :disabled="chatStore.isLoading" @send="handleSend" />
    </template>
  </div>
</template>
