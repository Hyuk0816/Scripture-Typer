<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useProgressStore } from '@/stores/progress'
import Spinner from '@/components/atoms/Spinner.vue'
import type { ReadingProgressResponse, TypingProgressResponse } from '@/types/progress'

const router = useRouter()
const progressStore = useProgressStore()

type TabId = 'reading' | 'typing'
const activeTab = ref<TabId>('reading')

onMounted(() => {
  progressStore.fetchAll()
})

// --- Reading tab data ---
const readingInProgress = computed(() =>
  progressStore.allReading.filter((p) => p.lastReadVerse > 0 && p.readCount === 0)
)
const readingCompleted = computed(() =>
  progressStore.allReading.filter((p) => p.readCount >= 1)
)

// --- Typing tab data ---
const typingInProgress = computed(() =>
  progressStore.allTyping.filter((p) => p.lastTypedVerse > 0 && p.readCount === 0)
)
const typingCompleted = computed(() =>
  progressStore.allTyping.filter((p) => p.readCount >= 1)
)

function navigateReading(p: ReadingProgressResponse) {
  router.push(`/reading/${p.bookName}/${p.chapter}`)
}

function navigateTyping(p: TypingProgressResponse, restart = false) {
  const query = restart ? { restart: 'true' } : undefined
  router.push({ path: `/typing/${p.bookName}/${p.chapter}`, query })
}
</script>

