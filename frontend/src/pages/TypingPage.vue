<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const bookName = computed(() => route.params.book as string)
const chapter = computed(() => Number(route.params.chapter))
</script>

<template>
  <div class="text-center py-12">
    <h2 class="text-xl font-semibold text-gray-700">필사 모드</h2>
    <p class="mt-2 text-gray-500">{{ bookName }} {{ chapter }}장</p>
    <p class="mt-4 text-sm text-gray-400">Phase 6에서 구현 예정</p>
  </div>
</template>