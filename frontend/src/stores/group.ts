import { ref } from 'vue'
import { defineStore } from 'pinia'
import { groupApi } from '@/utils/api'
import type {
  GroupPlanResponse,
  GroupPlanDetailResponse,
  GroupPlanRequest,
  GroupInviteResponse,
  AffiliationMemberResponse,
} from '@/types/group'

export const useGroupStore = defineStore('group', () => {
  const plans = ref<GroupPlanResponse[]>([])
  const currentPlan = ref<GroupPlanDetailResponse | null>(null)
  const pendingInvites = ref<GroupInviteResponse[]>([])
  const pendingInviteCount = ref(0)
  const affiliationMembers = ref<AffiliationMemberResponse[]>([])
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

  async function fetchPendingInvites() {
    try {
      const { data } = await groupApi.getPendingInvites()
      pendingInvites.value = data
      pendingInviteCount.value = data.length
    } catch (e) {
      console.error('fetchPendingInvites failed:', e)
    }
  }

  async function fetchPendingInviteCount() {
    try {
      const { data } = await groupApi.getPendingInviteCount()
      pendingInviteCount.value = data.count
    } catch (e) {
      console.error('fetchPendingInviteCount failed:', e)
    }
  }

  async function acceptInvite(planId: number) {
    await groupApi.acceptInvite(planId)
    pendingInvites.value = pendingInvites.value.filter((i) => i.planId !== planId)
    pendingInviteCount.value = Math.max(0, pendingInviteCount.value - 1)
    await fetchPlans()
  }

  async function declineInvite(planId: number) {
    await groupApi.declineInvite(planId)
    pendingInvites.value = pendingInvites.value.filter((i) => i.planId !== planId)
    pendingInviteCount.value = Math.max(0, pendingInviteCount.value - 1)
  }

  async function fetchAffiliationMembers() {
    try {
      const { data } = await groupApi.getAffiliationMembers()
      affiliationMembers.value = data
    } catch (e) {
      console.error('fetchAffiliationMembers failed:', e)
    }
  }

  return {
    plans,
    currentPlan,
    pendingInvites,
    pendingInviteCount,
    affiliationMembers,
    loading,
    fetchPlans,
    fetchPlanDetail,
    createPlan,
    completePlan,
    fetchPendingInvites,
    fetchPendingInviteCount,
    acceptInvite,
    declineInvite,
    fetchAffiliationMembers,
  }
})
