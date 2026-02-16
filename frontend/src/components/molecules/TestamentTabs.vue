<script setup lang="ts">
import { useUiStore, type Testament } from '@/stores/ui'

const uiStore = useUiStore()

function selectTestament(testament: Testament) {
  uiStore.setActiveTestament(testament)
}
</script>

<template>
  <div class="flex border-b border-gray-100">
    <button
      @click="selectTestament('OLD')"
      :class="[
        'flex-1 py-2.5 text-sm font-medium transition-colors',
        uiStore.activeTestament === 'OLD'
          ? 'text-amber-700 border-b-2 border-amber-700 bg-amber-50/50'
          : 'text-gray-500 hover:text-gray-700'
      ]"
    >
      구약
    </button>
    <button
      @click="selectTestament('NEW')"
      :class="[
        'flex-1 py-2.5 text-sm font-medium transition-colors',
        uiStore.activeTestament === 'NEW'
          ? 'text-amber-700 border-b-2 border-amber-700 bg-amber-50/50'
          : 'text-gray-500 hover:text-gray-700'
      ]"
    >
      신약
    </button>
  </div>
</template>