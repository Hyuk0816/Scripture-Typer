<script setup lang="ts">
import Badge from '@/components/atoms/Badge.vue'
import type { Role, UserStatus } from '@/types/auth'

const props = defineProps<{
  type: 'status' | 'role'
  value: UserStatus | Role
}>()

const statusMap: Record<UserStatus, { label: string; variant: 'amber' | 'green' | 'red' | 'gray' }> = {
  PENDING: { label: '대기중', variant: 'amber' },
  ACTIVE: { label: '활성', variant: 'green' },
  INACTIVE: { label: '비활성', variant: 'red' },
}

const roleMap: Record<Role, { label: string; variant: 'amber' | 'green' | 'red' | 'gray' | 'blue' }> = {
  ADMIN: { label: '관리자', variant: 'red' },
  PASTOR: { label: '목사', variant: 'blue' },
  MOKJANG: { label: '목장', variant: 'green' },
  USER: { label: '일반', variant: 'gray' },
}

function getConfig() {
  if (props.type === 'status') {
    return statusMap[props.value as UserStatus]
  }
  return roleMap[props.value as Role]
}
</script>

<template>
  <Badge :variant="getConfig().variant">{{ getConfig().label }}</Badge>
</template>
