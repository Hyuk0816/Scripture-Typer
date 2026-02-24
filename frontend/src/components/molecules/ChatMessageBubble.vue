<script setup lang="ts">
import type { ChatMessage } from '@/types/chat'

const props = defineProps<{
  message: ChatMessage
  isStreaming: boolean
}>()

const isUser = props.message.role === 'user'
</script>

<template>
  <div :class="['flex mb-3', isUser ? 'justify-end' : 'justify-start']">
    <div
      :class="[
        'max-w-[85%] px-3 py-2 rounded-2xl text-sm leading-relaxed',
        isUser
          ? 'bg-amber-600 text-white rounded-br-md'
          : 'bg-white text-gray-800 border border-gray-100 rounded-bl-md shadow-sm',
      ]"
    >
      <div class="whitespace-pre-wrap break-words">
        {{ message.content }}
        <span v-if="isStreaming && !message.content" class="inline-flex gap-1">
          <span
            class="w-1.5 h-1.5 bg-gray-400 rounded-full animate-bounce"
            style="animation-delay: 0ms"
          />
          <span
            class="w-1.5 h-1.5 bg-gray-400 rounded-full animate-bounce"
            style="animation-delay: 150ms"
          />
          <span
            class="w-1.5 h-1.5 bg-gray-400 rounded-full animate-bounce"
            style="animation-delay: 300ms"
          />
        </span>
        <span
          v-if="isStreaming && message.content"
          class="inline-block w-1 h-4 ml-0.5 bg-amber-600 animate-pulse align-text-bottom"
        />
      </div>
    </div>
  </div>
</template>
