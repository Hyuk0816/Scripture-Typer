<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useGroupStore } from '@/stores/group'
import { useBibleStore } from '@/stores/bible'
import Spinner from '@/components/atoms/Spinner.vue'
import ButtonPrimary from '@/components/atoms/ButtonPrimary.vue'
import type { GroupPlanRequest } from '@/types/group'

const groupStore = useGroupStore()
const bibleStore = useBibleStore()
const router = useRouter()

const showCreateForm = ref(false)
const newPlan = ref<GroupPlanRequest>({
  bookName: '',
  startChapter: 1,
  endChapter: 1,
  mode: 'READING',
})
const creating = ref(false)
const error = ref('')

const allBooks = computed(() => [...bibleStore.oldTestament, ...bibleStore.newTestament])

const selectedBook = computed(() =>
  allBooks.value.find((b) => b.bookName === newPlan.value.bookName),
)

const maxChapter = computed(() => selectedBook.value?.totalChapters ?? 1)

const chapterOptions = computed(() =>
  Array.from({ length: maxChapter.value }, (_, i) => i + 1),
)

// Reset chapter range when book changes
watch(
  () => newPlan.value.bookName,
  () => {
    newPlan.value.startChapter = 1
    newPlan.value.endChapter = maxChapter.value
  },
)

// Ensure endChapter >= startChapter
watch(
  () => newPlan.value.startChapter,
  (val) => {
    if (newPlan.value.endChapter < val) {
      newPlan.value.endChapter = val
    }
  },
)

const activePlans = computed(() => groupStore.plans.filter((p) => p.status === 'ACTIVE'))
const completedPlans = computed(() => groupStore.plans.filter((p) => p.status === 'COMPLETED'))

onMounted(() => {
  groupStore.fetchPlans()
  if (allBooks.value.length === 0) {
    bibleStore.fetchBooks()
  }
})

async function handleCreate() {
  if (!newPlan.value.bookName) {
    error.value = '성경 책을 선택해주세요'
    return
  }
  error.value = ''
  creating.value = true
  try {
    const plan = await groupStore.createPlan(newPlan.value)
    showCreateForm.value = false
    newPlan.value = { bookName: '', startChapter: 1, endChapter: 1, mode: 'READING' }
    router.push(`/group/${plan.id}`)
  } catch {
    error.value = '생성에 실패했습니다. 소속이 지정되어 있는지 확인해주세요.'
  } finally {
    creating.value = false
  }
}

function formatDate(dateStr: string) {
  return new Date(dateStr).toLocaleDateString('ko-KR', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  })
}

function formatChapterRange(plan: { startChapter: number; endChapter: number }) {
  if (plan.startChapter === plan.endChapter) return `${plan.startChapter}장`
  return `${plan.startChapter}–${plan.endChapter}장`
}
</script>

