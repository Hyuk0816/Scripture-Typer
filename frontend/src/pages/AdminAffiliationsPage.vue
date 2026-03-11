<script setup lang="ts">
import { ref, computed, reactive, onMounted } from 'vue'
import AdminTemplate from '@/components/templates/AdminTemplate.vue'
import Spinner from '@/components/atoms/Spinner.vue'
import PageNavigator from '@/components/molecules/PageNavigator.vue'
import type { AffiliationResponse, MainAffiliation } from '@/types/affiliation'
import { MAIN_AFFILIATION_LABELS } from '@/types/affiliation'
import { adminApi } from '@/utils/api'

const PER_PAGE = 10

const affiliations = ref<AffiliationResponse[]>([])
const loading = ref(false)
const saving = ref(false)
const deletingId = ref<number | null>(null)

const newMain = ref<MainAffiliation>('SARANGBANG')
const newSub = ref('')

const mainOptions = Object.entries(MAIN_AFFILIATION_LABELS) as [MainAffiliation, string][]

const grouped = computed(() => {
  const map = new Map<MainAffiliation, AffiliationResponse[]>()
  for (const aff of affiliations.value) {
    const list = map.get(aff.mainAffiliation) || []
    list.push(aff)
    map.set(aff.mainAffiliation, list)
  }
  return map
})

const pages = reactive(new Map<MainAffiliation, number>())

function getPage(main: MainAffiliation): number {
  return pages.get(main) || 1
}

function setPage(main: MainAffiliation, page: number) {
  pages.set(main, page)
}

function totalPages(items: AffiliationResponse[]): number {
  return Math.max(1, Math.ceil(items.length / PER_PAGE))
}

function pagedItems(main: MainAffiliation, items: AffiliationResponse[]): AffiliationResponse[] {
  const page = getPage(main)
  const start = (page - 1) * PER_PAGE
  return items.slice(start, start + PER_PAGE)
}

const displayName = computed(() => {
  if (newSub.value.trim()) return newSub.value.trim()
  return MAIN_AFFILIATION_LABELS[newMain.value]
})

async function fetchAffiliations() {
  loading.value = true
  try {
    const { data } = await adminApi.getAffiliations()
    affiliations.value = data
  } finally {
    loading.value = false
  }
}

async function handleCreate() {
  if (!newSub.value.trim() && affiliations.value.some(a => a.mainAffiliation === newMain.value && !a.subAffiliation)) {
    alert('이미 존재하는 소속입니다.')
    return
  }
  saving.value = true
  try {
    await adminApi.createAffiliation({
      mainAffiliation: newMain.value,
      subAffiliation: newSub.value.trim() || undefined,
      displayName: displayName.value,
    })
    newSub.value = ''
    await fetchAffiliations()
  } finally {
    saving.value = false
  }
}

async function handleDelete(id: number, name: string) {
  if (!confirm(`"${name}" 소속을 삭제하시겠습니까?\n해당 소속의 회원은 소속 미지정 상태가 됩니다.`)) return
  deletingId.value = id
  try {
    await adminApi.deleteAffiliation(id)
    await fetchAffiliations()
  } finally {
    deletingId.value = null
  }
}

onMounted(fetchAffiliations)
</script>

