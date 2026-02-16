<script setup lang="ts">
import { ref, reactive } from 'vue'
import FormField from '@/components/molecules/FormField.vue'
import ButtonPrimary from '@/components/atoms/ButtonPrimary.vue'
import ButtonSecondary from '@/components/atoms/ButtonSecondary.vue'
import ErrorMessage from '@/components/atoms/ErrorMessage.vue'
import { useAuthStore } from '@/stores/auth'
import type { SignupRequest } from '@/types/auth'
import axios from 'axios'

const authStore = useAuthStore()

const form = reactive<SignupRequest & { passwordConfirm: string }>({
  name: '',
  ttorae: 0,
  phone: '',
  email: '',
  password: '',
  passwordConfirm: '',
})

const errors = reactive({
  name: '',
  ttorae: '',
  phone: '',
  email: '',
  password: '',
  passwordConfirm: '',
})

const loading = ref(false)
const serverError = ref('')
const signupComplete = ref(false)

function validate(): boolean {
  let valid = true
  Object.keys(errors).forEach((key) => {
    ;(errors as Record<string, string>)[key] = ''
  })

  if (!form.name.trim()) {
    errors.name = '이름을 입력해주세요'
    valid = false
  }

  if (!form.ttorae || form.ttorae <= 0) {
    errors.ttorae = '또래를 입력해주세요'
    valid = false
  }

  if (!form.phone.trim()) {
    errors.phone = '전화번호를 입력해주세요'
    valid = false
  }

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
  } else if (form.password.length < 4) {
    errors.password = '비밀번호는 4자 이상이어야 합니다'
    valid = false
  }

  if (form.password !== form.passwordConfirm) {
    errors.passwordConfirm = '비밀번호가 일치하지 않습니다'
    valid = false
  }

  return valid
}

async function handleSubmit() {
  serverError.value = ''
  if (!validate()) return

  loading.value = true
  try {
    const { passwordConfirm: _, ...signupData } = form
    await authStore.signup(signupData)
    signupComplete.value = true
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
  <!-- 가입 완료 메시지 -->
  <div v-if="signupComplete" class="text-center space-y-4 animate-fadeIn">
    <div class="w-16 h-16 mx-auto bg-amber-100 rounded-full flex items-center justify-center">
      <svg class="w-8 h-8 text-amber-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
      </svg>
    </div>
    <h3 class="text-lg font-semibold text-gray-800">회원가입이 완료되었습니다</h3>
    <p class="text-sm text-gray-500">
      관리자 승인 후 로그인할 수 있습니다.<br />
      승인이 완료되면 로그인해주세요.
    </p>
    <router-link to="/login">
      <ButtonSecondary class="mt-4">로그인 페이지로 이동</ButtonSecondary>
    </router-link>
  </div>

  <!-- 가입 폼 -->
  <form v-else @submit.prevent="handleSubmit" class="space-y-4">
    <FormField
      id="name"
      v-model="form.name"
      label="이름"
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

    <FormField
      id="phone"
      v-model="form.phone"
      label="전화번호"
      placeholder="010-0000-0000"
      :error="errors.phone"
      required
    />

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

    <FormField
      id="passwordConfirm"
      v-model="form.passwordConfirm"
      label="비밀번호 확인"
      type="password"
      placeholder="비밀번호를 다시 입력하세요"
      :error="errors.passwordConfirm"
      required
    />

    <ErrorMessage v-if="serverError" :message="serverError" />

    <ButtonPrimary type="submit" :loading="loading" :disabled="loading">
      회원가입
    </ButtonPrimary>

    <p class="text-center text-sm text-gray-500">
      이미 계정이 있으신가요?
      <router-link to="/login" class="text-amber-600 hover:text-amber-700 font-medium">
        로그인
      </router-link>
    </p>
  </form>
</template>
