import axios from 'axios'
import type {
  SignupRequest,
  LoginRequest,
  RefreshRequest,
  TokenResponse,
  UserListResponse,
  UserStatus,
} from '@/types/auth'
import type { BooksResponse, ChapterResponse } from '@/types/bible'

// --- Token utilities ---

export function getAccessToken(): string | null {
  return localStorage.getItem('accessToken')
}

export function getRefreshToken(): string | null {
  return localStorage.getItem('refreshToken')
}

export function setTokens(tokens: TokenResponse): void {
  localStorage.setItem('accessToken', tokens.accessToken)
  localStorage.setItem('refreshToken', tokens.refreshToken)
}

export function clearTokens(): void {
  localStorage.removeItem('accessToken')
  localStorage.removeItem('refreshToken')
}

// --- Axios instance ---

const api = axios.create({
  baseURL: '/api',
  headers: {
    'Content-Type': 'application/json',
  },
})

// Request interceptor: attach Bearer token
api.interceptors.request.use((config) => {
  const token = getAccessToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// Response interceptor: handle 401 with token refresh
let isRefreshing = false
let failedQueue: Array<{
  resolve: (token: string) => void
  reject: (error: unknown) => void
}> = []

function processQueue(error: unknown, token: string | null = null): void {
  failedQueue.forEach(({ resolve, reject }) => {
    if (token) {
      resolve(token)
    } else {
      reject(error)
    }
  })
  failedQueue = []
}

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config

    if (error.response?.status !== 401 || originalRequest._retry) {
      return Promise.reject(error)
    }

    // Don't retry refresh or login requests
    if (
      originalRequest.url === '/auth/refresh' ||
      originalRequest.url === '/auth/login'
    ) {
      return Promise.reject(error)
    }

    if (isRefreshing) {
      return new Promise<string>((resolve, reject) => {
        failedQueue.push({ resolve, reject })
      }).then((token) => {
        originalRequest.headers.Authorization = `Bearer ${token}`
        return api(originalRequest)
      })
    }

    originalRequest._retry = true
    isRefreshing = true

    const refreshToken = getRefreshToken()
    if (!refreshToken) {
      isRefreshing = false
      clearTokens()
      window.location.href = '/login'
      return Promise.reject(error)
    }

    try {
      const { data } = await axios.post<TokenResponse>('/api/auth/refresh', {
        refreshToken,
      })
      setTokens(data)
      processQueue(null, data.accessToken)
      originalRequest.headers.Authorization = `Bearer ${data.accessToken}`
      return api(originalRequest)
    } catch (refreshError) {
      processQueue(refreshError, null)
      clearTokens()
      window.location.href = '/login'
      return Promise.reject(refreshError)
    } finally {
      isRefreshing = false
    }
  },
)

// --- Auth API ---

export const authApi = {
  signup(data: SignupRequest) {
    return api.post<void>('/auth/signup', data)
  },
  login(data: LoginRequest) {
    return api.post<TokenResponse>('/auth/login', data)
  },
  logout() {
    return api.post<void>('/auth/logout')
  },
  refresh(data: RefreshRequest) {
    return api.post<TokenResponse>('/auth/refresh', data)
  },
}

// --- Admin API ---

export const adminApi = {
  getUsers(status?: UserStatus) {
    const params = status ? { status } : undefined
    return api.get<UserListResponse[]>('/admin/users', { params })
  },
  activateUser(id: number) {
    return api.patch<void>(`/admin/users/${id}/activate`)
  },
  deactivateUser(id: number) {
    return api.patch<void>(`/admin/users/${id}/deactivate`)
  },
}

// --- Bible API ---

export const bibleApi = {
  getBooks() {
    return api.get<BooksResponse>('/bible/books')
  },
  getChapter(bookName: string, chapter: number) {
    return api.get<ChapterResponse>(`/bible/${bookName}/${chapter}`)
  },
}

export default api
