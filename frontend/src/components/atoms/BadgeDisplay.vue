<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  readCount: number
}>()

const badge = computed(() => {
  if (props.readCount >= 5) {
    return { emoji: '🥇', label: '골드', colorClass: 'text-yellow-500' }
  } else if (props.readCount >= 3) {
    return { emoji: '🥈', label: '실버', colorClass: 'text-gray-400' }
  } else {
    return { emoji: '🥉', label: '브론즈', colorClass: 'text-amber-600' }
  }
})
</script>

<template>
  <div class="flex flex-col items-center">
    <span class="text-5xl">{{ badge.emoji }}</span>
    <span :class="['text-sm font-medium mt-1', badge.colorClass]">{{ badge.label }}</span>
  </div>
</template>
