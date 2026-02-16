<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useBibleStore } from '@/stores/bible'
import { useUiStore } from '@/stores/ui'
import BookItem from './BookItem.vue'
import Spinner from '@/components/atoms/Spinner.vue'

const bibleStore = useBibleStore()
const uiStore = useUiStore()

const books = computed(() =>
  uiStore.activeTestament === 'OLD' ? bibleStore.oldTestament : bibleStore.newTestament
)

onMounted(() => {
  if (bibleStore.oldTestament.length === 0) {
    bibleStore.fetchBooks()
  }
})
</script>

<template>
  <div class="flex-1 overflow-y-auto">
    <div v-if="bibleStore.loading" class="flex items-center justify-center py-8">
      <Spinner />
    </div>
    <template v-else>
      <BookItem v-for="book in books" :key="book.bookName" :book="book" />
    </template>
  </div>
</template>