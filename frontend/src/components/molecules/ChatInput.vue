<script setup lang="ts">
import { ref } from 'vue'

const props = defineProps<{
  disabled: boolean
}>()

const emit = defineEmits<{
  send: [content: string]
}>()

const text = ref('')
const isComposing = ref(false)
const textareaRef = ref<HTMLTextAreaElement | null>(null)

function handleSubmit() {
  const trimmed = text.value.trim()
  if (!trimmed || props.disabled) return
  emit('send', trimmed)
  text.value = ''
  if (textareaRef.value) {
    textareaRef.value.style.height = 'auto'
  }
}

function handleKeyDown(e: KeyboardEvent) {
  if (e.key === 'Enter' && !e.shiftKey && !isComposing.value) {
    e.preventDefault()
    handleSubmit()
  }
}

function handleInput(e: Event) {
  const el = e.target as HTMLTextAreaElement
  el.style.height = 'auto'
  el.style.height = Math.min(el.scrollHeight, 120) + 'px'
}
</script>

<template>
  <div class="flex items-end gap-2 p-3 border-t border-gray-100 bg-white">
    <textarea
      ref="textareaRef"
      v-model="text"
      @input="handleInput"
      @keydown="handleKeyDown"
      @compositionstart="isComposing = true"
      @compositionend="isComposing = false"
      placeholder="질문을 입력하세요..."
      rows="1"
      class="flex-1 resize-none rounded-xl border border-gray-200 px-3 py-2 text-sm focus:outline-none focus:border-amber-400 focus:ring-1 focus:ring-amber-400"
      style="max-height: 120px"
      :disabled="disabled"
    />
    <button
      @click="handleSubmit"
      :disabled="!text.trim() || disabled"
      class="shrink-0 w-9 h-9 rounded-full bg-amber-600 hover:bg-amber-700 disabled:bg-gray-300 text-white flex items-center justify-center transition-colors"
      aria-label="전송"
    >
      <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
        <path stroke-linecap="round" stroke-linejoin="round" d="M5 12h14M12 5l7 7-7 7" />
      </svg>
    </button>
  </div>
</template>
