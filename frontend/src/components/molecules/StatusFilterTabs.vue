<script setup lang="ts">
import type { UserStatus } from '@/types/auth'

defineProps<{
  modelValue: UserStatus | null
  counts: {
    all: number
    PENDING: number
    ACTIVE: number
    INACTIVE: number
  }
}>()

defineEmits<{
  'update:modelValue': [value: UserStatus | null]
}>()

const tabs: Array<{ label: string; value: UserStatus | null; key: string }> = [
  { label: '전체', value: null, key: 'all' },
  { label: '대기중', value: 'PENDING', key: 'PENDING' },
  { label: '활성', value: 'ACTIVE', key: 'ACTIVE' },
  { label: '비활성', value: 'INACTIVE', key: 'INACTIVE' },
]
</script>

<template>
  <div class="flex gap-2 flex-wrap">
    <button
      v-for="tab in tabs"
      :key="tab.key"
      class="px-4 py-2 rounded-lg text-sm font-medium transition-colors duration-200"
      :class="
        modelValue === tab.value
          ? 'bg-amber-600 text-white'
          : 'bg-white text-gray-600 border border-gray-200 hover:bg-gray-50'
      "
      @click="$emit('update:modelValue', tab.value)"
    >
      {{ tab.label }}
      <span
        class="ml-1.5 px-1.5 py-0.5 rounded-full text-xs"
        :class="
          modelValue === tab.value
            ? 'bg-amber-500 text-white'
            : 'bg-gray-100 text-gray-500'
        "
      >
        {{ counts[tab.key as keyof typeof counts] }}
      </span>
    </button>
  </div>
</template>