<template>
  <AdminTemplate>
    <div class="space-y-6">
      <!-- Header with nav tabs -->
      <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3">
        <div>
          <h2 class="text-lg sm:text-xl font-bold text-gray-800">소속 관리</h2>
          <p class="mt-1 text-sm text-gray-500">사랑방 등 소속을 추가하거나 삭제할 수 있습니다</p>
        </div>
        <div class="flex gap-2">
          <router-link
            to="/admin/users"
            class="rounded-lg border border-gray-200 px-3 py-1.5 text-sm text-gray-600 hover:bg-gray-50 transition-colors"
          >
            회원 관리
          </router-link>
          <router-link
            to="/admin/affiliations"
            class="rounded-lg bg-amber-600 px-3 py-1.5 text-sm font-medium text-white"
          >
            소속 관리
          </router-link>
          <router-link
            to="/admin/stats"
            class="rounded-lg border border-gray-200 px-3 py-1.5 text-sm text-gray-600 hover:bg-gray-50 transition-colors"
          >
            통계
          </router-link>
        </div>
      </div>

      <!-- Add form -->
      <div class="bg-white rounded-2xl shadow-sm border border-gray-100 p-4 sm:p-6">
        <h3 class="text-sm font-semibold text-gray-700 mb-4">소속 추가</h3>
        <form class="flex flex-col sm:flex-row gap-3 items-start sm:items-end" @submit.prevent="handleCreate">
          <div class="w-full sm:w-auto">
            <label class="block text-xs text-gray-500 mb-1">메인 소속</label>
            <select
              v-model="newMain"
              class="w-full sm:w-36 text-sm border border-gray-200 rounded-lg px-3 py-2"
            >
              <option v-for="[value, label] in mainOptions" :key="value" :value="value">
                {{ label }}
              </option>
            </select>
          </div>
          <div class="w-full sm:w-auto flex-1">
            <label class="block text-xs text-gray-500 mb-1">세부 소속 (선택)</label>
            <input
              v-model="newSub"
              type="text"
              placeholder="예: 안재성 사랑방"
              class="w-full text-sm border border-gray-200 rounded-lg px-3 py-2"
            />
          </div>
          <div class="w-full sm:w-auto">
            <label class="block text-xs text-gray-500 mb-1">표시 이름</label>
            <span class="block text-sm text-gray-700 py-2">{{ displayName }}</span>
          </div>
          <button
            type="submit"
            :disabled="saving"
            class="w-full sm:w-auto px-4 py-2 text-sm font-medium text-white bg-amber-600 rounded-lg hover:bg-amber-700 transition-colors disabled:opacity-50"
          >
            {{ saving ? '추가 중...' : '추가' }}
          </button>
        </form>
      </div>

      <!-- Affiliation list -->
      <div class="bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden">
        <div v-if="loading" class="flex justify-center py-12">
          <Spinner />
        </div>

        <div v-else-if="affiliations.length === 0" class="text-center py-12 text-gray-400">
          등록된 소속이 없습니다
        </div>

        <div v-else class="divide-y divide-gray-100">
          <div v-for="[main, items] in grouped" :key="main" class="p-4 sm:p-6">
            <h3 class="text-sm font-semibold text-gray-700 mb-3">
              {{ MAIN_AFFILIATION_LABELS[main] }}
              <span class="text-gray-400 font-normal ml-1">{{ items.length }}개</span>
            </h3>
            <div class="space-y-2">
              <div
                v-for="aff in pagedItems(main, items)"
                :key="aff.id"
                class="flex items-center justify-between bg-gray-50 rounded-lg px-4 py-2.5"
              >
                <span class="text-sm text-gray-800">{{ aff.displayName }}</span>
                <button
                  :disabled="deletingId === aff.id"
                  class="px-3 py-1 text-xs font-medium text-red-600 hover:text-red-700 hover:bg-red-50 rounded-lg transition-colors disabled:opacity-50"
                  @click="handleDelete(aff.id, aff.displayName)"
                >
                  {{ deletingId === aff.id ? '삭제 중...' : '삭제' }}
                </button>
              </div>
            </div>
            <PageNavigator
              v-if="totalPages(items) > 1"
              :current-page="getPage(main)"
              :total-pages="totalPages(items)"
              class="pt-3"
              @prev="setPage(main, getPage(main) - 1)"
              @next="setPage(main, getPage(main) + 1)"
              @go-to="(p: number) => setPage(main, p)"
            />
          </div>
        </div>
      </div>
    </div>
  </AdminTemplate>
</template>
