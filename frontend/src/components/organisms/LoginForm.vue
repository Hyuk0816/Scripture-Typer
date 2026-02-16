<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import FormField from '@/components/molecules/FormField.vue'
import ButtonPrimary from '@/components/atoms/ButtonPrimary.vue'
import ErrorMessage from '@/components/atoms/ErrorMessage.vue'
import { useAuthStore } from '@/stores/auth'
import type { LoginRequest } from '@/types/auth'
import axios from 'axios'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const form = reactive<LoginRequest>({
  email: '',
  password: '',
})

const errors = reactive({
  email: '',
  password: '',
})

const loading = ref(false)
const serverError = ref('')

function validate(): boolean {
  let valid = true
  errors.email = ''
  errors.password = ''

  if (!form.email.trim()) {
    errors.email = '이메일을 입력해주세요'
    valid = false
  } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)) {
    errors.email = '올바른 이메일 형식이 아닙니다'
    valid = false
  }

  if (!form.password) {
    errors.password = '비밀번호를 입력해주세요'
    valid = false
  }

  return valid
}

async function handleSubmit() {
  serverError.value = ''
  if (!validate()) return

  loading.value = true
  try {
    await authStore.login(form)
    const redirect = (route.query.redirect as string) || '/'
    router.push(redirect)
  } catch (err) {
    if (axios.isAxiosError(err) && err.response) {
      const { errorCode } = err.response.data
      if (errorCode === 'AUTH_INVALID_CREDENTIALS') {
        serverError.value = '이메일 또는 비밀번호가 올바르지 않습니다'
      } else if (errorCode === 'AUTH_ACCOUNT_NOT_APPROVED') {
        serverError.value = '관리자 승인 대기 중인 계정입니다'
      } else {
        serverError.value = '로그인에 실패했습니다. 다시 시도해주세요'
      }
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
    <FormField
      id="email"
      v-model="form.email"
      label="이메일"
      type="email"
      placeholder="이메일을 입력하세요"
      :error="errors.email"
      required
    />

    <FormField
      id="password"
      v-model="form.password"
      label="비밀번호"
      type="password"
      placeholder="비밀번호를 입력하세요"
      :error="errors.password"
      required
    />

    <ErrorMessage v-if="serverError" :message="serverError" />

    <ButtonPrimary type="submit" :loading="loading" :disabled="loading">
      로그인
    </ButtonPrimary>

    <p class="text-center text-sm text-gray-500">
      계정이 없으신가요?
      <router-link to="/signup" class="text-amber-600 hover:text-amber-700 font-medium">
        회원가입
      </router-link>
    </p>
  </form>
</template>
