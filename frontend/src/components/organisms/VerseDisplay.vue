<script setup lang="ts">
defineProps<{
  verse: number
  content: string
}>()
</script>

<template>
  <div class="mb-6">
    <div class="flex items-baseline gap-3 mb-2">
      <span class="text-xs font-medium text-amber-700 bg-amber-50 px-2 py-0.5 rounded">
        {{ verse }}절
      </span>
    </div>
    <p
      class="text-lg leading-loose tracking-wide text-gray-500 select-none"
      style="font-family: 'Noto Serif KR', serif"
    >
      {{ content }}
    </p>
  </div>
</template>
