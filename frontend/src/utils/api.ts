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
import type {
  SaveReadingProgressRequest,
  CompleteReadingRequest,
  ReadingProgressResponse,
  SaveTypingProgressRequest,
  CompleteTypingRequest,
  TypingProgressResponse,
  RankingEntryResponse,
} from '@/types/progress'
import type {
  BoardListItem,
  BoardDetail,
  BoardRequest,
  ReplyRequest,
  PageResponse,
} from '@/types/board'
import type { ChatSession, ChatUsage } from '@/types/chat'

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

// --- Progress API ---

export const progressApi = {
  saveReadingProgress(data: SaveReadingProgressRequest) {
    return api.post<void>('/progress/reading/save', data)
  },
  completeReading(data: CompleteReadingRequest) {
    return api.post<void>('/progress/reading/complete', data)
  },
  getReadingProgress(bookName: string, chapter: number) {
    return api.get<ReadingProgressResponse>(`/progress/reading/${bookName}/${chapter}`)
  },
  getLatestReadingProgress() {
    return api.get<ReadingProgressResponse>('/progress/reading/latest')
  },
  getAllReadingProgress() {
    return api.get<ReadingProgressResponse[]>('/progress/reading')
  },
  saveTypingProgress(data: SaveTypingProgressRequest) {
    return api.post<void>('/progress/typing/save', data)
  },
  completeTyping(data: CompleteTypingRequest) {
    return api.post<void>('/progress/typing/complete', data)
  },
  getTypingProgress(bookName: string, chapter: number) {
    return api.get<TypingProgressResponse>(`/progress/typing/${bookName}/${chapter}`)
  },
  getLatestTypingProgress() {
    return api.get<TypingProgressResponse>('/progress/typing/latest')
  },
  getAllTypingProgress() {
    return api.get<TypingProgressResponse[]>('/progress/typing')
  },
}

// --- Ranking API ---

export const rankingApi = {
  getTypingRanking(limit?: number) {
    const params = limit ? { limit } : undefined
    return api.get<RankingEntryResponse[]>('/ranking/typing', { params })
  },
}

// --- Board API ---

export const boardApi = {
  getBoards(page = 0, size = 10, postType?: string) {
    const params: Record<string, string | number> = { page, size }
    if (postType) params.postType = postType
    return api.get<PageResponse<BoardListItem>>('/boards', { params })
  },
  getBoard(id: number) {
    return api.get<BoardDetail>(`/boards/${id}`)
  },
  createBoard(data: BoardRequest) {
    return api.post<void>('/boards', data)
  },
  updateBoard(id: number, data: BoardRequest) {
    return api.put<void>(`/boards/${id}`, data)
  },
  deleteBoard(id: number) {
    return api.delete<void>(`/boards/${id}`)
  },
  createReply(boardId: number, data: ReplyRequest) {
    return api.post<void>(`/boards/${boardId}/replies`, data)
  },
  updateReply(boardId: number, replyId: number, data: ReplyRequest) {
    return api.put<void>(`/boards/${boardId}/replies/${replyId}`, data)
  },
  deleteReply(boardId: number, replyId: number) {
    return api.delete<void>(`/boards/${boardId}/replies/${replyId}`)
  },
}

// --- Chat API ---

export const chatApi = {
  getSessions() {
    return api.get<ChatSession[]>('/chat/sessions')
  },
  getMessages(sessionId: number) {
    return api.get<Array<{ id: number; role: string; content: string; createdAt: string }>>(
      `/chat/sessions/${sessionId}/messages`,
    )
  },
  createSession(data: { title: string; bookName: string | null; chapter: number | null }) {
    return api.post<ChatSession>('/chat/sessions', data)
  },
  addMessage(sessionId: number, data: { role: string; content: string }) {
    return api.post<{ id: number; role: string; content: string; createdAt: string }>(
      `/chat/sessions/${sessionId}/messages`,
      data,
    )
  },
  deleteSession(sessionId: number) {
    return api.delete<void>(`/chat/sessions/${sessionId}`)
  },
  getUsage() {
    return api.get<ChatUsage>('/chat/usage')
  },
}

export default api
