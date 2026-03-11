<script setup lang="ts">
import type { UserProfileResponse } from '@/types/user'

defineProps<{
  profile: UserProfileResponse
}>()

const emit = defineEmits<{
  changePassword: []
}>()
</script>

<template>
  <div class="bg-white rounded-xl shadow-sm border border-gray-100 p-5 mb-6">
    <h2 class="text-sm font-semibold text-gray-500 uppercase tracking-wider mb-4">개인정보</h2>
    <div class="grid grid-cols-2 gap-y-3 gap-x-4 text-sm">
      <div>
        <span class="text-gray-400">이름</span>
        <p class="font-medium text-gray-800 mt-0.5">{{ profile.name }}</p>
      </div>
      <div>
        <span class="text-gray-400">또래</span>
        <p class="font-medium text-gray-800 mt-0.5">{{ profile.ttorae }}</p>
      </div>
      <div>
        <span class="text-gray-400">이메일</span>
        <p class="font-medium text-gray-800 mt-0.5">{{ profile.email }}</p>
      </div>
      <div>
        <span class="text-gray-400">소속</span>
        <p class="font-medium text-gray-800 mt-0.5">{{ profile.affiliationName || '없음' }}</p>
      </div>
    </div>
    <div class="mt-4 pt-4 border-t border-gray-100">
      <button
        @click="emit('changePassword')"
        class="text-sm text-amber-600 hover:text-amber-700 font-medium transition-colors"
      >
        비밀번호 변경
      </button>
    </div>
  </div>
</template>
