<script setup lang="ts">
import type { GroupMemberProgressResponse } from '@/types/group'
import MemberProgressBar from '@/components/molecules/MemberProgressBar.vue'

defineProps<{
  members: GroupMemberProgressResponse[]
  totalChapters: number
}>()
</script>

<template>
  <div class="bg-white rounded-2xl shadow-sm border border-gray-100 p-5">
    <h3 class="text-base font-bold text-gray-800 mb-4">멤버별 진행률</h3>

    <div v-if="members.length === 0" class="text-center py-6 text-gray-400">
      멤버가 없습니다
    </div>

    <div v-else class="space-y-1">
      <MemberProgressBar
        v-for="member in members"
        :key="member.userId"
        :user-name="member.userName"
        :completed-chapters="member.completedChapters"
        :total-chapters="totalChapters"
        :total-read-count="member.totalReadCount"
      />
    </div>
  </div>
</template>
