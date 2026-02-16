import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { AuthUser, JwtPayload, LoginRequest, SignupRequest } from '@/types/auth'
import { authApi, setTokens, clearTokens, getAccessToken } from '@/utils/api'

function decodeJwt(token: string): JwtPayload | null {
  try {
    const base64Url = token.split('.')[1]
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/')
    const jsonPayload = decodeURIComponent(
      atob(base64)
        .split('')
        .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
        .join(''),
    )
    return JSON.parse(jsonPayload) as JwtPayload
  } catch {
    return null
  }
}

function isTokenExpired(payload: JwtPayload): boolean {
  return Date.now() >= payload.exp * 1000
}

export const useAuthStore = defineStore('auth', () => {
  const user = ref<AuthUser | null>(null)

  const isAuthenticated = computed(() => user.value !== null)
  const isAdmin = computed(() => user.value?.role === 'ADMIN')

  function setUserFromToken(token: string): boolean {
    const payload = decodeJwt(token)
    if (!payload || isTokenExpired(payload)) {
      return false
    }
    user.value = {
      id: Number(payload.sub),
      email: payload.email,
      role: payload.role,
    }
    return true
  }

  function initializeAuth(): void {
    const token = getAccessToken()
    if (token) {
      if (!setUserFromToken(token)) {
        clearTokens()
        user.value = null
      }
    }
  }

  async function login(credentials: LoginRequest): Promise<void> {
    const { data } = await authApi.login(credentials)
    setTokens(data)
    setUserFromToken(data.accessToken)
  }

  async function signup(data: SignupRequest): Promise<void> {
    await authApi.signup(data)
  }

  async function logout(): Promise<void> {
    try {
      await authApi.logout()
    } finally {
      clearTokens()
      user.value = null
    }
  }

  async function refreshToken(): Promise<void> {
    const { data } = await authApi.refresh({
      refreshToken: localStorage.getItem('refreshToken') || '',
    })
    setTokens(data)
    setUserFromToken(data.accessToken)
  }

  return {
    user,
    isAuthenticated,
    isAdmin,
    initializeAuth,
    login,
    signup,
    logout,
    refreshToken,
  }
})
