<script setup lang="ts">
defineProps<{
  modelValue: 'READING' | 'TYPING'
}>()

const emit = defineEmits<{
  'update:modelValue': [value: 'READING' | 'TYPING']
}>()

const modes = [
  { value: 'READING' as const, label: '통독' },
  { value: 'TYPING' as const, label: '필사' },
]
</script>

<template>
  <div class="flex bg-gray-100 rounded-lg p-1">
    <button
      v-for="mode in modes"
      :key="mode.value"
      class="flex-1 px-4 py-2 text-sm font-medium rounded-md transition-all"
      :class="modelValue === mode.value
        ? 'bg-white text-amber-700 shadow-sm'
        : 'text-gray-500 hover:text-gray-700'"
      @click="emit('update:modelValue', mode.value)"
    >
      {{ mode.label }}
    </button>
  </div>
</template>
