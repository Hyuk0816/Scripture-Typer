<script setup lang="ts">
defineProps<{
  htmlFor?: string
  required?: boolean
}>()
</script>

<template>
  <label :for="htmlFor" class="block text-sm font-medium text-gray-700 mb-1">
    <slot />
    <span v-if="required" class="text-red-400 ml-0.5">*</span>
  </label>
</template>
