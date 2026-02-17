<script setup lang="ts">
import type { VerseData } from '@/types/bible'

defineProps<{
  verses: VerseData[]
  lastReadVerse: number
}>()
</script>

<template>
  <div class="space-y-0">
    <div
      v-for="verse in verses"
      :key="verse.id"
      :class="[
        'flex gap-3 py-3 px-4 rounded-lg transition-colors',
        verse.verse <= lastReadVerse ? 'bg-amber-50/50' : ''
      ]"
    >
      <span class="text-amber-700/70 font-semibold text-sm mt-0.5 select-none shrink-0 w-8 text-right">
        {{ verse.verse }}
      </span>
      <p class="text-gray-800 leading-relaxed text-base">
        {{ verse.content }}
      </p>
    </div>
  </div>
</template>