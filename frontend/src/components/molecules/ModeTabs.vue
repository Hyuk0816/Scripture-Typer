<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

const currentMode = computed(() => {
  if (route.path.startsWith('/reading')) return 'reading'
  if (route.path.startsWith('/typing')) return 'typing'
  return null
})

const bookName = computed(() => route.params.book as string)
const chapter = computed(() => route.params.chapter as string)

function switchMode(mode: 'reading' | 'typing') {
  if (!bookName.value || !chapter.value) return
  router.push(`/${mode}/${bookName.value}/${chapter.value}`)
}
</script>

<template>
  <div v-if="currentMode" class="flex rounded-lg border border-gray-200 overflow-hidden">
    <button
      @click="switchMode('reading')"
      :class="[
        'px-4 py-1.5 text-sm font-medium transition-colors',
        currentMode === 'reading'
          ? 'bg-amber-700 text-white'
          : 'bg-white text-gray-600 hover:bg-amber-50'
      ]"
    >
      통독
    </button>
    <button
      @click="switchMode('typing')"
      :class="[
        'px-4 py-1.5 text-sm font-medium transition-colors',
        currentMode === 'typing'
          ? 'bg-amber-700 text-white'
          : 'bg-white text-gray-600 hover:bg-amber-50'
      ]"
    >
      필사
    </button>
  </div>
</template>