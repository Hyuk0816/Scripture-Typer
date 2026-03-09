import { ref } from 'vue'
import { defineStore } from 'pinia'
import { groupApi } from '@/utils/api'
import type { GroupPlanResponse, GroupPlanDetailResponse, GroupPlanRequest } from '@/types/group'

export const useGroupStore = defineStore('group', () => {
  const plans = ref<GroupPlanResponse[]>([])
  const currentPlan = ref<GroupPlanDetailResponse | null>(null)
  const loading = ref(false)

  async function fetchPlans() {
    loading.value = true
    try {
      const { data } = await groupApi.getMyPlans()
      plans.value = data
    } catch (e) {
      console.error('fetchPlans failed:', e)
    } finally {
      loading.value = false
    }
  }

  async function fetchPlanDetail(id: number) {
    loading.value = true
    try {
      const { data } = await groupApi.getPlanDetail(id)
      currentPlan.value = data
    } catch (e) {
      console.error('fetchPlanDetail failed:', e)
    } finally {
      loading.value = false
    }
  }

  async function createPlan(request: GroupPlanRequest) {
    const { data } = await groupApi.createPlan(request)
    plans.value.unshift(data)
    return data
  }

  async function completePlan(id: number) {
    await groupApi.completePlan(id)
    await fetchPlans()
  }

  return {
    plans,
    currentPlan,
    loading,
    fetchPlans,
    fetchPlanDetail,
    createPlan,
    completePlan,
  }
})
