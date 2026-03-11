<script setup lang="ts">
import { ref } from 'vue'
import AuthTemplate from '@/components/templates/AuthTemplate.vue'
import ResetPasswordForm from '@/components/organisms/ResetPasswordForm.vue'
import NewPasswordModal from '@/components/organisms/NewPasswordModal.vue'
import type { VerifyIdentityRequest } from '@/types/user'

const showResetModal = ref(false)
const identityData = ref<VerifyIdentityRequest | null>(null)

function onVerified(data: VerifyIdentityRequest) {
  identityData.value = data
  showResetModal.value = true
}
</script>

<template>
  <AuthTemplate>
    <ResetPasswordForm @verified="onVerified" />
    <NewPasswordModal
      v-model="showResetModal"
      :identity-data="identityData"
    />
  </AuthTemplate>
</template>
