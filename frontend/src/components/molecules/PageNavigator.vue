<script setup lang="ts">
defineProps<{
  currentPage: number
  totalPages: number
}>()

const emit = defineEmits<{
  prev: []
  next: []
  goTo: [page: number]
}>()
</script>

<template>
  <div class="flex items-center justify-center gap-2">
    <button
      @click="emit('prev')"
      :disabled="currentPage <= 1"
      class="p-2 rounded-lg hover:bg-gray-100 disabled:opacity-30 disabled:cursor-not-allowed transition-colors"
      aria-label="이전 페이지"
    >
      <svg class="w-5 h-5 text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
      </svg>
    </button>

    <div class="flex items-center gap-1">
      <template v-for="page in totalPages" :key="page">
        <button
          v-if="totalPages <= 7 || page === 1 || page === totalPages || Math.abs(page - currentPage) <= 1"
          @click="emit('goTo', page)"
          :class="[
            'w-8 h-8 rounded-lg text-sm font-medium transition-colors',
            page === currentPage
              ? 'bg-amber-700 text-white'
              : 'text-gray-600 hover:bg-gray-100'
          ]"
        >
          {{ page }}
        </button>
        <span
          v-else-if="page === 2 && currentPage > 3 || page === totalPages - 1 && currentPage < totalPages - 2"
          class="text-gray-400 text-sm px-1"
        >
          ...
        </span>
      </template>
    </div>

    <button
      @click="emit('next')"
      :disabled="currentPage >= totalPages"
      class="p-2 rounded-lg hover:bg-gray-100 disabled:opacity-30 disabled:cursor-not-allowed transition-colors"
      aria-label="다음 페이지"
    >
      <svg class="w-5 h-5 text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
      </svg>
    </button>
  </div>
</template>