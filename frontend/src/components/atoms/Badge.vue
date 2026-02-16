<script setup lang="ts">
defineProps<{
  variant?: 'amber' | 'green' | 'red' | 'gray' | 'blue'
}>()
</script>

<template>
  <span
    class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium"
    :class="{
      'bg-amber-100 text-amber-800': variant === 'amber',
      'bg-green-100 text-green-800': variant === 'green',
      'bg-red-100 text-red-800': variant === 'red',
      'bg-gray-100 text-gray-800': variant === 'gray' || !variant,
      'bg-blue-100 text-blue-800': variant === 'blue',
    }"
  >
    <slot />
  </span>
</template>
