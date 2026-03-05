<script setup lang="ts">
import { computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useReadingStore } from '@/stores/reading'
import type { PerPage } from '@/stores/reading'
import Spinner from '@/components/atoms/Spinner.vue'
import VerseList from '@/components/organisms/VerseList.vue'
import PerPageSelector from '@/components/molecules/PerPageSelector.vue'
import PageNavigator from '@/components/molecules/PageNavigator.vue'

const route = useRoute()
const router = useRouter()
const store = useReadingStore()

const bookName = computed(() => route.params.book as string)
const chapter = computed(() => Number(route.params.chapter))

// Fetch chapter when route params change
watch(
  [bookName, chapter],
  ([book, ch]) => {
    if (book && ch) {
      store.fetchChapter(book, ch)
    }
  },
  { immediate: true },
)

function handlePerPage(value: PerPage) {
  store.setPerPage(value)
}

function goNextChapter() {
  const nextCh = chapter.value + 1
  router.push(`/reading/${bookName.value}/${nextCh}`)
}
</script>

<template>
  <div class="max-w-3xl mx-auto">
    <!-- Loading -->
    <div v-if="store.loading" class="flex justify-center py-20">
      <Spinner size="lg" />
    </div>

    <!-- Error -->
    <div v-else-if="store.error" class="text-center py-20">
      <p class="text-red-500">{{ store.error }}</p>
      <button
        @click="store.fetchChapter(bookName, chapter)"
        class="mt-4 px-4 py-2 text-sm text-amber-700 hover:bg-amber-50 rounded-lg transition-colors"
      >
        다시 시도
      </button>
    </div>

    <!-- Content -->
    <div v-else-if="store.verses.length > 0" class="animate-fadeIn">
      <!-- Header -->
      <div class="flex items-center justify-between mb-6">
        <div>
          <h1 class="text-2xl font-bold text-gray-800">
            {{ store.bookName }} {{ store.chapter }}장
          </h1>
          <p v-if="store.readCount > 0" class="text-sm text-amber-700 mt-1">
            {{ store.readCount }}회 통독 완료
          </p>
        </div>

        <PerPageSelector
          :model-value="store.perPage"
          @update:model-value="handlePerPage"
        />
      </div>

      <!-- Paginated verse view -->
      <div class="bg-white rounded-2xl shadow-sm border border-gray-100 p-4 sm:p-6 md:p-8">
        <VerseList
          :verses="store.pagedVerses"
          :last-read-verse="store.lastReadVerse"
        />

        <!-- Pagination -->
        <div class="mt-6 pt-4 border-t border-gray-100">
          <PageNavigator
            :current-page="store.currentPage"
            :total-pages="store.totalPages"
            @prev="store.prevPage()"
            @next="store.nextPage()"
            @go-to="store.goToPage($event)"
          />
        </div>
      </div>

      <!-- Complete button (last page only) -->
      <div v-if="store.isLastPage" class="mt-6 flex flex-col items-center gap-3">
        <button
          v-if="!store.chapterCompleted"
          @click="store.completeChapter()"
          :disabled="store.completing"
          class="px-8 py-3 text-white font-medium rounded-lg bg-gradient-to-r from-amber-600 to-amber-700 hover:from-amber-700 hover:to-amber-800 transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-2"
        >
          <svg
            v-if="store.completing"
            class="animate-spin h-5 w-5 text-white"
            xmlns="http://www.w3.org/2000/svg"
            fill="none"
            viewBox="0 0 24 24"
          >
            <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" />
            <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z" />
          </svg>
          통독 완료
        </button>

        <!-- After completion: next chapter button -->
        <div v-if="store.chapterCompleted" class="flex flex-col items-center gap-2">
          <p class="text-sm text-green-600 font-medium">
            {{ store.bookName }} {{ store.chapter }}장 통독 완료!
          </p>
          <button
            @click="goNextChapter()"
            class="px-8 py-3 text-white font-medium rounded-lg bg-gradient-to-r from-green-600 to-green-700 hover:from-green-700 hover:to-green-800 transition-all duration-200 flex items-center gap-2"
          >
            다음 장으로
            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
              <path stroke-linecap="round" stroke-linejoin="round" d="M13 7l5 5m0 0l-5 5m5-5H6" />
            </svg>
          </button>
        </div>
      </div>
    </div>

    <!-- Empty state -->
    <div v-else class="text-center py-20">
      <p class="text-gray-400">사이드바에서 성경 책과 장을 선택해주세요.</p>
    </div>
  </div>
</template>
