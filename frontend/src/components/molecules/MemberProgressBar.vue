<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  completedChapters: number
  totalChapters: number
  userName: string
  totalReadCount: number
}>()

const percentage = computed(() => {
  if (props.totalChapters === 0) return 0
  return Math.round((props.completedChapters / props.totalChapters) * 100)
})
</script>

<template>
  <div class="py-2">
    <div class="flex items-center justify-between mb-1">
      <span class="text-sm font-medium text-gray-700">{{ userName }}</span>
      <span class="text-xs text-gray-500">
        {{ completedChapters }}/{{ totalChapters }}장
        <span v-if="totalReadCount > 0" class="text-amber-600 ml-1">({{ totalReadCount }}회)</span>
      </span>
    </div>
    <div class="w-full bg-gray-100 rounded-full h-2.5">
      <div
        class="bg-amber-500 h-2.5 rounded-full transition-all duration-300"
        :style="{ width: percentage + '%' }"
      />
    </div>
  </div>
</template>
