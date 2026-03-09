<script setup lang="ts">
import { onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useGroupStore } from '@/stores/group'
import GroupProgressGrid from '@/components/organisms/GroupProgressGrid.vue'
import Spinner from '@/components/atoms/Spinner.vue'

const route = useRoute()
const router = useRouter()
const groupStore = useGroupStore()

const planId = computed(() => Number(route.params.planId))

onMounted(() => {
  groupStore.fetchPlanDetail(planId.value)
})

async function handleComplete() {
  if (!confirm('이 계획을 완료 처리하시겠습니까?')) return
  await groupStore.completePlan(planId.value)
  router.push('/group')
}

function formatDate(dateStr: string) {
  return new Date(dateStr).toLocaleDateString('ko-KR', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
  })
}
</script>

<template>
  <div class="max-w-2xl mx-auto px-4 py-6 space-y-6">
    <button
      @click="router.push('/group')"
      class="text-sm text-gray-500 hover:text-gray-700 flex items-center gap-1"
    >
      <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
      </svg>
      목록으로
    </button>

    <div v-if="groupStore.loading" class="flex justify-center py-12">
      <Spinner />
    </div>

    <template v-else-if="groupStore.currentPlan">
      <!-- Plan info -->
      <div class="bg-white rounded-2xl shadow-sm border border-gray-100 p-5">
        <div class="flex items-center justify-between">
          <div>
            <h1 class="text-xl font-bold text-gray-800">{{ groupStore.currentPlan.bookName }}</h1>
            <div class="flex items-center gap-2 mt-1">
              <span class="text-xs px-2 py-0.5 rounded-full" :class="groupStore.currentPlan.mode === 'READING' ? 'bg-blue-50 text-blue-600' : 'bg-green-50 text-green-600'">
                {{ groupStore.currentPlan.mode === 'READING' ? '통독' : '필사' }}
              </span>
              <span class="text-xs px-2 py-0.5 rounded-full" :class="groupStore.currentPlan.status === 'ACTIVE' ? 'bg-amber-50 text-amber-600' : 'bg-gray-100 text-gray-500'">
                {{ groupStore.currentPlan.status === 'ACTIVE' ? '진행 중' : '완료' }}
              </span>
            </div>
          </div>
          <button
            v-if="groupStore.currentPlan.status === 'ACTIVE'"
            class="text-sm px-3 py-1.5 bg-green-600 text-white rounded-lg hover:bg-green-700 transition-colors"
            @click="handleComplete"
          >
            완료 처리
          </button>
        </div>

        <div class="mt-3 text-sm text-gray-500 space-y-1">
          <p>소속: {{ groupStore.currentPlan.affiliationName }}</p>
          <p>생성자: {{ groupStore.currentPlan.createdByName }}</p>
          <p>생성일: {{ formatDate(groupStore.currentPlan.createdAt) }}</p>
          <p>범위: {{ groupStore.currentPlan.startChapter }}–{{ groupStore.currentPlan.endChapter }}장 (총 {{ groupStore.currentPlan.totalChapters }}장)</p>
        </div>
      </div>

      <!-- Member progress -->
      <GroupProgressGrid
        :members="groupStore.currentPlan.members"
        :total-chapters="groupStore.currentPlan.totalChapters"
      />
    </template>

    <div v-else class="text-center py-12 text-gray-400">
      계획을 찾을 수 없습니다
    </div>
  </div>
</template>
