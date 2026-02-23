<script setup lang="ts">
import { useTypingStore } from '@/stores/typing'
import { storeToRefs } from 'pinia'

const typingStore = useTypingStore()
const { currentVerseIndex, totalVerses, readCount } = storeToRefs(typingStore)
</script>

<template>
  <div class="flex items-center gap-3">
    <span class="text-sm text-gray-500">
      {{ currentVerseIndex + 1 }} / {{ totalVerses }}절
    </span>
    <span
      v-if="readCount > 0"
      class="text-xs px-2 py-0.5 rounded-full bg-amber-50 text-amber-700"
    >
      {{ readCount }}회독
    </span>
  </div>
</template>