<template>
  <div class="max-w-2xl mx-auto px-4 py-6 space-y-6">
    <div class="flex items-center justify-between">
      <h1 class="text-xl font-bold text-gray-800">그룹 통독/필사</h1>
      <button
        class="text-sm px-3 py-1.5 bg-amber-600 text-white rounded-lg hover:bg-amber-700 transition-colors"
        @click="showCreateForm = !showCreateForm"
      >
        {{ showCreateForm ? '취소' : '새 계획' }}
      </button>
    </div>

    <!-- Create form -->
    <div v-if="showCreateForm" class="bg-white rounded-2xl shadow-sm border border-gray-100 p-5 space-y-4">
      <h3 class="text-sm font-bold text-gray-700">새 그룹 계획 만들기</h3>

      <!-- Book selector -->
      <div>
        <label class="block text-sm font-medium text-gray-700 mb-1">성경 책</label>
        <select
          v-model="newPlan.bookName"
          class="w-full rounded-xl border border-gray-300 px-4 py-2.5 text-sm focus:border-amber-500 focus:ring-2 focus:ring-amber-200 outline-none bg-white"
        >
          <option value="" disabled>선택하세요</option>
          <optgroup label="구약">
            <option v-for="book in bibleStore.oldTestament" :key="book.bookName" :value="book.bookName">
              {{ book.bookName }} ({{ book.totalChapters }}장)
            </option>
          </optgroup>
          <optgroup label="신약">
            <option v-for="book in bibleStore.newTestament" :key="book.bookName" :value="book.bookName">
              {{ book.bookName }} ({{ book.totalChapters }}장)
            </option>
          </optgroup>
        </select>
      </div>

      <!-- Chapter range -->
      <div v-if="newPlan.bookName">
        <label class="block text-sm font-medium text-gray-700 mb-1">장 범위</label>
        <div class="flex items-center gap-2">
          <select
            v-model.number="newPlan.startChapter"
            class="flex-1 rounded-xl border border-gray-300 px-3 py-2.5 text-sm focus:border-amber-500 focus:ring-2 focus:ring-amber-200 outline-none bg-white"
          >
            <option v-for="ch in chapterOptions" :key="ch" :value="ch">{{ ch }}장</option>
          </select>
          <span class="text-gray-400 text-sm">~</span>
          <select
            v-model.number="newPlan.endChapter"
            class="flex-1 rounded-xl border border-gray-300 px-3 py-2.5 text-sm focus:border-amber-500 focus:ring-2 focus:ring-amber-200 outline-none bg-white"
          >
            <option v-for="ch in chapterOptions.filter((c) => c >= newPlan.startChapter)" :key="ch" :value="ch">
              {{ ch }}장
            </option>
          </select>
        </div>
        <p class="text-xs text-gray-400 mt-1">
          총 {{ newPlan.endChapter - newPlan.startChapter + 1 }}장
        </p>
      </div>

      <!-- Mode -->
      <div>
        <label class="block text-sm font-medium text-gray-700 mb-1">모드</label>
        <div class="flex gap-2">
          <button
            class="flex-1 px-4 py-2 text-sm rounded-lg border transition-colors"
            :class="newPlan.mode === 'READING' ? 'bg-amber-50 border-amber-300 text-amber-700' : 'border-gray-200 text-gray-500'"
            @click="newPlan.mode = 'READING'"
          >
            통독
          </button>
          <button
            class="flex-1 px-4 py-2 text-sm rounded-lg border transition-colors"
            :class="newPlan.mode === 'TYPING' ? 'bg-amber-50 border-amber-300 text-amber-700' : 'border-gray-200 text-gray-500'"
            @click="newPlan.mode = 'TYPING'"
          >
            필사
          </button>
        </div>
      </div>

      <p v-if="error" class="text-sm text-red-500">{{ error }}</p>

      <ButtonPrimary :loading="creating" :disabled="creating" @click="handleCreate">
        계획 생성
      </ButtonPrimary>
    </div>

    <div v-if="groupStore.loading" class="flex justify-center py-12">
      <Spinner />
    </div>

    <template v-else>
      <!-- Active plans -->
      <div v-if="activePlans.length > 0" class="space-y-3">
        <h2 class="text-sm font-semibold text-gray-500 uppercase tracking-wider">진행 중</h2>
        <div
          v-for="plan in activePlans"
          :key="plan.id"
          class="bg-white rounded-2xl shadow-sm border border-gray-100 p-4 cursor-pointer hover:border-amber-200 transition-colors"
          @click="router.push(`/group/${plan.id}`)"
        >
          <div class="flex items-center justify-between">
            <div>
              <span class="font-medium text-gray-800">{{ plan.bookName }}</span>
              <span class="ml-1 text-xs text-gray-500">{{ formatChapterRange(plan) }}</span>
              <span class="ml-2 text-xs px-2 py-0.5 rounded-full" :class="plan.mode === 'READING' ? 'bg-blue-50 text-blue-600' : 'bg-green-50 text-green-600'">
                {{ plan.mode === 'READING' ? '통독' : '필사' }}
              </span>
            </div>
            <span class="text-xs text-gray-400">{{ formatDate(plan.createdAt) }}</span>
          </div>
          <p class="text-xs text-gray-500 mt-1">{{ plan.affiliationName }} · {{ plan.createdByName }}</p>
        </div>
      </div>

      <!-- Completed plans -->
      <div v-if="completedPlans.length > 0" class="space-y-3">
        <h2 class="text-sm font-semibold text-gray-500 uppercase tracking-wider">완료</h2>
        <div
          v-for="plan in completedPlans"
          :key="plan.id"
          class="bg-gray-50 rounded-2xl border border-gray-100 p-4 cursor-pointer hover:border-gray-200 transition-colors"
          @click="router.push(`/group/${plan.id}`)"
        >
          <div class="flex items-center justify-between">
            <div>
              <span class="font-medium text-gray-600">{{ plan.bookName }}</span>
              <span class="ml-1 text-xs text-gray-400">{{ formatChapterRange(plan) }}</span>
              <span class="ml-2 text-xs px-2 py-0.5 rounded-full bg-gray-100 text-gray-500">완료</span>
            </div>
            <span class="text-xs text-gray-400">{{ formatDate(plan.createdAt) }}</span>
          </div>
        </div>
      </div>

      <!-- Empty state -->
      <div v-if="activePlans.length === 0 && completedPlans.length === 0" class="text-center py-12 text-gray-400">
        <p>아직 그룹 계획이 없습니다</p>
        <p class="text-sm mt-1">위의 "새 계획" 버튼으로 시작하세요</p>
      </div>
    </template>
  </div>
</template>
