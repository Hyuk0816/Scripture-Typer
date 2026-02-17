<script setup lang="ts">
import type { PerPage } from '@/stores/reading'

defineProps<{
  modelValue: PerPage
}>()

const emit = defineEmits<{
  'update:modelValue': [value: PerPage]
}>()

const options: PerPage[] = [1, 5, 10]
</script>

<template>
  <div class="flex items-center gap-1.5">
    <span class="text-xs text-gray-500">절/페이지</span>
    <div class="flex rounded-lg border border-gray-200 overflow-hidden">
      <button
        v-for="opt in options"
        :key="opt"
        @click="emit('update:modelValue', opt)"
        :class="[
          'px-3 py-1 text-xs font-medium transition-colors',
          modelValue === opt
            ? 'bg-amber-700 text-white'
            : 'bg-white text-gray-600 hover:bg-amber-50'
        ]"
      >
        {{ opt }}
      </button>
    </div>
  </div>
</template>