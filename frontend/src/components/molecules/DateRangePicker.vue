<script setup lang="ts">
import { ref, watch } from 'vue'

const props = defineProps<{
  startDate: string
  endDate: string
}>()

const emit = defineEmits<{
  'update:startDate': [value: string]
  'update:endDate': [value: string]
  search: []
}>()

const localStart = ref(props.startDate)
const localEnd = ref(props.endDate)

watch(() => props.startDate, (v) => { localStart.value = v })
watch(() => props.endDate, (v) => { localEnd.value = v })

function handleSearch() {
  emit('update:startDate', localStart.value)
  emit('update:endDate', localEnd.value)
  emit('search')
}
</script>

<template>
  <div class="flex flex-wrap items-center gap-2 sm:gap-3">
    <input
      v-model="localStart"
      type="date"
      class="rounded-lg border border-gray-200 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-amber-400"
    />
    <span class="text-gray-400 text-sm">~</span>
    <input
      v-model="localEnd"
      type="date"
      class="rounded-lg border border-gray-200 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-amber-400"
    />
    <button
      class="rounded-lg bg-amber-600 px-4 py-2 text-sm font-medium text-white hover:bg-amber-700 transition-colors"
      @click="handleSearch"
    >
      조회
    </button>
  </div>
</template>
