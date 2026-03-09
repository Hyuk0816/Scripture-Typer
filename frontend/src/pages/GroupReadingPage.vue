<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useGroupStore } from '@/stores/group'
import { useBibleStore } from '@/stores/bible'
import Spinner from '@/components/atoms/Spinner.vue'
import ButtonPrimary from '@/components/atoms/ButtonPrimary.vue'

const groupStore = useGroupStore()
const bibleStore = useBibleStore()
const router = useRouter()

const showCreateForm = ref(false)
const bookName = ref('')
const startChapter = ref(1)
const endChapter = ref(1)
const mode = ref<'READING' | 'TYPING'>('READING')
const selectedMemberIds = ref<number[]>([])
const creating = ref(false)
const error = ref('')

const allBooks = computed(() => [...bibleStore.oldTestament, ...bibleStore.newTestament])
const selectedBook = computed(() => allBooks.value.find((b) => b.bookName === bookName.value))
const maxChapter = computed(() => selectedBook.value?.totalChapters ?? 1)
const chapterOptions = computed(() => Array.from({ length: maxChapter.value }, (_, i) => i + 1))

watch(bookName, () => {
  startChapter.value = 1
  endChapter.value = maxChapter.value
})

watch(startChapter, (val) => {
  if (endChapter.value < val) endChapter.value = val
})

const activePlans = computed(() => groupStore.plans.filter((p) => p.status === 'ACTIVE'))
const completedPlans = computed(() => groupStore.plans.filter((p) => p.status === 'COMPLETED'))

const allMembersSelected = computed(
  () =>
    groupStore.affiliationMembers.length > 0 &&
    selectedMemberIds.value.length === groupStore.affiliationMembers.length,
)

function toggleAllMembers() {
  if (allMembersSelected.value) {
    selectedMemberIds.value = []
  } else {
    selectedMemberIds.value = groupStore.affiliationMembers.map((m) => m.userId)
  }
}

onMounted(() => {
  groupStore.fetchPlans()
  groupStore.fetchPendingInvites()
  if (allBooks.value.length === 0) bibleStore.fetchBooks()
})

function openCreateForm() {
  showCreateForm.value = true
  groupStore.fetchAffiliationMembers()
}

async function handleCreate() {
  if (!bookName.value) {
    error.value = '성경 책을 선택해주세요'
    return
  }
  if (selectedMemberIds.value.length === 0) {
    error.value = '초대할 멤버를 선택해주세요'
    return
  }
  error.value = ''
  creating.value = true
  try {
    const plan = await groupStore.createPlan({
      bookName: bookName.value,
      startChapter: startChapter.value,
      endChapter: endChapter.value,
      mode: mode.value,
      memberIds: selectedMemberIds.value,
    })
    showCreateForm.value = false
    bookName.value = ''
    startChapter.value = 1
    endChapter.value = 1
    mode.value = 'READING'
    selectedMemberIds.value = []
    router.push(`/group/${plan.id}`)
  } catch {
    error.value = '생성에 실패했습니다. 소속이 지정되어 있는지 확인해주세요.'
  } finally {
    creating.value = false
  }
}

async function handleAccept(planId: number) {
  await groupStore.acceptInvite(planId)
}

