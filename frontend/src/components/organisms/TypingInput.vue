<script setup lang="ts">
import { ref, watch, nextTick } from 'vue'
import { useTypingStore } from '@/stores/typing'
import { storeToRefs } from 'pinia'

const typingStore = useTypingStore()
const { verses, currentVerseIndex, typedText, isComposing } = storeToRefs(typingStore)

const textareaRef = ref<HTMLTextAreaElement | null>(null)
const cursorPos = ref(0)

// Smart quote normalization
function normalize(s: string): string {
  return s
    .replace(/[\u201C\u201D\u201E\u201F\u2033\u2036]/g, '"')
    .replace(/[\u2018\u2019\u201A\u201B\u2032\u2035]/g, "'")
}

function updateCursorPos() {
  if (textareaRef.value) {
    cursorPos.value = textareaRef.value.selectionStart ?? typedText.value.length
  }
}

function handleInput(e: Event) {
  const el = e.target as HTMLTextAreaElement
  typingStore.setTypedText(el.value)
  cursorPos.value = el.selectionStart ?? el.value.length
}

function handleCompositionStart() {
  typingStore.setIsComposing(true)
}

function handleCompositionEnd(e: CompositionEvent) {
  typingStore.setIsComposing(false)
  const el = e.target as HTMLTextAreaElement
  typingStore.setTypedText(el.value)
  cursorPos.value = el.selectionStart ?? el.value.length
}

function handleKeyDown(e: KeyboardEvent) {
  if (e.key === 'Enter' && !isComposing.value) {
    e.preventDefault()
    const currentVerse = verses.value[currentVerseIndex.value]
    if (!currentVerse) return

    if (normalize(typedText.value.trim()) === normalize(currentVerse.content.trim())) {
      typingStore.saveProgress(currentVerse.verse)
      typingStore.completeVerse()
    }
  }
}

function getCharColor(i: number): string {
  const currentVerse = verses.value[currentVerseIndex.value]
  if (!currentVerse) return ''

  const targetText = currentVerse.content
  const isComposingChar = isComposing.value && i >= cursorPos.value - 1 && i < cursorPos.value

  if (isComposingChar) {
    return 'text-blue-500'
  } else if (i < targetText.length && normalize(typedText.value[i] ?? '') === normalize(targetText[i] ?? '')) {
    return 'text-green-600'
  } else {
    return 'text-red-500'
  }
}

// Auto-focus when currentVerseIndex changes
watch(currentVerseIndex, async () => {
  cursorPos.value = 0
  await nextTick()
  textareaRef.value?.focus()
})
</script>

<template>
  <div class="border-t border-gray-100 pt-6">
    <div class="relative">
      <div
        class="text-lg leading-loose tracking-wide min-h-[3rem] whitespace-pre-wrap break-words pointer-events-none"
        style="font-family: 'Noto Serif KR', serif"
        aria-hidden="true"
      >
        <template v-for="(char, i) in typedText" :key="i">
          <span
            v-if="i === cursorPos && !isComposing"
            class="animate-pulse text-amber-700"
          >|</span>
          <span :class="getCharColor(i)">{{ char }}</span>
        </template>
        <span
          v-if="cursorPos >= typedText.length && !isComposing"
          class="animate-pulse text-amber-700"
        >|</span>
      </div>
      <textarea
        ref="textareaRef"
        :value="typedText"
        @input="handleInput"
        @compositionstart="handleCompositionStart"
        @compositionend="handleCompositionEnd"
        @keydown="handleKeyDown"
        @keyup="updateCursorPos"
        @click="updateCursorPos"
        class="absolute inset-0 w-full h-full opacity-0 resize-none text-lg"
        style="font-family: 'Noto Serif KR', serif"
        autofocus
        spellcheck="false"
        autocomplete="off"
        autocorrect="off"
      />
    </div>
    <p class="mt-4 text-xs text-gray-400 text-center">
      위 구절을 그대로 타이핑한 후 Enter를 누르세요
    </p>
  </div>
</template>
