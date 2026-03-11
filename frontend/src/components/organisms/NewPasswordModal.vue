<script setup lang="ts">
import { ref, reactive, watch } from 'vue'
import { useRouter } from 'vue-router'
import FormField from '@/components/molecules/FormField.vue'
import ButtonPrimary from '@/components/atoms/ButtonPrimary.vue'
import ErrorMessage from '@/components/atoms/ErrorMessage.vue'
import { authApi } from '@/utils/api'
import type { VerifyIdentityRequest } from '@/types/user'
import axios from 'axios'

const props = defineProps<{
  modelValue: boolean
  identityData: VerifyIdentityRequest | null
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
}>()

const router = useRouter()

const form = reactive({
  newPassword: '',
  newPasswordConfirm: '',
})

const errors = reactive({
  newPassword: '',
  newPasswordConfirm: '',
})

const loading = ref(false)
const serverError = ref('')
const successMessage = ref('')

watch(() => props.modelValue, (open) => {
  if (open) {
    form.newPassword = ''
    form.newPasswordConfirm = ''
    errors.newPassword = ''
    errors.newPasswordConfirm = ''
    serverError.value = ''
    successMessage.value = ''
  }
})

function validate(): boolean {
  let valid = true
  errors.newPassword = ''
  errors.newPasswordConfirm = ''

  if (!form.newPassword) {
    errors.newPassword = '새 비밀번호를 입력해주세요'
    valid = false
  } else if (form.newPassword.length < 4) {
    errors.newPassword = '비밀번호는 4자 이상이어야 합니다'
    valid = false
  }
  if (!form.newPasswordConfirm) {
    errors.newPasswordConfirm = '새 비밀번호 확인을 입력해주세요'
    valid = false
  } else if (form.newPassword !== form.newPasswordConfirm) {
    errors.newPasswordConfirm = '새 비밀번호가 일치하지 않습니다'
    valid = false
  }

  return valid
}

async function handleSubmit() {
  serverError.value = ''
  successMessage.value = ''
  if (!validate() || !props.identityData) return

  loading.value = true
  try {
    await authApi.resetPassword({
      ...props.identityData,
      newPassword: form.newPassword,
      newPasswordConfirm: form.newPasswordConfirm,
    })
    successMessage.value = '비밀번호가 재설정되었습니다. 로그인 페이지로 이동합니다.'
    setTimeout(() => {
      emit('update:modelValue', false)
      router.push('/login')
    }, 1500)
  } catch (err) {
    if (axios.isAxiosError(err) && err.response?.data?.message) {
      serverError.value = err.response.data.message
    } else {
      serverError.value = '네트워크 오류가 발생했습니다'
    }
  } finally {
    loading.value = false
  }
}

function handleBackdrop(e: MouseEvent) {
  if (e.target === e.currentTarget) emit('update:modelValue', false)
}
</script>

<template>
  <Teleport to="body">
    <div
      v-if="modelValue"
      class="fixed inset-0 z-50 flex items-center justify-center bg-black/40"
      @click="handleBackdrop"
    >
      <div class="bg-white rounded-2xl shadow-xl w-[90vw] max-w-md mx-4">
        <!-- Header -->
        <div class="flex items-center justify-between px-5 py-4 border-b border-gray-100">
          <h3 class="font-semibold text-gray-800">비밀번호 재설정</h3>
          <button
            @click="emit('update:modelValue', false)"
            class="p-1 rounded-lg hover:bg-gray-100 transition-colors"
            aria-label="닫기"
          >
            <svg class="w-5 h-5 text-gray-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>

        <!-- Body -->
        <form @submit.prevent="handleSubmit" class="p-5 space-y-4">
          <FormField
            id="newPasswordReset"
            v-model="form.newPassword"
            label="새 비밀번호"
            type="password"
            placeholder="새 비밀번호를 입력하세요"
            :error="errors.newPassword"
            required
          />
          <FormField
            id="newPasswordConfirmReset"
            v-model="form.newPasswordConfirm"
            label="새 비밀번호 확인"
            type="password"
            placeholder="새 비밀번호를 다시 입력하세요"
            :error="errors.newPasswordConfirm"
            required
          />

          <ErrorMessage v-if="serverError" :message="serverError" />

          <div v-if="successMessage" class="text-sm text-green-600 font-medium text-center py-1">
            {{ successMessage }}
          </div>

          <ButtonPrimary type="submit" :loading="loading" :disabled="loading">
            비밀번호 재설정
          </ButtonPrimary>
        </form>
      </div>
    </div>
  </Teleport>
</template>
