<script setup lang="ts">
import { useUiStore } from '@/stores/ui'
import TestamentTabs from '@/components/molecules/TestamentTabs.vue'
import BookList from './BookList.vue'

const uiStore = useUiStore()
</script>

<template>
  <!-- Mobile backdrop -->
  <div
    v-if="uiStore.sidebarOpen"
    class="fixed inset-0 bg-black/30 z-40 md:hidden"
    @click="uiStore.toggleSidebar()"
  />

  <aside
    :class="[
      uiStore.sidebarOpen ? 'w-64' : 'w-0',
      'bg-white border-r border-gray-200 transition-all duration-300 overflow-hidden flex flex-col shrink-0',
      'md:relative fixed inset-y-0 left-0 z-50 md:z-auto shadow-lg md:shadow-none'
    ]"
  >
    <div class="p-4 border-b border-gray-100">
      <h2 class="text-sm font-semibold text-gray-500 uppercase tracking-wider">성경 목차</h2>
    </div>
    <TestamentTabs />
    <BookList />
  </aside>
</template>