async function handleDecline(planId: number) {
  if (!confirm('이 초대를 거절하시겠습니까?')) return
  await groupStore.declineInvite(planId)
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
        @click="showCreateForm ? (showCreateForm = false) : openCreateForm()"
      >
        {{ showCreateForm ? '취소' : '새 계획' }}
      </button>
    </div>

    <!-- Pending invites -->
    <div v-if="groupStore.pendingInvites.length > 0" class="space-y-3">
      <h2 class="text-sm font-semibold text-amber-600 uppercase tracking-wider">받은 초대</h2>
      <div
        v-for="invite in groupStore.pendingInvites"
        :key="invite.planId"
        class="bg-amber-50 rounded-2xl border border-amber-200 p-4"
      >
        <div class="flex items-center justify-between">
          <div>
            <span class="font-medium text-gray-800">{{ invite.bookName }}</span>
            <span class="ml-1 text-xs text-gray-500">{{ formatChapterRange(invite) }}</span>
            <span
              class="ml-2 text-xs px-2 py-0.5 rounded-full"
              :class="invite.mode === 'READING' ? 'bg-blue-50 text-blue-600' : 'bg-green-50 text-green-600'"
            >
              {{ invite.mode === 'READING' ? '통독' : '필사' }}
            </span>
          </div>
        </div>
        <p class="text-xs text-gray-500 mt-1">{{ invite.createdByName }}님이 초대</p>
        <div class="flex gap-2 mt-3">
          <button
            class="flex-1 text-sm py-1.5 bg-amber-600 text-white rounded-lg hover:bg-amber-700 transition-colors"
            @click="handleAccept(invite.planId)"
          >
            수락
          </button>
          <button
            class="flex-1 text-sm py-1.5 border border-gray-300 text-gray-500 rounded-lg hover:bg-gray-50 transition-colors"
            @click="handleDecline(invite.planId)"
          >
            거절
          </button>
        </div>
      </div>
    </div>

    <!-- Create form -->
    <div v-if="showCreateForm" class="bg-white rounded-2xl shadow-sm border border-gray-100 p-5 space-y-4">
      <h3 class="text-sm font-bold text-gray-700">새 그룹 계획 만들기</h3>

      <!-- Book selector -->
      <div>
        <label class="block text-sm font-medium text-gray-700 mb-1">성경 책</label>
        <select
          v-model="bookName"
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
      <div v-if="bookName">
        <label class="block text-sm font-medium text-gray-700 mb-1">장 범위</label>
        <div class="flex items-center gap-2">
          <select
            v-model.number="startChapter"
            class="flex-1 rounded-xl border border-gray-300 px-3 py-2.5 text-sm focus:border-amber-500 focus:ring-2 focus:ring-amber-200 outline-none bg-white"
          >
            <option v-for="ch in chapterOptions" :key="ch" :value="ch">{{ ch }}장</option>
          </select>
          <span class="text-gray-400 text-sm">~</span>
          <select
            v-model.number="endChapter"
            class="flex-1 rounded-xl border border-gray-300 px-3 py-2.5 text-sm focus:border-amber-500 focus:ring-2 focus:ring-amber-200 outline-none bg-white"
          >
            <option v-for="ch in chapterOptions.filter((c) => c >= startChapter)" :key="ch" :value="ch">
              {{ ch }}장
            </option>
          </select>
        </div>
        <p class="text-xs text-gray-400 mt-1">총 {{ endChapter - startChapter + 1 }}장</p>
      </div>

      <!-- Mode -->
      <div>
        <label class="block text-sm font-medium text-gray-700 mb-1">모드</label>
        <div class="flex gap-2">
          <button
            class="flex-1 px-4 py-2 text-sm rounded-lg border transition-colors"
            :class="mode === 'READING' ? 'bg-amber-50 border-amber-300 text-amber-700' : 'border-gray-200 text-gray-500'"
            @click="mode = 'READING'"
          >
            통독
          </button>
          <button
            class="flex-1 px-4 py-2 text-sm rounded-lg border transition-colors"
            :class="mode === 'TYPING' ? 'bg-amber-50 border-amber-300 text-amber-700' : 'border-gray-200 text-gray-500'"
            @click="mode = 'TYPING'"
          >
            필사
          </button>
        </div>
      </div>

      <!-- Member selection -->
      <div>
        <div class="flex items-center justify-between mb-1">
          <label class="block text-sm font-medium text-gray-700">멤버 초대</label>
          <button
            v-if="groupStore.affiliationMembers.length > 0"
            class="text-xs text-amber-600 hover:text-amber-700"
            @click="toggleAllMembers"
          >
            {{ allMembersSelected ? '전체 해제' : '전체 선택' }}
          </button>
        </div>
        <div
          v-if="groupStore.affiliationMembers.length > 0"
          class="border border-gray-200 rounded-xl max-h-48 overflow-y-auto divide-y divide-gray-100"
        >
          <label
            v-for="member in groupStore.affiliationMembers"
            :key="member.userId"
            class="flex items-center gap-3 px-4 py-2.5 hover:bg-gray-50 cursor-pointer"
          >
            <input
              type="checkbox"
              :value="member.userId"
              v-model="selectedMemberIds"
              class="rounded border-gray-300 text-amber-600 focus:ring-amber-500"
            />
            <span class="text-sm text-gray-700">{{ member.name }}</span>
          </label>
        </div>
        <div v-else class="text-sm text-gray-400 space-y-1">
          <p>같은 소속의 멤버가 없습니다</p>
          <p class="text-xs">관리자 페이지에서 본인과 멤버들의 소속을 설정해주세요</p>
        </div>
        <p v-if="selectedMemberIds.length > 0" class="text-xs text-gray-400 mt-1">
          {{ selectedMemberIds.length }}명 선택됨
        </p>
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
              <span
                class="ml-2 text-xs px-2 py-0.5 rounded-full"
                :class="plan.mode === 'READING' ? 'bg-blue-50 text-blue-600' : 'bg-green-50 text-green-600'"
              >
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
      <div
        v-if="activePlans.length === 0 && completedPlans.length === 0 && groupStore.pendingInvites.length === 0"
        class="text-center py-12 text-gray-400"
      >
        <p>아직 그룹 계획이 없습니다</p>
        <p class="text-sm mt-1">위의 "새 계획" 버튼으로 시작하세요</p>
      </div>
    </template>
  </div>
</template>