<template>
  <div class="max-w-3xl mx-auto">
    <!-- Header -->
    <div class="mb-6 mt-2">
      <h1 class="text-2xl font-bold text-gray-800">마이페이지</h1>
      <p class="text-sm text-gray-500 mt-1">나의 통독/필사 현황을 확인하세요</p>
    </div>

    <!-- Tab Bar -->
    <div class="flex gap-1 bg-gray-100 rounded-lg p-1 mb-6">
      <button
        @click="activeTab = 'reading'"
        class="flex-1 py-2 text-sm font-medium rounded-md transition-colors"
        :class="activeTab === 'reading' ? 'bg-white text-amber-700 shadow-sm' : 'text-gray-500 hover:text-gray-700'"
      >
        통독 현황
      </button>
      <button
        @click="activeTab = 'typing'"
        class="flex-1 py-2 text-sm font-medium rounded-md transition-colors"
        :class="activeTab === 'typing' ? 'bg-white text-amber-700 shadow-sm' : 'text-gray-500 hover:text-gray-700'"
      >
        필사 현황
      </button>
    </div>

    <!-- Loading -->
    <div v-if="progressStore.loadingAll" class="flex justify-center py-12">
      <Spinner />
    </div>

    <template v-else>
      <!-- 통독 탭 -->
      <div v-if="activeTab === 'reading'">
        <!-- 통계 그리드 -->
        <div class="grid grid-cols-3 gap-3 mb-6">
          <div class="bg-white rounded-xl shadow-sm border border-gray-100 p-4 text-center">
            <div class="text-2xl font-bold text-amber-600">{{ progressStore.readingStats.inProgress }}</div>
            <div class="text-xs text-gray-500 mt-1">통독중</div>
          </div>
          <div class="bg-white rounded-xl shadow-sm border border-gray-100 p-4 text-center">
            <div class="text-2xl font-bold text-green-600">{{ progressStore.readingStats.completed }}</div>
            <div class="text-xs text-gray-500 mt-1">통독완료</div>
          </div>
          <div class="bg-white rounded-xl shadow-sm border border-gray-100 p-4 text-center">
            <div class="text-2xl font-bold text-gray-700">{{ progressStore.readingStats.totalReadCount }}</div>
            <div class="text-xs text-gray-500 mt-1">총 회독수</div>
          </div>
        </div>

        <!-- 빈 상태 -->
        <div v-if="readingInProgress.length === 0 && readingCompleted.length === 0" class="bg-white rounded-xl shadow-sm border border-gray-100 p-10 text-center">
          <div class="text-4xl mb-4">📖</div>
          <h3 class="text-lg font-semibold text-gray-700 mb-2">아직 통독 기록이 없습니다</h3>
          <p class="text-sm text-gray-500">사이드바에서 원하는 책과 장을 선택하여 통독을 시작하세요.</p>
        </div>

        <!-- 통독중 -->
        <section v-if="readingInProgress.length > 0" class="mb-6">
          <h2 class="text-sm font-semibold text-amber-700 uppercase tracking-wider mb-3 flex items-center gap-2">
            <span class="w-2 h-2 bg-amber-500 rounded-full" />
            통독중 ({{ readingInProgress.length }})
          </h2>
          <div class="space-y-2">
            <button
              v-for="p in readingInProgress"
              :key="`${p.bookName}-${p.chapter}`"
              @click="navigateReading(p)"
              class="w-full bg-white rounded-xl shadow-sm border border-gray-100 p-4 text-left hover:shadow-md hover:border-gray-200 transition-all group"
            >
              <div class="flex items-center justify-between mb-1">
                <div class="flex items-center gap-3">
                  <h3 class="font-semibold text-gray-800">{{ p.bookName }} {{ p.chapter }}장</h3>
                  <span class="text-[10px] px-2 py-0.5 rounded-full bg-amber-100 text-amber-700 font-medium">통독중</span>
                </div>
              </div>
              <div class="mt-2 text-xs text-gray-400 group-hover:text-amber-600 transition-colors">
                말씀 읽기 &rarr;
              </div>
            </button>
          </div>
        </section>

        <!-- 통독완료 -->
        <section v-if="readingCompleted.length > 0">
          <h2 class="text-sm font-semibold text-green-700 uppercase tracking-wider mb-3 flex items-center gap-2">
            <span class="w-2 h-2 bg-green-500 rounded-full" />
            통독완료 ({{ readingCompleted.length }})
          </h2>
          <div class="space-y-2">
            <button
              v-for="p in readingCompleted"
              :key="`${p.bookName}-${p.chapter}`"
              @click="navigateReading(p)"
              class="w-full bg-white rounded-xl shadow-sm border border-gray-100 p-4 text-left hover:shadow-md hover:border-gray-200 transition-all group"
            >
              <div class="flex items-center justify-between mb-1">
                <div class="flex items-center gap-3">
                  <h3 class="font-semibold text-gray-800">{{ p.bookName }} {{ p.chapter }}장</h3>
                  <span class="text-[10px] px-2 py-0.5 rounded-full bg-green-100 text-green-700 font-medium">
                    {{ p.readCount }}회독
                  </span>
                </div>
              </div>
              <div class="mt-2 text-xs text-gray-400 group-hover:text-green-600 transition-colors">
                말씀 읽기 &rarr;
              </div>
            </button>
          </div>
        </section>
      </div>

      <!-- 필사 탭 -->
      <div v-if="activeTab === 'typing'">
        <!-- 통계 그리드 -->
        <div class="grid grid-cols-3 gap-3 mb-6">
          <div class="bg-white rounded-xl shadow-sm border border-gray-100 p-4 text-center">
            <div class="text-2xl font-bold text-amber-600">{{ progressStore.typingStats.inProgress }}</div>
            <div class="text-xs text-gray-500 mt-1">필사중</div>
          </div>
          <div class="bg-white rounded-xl shadow-sm border border-gray-100 p-4 text-center">
            <div class="text-2xl font-bold text-green-600">{{ progressStore.typingStats.completed }}</div>
            <div class="text-xs text-gray-500 mt-1">필사완료</div>
          </div>
          <div class="bg-white rounded-xl shadow-sm border border-gray-100 p-4 text-center">
            <div class="text-2xl font-bold text-gray-700">{{ progressStore.typingStats.totalReadCount }}</div>
            <div class="text-xs text-gray-500 mt-1">총 회독수</div>
          </div>
        </div>

        <!-- 빈 상태 -->
        <div v-if="typingInProgress.length === 0 && typingCompleted.length === 0" class="bg-white rounded-xl shadow-sm border border-gray-100 p-10 text-center">
          <div class="text-4xl mb-4">✍️</div>
          <h3 class="text-lg font-semibold text-gray-700 mb-2">아직 필사 기록이 없습니다</h3>
          <p class="text-sm text-gray-500">사이드바에서 원하는 책과 장을 선택하여 필사를 시작하세요.</p>
        </div>

        <!-- 필사중 -->
        <section v-if="typingInProgress.length > 0" class="mb-6">
          <h2 class="text-sm font-semibold text-amber-700 uppercase tracking-wider mb-3 flex items-center gap-2">
            <span class="w-2 h-2 bg-amber-500 rounded-full" />
            필사중 ({{ typingInProgress.length }})
          </h2>
          <div class="space-y-2">
            <button
              v-for="p in typingInProgress"
              :key="`${p.bookName}-${p.chapter}`"
              @click="navigateTyping(p)"
              class="w-full bg-white rounded-xl shadow-sm border border-gray-100 p-4 text-left hover:shadow-md hover:border-gray-200 transition-all group"
            >
              <div class="flex items-center justify-between mb-2">
                <div class="flex items-center gap-3">
                  <h3 class="font-semibold text-gray-800">{{ p.bookName }} {{ p.chapter }}장</h3>
                  <span class="text-[10px] px-2 py-0.5 rounded-full bg-amber-100 text-amber-700 font-medium">필사중</span>
                </div>
                <span class="text-xs text-gray-500">{{ p.lastTypedVerse }}/{{ p.totalVerses }}절</span>
              </div>
              <div class="flex items-center gap-3">
                <div class="flex-1 bg-gray-100 rounded-full h-1.5 overflow-hidden">
                  <div
                    class="h-full rounded-full bg-amber-500 transition-all duration-500"
                    :style="{ width: `${Math.round((p.lastTypedVerse / p.totalVerses) * 100)}%` }"
                  />
                </div>
              </div>
              <div class="mt-2 text-xs text-gray-400 group-hover:text-amber-600 transition-colors">
                이어서 필사하기 &rarr;
              </div>
            </button>
          </div>
        </section>

        <!-- 필사완료 -->
        <section v-if="typingCompleted.length > 0">
          <h2 class="text-sm font-semibold text-green-700 uppercase tracking-wider mb-3 flex items-center gap-2">
            <span class="w-2 h-2 bg-green-500 rounded-full" />
            필사완료 ({{ typingCompleted.length }})
          </h2>
          <div class="space-y-2">
            <button
              v-for="p in typingCompleted"
              :key="`${p.bookName}-${p.chapter}`"
              @click="navigateTyping(p, true)"
              class="w-full bg-white rounded-xl shadow-sm border border-gray-100 p-4 text-left hover:shadow-md hover:border-gray-200 transition-all group"
            >
              <div class="flex items-center justify-between mb-2">
                <div class="flex items-center gap-3">
                  <h3 class="font-semibold text-gray-800">{{ p.bookName }} {{ p.chapter }}장</h3>
                  <span class="text-[10px] px-2 py-0.5 rounded-full bg-green-100 text-green-700 font-medium">
                    {{ p.readCount }}회독
                  </span>
                </div>
                <span class="text-xs text-gray-500">{{ p.lastTypedVerse }}/{{ p.totalVerses }}절</span>
              </div>
              <div class="flex items-center gap-3">
                <div class="flex-1 bg-gray-100 rounded-full h-1.5 overflow-hidden">
                  <div class="h-full rounded-full bg-green-500 w-full" />
                </div>
              </div>
              <div class="mt-2 text-xs text-gray-400 group-hover:text-green-600 transition-colors">
                다시 필사하기 &rarr;
              </div>
            </button>
          </div>
        </section>
      </div>
    </template>
  </div>
</template>
