<script setup lang="ts">
import { ref, reactive } from 'vue'
import FormField from '@/components/molecules/FormField.vue'
import AffiliationSelector from '@/components/molecules/AffiliationSelector.vue'
import ButtonPrimary from '@/components/atoms/ButtonPrimary.vue'
import ErrorMessage from '@/components/atoms/ErrorMessage.vue'
import { authApi } from '@/utils/api'
import type { VerifyIdentityRequest } from '@/types/user'
import axios from 'axios'

const emit = defineEmits<{
  verified: [identityData: VerifyIdentityRequest]
}>()

const form = reactive<VerifyIdentityRequest>({
  name: '',
  ttorae: 0,
  affiliationId: null,
  email: '',
})

const errors = reactive({
  name: '',
  ttorae: '',
  email: '',
})

const loading = ref(false)
const serverError = ref('')

function validate(): boolean {
  let valid = true
  errors.name = ''
  errors.ttorae = ''
  errors.email = ''

  if (!form.name.trim()) {
    errors.name = '이름을 입력해주세요'
    valid = false
  }
  if (!form.ttorae) {
    errors.ttorae = '또래를 입력해주세요'
    valid = false
  }
  if (!form.email.trim()) {
    errors.email = '이메일을 입력해주세요'
    valid = false
  } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)) {
    errors.email = '올바른 이메일 형식이 아닙니다'
    valid = false
  }

  return valid
}

async function handleSubmit() {
  serverError.value = ''
  if (!validate()) return

  loading.value = true
  try {
    await authApi.verifyIdentity(form)
    emit('verified', { ...form })
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
</script>

<template>
  <form @submit.prevent="handleSubmit" class="space-y-5">
    <h2 class="text-lg font-semibold text-gray-800 mb-1">비밀번호 찾기</h2>
    <p class="text-sm text-gray-500 mb-4">가입 시 입력한 정보를 입력해 본인 확인을 진행합니다.</p>

    <FormField
      id="name"
      v-model="form.name"
      label="이름"
      type="text"
      placeholder="이름을 입력하세요"
      :error="errors.name"
      required
    />

    <FormField
      id="ttorae"
      v-model.number="form.ttorae"
      label="또래"
      type="number"
      placeholder="또래를 입력하세요"
      :error="errors.ttorae"
      required
    />

    <AffiliationSelector v-model="form.affiliationId" />

    <FormField
      id="email"
      v-model="form.email"
      label="이메일"
      type="email"
      placeholder="가입 시 사용한 이메일"
      :error="errors.email"
      required
    />

    <ErrorMessage v-if="serverError" :message="serverError" />

    <ButtonPrimary type="submit" :loading="loading" :disabled="loading">
      본인 확인
    </ButtonPrimary>

    <p class="text-center text-sm text-gray-500">
      <router-link to="/login" class="text-amber-600 hover:text-amber-700 font-medium">
        로그인으로 돌아가기
      </router-link>
    </p>
  </form>
</template>
