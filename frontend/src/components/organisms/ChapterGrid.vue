<script setup lang="ts">
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const props = defineProps<{
  bookName: string
  totalChapters: number
}>()

const router = useRouter()
const route = useRoute()

const chapters = computed(() =>
  Array.from({ length: props.totalChapters }, (_, i) => i + 1)
)

function handleChapterClick(chapter: number) {
  router.push(`/reading/${props.bookName}/${chapter}`)
}

function isActive(ch: number): boolean {
  return route.params.book === props.bookName && Number(route.params.chapter) === ch
}
</script>

<template>
  <div class="px-4 pb-3 grid grid-cols-6 gap-1.5">
    <button
      v-for="ch in chapters"
      :key="ch"
      @click="handleChapterClick(ch)"
      :class="[
        'text-xs py-1.5 rounded transition-colors',
        isActive(ch)
          ? 'bg-amber-700 text-white'
          : 'bg-gray-100 text-gray-600 hover:bg-amber-100 hover:text-amber-800'
      ]"
    >
      {{ ch }}
    </button>
  </div>
</template>