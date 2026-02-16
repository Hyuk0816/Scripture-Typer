<script setup lang="ts">
import { computed } from 'vue'
import type { BookSummary } from '@/types/bible'
import { useUiStore } from '@/stores/ui'
import ChapterGrid from './ChapterGrid.vue'

const props = defineProps<{
  book: BookSummary
}>()

const uiStore = useUiStore()

const isExpanded = computed(() => uiStore.expandedBook === props.book.bookName)

function toggle() {
  uiStore.setExpandedBook(isExpanded.value ? null : props.book.bookName)
}
</script>

<template>
  <div class="border-b border-gray-50">
    <button
      @click="toggle"
      :class="[
        'w-full px-4 py-2.5 flex items-center justify-between hover:bg-gray-50 transition-colors text-left',
        isExpanded ? 'bg-amber-50/40' : ''
      ]"
    >
      <span :class="['text-sm', isExpanded ? 'text-amber-800 font-medium' : 'text-gray-700']">
        {{ book.bookName }}
      </span>
      <svg
        :class="['w-4 h-4 text-gray-400 transition-transform', isExpanded ? 'rotate-180' : '']"
        fill="none"
        stroke="currentColor"
        viewBox="0 0 24 24"
      >
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" />
      </svg>
    </button>
    <ChapterGrid
      v-if="isExpanded"
      :book-name="book.bookName"
      :total-chapters="book.totalChapters"
    />
  </div>
</template>