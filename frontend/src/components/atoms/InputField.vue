<script setup lang="ts">
defineProps<{
  modelValue: string
  type?: string
  placeholder?: string
  error?: boolean
  disabled?: boolean
  id?: string
}>()

defineEmits<{
  'update:modelValue': [value: string]
}>()
</script>

<template>
  <input
    :id="id"
    :type="type ?? 'text'"
    :value="modelValue"
    :placeholder="placeholder"
    :disabled="disabled"
    class="w-full px-4 py-3 border rounded-xl text-sm transition-colors duration-200 outline-none"
    :class="[
      error
        ? 'border-red-400 focus:border-red-500 focus:ring-1 focus:ring-red-400'
        : 'border-gray-200 focus:border-amber-400 focus:ring-1 focus:ring-amber-400',
      disabled ? 'bg-gray-50 text-gray-400 cursor-not-allowed' : 'bg-white',
    ]"
    @input="$emit('update:modelValue', ($event.target as HTMLInputElement).value)"
  />
</template>
