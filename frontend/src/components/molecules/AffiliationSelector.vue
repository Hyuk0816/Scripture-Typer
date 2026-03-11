<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import { affiliationApi } from '@/utils/api'
import type { AffiliationResponse, MainAffiliation } from '@/types/affiliation'
import { MAIN_AFFILIATION_LABELS } from '@/types/affiliation'

const emit = defineEmits<{
  'update:modelValue': [value: number | null]
}>()

defineProps<{
  modelValue: number | null
}>()

const affiliations = ref<AffiliationResponse[]>([])
const selectedMain = ref<MainAffiliation | ''>('')
const selectedAffiliationId = ref<number | null>(null)

const mainOptions: MainAffiliation[] = ['SARANGBANG', 'SAEGAJOK', 'IMWONDAN', 'MOKJANG', 'SINON']

const filteredSubs = ref<AffiliationResponse[]>([])

onMounted(async () => {
  try {
    const { data } = await affiliationApi.getAll()
    affiliations.value = data
  } catch {
    // silent
  }
})

watch(selectedMain, (main) => {
  selectedAffiliationId.value = null
  if (!main) {
    filteredSubs.value = []
    emit('update:modelValue', null)
    return
  }
  filteredSubs.value = affiliations.value.filter((a) => a.mainAffiliation === main)

  // 세부 소속이 없는 경우 (임원단, 목장, 신혼부부) 자동 선택
  const first = filteredSubs.value[0]
  if (filteredSubs.value.length === 1 && first && !first.subAffiliation) {
    selectedAffiliationId.value = first.id
    emit('update:modelValue', first.id)
  }
})

watch(selectedAffiliationId, (id) => {
  emit('update:modelValue', id)
})

const needsSubSelector = ref(false)
watch(filteredSubs, (subs) => {
  needsSubSelector.value = subs.length > 1 || (subs.length === 1 && subs[0]?.subAffiliation !== null)
})
</script>

<template>
  <div class="space-y-3">
    <div>
      <label class="block text-sm font-medium text-gray-700 mb-1">소속</label>
      <select
        v-model="selectedMain"
        class="w-full rounded-xl border border-gray-300 px-4 py-2.5 text-sm focus:border-amber-500 focus:ring-2 focus:ring-amber-200 outline-none transition-all bg-white"
      >
        <option value="">소속을 선택하세요</option>
        <option v-for="main in mainOptions" :key="main" :value="main">
          {{ MAIN_AFFILIATION_LABELS[main] }}
        </option>
      </select>
    </div>

    <div v-if="needsSubSelector">
      <label class="block text-sm font-medium text-gray-700 mb-1">세부 소속</label>
      <select
        v-model="selectedAffiliationId"
        class="w-full rounded-xl border border-gray-300 px-4 py-2.5 text-sm focus:border-amber-500 focus:ring-2 focus:ring-amber-200 outline-none transition-all bg-white"
      >
        <option :value="null">세부 소속 선택</option>
        <option v-for="aff in filteredSubs" :key="aff.id" :value="aff.id">
          {{ aff.subAffiliation || aff.displayName }}
        </option>
      </select>
    </div>
  </div>
</template>
